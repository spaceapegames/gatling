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
import scala.collection.JavaConversions._

/**
 * Space Ape Games
 */
class ReleasePlayersAfterAttackAction(requestName: Expression[String], next: ActorRef)(implicit repoFactory: RepositoryFactory) extends SocketAction(requestName, next) with TicketGenerator {

	def buildRequest(requestName: String, session: Session): SendMessage = {
		(session("defenderToRelease").asOption[String]) match {
			case Some(defender) =>
				val req = createBaseReqBuilder(session).
					setType(ReqRepType.DebugRemoveCloak).
					setReqDebugRemoveCloak(ReqDebugRemoveCloak.newBuilder().setClide(defender)).
					build
				new SendMessage(requestName, session, System.currentTimeMillis(), buildHeader("releasecloak"), req.toByteArray)
			case None => null
		}
	}

	def processResponse(s: SendComplete): Session =
		{
			s.request.session
		}
}
