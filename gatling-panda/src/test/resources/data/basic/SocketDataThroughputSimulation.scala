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
class SocketDataThroughputSimulation extends Simulation {

	implicit val repositoryFactory = new RepositoryFactory

  //repositoryFactory.socketGateway = "loadtest-socket-gateway-1.use1a.apelabs.net"
  repositoryFactory.socketGateway = "loadtest-socket-gateway-1.use1a.apelabs.net"

	val clides = csv("test-clides-loadtest.csv").circular

	val scn = scenario("Socket Gateway")
		.feed(clides)
		.group("datatest") {
			exec(connect("connect"))
				.doIf(isConnected(_)) {
					repeat(10)(exec(fetchDummyFutureData("fetch dummy data"))).
        exec(disconnect("disconnect"))
				}
		}

	setUp(
		scn.inject(rampRate(1 usersPerSec) to (90 usersPerSec) during (1 minutes)))
//    scn.inject(ramp(3 users) over (3 seconds)))

	case class SendPing() extends SocketProcessor {
		def processReq(header: ClientServerMessageHeader, payload: Array[Byte]) {}

		def processResp(header: ServerClientMessageHeader, payload: Array[Byte]) {}
	}

	//assertThat(global.successfulRequests.percent.is(100),details("Login" / "request_2").responseTime.max.lessThan(2000))
	//assertThat(details("request_9").requestsPerSec.greaterThan(10))
	override def tearDown {
		repositoryFactory.shutdown
	}


}
