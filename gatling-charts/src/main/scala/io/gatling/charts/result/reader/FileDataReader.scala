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

import java.io.{ FileInputStream, InputStream }

import scala.collection.mutable
import scala.io.Source

import com.typesafe.scalalogging.slf4j.Logging

import io.gatling.charts.result.reader.buffers.{ CountBuffer, RangeBuffer }
import io.gatling.charts.result.reader.stats.StatsHelper
import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.config.GatlingFiles.simulationLogDirectory
import io.gatling.core.result._
import io.gatling.core.result.message.{ GroupMessageType, KO, OK, RequestMessageType, RunMessage, RunMessageType, ScenarioMessageType, Status }
import io.gatling.core.result.reader.{ DataReader, GeneralStats }
import io.gatling.core.util.DateHelper.parseTimestampString
import io.gatling.core.result.Group
import io.gatling.core.result.RequestStatsPath
import scala.Some
import io.gatling.core.result.IntRangeVsTimePlot
import io.gatling.core.result.GroupStatsPath
import io.gatling.core.result.IntVsTimePlot
import io.gatling.core.result.message.RunMessage

object FileDataReader {
	val logStep = 100000
	val secMillisecRatio = 1000.0
	val noPlotMagicValue = -1L
	val simulationFilesNamePattern = """.*\.log"""
}

class FileDataReader(runUuid: String) extends DataReader(runUuid) with GraphiteReader with Logging {

	println("Parsing log file(s)...")

	val inputFiles = simulationLogDirectory(runUuid, create = false).files
		.collect { case file if (file.name.matches(FileDataReader.simulationFilesNamePattern)) => file.jfile }
		.toList

	logger.info(s"Collected $inputFiles from $runUuid")
	require(!inputFiles.isEmpty, "simulation directory doesn't contain any log file.")

	private def doWithInputFiles[T](f: Iterator[String] => T): T = {

		def multipleFileIterator(streams: Seq[InputStream]): Iterator[String] = streams.map(Source.fromInputStream(_, configuration.core.encoding).getLines).reduce((first, second) => first ++ second)

		val streams = inputFiles.map(new FileInputStream(_))
		try f(multipleFileIterator(streams))
		finally streams.foreach(_.close)
	}

	private def firstPass(records: Iterator[String]) = {

		logger.info("First pass")

		var count = 0
		var runStart = Long.MaxValue
		var runEnd = Long.MinValue
		val runMessages = mutable.ListBuffer.empty[RunMessage]

		records.foreach { line =>
			count += 1
			if (count % FileDataReader.logStep == 0) logger.info(s"First pass, read $count lines")

			line match {
				case RequestMessageType(array) =>
					runStart = math.min(runStart, array(5).toLong)
					runEnd = math.max(runEnd, array(8).toLong)

				case ScenarioMessageType(array) =>
					runStart = math.min(runStart, array(3).toLong)
					runEnd = math.max(runEnd, array(4).toLong)

				case RunMessageType(array) =>
					runMessages += RunMessage(parseTimestampString(array(1)), array(2), array(3).trim)
				case _ =>
			}
		}

		logger.info(s"First pass done: read $count lines")

		(runStart, runEnd, runMessages.head)
	}

	val (runStart, runEnd, runMessage) = doWithInputFiles(firstPass)

	val step = StatsHelper.step(math.floor(runStart / FileDataReader.secMillisecRatio).toInt, math.ceil(runEnd / FileDataReader.secMillisecRatio).toInt, configuration.charting.maxPlotsPerSeries) * FileDataReader.secMillisecRatio
	val bucketFunction = StatsHelper.bucket(_: Int, 0, (runEnd - runStart).toInt, step, step / 2)
	val buckets = StatsHelper.bucketsList(0, (runEnd - runStart).toInt, step)

	private def secondPass(bucketFunction: Int => Int)(records: Iterator[String]): ResultsHolder = {

		logger.info("Second pass")

		val resultsHolder = new ResultsHolder(runStart, runEnd)

		var count = 0

		records
			.foreach { line =>
				count += 1
				if (count % FileDataReader.logStep == 0) logger.info(s"Second pass, read $count lines")

				line match {
					case RequestMessageType(array) => resultsHolder.addRequestRecord(RecordParser.parseRequestRecord(array, bucketFunction, runStart))
					case GroupMessageType(array) => resultsHolder.addGroupRecord(RecordParser.parseGroupRecord(array, bucketFunction, runStart))
					case ScenarioMessageType(array) => resultsHolder.addScenarioRecord(RecordParser.parseScenarioRecord(array, bucketFunction, runStart))
					case _ =>
				}
			}

		logger.info(s"Second pass: read $count lines")

		resultsHolder
	}

	val resultsHolder = doWithInputFiles(secondPass(bucketFunction))

	println("Parsing log file(s) done")

	def statsPaths: List[StatsPath] =
		resultsHolder.groupAndRequestsNameBuffer.map.toList.map {
			case (path @ RequestStatsPath(request, group), time) => (path, (time, group.map(_.hierarchy.size + 1).getOrElse(0)))
			case (path @ GroupStatsPath(group), time) => (path, (time, group.hierarchy.size))
			case _ => throw new UnsupportedOperationException
		}.sortBy(_._2).map(_._1)

