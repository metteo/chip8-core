package net.novaware.chip8.core.cpu

import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.MemoryModule
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters
import static net.novaware.chip8.core.memory.MappedMemoryHelper.newMappedMemory

class CpuIT extends Specification {

    Cpu.Config config = Mock()

    Cpu cpu

    Memory memory

    void setup() {
        def registers = newRegisters()
        memory = newMappedMemory(registers.getVariables())

        cpu = new Cpu(config, memory, registers)
        cpu.initialize()
    }

    def "should properly initialize cpu"() {
        expect:
        MemoryModule.PROGRAM_START == cpu.getRegisters().getProgramCounter().get()
        MemoryModule.STACK_START == cpu.getRegisters().getStackPointer().get()
        MemoryModule.STACK_START == cpu.getRegisters().getStackSegment().get()
    }

    def "should properly fetch first instruction"() {
        given:
        short address = 0x0200
        short instruction = 0x1234 //invalid but good for this test

        memory.setWord(address, instruction)

        when:
        cpu.cycle()

        then:
        def actual = cpu.getRegisters().getCurrentInstruction().get()
        actual == instruction
    }

    def "should properly fetch 'last' instruction"() {
        given:
        short address = 0x0FF0
        short instruction = 0x1234 as short

        memory.setWord(address, instruction)

        cpu.getRegisters().getProgramCounter().set(address)

        when:
        cpu.cycle()

        then:
        def actual = cpu.getRegisters().getCurrentInstruction().get()
        actual == instruction
    }

    def "should properly decode MVI / LD I instruction"() {
        given:
        short address = 0x0FF0
        short instruction = 0xA321 as short

        memory.setWord(address, instruction)

        cpu.getRegisters().getProgramCounter().set(address)

        when:
        cpu.cycle()

        then:
        def decoded = cpu.getRegisters().getDecodedInstruction()
        decoded[0].get() == (short)0xA000
        decoded[1].get() == (short)0x0321
        decoded[2].get() == (short)0x0000
        decoded[3].get() == (short)0x0000
    }
}
