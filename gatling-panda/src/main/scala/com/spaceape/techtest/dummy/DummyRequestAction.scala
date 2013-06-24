package com.spaceape.techtest.dummy

import akka.actor.{ Props, ActorRef }
import io.gatling.core.action.Interruptable
import io.gatling.core.session.{ Expression, Session }
import scala.concurrent.ExecutionContext

/**
 * Space Ape Games
 */
class DummyRequestAction(val requestName: Expression[String], val clide: Expression[String], val next: ActorRef)(implicit ec: ExecutionContext) extends Interruptable {

	def execute(session: Session) {
		println("exec request")
		def sendRequest1(requestName: String, clide: String) = {
			println("send request")
			val actor = context.actorOf(Props(new DummyWaitingActor(requestName, clide, session, next)))
			actor ! new StartWait()
		}

		val execution = for {
			resolvedRequestName <- requestName(session)
			resolvedClide <- clide(session)
		} yield sendRequest1(resolvedRequestName, resolvedClide)

		execution
	}

}
