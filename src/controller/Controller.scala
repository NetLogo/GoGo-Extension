package org.nlogo.extensions.gogo.controller

import jssc. {SerialPort, SerialPortEventListener}

import Constants._

import org.nlogo.api.ExtensionException

class Controller(override protected val portName: String)
  extends HasPortsAndStreams
  with PortCloser
  with PortOpener
  with Waiter
  with OutputPortController
  with CommandWriter
  with SensorReader
  with BurstReaderManager
  with SerialPortEventListener {

  def currentPortName = portOpt map (_.getPortName) getOrElse "INVALID"

  def currentPort: Option[SerialPort] = portOpt

  //@ c@ per googo board protocol doc, ping returns an ack PLUS 3 version bytes.  however, these are quite often corrupted/truncated.
  //so we are just checking for the ack part in the writeAndWait() method (more rigorous check yielded many false failures on ping). CEB 7/3/13
  def ping():           Boolean = portOpt map { _ => writeAndWait(CmdPing) } getOrElse false
  def beep():           Boolean = portOpt map { _ => writeAndWait(CmdBeep, 0x00.toByte) } getOrElse false
  def led(on: Boolean): Boolean = portOpt map { _ => writeAndWait(if (on) CmdLedOn else CmdLedOff, 0x00.toByte) } getOrElse false

  def talkToOutputPorts(outputPortMask: Int) {
    writeAndWait(CmdTalkToOutputPort, outputPortMask.toByte)
  }

  def setOutputPortPower(level: Int) {
    if ((level < 0) || (level > 7)) throw new ExtensionException("Power level out of range: " + level)
    writeAndWait((CmdOutputPortPower | level << 2).toByte)
  }

  def setServoPosition(value: Int) {
    if ((value < 20) || (value > 40)) throw new ExtensionException("Requested servo position (%s) is out of safe range (20-40)".format(value))
    writeAndWait(CmdPwmServo, value.toByte)
  }


}
