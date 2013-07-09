package data.basic

import io.gatling.core.Predef._
import scala.concurrent.duration._
import bootstrap._
import com.spaceape.techtest.Predef._
import io.gatling.core.session._
import com.spaceape.panda.proto.Commands._
import com.spaceape.panda.proto.Audits.{ AuditChangeType, BaseAuditChange, UpdateActiveTimeAuditChange }
import io.gatling.core.validation.Validation
import com.spaceape.techtest.{ ReqRepProcessor, RepositoryFactory }
import com.spaceape.common.core.compress.Compression
import com.spaceape.common.core.rest.Json
import play.api.libs.json.JsObject

/**
 * Space Ape Games
 */
class SyncDirectSimulation extends Simulation {

	implicit val repositoryFactory = new RepositoryFactory

	repositoryFactory.serviceUrl = "http://loadtest-panda-game-service-5.use1a.apelabs.net:8024/v1/reqrep"

	val clides = csv("test-clides-loadtest.csv").random

	val scn = scenario("Sync Direct")
		.feed(clides)
		.group("Login") {
			exec(basereq("SyncProfile", SyncProfile()))
 		}

	setUp(
    scn.inject(rampRate(1 usersPerSec) to (90 usersPerSec) during (3 minutes)))

	case class SyncProfile() extends ReqRepProcessor {

		def processReq(req: BaseReq.Builder, session: Session): BaseReq = {
			req.setType(ReqRepType.SyncProfile)
			req.setReqSyncProfile(ReqSyncProfile.newBuilder())
			req.build()
		}

		def processResp(res: BaseResp, session: Session) = {
			session.set("synced", true)
		}
	}

	def isSynced(session: Session): Validation[Boolean] = {
		session("synced").asOption[Boolean] == Some(true)
	}
	case class Poll() extends ReqRepProcessor {

		def processReq(req: BaseReq.Builder, session: Session): BaseReq = {
			req.setType(ReqRepType.AuditChange)

			val auditChange = UpdateActiveTimeAuditChange.newBuilder().build()
			val baseAuditChange = BaseAuditChange.newBuilder().setAuditChangeTime(System.currentTimeMillis()).setType(AuditChangeType.UpdateActiveTime).setUpdateActiveTime(auditChange).build()

			req.setReqAuditChange(ReqAuditChange.newBuilder().addAuditChanges(baseAuditChange))
			req.build()
		}

		def processResp(res: BaseResp, session: Session) = {
      var newSession = session
      if (res.getRespSyncProfile.hasGameContent){
        val rawJson = Json.parse[JsObject](Compression.uncompress(res.getRespSyncProfile.getGameContent.getRawJSON))
        val version = (rawJson \ "version").as[String]
        newSession = session.set("GameContentVersion",version)
      }
			newSession
		}
	}

	//assertThat(global.successfulRequests.percent.is(100),details("Login" / "request_2").responseTime.max.lessThan(2000))
	//assertThat(details("request_9").requestsPerSec.greaterThan(10))
	override def tearDown {
		repositoryFactory.shutdown
	}
}
