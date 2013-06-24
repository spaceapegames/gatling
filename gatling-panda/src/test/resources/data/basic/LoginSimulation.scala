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

/**
 * Space Ape Games
 */
class LoginSimulation extends Simulation {

	implicit val repositoryFactory = new RepositoryFactory

	val clides = csv("test-clides-loadtest.csv").circular

	val scn = scenario("Login And Poll")
		.feed(clides)
		.exec(
			basereq("SyncProfile", SyncProfile()))

	//setUp(scn.inject(ramp(260 user) ))
	setUp(scn.inject(constantRate(10 userPerSec) during (5 minutes)))

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
			session
		}
	}
	//assertThat(global.successfulRequests.percent.is(100),details("Login" / "request_2").responseTime.max.lessThan(2000))
	//assertThat(details("request_9").requestsPerSec.greaterThan(10))
}
