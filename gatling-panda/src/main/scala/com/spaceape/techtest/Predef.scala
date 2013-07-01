package com.spaceape.techtest

import io.gatling.core.session._
import com.spaceape.techtest.socket.{SocketClientThread, SessionKey, SocketActionBuilder}
import com.spaceape.techtest.http_gameservice.BaseRequestActionBuilder
import com.spaceape.techtest.dummy.DummyRequestBaseBuilder
import io.gatling.core.validation.{Success, Validation}

/**
 * Space Ape Games
 */
object Predef {

	def dummyreq(requestName: Expression[String], clide: Expression[String])(implicit repoFactory: RepositoryFactory) = DummyRequestBaseBuilder.dummyrequest(requestName, clide)
	def basereq(requestName: Expression[String], processor: ReqRepProcessor)(implicit repoFactory: RepositoryFactory) = BaseRequestActionBuilder(requestName, processor)
	def pingGateway(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = SocketActionBuilder.ping(requestName)

  def isConnected(session: Session): Validation[Boolean] = {
    session(SessionKey.SocketClient).asOption[SocketClientThread] match {
      case Some(client) => Success(client.connected)
      case None => Success(false)
    }
  }
}
