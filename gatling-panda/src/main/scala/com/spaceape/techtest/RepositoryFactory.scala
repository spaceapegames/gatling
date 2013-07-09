package com.spaceape.techtest

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import com.ning.http.client.AsyncHttpClient
import io.gatling.core.action._
import com.spaceape.game.security.AuthenticationConfiguration
import io.gatling.http.ahc.HttpClient
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel.{ SimpleChannelHandler, Channels, ChannelPipelineFactory }
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder
import com.spaceape.betsnap.future.FutureService
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousSocketChannel}

/**
 * Space Ape Games
 */
class RepositoryFactory {

	var serviceUrl: String = "http://loadtest-panda-game-service-0.use1a.apelabs.net:8024/v1/reqrep"
	var socketGateway: String = "localhost"
	var socketGatewayPort: Int = 8000
	var frame_len_bytes = 4
	var max_message_size = 200000
	val executorService = Executors.newCachedThreadPool()
	implicit val ec = ExecutionContext.fromExecutorService(executorService)
  val futureService = new FutureService()
	val client = new AsyncHttpClient(HttpClient.defaultAhcConfig)
	system.registerOnTermination(client.close)
  val asyncChannelGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 100);
  val asyncSocketClient = AsynchronousSocketChannel.open(asyncChannelGroup);

	val authConfig: AuthenticationConfiguration = new AuthenticationConfiguration


	def shutdown() {
		// Shut down thread pools to exit.
		println("Stop")
	}

}
