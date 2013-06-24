package com.spaceape.techtest.socket

import io.gatling.core.session._
import com.spaceape.panda.proto.Commands.ClientServerMessageHeader
import akka.actor.ActorRef
import io.gatling.core.action.Interruptable
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.socket.{ SendComplete, SocketAction }
import com.spaceape.techtest.RepositoryFactory

/**
 * Space Ape Games
 */
class PingAction(requestName: Expression[String], header: ClientServerMessageHeader, next: ActorRef)(implicit repoFactory: RepositoryFactory) extends SocketAction(requestName, next) {

	def buildRequest(requestName: String, session: Session): SendMessage = {
		new SendMessage(requestName, session, System.currentTimeMillis(), header, Array.empty[Byte])
	}

	def processResponse(s: SendComplete) = {
		val session = s.request.session

		DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, s.request.requestName,
			s.requestSendingEndTime, s.requestSendingEndTime, s.receivingEndTime, s.receivingEndTime, s.status, s.message))

		session
	}
}
