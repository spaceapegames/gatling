package data.basic

import io.gatling.core.Predef._
import scala.concurrent.duration._
import bootstrap._
import io.gatling.core.validation.Validation
import com.spaceape.techtest._
import akka.actor.ActorRef
import io.gatling.core.session.Session
import com.spaceape.techtest.socket.SocketActionBuilder
import SocketActionBuilder._

/**
 * Space Ape Games
 */
class SyncProfileSimulation extends Simulation {
	implicit val repositoryFactory = new RepositoryFactory

	repositoryFactory.socketGateway = "loadtest-socket-gateway-1.use1a.apelabs.net"

	val clides = csv("test-clides-loadtest.csv").random

	val scn = scenario("New Player")
		.feed(clides)
		.group("new player") {
			exec(connect("connect"))
				.doIf(isConnected(_)) {
					exec(syncProfile("sync profile"))
						.doIf(isSynced(_)) {
							repeat(2)(
								pause(3 seconds, 7 seconds)
									.exec(poll("poll")))
						}
						.exec(disconnect("disconnect"))
				}
		}

	setUp(
		//scn.inject(atOnce(5 users))  )
		scn.inject(rampRate(1 usersPerSec) to (300 usersPerSec) during (8 minutes)))

	//assertThat(global.successfulRequests.percent.is(100),details("Login" / "request_2").responseTime.max.lessThan(2000))
	//assertThat(details("request_9").requestsPerSec.greaterThan(10))
	override def tearDown {
		repositoryFactory.shutdown
	}

	def isConnected(session: Session): Validation[Boolean] = {
		session("socketClientActor").asOption[ActorRef] != None
	}

	def isSynced(session: Session): Validation[Boolean] = {
		session("synced").asOption[Boolean] == Some(true)
	}

}
