package net.novaware.chip8.core.port.impl

import net.novaware.chip8.core.cpu.register.ByteRegister
import spock.lang.Specification

class AudioPortImplSpec extends Specification {

    def soundOn = new ByteRegister("SO")

    def instance = new AudioPortImpl(soundOn)

    def "should trigger receiver when sound is on"() {
        given:
        def gotPacket = false;
        instance.connect({ p -> gotPacket = p.isSoundOn() })
        instance.attachToRegister()

        when:
        soundOn.set(1)

        then:
        gotPacket

        and:
        instance.disconnect()
        soundOn.set(0)

        then:
        gotPacket //still on, receiver was disconnected
    }
}
