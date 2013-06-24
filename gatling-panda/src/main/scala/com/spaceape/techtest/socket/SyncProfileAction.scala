package com.spaceape.techtest.socket

import io.gatling.core.session._
import com.spaceape.panda.proto.Commands._
import akka.actor.ActorRef
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.{ KO, RequestMessage }
import com.spaceape.techtest.RepositoryFactory
import com.spaceape.http.client.header
import io.gatling.core.session.Session
import com.spaceape.http.client.header
import com.spaceape.techtest.socket.SendComplete
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.SendMessage
import scala.Some
import io.gatling.core.session.Session
import com.spaceape.http.client.header
import com.spaceape.techtest.socket.SendComplete
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.SendMessage
import scala.Some
import io.gatling.core.validation.{ Success, Validation }
import com.spaceape.game.security.TicketGenerator

/**
 * Space Ape Games
 */
class SyncProfileAction(requestName: Expression[String], next: ActorRef)(implicit repoFactory: RepositoryFactory) extends SocketAction(requestName, next) with TicketGenerator {

	def buildRequest(requestName: String, session: Session): SendMessage = {
		val req = BaseReq.newBuilder().
			setType(ReqRepType.SyncProfile).
			setId(1).
			setReqSyncProfile(ReqSyncProfile.newBuilder()).
			setAuthenticationTicket(getTicketFromSession(session)).
			build
		new SendMessage(requestName, session, System.currentTimeMillis(), buildHeader("syncprofile"), req.toByteArray)
	}

	def processResponse(s: SendComplete): Session =
		{
			var session = s.request.session
			var status = s.status
			var message = s.message

			(s.response) match {
				case Some(response) =>
					val baseResp = BaseResp.parseFrom(response.payload)
					if (baseResp.getStatus == ReqRepStatus.OK) {
						session = session.set("synced", true)
					} else {
						session = session.set("synced", false)
						status = KO
						if (baseResp.hasErrorMessage) {
							message = Some(baseResp.getErrorMessage)
						}
					}
				case None =>
			}
			DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, s.request.requestName,
				s.requestSendingEndTime, s.requestSendingEndTime, s.receivingEndTime, s.receivingEndTime, status, s.message))

			session
		}
}
