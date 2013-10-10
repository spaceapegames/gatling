package data.basic

import io.gatling.http.Predef._
import io.gatling.core.Predef._
import com.spaceape.panda.proto.Commands.{ ReqRepType, BaseReq, ReqPlayersToAttack }
import scala.concurrent.duration._
import io.gatling.core.session.Session
import io.gatling.core.Predef.Session
import com.spaceape.game.security.{ AuthenticationConfiguration, TicketGenerator }

class DynamoReadSimulation extends Simulation {
	val clides = csv("test-clides-loadtest.csv").circular

	val httpProtocol = http
		.baseURL("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024")
		.shareConnections

	val scn = scenario("DynamoSimulation")
		.feed(clides)
		.exec(
			http("read rate")
				//      .post("/v1/loadtest/dynamo/findOne")
				.post("/v1/loadtest/dynamo/ping")
				.byteArrayBody(session => getByteArrayBody(session)))

	//setUp(scn.inject(constantRate(10 usersPerSec) during (1 seconds))).protocols(httpProtocol)
	setUp(scn.inject(rampRate(100 usersPerSec) to (500 usersPerSec) during (10 seconds), constantRate(500 usersPerSec) during (15 seconds))).protocols(httpProtocol)
	//setUp(scn.inject(rampRate(100 usersPerSec) to (300 usersPerSec) during (10 seconds), constantRate(300 usersPerSec) during (15 seconds))).protocols(httpProtocol)

	val getByteArrayBody = (session: Session) => {
		//    session("clide").as[String].getBytes
		"http://loadtest-panda-game-service-5.use1a.apelabs.net:8024/v1/loadtest/dynamo/load".getBytes
	}

	def getTicketFromSession(session: Session): String = {
		session("ticket").asOption[String] match {
			case Some(ticket) => ticket
			case None => getTicketForClide(session("clide").as[String], session)
		}
	}

	def getTicketForClide(clide: String, session: Session): String = {
		ticketGenerator.generateTicket(clide, System.currentTimeMillis())
	}

	val ticketGenerator = new TicketGenerator {
		val authConfig: AuthenticationConfiguration = new AuthenticationConfiguration
	}
}