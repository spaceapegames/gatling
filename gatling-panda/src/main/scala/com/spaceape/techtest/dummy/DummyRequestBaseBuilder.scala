package com.spaceape.techtest.dummy

import io.gatling.core.session._
import com.spaceape.techtest.RepositoryFactory

/**
 * Space Ape Games
 */

object DummyRequestBaseBuilder {

	/**
	 * This method is used in DSL to declare a new HTTP request
	 */
	def dummyrequest(requestName: Expression[String], clide: Expression[String])(implicit repoFactory: RepositoryFactory) = DummyRequestActionBuilder(requestName, clide)
}

class DummyRequestBaseBuilder() {

}
