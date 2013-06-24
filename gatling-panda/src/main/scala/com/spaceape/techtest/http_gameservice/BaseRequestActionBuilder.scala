package com.spaceape.techtest.http_gameservice

import io.gatling.core.session._
import io.gatling.core.action.builder.ActionBuilder
import akka.actor.{ Props, ActorRef }
import io.gatling.core.action._
import com.spaceape.game.security.TicketGenerator
import com.spaceape.techtest.http_gameservice.BaseRequestAction
import com.spaceape.techtest.{ RepositoryFactory, ReqRepProcessor }

/**
 * Space Ape Games
 */
object BaseRequestActionBuilder {

	def apply(requestName: Expression[String], processor: ReqRepProcessor)(implicit repoFactory: RepositoryFactory) = {
		new BaseRequestActionBuilder(requestName, processor)
	}
}

class BaseRequestActionBuilder(requestName: Expression[String], val processor: ReqRepProcessor)(implicit repoFactory: RepositoryFactory) extends ActionBuilder {

	def build(next: ActorRef): ActorRef = {
		implicit val ec = repoFactory.ec
		system.actorOf(Props(new BaseRequestAction(requestName, processor, next)))
	}

}

