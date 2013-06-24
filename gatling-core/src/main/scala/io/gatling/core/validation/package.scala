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
package io.gatling.core

import scala.annotation.tailrec

package object validation {

	implicit class SuccessWrapper[T](val value: T) extends AnyVal {
		def success: Validation[T] = Success(value)
	}

	implicit class FailureWrapper(val message: String) extends AnyVal {
		def failure[T]: Validation[T] = Failure(message)
	}

	implicit class ValidationList[T](val validations: List[Validation[T]]) extends AnyVal {
		def sequence: Validation[List[T]] = {

			val t: List[T] = validations.map(_ match {
				case Success(v) => v
			})

			Success(t).asInstanceOf[Validation[List[T]]]
		}
	}
}