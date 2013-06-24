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
package io.gatling.charts.result.reader

import io.gatling.core.result.{ Series, IntVsTimePlot }
import com.spaceape.http.client.{ DefaultHttpClientConfiguration, HttpClient }
import com.spaceape.common.core.rest.Json
import com.spaceape.http.client.Tokens._
import java.text.SimpleDateFormat
import java.util.{ TimeZone, Locale }
import com.typesafe.scalalogging.slf4j.Logging
import io.gatling.charts.util.Colors
import io.gatling.charts.util.Colors._
import io.gatling.core.result.IntVsTimePlot
import io.gatling.charts.result.reader.GraphiteSeries
import io.gatling.core.result.Series
import io.gatling.charts.util.Colors

/**
 * Space Ape Games
 */
trait GraphiteReader extends Logging {

	val runStart: Long
	val runEnd: Long
	val httpClient = HttpClient(new DefaultHttpClientConfiguration {})
	val sdf = new SimpleDateFormat("HH:mm_yyyyMMdd")
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"))

	def graphiteStatistics(target: String): Seq[Series[IntVsTimePlot]] = {
		//fetch the stats
		val t = "http://graphite.apelabs.net/render"
		var i = 0

		try {
			val start = sdf.format(runStart - 60000) //only accurate to the minute
			var httpResp = httpClient get (t ? ("target" `=` target) & ("from" `=` start) & ("until" `=` sdf.format(runEnd)) & ("format" `=` "json"))
			val series = Json.parse[List[GraphiteSeries]](httpResp.contentBytes)
			println(series)
			var result = series.map { s =>
				val timePlot = s.datapoints.
					//filter(dp => (dp._2 * 1000) >= runStart).
					map(dp => IntVsTimePlot(((dp._2 * 1000) - runStart).toInt, if (dp._1 == null) 0 else dp._1.toInt)).
					sortBy(_.time)
				val c = Colors(i % Colors.values.size)
				i = i + 1
				new Series(s.target, timePlot, List(c))
			}
			result = result.filter(_.data.exists(_.value > 10))

			println(result)
			result
		} catch {
			case e =>
				logger.info(s"Error on $target", e)
				Seq.empty[Series[IntVsTimePlot]]
		}
	}
}

case class GraphiteSeries(target: String, datapoints: List[(Double, Integer)]) {
}

object GraphiteReaderMain extends GraphiteReader {

	val runStart: Long = System.currentTimeMillis() - 20020000
	val runEnd: Long = System.currentTimeMillis()

	def main(args: Array[String]) {
		println(graphiteStatistics("servers.loadtest-*.cpu-*.cpu-user"))
	}
}

