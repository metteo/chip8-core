package net.novaware.chip8.core.bus

import net.novaware.chip8.core.memory.Memory
import spock.lang.Specification

class MemoryBusSpec extends Specification {

    def "should allow read under specified address" (){
        given:
        Memory memory = Mock()
        Bus instance = new MemoryBus(memory)

        short address = 0x200 as short

        when:
        instance.specify(address)
        instance.readByte()

        then:

        1 * memory.getByte(address)
    }

    def "should allow write under specified address" (){
        given:
        Memory memory = Mock()
        Bus instance = new MemoryBus(memory)

        short address = 0x201
        byte data = 0x23

        when:
        instance.specify(address)
        instance.writeByte(data)

        then:
        1 * memory.setByte(address, data)
    }
}
