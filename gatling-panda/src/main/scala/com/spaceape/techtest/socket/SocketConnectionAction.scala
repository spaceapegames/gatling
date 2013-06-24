package com.spaceape.techtest.socket

import org.jboss.netty.channel._
import java.net.InetSocketAddress
import org.jboss.netty.buffer.{ ChannelBuffers, ChannelBuffer }
import com.spaceape.common.core.io.IOUtil
import com.spaceape.panda.proto.Commands.{ BaseResp, BaseReq, ClientServerMessageHeader, ServerClientMessageHeader }
import com.spaceape.common.core.logging.Loggable
import io.gatling.core.action._
import scala.concurrent.duration._
import io.gatling.core.session._
import akka.actor.{ Props, ActorRef }
import io.gatling.core.result.message.{ OK, KO, RequestMessage, Status }
import io.gatling.core.util.StringHelper._
import io.gatling.core.session.Session
import io.gatling.core.result.writer.DataWriter
import com.spaceape.http.client.header
import com.spaceape.techtest.RepositoryFactory
import org.jboss.netty.handler.codec.frame.{ FrameDecoder, LengthFieldBasedFrameDecoder }

class SocketConnectionAction(val requestName: Expression[String], val next: ActorRef)(implicit repoFactory: RepositoryFactory) extends Interruptable {

	def execute(session: Session) {
		// Start the connection attempt.
		def connect(requestName: String) {
			val startDate = System.currentTimeMillis()

			val future = repoFactory.socketGatewayBootstrap.connect(new InetSocketAddress(repoFactory.socketGateway, repoFactory.socketGatewayPort));
			future.addListener(new ChannelFutureListener {
				def operationComplete(future: ChannelFuture) {

					var nextSession = session
					var status: Status = KO
					var message: Option[String] = None

					if (future.getCause != null) {
						message = Some(future.getCause().getMessage)
					}

					if (future.isSuccess) {
						status = OK
						val actor = system.actorOf(Props(new SocketClientActor(future.getChannel, session("clide").as[String])))
						nextSession = session.set("socketClientActor", actor)
						actor ! new ChannelConnected()
					}

					DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, requestName,
						startDate, startDate, System.currentTimeMillis(), System.currentTimeMillis(),
						status, message))

					next ! nextSession
				}
			})
		}

		val execution = for {
			resolvedRequestName <- requestName(session)
		} yield connect(resolvedRequestName)

		execution
	}
}

case class ChannelConnected() {

}

case class TimeoutCheck() {

}

case class StopClient() {

}

case class ChannelError(message: String) {

}

case class SendMessage(requestName: String, session: Session, sendTime: Long, header: ClientServerMessageHeader, payload: Array[Byte]) {

}

case class ReceivedMessage(receivingEndTime: Long, header: ServerClientMessageHeader, payload: Array[Byte]) {

}

case class SendComplete(status: Status, requestSendingEndTime: Long, receivingEndTime: Long, request: SendMessage, response: Option[ReceivedMessage], message: Option[String]) {

}

class SocketClientActor(channel: Channel, clide: String)(implicit repoFactory: RepositoryFactory) extends BaseActor {

	implicit val ec = repoFactory.ec

	var startTime: Long = 0
	var requestSendingEndTime: Long = 0
	var responseReceivingTime: Long = 0
	var finishTime: Long = 0
	var timeout = 10 seconds
	var handler = new SocketClientHandler(self)
	var sendMessage: SendMessage = null
	var sendingActor: ActorRef = null

