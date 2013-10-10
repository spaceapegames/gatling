package com.spaceape.panda.model

import java.io._
import com.spaceape.game.security.{ AuthenticationConfiguration, TicketGenerator }

/**
 * Space Ape Games
 */
object GenerateAuthTokens extends TicketGenerator {

	val authConfig: AuthenticationConfiguration = new AuthenticationConfiguration

	def main(args: Array[String]) {

		var input = new BufferedReader(new InputStreamReader(GenerateAuthTokens.getClass.getResourceAsStream("/data/test-clides-loadtest.csv")))
		var output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("processed.csv"))))
		var line: String = null
		line = input.readLine()
		while (line != null) {
			var ticket = generateTicket(line, System.currentTimeMillis())
			output.write(line)
			output.write(",")
			output.write(ticket)
			output.write("\n")

			line = input.readLine()
		}

		output.close()
		input.close()
	}
}
