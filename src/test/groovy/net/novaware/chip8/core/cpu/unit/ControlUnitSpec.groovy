package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.Registers
import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.PhysicalMemory
import spock.lang.Ignore
import spock.lang.Specification

class ControlUnitSpec extends Specification {

    Registers registers = new Registers()

    Memory memory = new PhysicalMemory("test", 4096)

    ControlUnit cu = new ControlUnit(registers, memory, null, null, null, null)

    def "should fetch instruction from memory pointed by PC"() {
        given:
        registers.getProgramCounter().set(0x200)

        memory.setByte(0x200 as short, 0x12 as byte)
        memory.setByte(0x201 as short, 0x34 as byte)

        when:
        cu.fetch()

        then:
        registers.getFetchedInstruction().getAsInt() == 0x1234
    }

    @Ignore("enable when strict / lenient mode is implemented")
    def "should fail fast when PC overflows 12 bits"() {
        given:
        registers.getProgramCounter().set(0x1001)

        when:
        cu.fetch()

        then:
        thrown(AssertionError)
    }
}
