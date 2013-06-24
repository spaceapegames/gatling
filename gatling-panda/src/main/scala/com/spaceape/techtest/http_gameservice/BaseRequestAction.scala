package com.spaceape.techtest.http_gameservice

import akka.actor.{ Props, ActorRef }
import scala.concurrent.ExecutionContext
import io.gatling.core.action.Interruptable
import com.ning.http.client.{ HttpResponseHeaders, Response, AsyncCompletionHandlerBase, RequestBuilder }
import com.spaceape.panda.proto.Commands._
import io.gatling.core.Predef._
import io.gatling.core.session.{ Session, EL }
import io.gatling.core.Predef.Session
import io.gatling.core.validation.{ Validation, Failure, Success }
import com.typesafe.scalalogging.slf4j.Logging
import com.spaceape.game.security.{ AuthenticationConfiguration, TicketGenerator }
import com.ning.http.client
import io.gatling.core.Predef.Session
import io.gatling.core.session.Session
import io.gatling.core.result.message.{ KO, OK, RequestMessage, Status }
import io.gatling.core.util.StringHelper._
import io.gatling.core.session.Session
import io.gatling.core.Predef.Session
import io.gatling.core.validation.Failure
import scala.Some
import io.gatling.core.validation.Success
import io.gatling.core.result.writer.DataWriter
import com.ning.http.client.AsyncHandler.STATE
import io.gatling.core.session.{ Expression, Session }
import com.spaceape.techtest.{ RepositoryFactory, ReqRepProcessor }

/**
 * Space Ape Games
 */
object BaseRequestAction {
	var id = 0
}
class BaseRequestAction(val requestName: Expression[String], val reqRepProccessor: ReqRepProcessor, val next: ActorRef)(implicit repoFactory: RepositoryFactory) extends Interruptable with TicketGenerator {

	val authConfig = repoFactory.authConfig
	val client = repoFactory.client

	def execute(session: Session) {

		def sendRequest(requestName: String, ticket: String) = {
			val startTime = System.currentTimeMillis()
			val sendTime = System.currentTimeMillis()
			val baseReqBuilder = BaseReq.newBuilder().setId(0).setAuthenticationTicket(ticket)
			val baseReq = reqRepProccessor.processReq(baseReqBuilder, session)

			try {
				BaseRequestAction.id = BaseRequestAction.id + 1
				//println("Executing " + BaseRequestAction.id)
				val request = new RequestBuilder().setMethod("POST").setUrl(repoFactory.serviceUrl).setBody(baseReq.toByteArray).build()

				client.executeRequest(request, new AsyncCompletionHandlerBase {
					var responseReceivedTime = 0l
					override def onHeadersReceived(headers: HttpResponseHeaders): STATE = {
						responseReceivedTime = System.currentTimeMillis()
						super.onHeadersReceived(headers)
					}

					override def onCompleted(response: Response): Response = {

						val baseResp = BaseResp.parseFrom(response.getResponseBodyAsBytes)
						val status = if (baseResp.getStatus == ReqRepStatus.ERROR) KO else OK
						val responseWrapper = new BaseResponseWrapper(requestName, baseReq, baseResp, startTime, sendTime, responseReceivedTime, System.currentTimeMillis())

						logRequest(session, status, responseWrapper)

						val updatedSession = reqRepProccessor.processResp(baseResp, session)

						next ! updatedSession

						response
					}

					override def onThrowable(t: Throwable) {
						val responseWrapper = new BaseResponseWrapper(requestName, baseReq, null, startTime, sendTime, responseReceivedTime, System.currentTimeMillis())
						logRequest(session, KO, responseWrapper, Some(t.getMessage))

						super.onThrowable(t)

						next ! session
					}
				})

			} catch {
				case e: Throwable =>
					logger.error("failed to send", e)
					val responseWrapper = new BaseResponseWrapper(requestName, baseReq, null, startTime, sendTime, System.currentTimeMillis(), System.currentTimeMillis())
					logRequest(session, KO, responseWrapper, Some(e.getMessage))
					next ! session
			}
		}

		val execution = for {
			resolvedRequestName <- requestName(session)
			resolvedTicket <- getTicketFromSession(session)
		} yield sendRequest(resolvedRequestName, resolvedTicket)

		execution
	}

	case class BaseResponseWrapper(requestName: String, baseReq: BaseReq, baseResp: BaseResp, startTime: Long, requestSendingEndTime: Long, responseReceivingTime: Long, finishTime: Long) {

	}

	private def logRequest(
		session: Session,
		status: Status,
		response: BaseResponseWrapper,
		errorMessage: Option[String] = None) {

		def dump = {
			val buff = new StringBuilder
			buff.append(eol).append(">>>>>>>>>>>>>>>>>>>>>>>>>>").append(eol)
			buff.append("request was:").append(eol)
			buff.append(response.baseReq.toString)
			buff.append("=========================").append(eol)
			buff.append("response was:").append(eol)
			buff.append(response.baseResp.toString)
			buff.append(eol).append("<<<<<<<<<<<<<<<<<<<<<<<<<")
			buff.toString
		}

		if (status == KO) {
			logger.warn(s"Request '$requestName' failed : ${errorMessage.getOrElse("")}")
			if (!logger.underlying.isTraceEnabled) logger.debug(dump)
		}
		logger.trace(dump)

		DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, response.requestName,
			response.startTime, response.requestSendingEndTime, response.responseReceivingTime, response.finishTime,
			status, errorMessage))
	}

	def getTicketFromSession(session: Session): Validation[String] = {

		session("ticket").asOption[String] match {
			case Some(ticket) => Success(ticket)
			case None => Success(generateTicket(session("clide").as[String], System.currentTimeMillis()))
		}

	}

}

