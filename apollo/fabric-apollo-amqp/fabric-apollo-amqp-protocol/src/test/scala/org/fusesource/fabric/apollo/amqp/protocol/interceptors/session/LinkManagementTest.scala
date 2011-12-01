package org.fusesource.fabric.apollo.amqp.protocol.interceptors.session

import org.scalatest.matchers.ShouldMatchers
import org.apache.activemq.apollo.util.{FunSuiteSupport, Logging}
import org.fusesource.fabric.apollo.amqp.protocol.AMQPSession
import org.fusesource.fabric.apollo.amqp.protocol.utilities.Tasks
import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPFrame
import org.fusesource.fabric.apollo.amqp.codec.types._
import org.fusesource.fabric.apollo.amqp.protocol.interceptors.test_interceptors.{TestSendInterceptor, FrameDroppingInterceptor}
import collection.mutable.Queue


/**
 *
 */



class LinkManagementTest extends FunSuiteSupport with ShouldMatchers with Logging {
  implicit def source2source(s:Source) = s.asInstanceOf[org.fusesource.fabric.apollo.amqp.codec.interfaces.Source]
  implicit def target2target(t:Target) = t.asInstanceOf[org.fusesource.fabric.apollo.amqp.codec.interfaces.Target]

  test("Create session, send attach frame and cause link to be created") {
    val session = new AMQPSession
    val attach = new Attach("foo", 0L, Role.SENDER.getValue, SenderSettleMode.SETTLED.getValue, ReceiverSettleMode.FIRST.getValue, new Source(), new Target())
    
    session.head.outgoing = new TestSendInterceptor((frame:AMQPFrame,  tasks:Queue[() => Unit]) => {
      frame match {
        case t:AMQPTransportFrame =>
          t.getPerformative match {
            case a:Attach =>
              a.getRole should be (Role.RECEIVER.getValue)
              a.getName should be (attach.getName)
              // TODO - when these are implemented
              //a.getSndSettleMode should be (attach.getSndSettleMode)
              //a.getRcvSettleMode should be (attach.getRcvSettleMode)
              a.getSource should be (attach.getSource)
              a.getTarget should be (attach.getTarget)
          }
      }
      
    })

    session.head.outgoing = new FrameDroppingInterceptor
    session.head.incoming.receive(new AMQPTransportFrame(attach), Tasks())

  }

}