	def scenarioNames: List[String] = resultsHolder.scenarioNameBuffer
		.map
		.toList
		.sortBy(_._2)
		.map(_._1)

	def numberOfActiveSessionsPerSecond(scenarioName: Option[String]): Seq[IntVsTimePlot] = resultsHolder
		.getSessionDeltaPerSecBuffers(scenarioName)
		.compute(buckets)

	private def countBuffer2IntVsTimePlots(buffer: CountBuffer): Seq[IntVsTimePlot] = buffer
		.map
		.values.toSeq
		.map(plot => plot.copy(value = (plot.value / step * FileDataReader.secMillisecRatio).toInt))
		.sortBy(_.time)

	def numberOfRequestsPerSecond(status: Option[Status], requestName: Option[String], group: Option[Group]): Seq[IntVsTimePlot] =
		countBuffer2IntVsTimePlots(resultsHolder.getRequestsPerSecBuffer(requestName, group, status))

	def numberOfTransactionsPerSecond(status: Option[Status], requestName: Option[String], group: Option[Group]): Seq[IntVsTimePlot] =
		countBuffer2IntVsTimePlots(resultsHolder.getTransactionsPerSecBuffer(requestName, group, status))

	def responseTimeDistribution(slotsNumber: Int, requestName: Option[String], group: Option[Group]): (Seq[IntVsTimePlot], Seq[IntVsTimePlot]) = {

		// get main and max for request/all status
		val requestStats = resultsHolder.getGeneralStatsBuffers(requestName, group, None).stats
		val min = requestStats.min
		val max = requestStats.max

		val size = requestStats.count
		val step = StatsHelper.step(min, max, 100)
		val halfStep = step / 2
		val buckets = StatsHelper.bucketsList(min, max, step)
		val ok = resultsHolder.getGeneralStatsBuffers(requestName, group, Some(OK)).map.values.toSeq
		val ko = resultsHolder.getGeneralStatsBuffers(requestName, group, Some(KO)).map.values.toSeq

		val bucketFunction = StatsHelper.bucket(_: Int, min, max, step, halfStep)

		def process(buffer: Seq[IntVsTimePlot]): Seq[IntVsTimePlot] = {

			val bucketsWithValues = buffer
				.map(record => (bucketFunction(record.time), record))
				.groupBy(_._1)
				.map {
					case (responseTimeBucket, recordList) =>

						val sizeBucket = recordList.foldLeft(0) {
							(partialSize, record) => partialSize + record._2.value
						}

						(responseTimeBucket, math.round(sizeBucket * 100.0 / size).toInt)
				}
				.toMap

			buckets.map {
				bucket => IntVsTimePlot(bucket, bucketsWithValues.getOrElse(bucket, 0))
			}
		}

		(process(ok), process(ko))
	}

	def generalStats(status: Option[Status], requestName: Option[String], group: Option[Group]): GeneralStats = resultsHolder
		.getGeneralStatsBuffers(requestName, group, status)
		.stats

	def numberOfRequestInResponseTimeRange(requestName: Option[String], group: Option[Group]): Seq[(String, Int)] = {

		val counts = resultsHolder.getResponseTimeRangeBuffers(requestName, group)
		val lowerBound = configuration.charting.indicators.lowerBound
		val higherBound = configuration.charting.indicators.higherBound

		List((s"t < $lowerBound ms", counts.low),
			(s"$lowerBound ms < t < $higherBound ms", counts.middle),
			(s"t > $higherBound ms", counts.high),
			("failed", counts.ko))
	}

	private def rangeBuffer2IntRangeVsTimePlots(buffer: RangeBuffer): Seq[IntRangeVsTimePlot] = buffer
		.map
		.values
		.toSeq
		.sortBy(_.time)

	def responseTimeGroupByExecutionStartDate(status: Status, requestName: Option[String], group: Option[Group]): Seq[IntRangeVsTimePlot] =
		rangeBuffer2IntRangeVsTimePlots(resultsHolder.getResponseTimePerSecBuffers(requestName, group, Some(status)))

	def latencyGroupByExecutionStartDate(status: Status, requestName: Option[String], group: Option[Group]): Seq[IntRangeVsTimePlot] =
		rangeBuffer2IntRangeVsTimePlots(resultsHolder.getLatencyPerSecBuffers(requestName, group, Some(status)))

	def responseTimeAgainstGlobalNumberOfRequestsPerSec(status: Status, requestName: Option[String], group: Option[Group]): Seq[IntVsTimePlot] = {

		val globalCountsByBucket = resultsHolder.getRequestsPerSecBuffer(None, None, None).map

		resultsHolder
			.getResponseTimePerSecBuffers(requestName, group, Some(status))
			.map
			.toSeq
			.map {
				case (bucket, responseTimes) =>
					val count = globalCountsByBucket(bucket).value
					IntVsTimePlot(math.round(count / step * 1000).toInt, responseTimes.higher)
			}.sortBy(_.time)
	}

	def graphiteCpuStatistics(): Seq[Series[IntVsTimePlot]] = {
		graphiteStatistics(configuration.charting.graphiteCpuTarget)
	}

}