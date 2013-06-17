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
package io.gatling.charts.component.impl

import io.gatling.charts.component.{ Component, ComponentLibrary }
import io.gatling.core.result.{ IntRangeVsTimePlot, IntVsTimePlot, PieSlice, Series }

/**
 * Mock implementation that is removed from the binary.
 * A unique implementation is expected to be present in the classpath.
 *
 * @author stephanelandelle
 */
class ComponentLibraryImpl extends ComponentLibrary {

	def getAllSessionsJs(runStart: Long, series: Series[IntVsTimePlot]): String = throw new UnsupportedOperationException
	def getActiveSessionsChartComponent(runStart: Long, series: Seq[Series[IntVsTimePlot]]): Component = throw new UnsupportedOperationException
	def getRequestsChartComponent(runStart: Long, allRequests: Series[IntVsTimePlot], failedRequests: Series[IntVsTimePlot], succeededRequests: Series[IntVsTimePlot], pieSeries: Series[PieSlice]): Component = throw new UnsupportedOperationException
	def getTransactionsChartComponent(runStart: Long, allTransactions: Series[IntVsTimePlot], failedTransactions: Series[IntVsTimePlot], succeededTransactions: Series[IntVsTimePlot], pieSeries: Series[PieSlice]): Component = throw new UnsupportedOperationException
	def getRequestDetailsResponseTimeChartComponent(runStart: Long, responseTimesSuccess: Series[IntRangeVsTimePlot], responseTimesFailures: Series[IntRangeVsTimePlot]): Component = throw new UnsupportedOperationException
	def getRequestDetailsResponseTimeDistributionChartComponent(responseTimesSuccess: Series[IntVsTimePlot], responseTimesFailures: Series[IntVsTimePlot]): Component = throw new UnsupportedOperationException
	def getRequestDetailsLatencyChartComponent(runStart: Long, latencySuccess: Series[IntRangeVsTimePlot], latencyFailures: Series[IntRangeVsTimePlot]): Component = throw new UnsupportedOperationException
	def getRequestDetailsScatterChartComponent(successData: Series[IntVsTimePlot], failuresData: Series[IntVsTimePlot]): Component = throw new UnsupportedOperationException
	def getRequestDetailsIndicatorChartComponent: Component = throw new UnsupportedOperationException
	def getNumberOfRequestsChartComponent: Component = throw new UnsupportedOperationException
	def getGroupDurationChartComponent(runStart: Long, durationsSuccess: Series[IntRangeVsTimePlot], durationsFailure: Series[IntRangeVsTimePlot]): Component = throw new UnsupportedOperationException
	def getGroupDetailsDurationDistributionChartComponent(durationsSuccess: Series[IntVsTimePlot], durationsFailure: Series[IntVsTimePlot]): Component = throw new UnsupportedOperationException
	def getServerStatsChartComponent(runStart: Long, runEnd: Long, cpuPerformance: Seq[Series[IntVsTimePlot]]): Component = throw new UnsupportedOperationException
}