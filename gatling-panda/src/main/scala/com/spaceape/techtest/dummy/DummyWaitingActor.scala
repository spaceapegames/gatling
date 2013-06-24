package com.spaceape.techtest.dummy

import io.gatling.core.action.BaseActor
import io.gatling.core.result.message.{ OK, KO, RequestMessage, Status }
import io.gatling.core.util.StringHelper._
import io.gatling.core.session.Session
import io.gatling.core.result.writer.DataWriter
import scala.concurrent.duration._
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext

/**
 * Space Ape Games
 */
class DummyWaitingActor(requestName: String, clide: String, session: Session, next: ActorRef)(implicit ec: ExecutionContext) extends BaseActor {

	var startTime: Long = 0
	var requestSendingEndTime: Long = 0
	var responseReceivingTime: Long = 0
	var finishTime: Long = 0

	def receive = {
		case m: StartWait =>
			println("starting clide " + clide)
			startTime = System.currentTimeMillis()
			requestSendingEndTime = System.currentTimeMillis()
			context.system.scheduler.scheduleOnce(100 milliseconds, self, new EndWait())
		case m: EndWait =>
			println("ending clide " + clide)
			responseReceivingTime = System.currentTimeMillis()
			finishTime = System.currentTimeMillis()

			next ! session

			logRequest(session, OK, DummyResponse(requestName, startTime, requestSendingEndTime, responseReceivingTime, finishTime))
	}

	case class DummyResponse(requestName: String, startTime: Long, requestSendingEndTime: Long, responseReceivingTime: Long, finishTime: Long) {

	}

	private def logRequest(
		session: Session,
		status: Status,
		response: DummyResponse,
		errorMessage: Option[String] = None) {

		def dump = {
			val buff = new StringBuilder
			buff.append(eol).append(">>>>>>>>>>>>>>>>>>>>>>>>>>").append(eol)
			buff.append("request was:").append(eol)
			//buff.appendAHCRequest(request)
			buff.append("=========================").append(eol)
			buff.append("response was:").append(eol)
			//buff.appendResponse(response)
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

}

case class StartWait()
case class EndWait()