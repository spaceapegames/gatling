package data.basic

import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

class RedisMakingSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024")
		.shareConnections

	val scn = scenario("My scenario")
		.exec(http("My Page")
			.get("/v1/match/making"))

	//	setUp(scn.inject(constantRate(20 usersPerSec) during (15 seconds))).protocols(httpProtocol)
	//  setUp(scn.inject(rampRate(1000 usersPerSec) to (4000 usersPerSec) during (10 seconds), constantRate(4000 usersPerSec) during (15 seconds))).protocols(httpProtocol)
}
