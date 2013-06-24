package com.spaceape.techtest

import com.spaceape.panda.proto.Commands.{ ClientServerMessageHeader, ServerClientMessageHeader, BaseResp, BaseReq }
import io.gatling.core.session.Session

/**
 * Space Ape Games
 */
trait ReqRepProcessor {

	def processReq(req: BaseReq.Builder, session: Session): BaseReq
	def processResp(res: BaseResp, session: Session): Session

}

trait SocketProcessor {

	def processReq(header: ClientServerMessageHeader, payload: Array[Byte])
	def processResp(header: ServerClientMessageHeader, payload: Array[Byte])

}