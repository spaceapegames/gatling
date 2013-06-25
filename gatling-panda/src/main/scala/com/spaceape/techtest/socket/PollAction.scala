package com.spaceape.techtest.socket

import io.gatling.core.session._
import com.spaceape.panda.proto.Commands._
import akka.actor.ActorRef
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.KO
import com.spaceape.techtest.RepositoryFactory
import io.gatling.core.session.Session
import io.gatling.core.result.message.RequestMessage
import scala.Some
import com.spaceape.game.security.TicketGenerator
import com.spaceape.panda.proto.Audits.{ AuditChangeType, BaseAuditChange, UpdateActiveTimeAuditChange }

/**
 * Space Ape Games
 */
class PollAction(requestName: Expression[String], next: ActorRef)(implicit repoFactory: RepositoryFactory) extends SocketAction(requestName, next) {

	def buildRequest(requestName: String, session: Session): SendMessage = {
		val req = createBaseReqBuilder(session)
		req.setType(ReqRepType.AuditChange)

		val auditChange = UpdateActiveTimeAuditChange.newBuilder().build()
		val baseAuditChange = BaseAuditChange.newBuilder().setAuditChangeTime(System.currentTimeMillis()).setType(AuditChangeType.UpdateActiveTime).setUpdateActiveTime(auditChange).build()

		req.setReqAuditChange(ReqAuditChange.newBuilder().addAuditChanges(baseAuditChange))
		req.build()

		new SendMessage(requestName, session, System.currentTimeMillis(), buildHeader("poll"), req.build.toByteArray)
	}

	def processResponse(s: SendComplete): Session =
		{
			var session = s.request.session
			var status = s.status
			var message = s.message

			(s.response) match {
				case Some(response) =>
					val baseResp = BaseResp.parseFrom(response.payload)
					if (baseResp.getStatus != ReqRepStatus.OK) {
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
