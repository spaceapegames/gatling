package com.spaceape.techtest.socket

import io.gatling.core.session._
import com.spaceape.panda.proto.Commands.ClientServerMessageHeader
import akka.actor.ActorRef
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.RequestMessage
import com.spaceape.techtest.RepositoryFactory

/**
 * Space Ape Games
 */
class DummyDataAction(requestName: Expression[String], header: ClientServerMessageHeader, next: ActorRef)(implicit repoFactory: RepositoryFactory) extends SocketAction(requestName, next) {

	def buildRequest(requestName: String, session: Session): SendMessage = {
		new SendMessage(requestName, session, System.currentTimeMillis(), header, Array.empty[Byte])
	}

	def processResponse(s: SendComplete) = {
		val session = s.request.session

		println(s.response.get.payload.length)
		val endTime = s.response.map(_.finishReadTime).getOrElse(s.receivingEndTime)

		DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, s.request.requestName,
			s.requestSendingEndTime, s.requestSendingEndTime, s.receivingEndTime, endTime, s.status, s.message))

		session
	}
}
