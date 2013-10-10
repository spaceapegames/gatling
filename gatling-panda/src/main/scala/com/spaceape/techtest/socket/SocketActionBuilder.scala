package com.spaceape.techtest.socket

import io.gatling.core.session._
import io.gatling.core.action.builder.ActionBuilder
import akka.actor.{ Props, ActorRef }
import io.gatling.core.action._
import com.spaceape.panda.proto.Commands.{ ReqSyncProfile, ReqRepType, BaseReq, ClientServerMessageHeader }
import scala.util.Random
import com.spaceape.http.client.header
import com.spaceape.techtest.{ RepositoryFactory }

/**
 * Space Ape Games
 */
object SocketActionBuilder {

	def connect(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				implicit val ec = repoFactory.ec
				system.actorOf(Props(new SocketConnectionAction(requestName, next)))
			}
		}
	}

	def ping(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		val ping = ClientServerMessageHeader.newBuilder().setService("ping").setTokenid("1").build()

		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				implicit val ec = repoFactory.ec
				system.actorOf(Props(new PingAction(requestName, ping, next)))
			}
		}
	}

	def fetchDummyData(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		val ping = ClientServerMessageHeader.newBuilder().setService("dummy").setTokenid("1").build()

		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				implicit val ec = repoFactory.ec
				system.actorOf(Props(new DummyDataAction(requestName, ping, next)))
			}
		}
	}

	def fetchDummyFutureData(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		val ping = ClientServerMessageHeader.newBuilder().setService("dummyfuture").setTokenid("1").build()

		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				implicit val ec = repoFactory.ec
				system.actorOf(Props(new DummyDataAction(requestName, ping, next)))
			}
		}
	}

	def syncProfile(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				system.actorOf(Props(new SyncProfileAction(requestName, next)))
			}
		}
	}

	def createNewProfile(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				system.actorOf(Props(new CreateNewProfileAction(requestName, next)))
			}
		}
	}

	def poll(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				system.actorOf(Props(new PollAction(requestName, next)))
			}
		}
	}

//	def findPlayersToAttack(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
//		new ActionBuilder {
//			def build(next: ActorRef): ActorRef = {
//				system.actorOf(Props(new PlayersToAttackAction(requestName, next)))
//			}
//		}
//	}

	def payForBattle(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				system.actorOf(Props(new PayForBattleAction(requestName, next)))
			}
		}
	}

	def disconnect(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new SocketStopBuilder(requestName)
	}

	def releaseAttackedPlayer(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) = {
		new ActionBuilder {
			def build(next: ActorRef): ActorRef = {
				system.actorOf(Props(new ReleasePlayersAfterAttackAction(requestName, next)))
			}
		}
	}
}

class SocketStopBuilder(requestName: Expression[String])(implicit repoFactory: RepositoryFactory) extends ActionBuilder {

	def build(next: ActorRef): ActorRef = {
		implicit val ec = repoFactory.ec
		system.actorOf(Props(new SocketStop(requestName, next)))
	}

}