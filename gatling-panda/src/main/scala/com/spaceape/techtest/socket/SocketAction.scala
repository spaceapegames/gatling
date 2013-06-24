package com.spaceape.techtest.socket

import io.gatling.core.session._
import com.spaceape.techtest._
import akka.actor.ActorRef
import io.gatling.core.action.Interruptable
import com.spaceape.panda.proto.Commands.{ BaseResp, ClientServerMessageHeader }
import com.spaceape.http.client.header
import com.redis.S
import io.gatling.core.result.message.{ KO, RequestMessage, Status }
import io.gatling.core.util.StringHelper._
import io.gatling.core.session.Session
import com.spaceape.http.client.header
import io.gatling.core.result.writer.DataWriter
import io.gatling.http.response
import io.gatling.http
import io.gatling.core.validation.{ Success, Failure }
import io.gatling.core.session.Session
import io.gatling.core.validation.Success
import com.spaceape.http.client.header
import com.spaceape.techtest.socket.SendComplete
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.SendMessage
import com.spaceape.techtest.socket.StopClient
import io.gatling.core.validation.Failure
import io.gatling.core.session.Session
import io.gatling.core.validation.Success
import com.spaceape.http.client.header
import com.spaceape.techtest.socket.SendComplete
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.StopClient
import io.gatling.core.validation.Failure
import io.gatling.core.session.Session
import io.gatling.core.validation.Success
import com.spaceape.http.client.header
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.SendComplete
import com.spaceape.techtest.socket.SendMessage
import com.spaceape.techtest.socket.StopClient
import io.gatling.core.validation.Failure
import io.gatling.core.session.Session
import io.gatling.core.validation.Success
import com.spaceape.http.client.header
import com.spaceape.techtest.socket.SendComplete
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.SendMessage
import io.gatling.core.validation.Failure
import com.spaceape.game.security.TicketGenerator

/**
 * Space Ape Games
 */
abstract class SocketAction(val requestName: Expression[String], val next: ActorRef)(implicit repoFactory: RepositoryFactory) extends Interruptable with TicketGenerator {

	var userId: Int = 0
	var scenarioName: String = ""
	val authConfig = repoFactory.authConfig
	/**
	 * Core method executed when the Action received a Session message
	 *
	 * @param session the session of the virtual user
	 * @return Nothing
	 */
	def execute(session: Session) {
		scenarioName = session.scenarioName
		userId = session.userId

		requestName(session) match {
			case Success(reName) =>
				val sendMessage = buildRequest(reName, session)
				if (sendMessage == null) {
					next ! session
				} else {
					session("socketClientActor").as[ActorRef] ! sendMessage
				}
			case Failure(msg) =>
				logger.error(msg)
				next ! session
		}
	}

	override def receive = {

		case session: Session => execute(session)
		case s: SendComplete =>
			var session = s.request.session

			def dump = {
				val buff = new StringBuilder
				buff.append(eol).append(">>>>>>>>>>>>>>>>>>>>>>>>>>").append(eol)
				buff.append("request was:").append(eol)
				buff.append("=========================").append(eol)
				buff.append("response was:").append(eol)
				buff.append(eol).append("<<<<<<<<<<<<<<<<<<<<<<<<<")
				buff.toString
			}

			if (s.status == KO) {
				logger.warn(s"Request '$requestName' failed : ${s.message.getOrElse("")}")
				if (!logger.underlying.isTraceEnabled) logger.debug(dump)

				DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, s.request.requestName,
					s.requestSendingEndTime, s.requestSendingEndTime, s.receivingEndTime, s.receivingEndTime, s.status, s.message))

			} else {
				try {
					session = processResponse(s)
				} catch {
					case e: Throwable =>
						logger.warn(s"'${e.getMessage}'")
						DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, s.request.requestName,
							s.requestSendingEndTime, s.requestSendingEndTime, s.receivingEndTime, s.receivingEndTime, KO, Some(e.getMessage)))
				}
			}

			next ! session
	}

	def processResponse(sendComplete: SendComplete): Session
	def buildRequest(requestName: String, session: Session): SendMessage

	def buildHeader(tokenPrefix: String) = {
		ClientServerMessageHeader.newBuilder().setService("gameservice").setTokenid(tokenPrefix + System.currentTimeMillis()).build()
	}

	def getTicketFromSession(session: Session): String = {
		session("ticket").asOption[String] match {
			case Some(ticket) => ticket
			case None => getTicketForClide(session("clide").as[String], session)
		}
	}

	def getTicketForClide(clide: String, session: Session): String = {
		generateTicket(clide, System.currentTimeMillis())
	}

}

class SocketStop(val requestName: Expression[String], val next: ActorRef)(implicit repoFactory: RepositoryFactory) extends Interruptable {
	/**
	 * Core method executed when the Action received a Session message
	 *
	 * @param session the session of the virtual user
	 * @return Nothing
	 */
	def execute(session: Session) {
		session("socketClientActor").as[ActorRef] ! new StopClient
		next ! session
	}
}