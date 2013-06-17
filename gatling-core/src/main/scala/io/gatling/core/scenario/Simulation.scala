/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.core.scenario

import io.gatling.core.config.{ Protocol, ProtocolRegistry }
import io.gatling.core.structure.{ Assertion, Metric, ProfiledScenarioBuilder }

abstract class Simulation {

	private[scenario] var _scenarios = Seq.empty[ProfiledScenarioBuilder]
	private[scenario] var _protocols = Seq.empty[Protocol]
	private[scenario] var _assertions = Seq.empty[Assertion]

	def scenarios: Seq[Scenario] = {
		require(!_scenarios.isEmpty, "No scenario set up")
		ProtocolRegistry.setUp(_protocols)
		_scenarios.map(_.build)
	}

	def protocols = _protocols
	def assertions = _assertions

	def setUp(scenario: ProfiledScenarioBuilder, scenarios: ProfiledScenarioBuilder*) = {
		_scenarios = scenario :: scenarios.toList
		new SetUp(this) with Protocols with Assertions
	}

	def tearDown {

	}

	class SetUp(val simulation: Simulation)
	trait Protocols { this: SetUp =>
		def protocols(protocol: Protocol, protocols: Protocol*) = {
			simulation._protocols = protocol :: protocols.toList
			new SetUp(simulation) with Assertions
		}
	}
	trait Assertions { this: SetUp =>
		def assertions(metric: Metric, metrics: Metric*) {
			simulation._assertions = metric.assertions ++ metrics.flatMap(_.assertions)
		}
	}
}