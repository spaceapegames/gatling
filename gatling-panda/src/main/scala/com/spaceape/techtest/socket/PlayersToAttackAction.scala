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
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Space Ape Games
 */
class PlayersToAttackAction(requestName: Expression[String], next: ActorRef)(implicit repoFactory: RepositoryFactory) extends SocketAction(requestName, next) with TicketGenerator {

	def buildRequest(requestName: String, session: Session): SendMessage = {
		val req = BaseReq.newBuilder().
			setType(ReqRepType.PlayersToAttack).
			setId(1).
			setReqPlayersToAttack(ReqPlayersToAttack.newBuilder()).
			setAuthenticationTicket(getTicketFromSession(session)).
			build
		new SendMessage(requestName, session, System.currentTimeMillis(), buildHeader("playersToAttack"), req.toByteArray)
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
						if (baseResp.getRespPlayersToAttack.getOtherPlayersCount == 0) {
							logger.warn("No players found to attack")
						} else {
							val playersList = baseResp.getRespPlayersToAttack.getOtherPlayersList.toList.map(_.getProfile.getClide)
							session = session.set("defenderToRelease", playersList.head)
							session = session.set("otherDefenders", playersList.tail)
						}
					} else {
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
