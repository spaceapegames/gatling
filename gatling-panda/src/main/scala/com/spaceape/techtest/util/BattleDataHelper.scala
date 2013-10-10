package com.spaceape.techtest.util

import io.gatling.core.Predef._
import io.gatling.core.config.GatlingConfiguration
import io.gatling.recorder.config.CoreConfiguration
import scala.io.Source
import java.io.{ BufferedWriter, File }
import util.Random
import com.spaceape.common.core.logging.ResponseTime

object BattleDataHelper extends ResponseTime {
	def main(args: Array[String]) {

		val file = "/Users/keren/Dev/3rdparty/gatling/gatling-panda/src/test/resources/data/test-clides-loadtest.csv"
		val battlefile = "/Users/keren/Dev/3rdparty/gatling/gatling-panda/src/test/resources/data/test-battle-clides-loadtest.csv"

		var clides = List.empty[String]
		for (line <- Source.fromFile(new File(file)).getLines()) {
			if (!line.equalsIgnoreCase("clide")) {
				clides = clides.::(line)
			}
		}

		println("total lines: " + clides.size)
		var counter = 0

		printToFile(new File(battlefile)) {
			op =>
				val writer = new BufferedWriter(op)
				writer.write("clide,defender\n")

				response_time(1000)("write to file") {
					val rand = new Random()
					clides foreach {
						clide =>
							var picked = getRandom(rand, clides)
							while (picked.equals(clide)) {
								picked = getRandom(rand, clides)
							}
							writer.write(clide + "," + picked + "\n")
							counter = counter + 1
					}
				}

				writer.flush()
		}

		println("end with: " + counter)
	}

	def getRandom(rand: Random, clides: List[String]): String = {
		clides(rand.nextInt(clides.size))
	}

	def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
		val p = new java.io.PrintWriter(f)
		try {
			op(p)
		} finally {
			p.close()
		}
	}
}