	def receive = {
		case c: ChannelConnected =>
			channel.getPipeline.addLast("client handler", handler)
		case s: SendMessage =>
			//println("sending " + s.header.getTokenid)
			sendingActor = sender
			sendMessage = s
			if (channel.isOpen) {
				startTime = System.currentTimeMillis()
				SocketGatewayClientEncoder.encode(channel, s.header.toByteArray, s.payload)
				requestSendingEndTime = System.currentTimeMillis()
				context.system.scheduler.scheduleOnce(timeout, self, new TimeoutCheck())
			} else {
				sendingActor ! new SendComplete(KO, System.currentTimeMillis(), System.currentTimeMillis(), s, None, Some("Not connected"))
			}
		case r: ReceivedMessage =>
			startTime = 0
			//println("received " + r.header.getTokenid)
			sendingActor ! new SendComplete(OK, requestSendingEndTime, r.receivingEndTime, sendMessage, Some(r), None)
		case t: TimeoutCheck =>
			if (startTime > 0 && (System.currentTimeMillis() > (startTime + timeout.toMillis))) {
				sendingActor ! new SendComplete(KO, requestSendingEndTime, System.currentTimeMillis(), sendMessage, None, Some("TIMEOUT"))
				startTime = 0
			}
		case e: ChannelError =>
			if (startTime > 0 && (System.currentTimeMillis() > (startTime + timeout.toMillis))) {
				sendingActor ! new SendComplete(KO, requestSendingEndTime, System.currentTimeMillis(), sendMessage, None, Some("ERROR:" + e.message))
				startTime = 0
			}
		case s: StopClient =>
			channel.close()
	}

	case class SocketResponseWrapper(requestName: String, startTime: Long, requestSendingEndTime: Long, responseReceivingTime: Long, finishTime: Long) {

	}

	class SocketClientHandler(socketClientActor: ActorRef) extends FrameDecoder {

		def decode(ctx: ChannelHandlerContext, channel: Channel, b: ChannelBuffer): AnyRef = {

			if (nextFrameSize == -1)
				readNextFrameSize(b)

			if (nextFrameSize > 0 && b.readableBytes() >= nextFrameSize) {
				//read the header size
				b.readBytes(headerSizeBytes, 0, HEADER_LENGTH_SIZE)
				val headerSize = IOUtil.byteArrayToInt(headerSizeBytes)
				val payloadSize = nextFrameSize - headerSize - HEADER_LENGTH_SIZE
				val headerBuffer = new Array[Byte](headerSize)
				val payloadBuffer = new Array[Byte](payloadSize)
				b.readBytes(headerBuffer, 0, headerSize)
				val header = ServerClientMessageHeader.parseFrom(headerBuffer)
				b.readBytes(payloadBuffer, 0, payloadSize)

				val msg = new ReceivedMessage(receivingTime, header, payloadBuffer)

				nextFrameSize = -1

				socketClientActor ! msg

				return msg
			} else {
				return null
			}

		}

		override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
			e.getChannel().close();
			socketClientActor ! ChannelError(e.getCause.getMessage)
		}

		val FRAME_LENGTH_SIZE = 4
		val HEADER_LENGTH_SIZE = 4

		val frameSizeBytes = new Array[Byte](FRAME_LENGTH_SIZE)
		val headerSizeBytes = new Array[Byte](HEADER_LENGTH_SIZE)
		var nextFrameSize = -1
		var receivingTime: Long = 0

		def readNextFrameSize(b: ChannelBuffer) {
			if (b.readable()) {
				b.readBytes(frameSizeBytes, 0, FRAME_LENGTH_SIZE)
				nextFrameSize = IOUtil.byteArrayToInt(frameSizeBytes)
				receivingTime = System.currentTimeMillis()
			}
		}

	}
}

object SocketGatewayClientEncoder {
	val FRAME_LENGTH_SIZE = 4;

	def encode(c: Channel, header: Array[Byte], payload: Array[Byte]) {
		val headerLenData = IOUtil.intToByteArray(header.size)
		val buff = ChannelBuffers.buffer(FRAME_LENGTH_SIZE + headerLenData.size + header.size + payload.size)
		buff.writeBytes(IOUtil.intToByteArray(headerLenData.size + header.size + payload.size))
		buff.writeBytes(headerLenData);
		buff.writeBytes(header);
		buff.writeBytes(payload);
		c.write(buff)
	}
}
