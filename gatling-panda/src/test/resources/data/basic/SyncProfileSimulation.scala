package data.basic

import io.gatling.core.Predef._
import scala.concurrent.duration._
import bootstrap._
import io.gatling.core.validation.{ Success, Validation }
import com.spaceape.techtest._
import akka.actor.ActorRef
import io.gatling.core.session.Session
import com.spaceape.techtest.socket.{ SocketClientThread, SessionKey, SocketActionBuilder }
import SocketActionBuilder._
import com.spaceape.techtest.Predef._

/**
 * Space Ape Games
 */
class SyncProfileSimulation extends Simulation {
	implicit val repositoryFactory = new RepositoryFactory

	repositoryFactory.socketGateway = "loadtest-socket-gateway-1.use1a.apelabs.net"

	val clides = csv("test-clides-loadtest.csv").random

	val scn = scenario("Sync Player")
		.feed(clides)
		.group("sync player") {
			exec(connect("connect"))
				.doIf(isConnected(_)) {
					exec(session => session.set(SessionKey.GameContentVersion, "18"))
						.exec(syncProfile("sync profile"))
						.exec(disconnect("disconnect"))
				}
		}

	setUp(
		//	scn.inject(atOnce(5 users))  )
		scn.inject(rampRate(1 usersPerSec) to (90 usersPerSec) during (1 minutes)))

	//assertThat(global.successfulRequests.percent.is(100),details("Login" / "request_2").responseTime.max.lessThan(2000))
	//assertThat(details("request_9").requestsPerSec.greaterThan(10))
	override def tearDown {
		repositoryFactory.shutdown
	}

	def isSynced(session: Session): Validation[Boolean] = {
		session("synced").asOption[Boolean] == Some(true)
	}

}
