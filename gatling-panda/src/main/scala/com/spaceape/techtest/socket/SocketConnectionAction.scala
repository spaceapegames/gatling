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
import com.spaceape.techtest.{TimeoutFuture, RepositoryFactory}
import org.jboss.netty.handler.codec.frame.{ FrameDecoder, LengthFieldBasedFrameDecoder }
import java.net.Socket
import java.io.{BufferedOutputStream, BufferedInputStream, InputStreamReader, BufferedReader}
import scala.util.{Failure, Success}

class SocketConnectionAction(val requestName: Expression[String], val next: ActorRef)(implicit repoFactory: RepositoryFactory) extends Interruptable {
  implicit val ec = repoFactory.ec

	def execute(session: Session) {
		// Start the connection attempt.
		def connect(requestName: String) {
			val startDate = System.currentTimeMillis()

      val future = TimeoutFuture(10 seconds){
        new SocketClientThread(session("clide").as[String])
      }

      future onComplete {
        case Success(socketClient) =>
          DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, requestName,
            startDate, startDate, System.currentTimeMillis(), System.currentTimeMillis(),
            OK, None))

          val newSession = session.set(SessionKey.SocketClient,socketClient)

          next ! newSession


        case Failure(exception) =>
          DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, requestName,
            startDate, startDate, System.currentTimeMillis(), System.currentTimeMillis(),
            KO, Some(exception.getMessage)))

          next ! session
      }
		}

		val execution = for {
			resolvedRequestName <- requestName(session)
		} yield connect(resolvedRequestName)

		execution
	}
}

case class SendMessage(requestName: String, session: Session, sendTime: Long, header: ClientServerMessageHeader, payload: Array[Byte]) {

}

case class ReceivedMessage(receivingEndTime: Long,finishReadTime: Long, header: ServerClientMessageHeader, payload: Array[Byte]) {

}

case class SendComplete(status: Status, requestSendingEndTime: Long, receivingEndTime: Long, request: SendMessage, response: Option[ReceivedMessage], message: Option[String]) {

}

class SocketClientThread(clide: String)(implicit repoFactory: RepositoryFactory) {
  implicit val ec = repoFactory.ec

  val socket = new Socket(repoFactory.socketGateway,repoFactory.socketGatewayPort)
  val in = new BufferedInputStream(socket.getInputStream())
  val output = new BufferedOutputStream(socket.getOutputStream())
  val connected = true
  val FRAME_LENGTH_SIZE = 4
  val HEADER_LENGTH_SIZE = 4

  val frameSizeBytes = new Array[Byte](FRAME_LENGTH_SIZE)
  val headerSizeBytes = new Array[Byte](HEADER_LENGTH_SIZE)

  private def readComplete(buffer: Array[Byte]) {
    var read = 0
    while(read != buffer.length){
      val readSize = in.read(buffer,read,buffer.length-read)
      if (readSize == -1){
        throw new Exception("input stream closed")
      }

      read += readSize
    }
  }

  def sendMessage(s: SendMessage, callback: ActorRef){

    val startTime = System.currentTimeMillis()
    val future = TimeoutFuture(10 seconds){
      val startSendStartedDate = System.currentTimeMillis()
      output.write(SocketGatewayClientEncoder.encode(s.header.toByteArray,s.payload))
      output.flush()
      val startSendCompleteDate = System.currentTimeMillis()

      readComplete(frameSizeBytes)
      val receivingTime = System.currentTimeMillis()
      val nextFrameSize = IOUtil.byteArrayToInt(frameSizeBytes)
      readComplete(headerSizeBytes)
      val headerSize = IOUtil.byteArrayToInt(headerSizeBytes)
      val payloadSize = nextFrameSize - headerSize - HEADER_LENGTH_SIZE
      val headerBuffer = new Array[Byte](headerSize)
      val payloadBuffer = new Array[Byte](payloadSize)
      readComplete(headerBuffer)
      readComplete(payloadBuffer)
      val receiveCompleteTime = System.currentTimeMillis()

      val header = ServerClientMessageHeader.parseFrom(headerBuffer)

      SendComplete(OK,startSendStartedDate,startSendCompleteDate,s,Some(ReceivedMessage(receivingTime,receiveCompleteTime,header,payloadBuffer)),None)
    }

    future onComplete {
      case Success(sendComplete) =>
        callback ! sendComplete
      case Failure(exception) =>
        callback ! SendComplete(KO,startTime,startTime,s,None,Some(exception.getMessage))
    }
  }

  def close(){
    in.close()
    output.close()
    socket.close()
  }
}

object SocketGatewayClientEncoder {
	val FRAME_LENGTH_SIZE = 4;

	def encode(header: Array[Byte], payload: Array[Byte]) = {
		val headerLenData = IOUtil.intToByteArray(header.size)
		val buff = ChannelBuffers.buffer(FRAME_LENGTH_SIZE + headerLenData.size + header.size + payload.size)
		buff.writeBytes(IOUtil.intToByteArray(headerLenData.size + header.size + payload.size))
		buff.writeBytes(headerLenData);
		buff.writeBytes(header);
		buff.writeBytes(payload);
		buff.array()
	}
}
