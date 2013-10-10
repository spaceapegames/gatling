package com.spaceape.techtest

//import com.spaceape.common.persistence.core.DynamodbConfig
//import com.spaceape.panda.PandaServiceConfig
import com.spaceape.panda.repository.{ PandaSchema, ProductionRepositoryFactory }
//import scala.util.Random
//import org.apache.commons.lang.RandomStringUtils
//import java.util.UUID
//import com.spaceape.panda.model._
//import java.io.{ BufferedWriter, FileWriter }
//import com.spaceape.common.persistence.hydra.Hydra

/**
 * Space Ape Games
 */
object CreateTestAccounts {

	//	def main(args: Array[String]) {
	//		val dbConfig = new DynamodbConfig();
	//		dbConfig.accesskey = "AKIAJSFDH4YIKXHY2IOA";
	//		dbConfig.secretkey = "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";
	//		dbConfig.tablePrefix = "loadtest_";
	//		dbConfig.enabled = true;
	//		dbConfig.inMemory = false;
	//
	//		val configuration = new PandaServiceConfig()
	//		configuration.dynamodbConfig = dbConfig
	//		configuration.gameContentConfig.isUrl = false
	//
	//		val prodFactory = new ProductionRepositoryFactory(configuration)
	//		val pandaSchema = PandaSchema.initConnection(dbConfig)
	//		prodFactory.villageRepository.loadVillages()
	//
	//		val rnd = new Random
	//
	//		val repo = prodFactory.gamePlayerRepository
	//		//create 15 members per clan
	//		val writer = new BufferedWriter(new FileWriter("clides.csv"))
	//
	//		implicit val gameContent = prodFactory.gamePlayerRepository.getGameContent("system")
	//		implicit val persistentContext = GamePersistentContext(prodFactory, gameContent)
	//
	//		implicit val gameClock = prodFactory.clock
	//		val clans = 20000
	//		for (c <- 1 to clans) {
	//			try {
	//				Hydra.openSession(persistentContext)
	//				val clanName = RandomStringUtils.randomAlphabetic(25).toLowerCase()
	//				val membersPerClan = 5
	//				var clan: Clan = null
	//				var clanLeader: GamePlayer = null
	//
	//				for (i <- 1 to membersPerClan) {
	//					val clide = UUID.randomUUID().toString
	//
	//					val player = repo.getOrCreatePlayer(clide, Set(ProfileField, ClanField, WorldField, MembershipField))
	//					player.getProfile.removeShield()
	//					player.getProfile.setTrophy(rnd.nextInt(5000))
	//					player.getProfile.username = RandomStringUtils.randomAlphabetic(25).toLowerCase()
	//					if (clan == null) {
	//						println("creating clan " + clanName)
	//						clan = player.createClan(clanName)
	//						clanLeader = player
	//					} else {
	//						player.requestJoinClan(clanName)
	//					}
	//					writer.write(clide)
	//					writer.write('\n')
	//
	//				}
	//				Hydra.flush()
	//			} catch {
	//				case c: ClanAlreadyExistsException =>
	//			}
	//			Hydra.commit()
	//		}
	//		writer.close()
	//
	//	}
}
