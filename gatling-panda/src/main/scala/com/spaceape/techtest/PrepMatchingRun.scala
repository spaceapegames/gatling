package com.spaceape.techtest

import com.spaceape.common.persistence.core.DynamodbConfig
import com.spaceape.panda.PandaServiceConfig
import com.spaceape.panda.repository.{ PandaSchema, ProductionRepositoryFactory }
import com.spaceape.common.persistence.hydra.Hydra
import com.spaceape.panda.model._
import analytics.InstrumentedMatchingEngine
import com.spaceape.panda.matching.MatchingEngineImpl

/**
 * Space Ape Games
 */
object PrepMatchingRun {

	def main(args: Array[String]) {
		val dbConfig = new DynamodbConfig();
		dbConfig.accesskey = "AKIAJSFDH4YIKXHY2IOA";
		dbConfig.secretkey = "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";
		dbConfig.tablePrefix = "loadtest_";
		dbConfig.enabled = true;
		dbConfig.inMemory = false;

		val configuration = new PandaServiceConfig()
		configuration.dynamodbConfig = dbConfig
		configuration.gameContentConfig.isUrl = false
		configuration.swrvConfig.enabled = false

		val prodFactory = new ProductionRepositoryFactory(configuration)
		val pandaSchema = PandaSchema.initConnection(dbConfig)

		implicit val gameContent = prodFactory.gamePlayerRepository.getGameContent("system")
		implicit val persistentContext = GamePersistentContext(prodFactory, gameContent)

		val matchingEngine = new InstrumentedMatchingEngine(new MatchingEngineImpl(prodFactory))
		var i = 0
		Hydra.openSession(persistentContext)
		prodFactory.gamePlayerRepository.deletePlayer("c0375c2f-2dba-4d06-a2a3-d151ce0f4cb0")

		PlayerProfile.scan(prodFactory.gamePlayerRepository)(200) { profiles =>
			profiles.foreach { profile =>
				profile.removeShield()
				/*
        val player = prodFactory.gamePlayerRepository.getPlayer(profile.clide,Set(ProfileField,WorldField)).get
        if (player.getWorld != null){
          player.topupToCapSolid()
          player.topupToCapLiquid()
          player.getProfile.removeShield()
        }
        */
				i = i + 1
				if (i % 1000 == 0)
					Hydra.flush()
			}
		}

		Hydra.commit()
	}
}
