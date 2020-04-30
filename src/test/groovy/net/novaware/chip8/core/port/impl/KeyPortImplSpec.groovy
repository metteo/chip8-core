package net.novaware.chip8.core.port.impl

import net.novaware.chip8.core.cpu.register.WordRegister
import net.novaware.chip8.core.port.KeyPort
import spock.lang.Specification

import java.util.function.Consumer

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte

class KeyPortImplSpec extends Specification {

    def inputRegister = new WordRegister("IN")
    def inputCheckReg = new WordRegister("IC")

    def instance = new KeyPortImpl(inputRegister, inputCheckReg)

    def "should construct and attach properly"() {
        when:
        instance.attachToRegister()

        then:
        inputRegister.pubSub.subscribers.isEmpty()
        !inputCheckReg.pubSub.subscribers.isEmpty()
    }

    def "should allow connection, receive input and disconnect"() {
        given:
        inputRegister.set(0b111)

        when:
        Consumer<KeyPort.InputPacket> endpoint = instance.connect({-> })
        endpoint.accept(new KeyPort.InputPacket() {
            @Override
            KeyPort.Direction getDirection() {
                return KeyPort.Direction.DOWN
            }

            @Override
            byte getKeyCode() {
                return 0xA as byte
            }
        })

        then:
        inputCheckReg.getAsInt() == 0
        inputRegister.getAsInt() == 0b10000000111

        and:
        endpoint.accept(new KeyPort.InputPacket() {
            @Override
            KeyPort.Direction getDirection() {
                return KeyPort.Direction.UP
            }

            @Override
            byte getKeyCode() {
                return 0x1 as byte
            }
        })

        then:
        inputCheckReg.getAsInt() == 0
        inputRegister.getAsInt() == 0b10000000101

        and:
        instance.disconnect()
        endpoint.accept(null) // doesn't matter what we send now, it shouldn't affect the port

        then:
        inputCheckReg.getAsInt() == 0
        inputRegister.getAsInt() == 0b10000000101 //no change from last time
    }

    def "should combine input checks and report on output"() {
        given:
        instance.attachToRegister()

        def keysUsed = new ArrayList()

        instance.connect({p ->
            for (int k = 0; k < 0x10; ++k) {
                if (p.isKeyUsed(ubyte(k))) {
                    keysUsed.add(k)
                }
            }
        })

        when:
        inputCheckReg.set(0x4)
        inputCheckReg.set(0xB)

        then:
        // first check adds 0x4
        // second check adds 0x4 again and then 0xB
        keysUsed == [0x4, 0x4, 0xB]
    }
}
