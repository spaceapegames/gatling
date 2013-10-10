package data.basic

import io.gatling.http.Predef._
import io.gatling.core.Predef._
import scala.concurrent.duration._
import io.gatling.core.Predef.Session
import com.spaceape.game.security.{ AuthenticationConfiguration, TicketGenerator }

class JettySimulation extends Simulation {
	val clides = csv("test-clides-loadtest.csv").circular

	val httpProtocol = http
		.baseURL("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024")
		.shareConnections

	val scn = scenario("JettySimulation")
		.exec(
			http("making")
				.post("/debug/v1/test/random/500/50"))

	setUp(scn.inject(constantRate(2 usersPerSec) during (1 hour))).protocols(httpProtocol)
	//  setUp(scn.inject(rampRate(1000 usersPerSec) to (4000 usersPerSec) during (10 seconds), constantRate(4000 usersPerSec) during (15 seconds))).protocols(httpProtocol)

}
