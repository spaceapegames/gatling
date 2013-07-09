package data.basic

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import scala.Some
import io.gatling.http.Predef._
import scala.Some
import com.spaceape.game.security.{AuthenticationConfiguration, TicketGenerator}
import com.spaceape.panda.proto.Commands.{ReqRepType, BaseReq, ReqAuditChange}
import com.spaceape.panda.proto.Audits.{AuditChangeType, BaseAuditChange, SaveBattleAuditChange}
import com.spaceape.panda.proto.Model.{BattleReplayTO, BattleResultTO}
import com.spaceape.common.core.rest.Json
import scala.concurrent.duration._

class SaveBattleResultGSOnlySimulation extends Simulation {
  val clides = csv("test-battle-clides-loadtest.csv").circular

  val httpProtocol = http
    .baseURL("http://loadtest-panda-game-service-6.use1a.apelabs.net:8024")
    .shareConnections

  val scn = scenario("SaveBattleResultGSOnlySimulation")
    .feed(clides)
    .exec(
    http("save battle")
      .post("/v1/loadtest/battle/save")
      .byteArrayBody(session => getByteArrayBody(session))
  )

  setUp(scn.inject(constantRate(2 usersPerSec) during (1 seconds))).protocols(httpProtocol)
  //  setUp(scn.inject(rampRate(1000 usersPerSec) to (4000 usersPerSec) during (10 seconds), constantRate(4000 usersPerSec) during (15 seconds))).protocols(httpProtocol)

  val getByteArrayBody = (session: Session) => {
    val clide = session("clide").as[String]
    val ticket = getTicketFromSession(session)
    val result = BattleResultTO.newBuilder()
       .setAttackerClide(clide)
      .setDefenderClide(session("defender").as[String])
      .setAttackDonatedTroopsUsed(false)
      .setAttackerName("fakename")
      .setAttackTime(System.currentTimeMillis())
      .setAttackTrophiesBefore(100)
      .setAttackTrophiesEarned(20)
      .setCastleDestroyed(false)
      .setCollectableWasStolen(false)
      .setDamagePercent(80)
      .setDefenceDonatedTroopsUsed(false)
      .setDefendTrophiesBefore(100)
      .setDefendTrophiesEarned(-20)
      .build()
    val replay = Json.parse[BattleReplayTO](fakeReplay)
    val saveBattle = SaveBattleAuditChange.newBuilder().setBattleResult(result).setBattleReplay(replay).build()
    val baseAudit = BaseAuditChange.newBuilder().setSaveBattle(saveBattle).setType(AuditChangeType.SaveBattleType).build()
    val req = ReqAuditChange.newBuilder().addAuditChanges(baseAudit).build()
    val baseReq = BaseReq.newBuilder().setId(0).setSimulateProductionMode(true).setReqAuditChange(req).setType(ReqRepType.AuditChange).setAuthenticationTicket(ticket).build()
    baseReq.toByteArray
  }

  def getTicketFromSession(session: Session): String = {
    session("ticket").asOption[String] match {
      case Some(ticket) => ticket
      case None => getTicketForClide(session("clide").as[String])
    }
  }

  def getTicketForClide(clide: String): String = {
    ticketGenerator.generateTicket(clide, System.currentTimeMillis())
  }

  val ticketGenerator = new TicketGenerator {
    val authConfig: AuthenticationConfiguration = new AuthenticationConfiguration
  }

