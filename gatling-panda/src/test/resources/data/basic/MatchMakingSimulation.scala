package data.basic

import io.gatling.core.Predef._
import scala.concurrent.duration._
import bootstrap._
import com.spaceape.techtest.Predef._
import com.spaceape.panda.proto.Commands._
import io.gatling.core.validation.Validation
import com.spaceape.techtest._
import akka.actor.ActorRef
import io.gatling.core.session.Session
import com.spaceape.techtest.socket.SocketActionBuilder
import SocketActionBuilder._
import com.spaceape.techtest.Predef._
/**
 * Space Ape Games
 */
//class MatchMakingSimulation extends Simulation {
//
//	implicit val repositoryFactory = new RepositoryFactory
//
//	repositoryFactory.socketGateway = "loadtest-socket-gateway-1.use1a.apelabs.net"
//	//init all the players for battle
//
//	val clides = csv("test-clides-loadtest.csv").circular
//
//	val scn = scenario("Match making")
//		.feed(clides)
//		.group("match making") {
//			exec(connect("connect"))
//				.doIf(isConnected(_)) {
//					repeat(1)(
//						pause(2 seconds)
//							.exec(syncProfile("syncProfile"))
//							.doIf(isSynced(_)) {
//								repeat(1)(
//									pause(3 seconds, 7 seconds)
//										.exec(poll("poll")))
//									.exec(findPlayersToAttack("findPlayersToAttack"))
//									.exec(releaseAttackedPlayer("payForBattle"))
//							}).exec(disconnect("disconnect"))
//				}
//		}
//
//	setUp(
//		scn.inject(ramp(1 users) over (1 seconds)))
//
//	//assertThat(global.successfulRequests.percent.is(100),details("Login" / "request_2").responseTime.max.lessThan(2000))
//	//assertThat(details("request_9").requestsPerSec.greaterThan(10))
//	override def tearDown {
//		repositoryFactory.shutdown
//	}
//
//	def isSynced(session: Session): Validation[Boolean] = {
//		session("synced").asOption[Boolean] == Some(true)
//	}
//
//}
