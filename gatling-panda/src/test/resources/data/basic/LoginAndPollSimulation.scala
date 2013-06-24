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
class LoginAndPollSimulation extends Simulation {

	implicit val repositoryFactory = new RepositoryFactory

	repositoryFactory.serviceUrl = "http://loadtest-panda-game-service-4.use1a.apelabs.net:8024/v1/reqrep"

	val clides = csv("test-clides-loadtest.csv").circular

	val scn = scenario("Login And Poll")
		.feed(clides)
		.group("LoginAndPoll") {
			exec(
				basereq("SyncProfile", SyncProfile()))
				.doIf(isSynced(_)) {
					repeat(5)(
						pause(3 seconds, 7 seconds)
							.exec(
								basereq("poll", Poll())))
				}
		}

	val scn1 = scenario("Short Login")
		.feed(clides)
		.group("ShortLogin") {
			exec(
				basereq("SyncProfile", SyncProfile()))
				.doIf(isSynced(_)) {
					repeat(1)(
						pause(3 seconds, 7 seconds)
							.exec(
								basereq("poll", Poll())))
				}
		}
	setUp(
		scn.inject(ramp(500 users) over (30 seconds)),
		scn1.inject(constantRate(1 userPerSec) during (2 minutes)))

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
	override def tearDown {
		repositoryFactory.shutdown
	}
}
