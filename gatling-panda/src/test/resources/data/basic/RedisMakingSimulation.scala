package data.basic

import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

class RedisMakingSimulation extends Simulation {

	//  val httpProtocol = http
	//    .baseURL("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024/v1/match/making")

	val scn = scenario("My scenario")
		.exec(http("My Page")
			.get("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024/v1/match/making"))

	setUp(scn.inject(ramp(3000 users) over (10 seconds)))
}
