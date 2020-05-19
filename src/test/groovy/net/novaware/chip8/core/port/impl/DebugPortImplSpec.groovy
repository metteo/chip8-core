package net.novaware.chip8.core.port.impl

import net.novaware.chip8.core.cpu.register.ByteRegister
import net.novaware.chip8.core.cpu.unit.PowerMgmt
import net.novaware.chip8.core.port.DebugPort
import spock.lang.Specification

class DebugPortImplSpec extends Specification {

    def delay = new ByteRegister("DT")

    def sound = new ByteRegister("ST")

    def cpuState = new ByteRegister("PS")

    def powerMgmt = new PowerMgmt(cpuState)

    def instance = new DebugPortImpl(delay, sound, cpuState, powerMgmt)

    def "should connect and disconnect receiver"() {
        given:
        def mockReceiver = Mock(DebugPort.Receiver)

        when:
        instance.connect(mockReceiver)

        then:
        instance.hasReceiver()

        and:
        instance.disconnect()
        !instance.hasReceiver()
    }

    def "should attach to registers and report their changes" () {
        def mockReceiver = Mock(DebugPort.Receiver)

        instance.connect(mockReceiver)

        when:
        instance.attachToRegister()
        delay.set(3)
        sound.set(4)
        powerMgmt.sleep()

        then:
        delay.pubSub.getSubscribers().size() == 1
        1 * mockReceiver.onDelayTimerChange(3)

        sound.pubSub.getSubscribers().size() == 1
        1 * mockReceiver.onSoundTimerChange(4)

        cpuState.pubSub.getSubscribers().size() == 1
        1 * mockReceiver.onStateChange(true)
    }

    def "should forward exception and frequency updates"() {
        given:
        def mockReceiver = Mock(DebugPort.Receiver)

        instance.connect(mockReceiver)

        Exception e = new IllegalArgumentException()

        when:
        instance.onException(e)
        instance.onCpuFrequencyChange(501)

        then:
        1 * mockReceiver.onException(e)
        1 * mockReceiver.onCpuFrequencyChange(501)
    }
}