  val fakeReplay =
    """{"battleInputs":[{"type":"ADD_UNITS","addUnit":{"frameNumber":1,"x":34,"y":23,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":10,"x":34,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":16,"x":34,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":21,"x":34,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":27,"x":33,"y":23,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":33,"x":34,"y":23,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":45,"x":34,"y":20,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":50,"x":34,"y":21,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":72,"x":11,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":78,"x":12,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":84,"x":11,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":89,"x":11,"y":25,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":94,"x":11,"y":25,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":99,"x":11,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":105,"x":11,"y":25,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":110,"x":11,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":115,"x":11,"y":25,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":120,"x":11,"y":24,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":149,"x":11,"y":25,"unitID":"TRP_swordsman_4"}},{"type":"ADD_UNITS","addUnit":{"frameNumber":153,"x":12,"y":24,"unitID":"TRP_swordsman_4"}}],"defenderState":{"profile":{"clide":"23de33ae-eb2f-49ed-9373-9998fb592c22","username":"stetesttwo","xp":0,"level":1,"trophy":0,"lastLoginTime":0,"lastTroopRequestTime":0,"resources":[{"resourceType":"Liquid","amount":3000},{"resourceType":"Solid","amount":3000},{"resourceType":"Premium","amount":750}]},"world":{"buildings":[{"refID":"BLD_barracks_1","x":26,"y":24,"productionQueue":{},"id":-7,"upgradeId":"BLD_barracks_2"},{"refID":"BLD_solidresourcemine_1","x":20,"y":17,"resources":[{"resource":{"resourceType":"Solid"}}],"id":-8,"upgradeId":"BLD_solidresourcemine_2"},{"refID":"BLD_carpenter_5","x":10,"y":10,"id":-3},{"refID":"BLD_troopcamp_1","x":20,"y":5,"id":-12,"upgradeId":"BLD_troopcamp_2"},{"refID":"DEF_temple_1","x":10,"y":5,"productionQueue":{},"id":-11,"upgradeId":"DEF_temple_2"},{"refID":"BLD_guildhall_2","x":24,"y":20,"id":-4},{"refID":"DEF_archerTower_1","x":14,"y":20,"id":-15,"upgradeId":"DEF_archerTower_2"},{"refID":"BLD_solidresourcestorage_1","x":20,"y":24,"resources":[{"resource":{"resourceType":"Solid","amount":1500}}],"id":-5,"upgradeId":"BLD_solidresourcestorage_2"},{"refID":"DEF_workshop_1","x":5,"y":5,"productionQueue":{},"id":-10,"upgradeId":"DEF_workshop_2"},{"refID":"DEF_archerTower_1","x":28,"y":20,"id":-14,"upgradeId":"DEF_archerTower_2"},{"refID":"BLD_liquidresourcemine_1","x":23,"y":17,"resources":[{"resource":{"resourceType":"Liquid"}}],"id":-9,"upgradeId":"BLD_liquidresourcemine_2"},{"refID":"BLD_liquidresourcestorage_1","x":23,"y":24,"resources":[{"resource":{"resourceType":"Liquid","amount":1500}}],"id":-6,"upgradeId":"BLD_liquidresourcestorage_2"},{"refID":"DEF_blacksmith_1","x":25,"y":5,"productionQueue":{},"id":-13,"upgradeId":"DEF_blacksmith_2"},{"refID":"BLD_castle_2","x":15,"y":15,"resources":[{"resource":{"resourceType":"Solid","amount":1500}},{"resource":{"resourceType":"Liquid","amount":1500}}],"id":-2,"upgradeId":"BLD_castle_3"}],"nextItemId":0},"troops":[{"type":"ramcrew","count":0,"level":1},{"type":"archer","count":0,"level":1},{"type":"commander","count":0,"level":1},{"type":"jadelion","count":0,"level":1},{"type":"ninja","count":0,"level":1},{"type":"swordsman","count":0,"level":1},{"type":"healer","count":0,"level":1},{"type":"freezespell","count":0,"level":1},{"type":"ram","count":0,"level":1},{"type":"oni","count":0,"level":1},{"type":"villager","count":0,"level":1},{"type":"ramcrew","count":0,"level":1},{"type":"archer","count":0,"level":1},{"type":"commander","count":0,"level":1},{"type":"jadelion","count":0,"level":1},{"type":"ninja","count":0,"level":1},{"type":"swordsman","count":0,"level":1},{"type":"healer","count":0,"level":1},{"type":"freezespell","count":0,"level":1},{"type":"ram","count":0,"level":1},{"type":"oni","count":0,"level":1},{"type":"villager","count":0,"level":1}]},"attackerState":{"profile":{"clide":"1f1a294e-4061-4b6b-8d29-8c1ec272a2d7","username":"qajc","xp":96,"level":9,"trophy":0,"lastLoginTime":0,"lastTroopRequestTime":0,"resources":[{"resourceType":"Liquid","amount":14500},{"resourceType":"Solid","amount":280},{"resourceType":"Premium","amount":968499}]},"troops":[{"type":"ramcrew","count":0,"level":1},{"type":"archer","count":0,"level":1},{"type":"commander","count":0,"level":1},{"type":"jadelion","count":0,"level":1},{"type":"ninja","count":0,"level":1},{"type":"swordsman","count":20,"level":4},{"type":"healer","count":0,"level":1},{"type":"freezespell","count":0,"level":1},{"type":"ram","count":0,"level":1},{"type":"oni","count":0,"level":3},{"type":"villager","count":0,"level":1}],"nodeProgress":[{"nodeId":"node_4","completed":true,"stars":3,"solidResourceStolen":1250,"liquidResourceStolen":1250},{"nodeId":"node_2","completed":false,"stars":1,"solidResourceStolen":653,"liquidResourceStolen":653},{"nodeId":"node_1","completed":true,"stars":10}]}}"""
}
