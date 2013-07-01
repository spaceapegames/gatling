package com.spaceape.techtest

import java.util.concurrent.{TimeUnit, TimeoutException}
import org.jboss.netty.util.{Timeout, TimerTask, HashedWheelTimer}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.{FiniteDuration, Duration}
import akka.actor.ActorSystem
import scala.actors.threadpool.ExecutorService


/**
 * Space Ape Games
 */
object TimeoutFuture {

  val system = ActorSystem("timeout")
  def apply[A](timeout: FiniteDuration)(block: => A)(implicit ec: ExecutionContext): Future[A] = {

    val prom = Promise[A]

    // timeout logic
    system.scheduler.scheduleOnce(timeout) {
      prom tryFailure new java.util.concurrent.TimeoutException
    }

    // business logic
    Future {
      prom success block
    }

    prom.future
  }
}
