package com.spaceape.techtest.dummy

import akka.actor.{ Props, ActorRef }
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session._
import io.gatling.core.action.system
import com.spaceape.techtest.dummy.DummyRequestAction
import com.spaceape.techtest.RepositoryFactory

object DummyRequestActionBuilder {

	/**
	 * This is the default HTTP check used to verify that the response status is 2XX
	 */
	def apply(requestName: Expression[String], clide: Expression[String])(implicit repoFactory: RepositoryFactory) = {

		new DummyRequestActionBuilder(requestName, clide, repoFactory: RepositoryFactory)
	}
}

/**
 * Space Ape Games
 */
class DummyRequestActionBuilder(requestName: Expression[String], clide: Expression[String], repoFactory: RepositoryFactory) extends ActionBuilder {

	def build(next: ActorRef): ActorRef = {
		implicit val ec = repoFactory.ec
		system.actorOf(Props(new DummyRequestAction(requestName, clide, next)))
	}

}
