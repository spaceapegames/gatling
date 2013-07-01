package data.basic

import io.gatling.http.Predef._
import io.gatling.core.Predef._
import com.spaceape.panda.proto.Commands.{ReqRepType, BaseReq, ReqPlayersToAttack}
import scala.concurrent.duration._
import io.gatling.core.session.Session
import io.gatling.core.Predef.Session
import com.spaceape.game.security.{AuthenticationConfiguration, TicketGenerator}

class DynamoMatchSimulation extends Simulation {
  val clides = csv("test-clides-loadtest.csv").circular

  val httpProtocol = http
    .baseURL("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024")
    .shareConnections

  val scn = scenario("DynamoMatchSimulation")
    .feed(clides)
    .exec(
    http("making")
      .post("/v1/reqrep")
      .byteArrayBody(session => getByteArrayBody(session))
  )

  setUp(scn.inject(constantRate(2 usersPerSec) during (1 seconds))).protocols(httpProtocol)
  //  setUp(scn.inject(rampRate(1000 usersPerSec) to (4000 usersPerSec) during (10 seconds), constantRate(4000 usersPerSec) during (15 seconds))).protocols(httpProtocol)

  val getByteArrayBody = (session: Session) => {
    val ticket = getTicketFromSession(session)
    val req = ReqPlayersToAttack.newBuilder().build()
    val baseReq = BaseReq.newBuilder().setId(0).setReqPlayersToAttack(req).setType(ReqRepType.PlayersToAttack).setAuthenticationTicket(ticket).build()
    baseReq.toByteArray
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
