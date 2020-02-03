package net.novaware.chip8.core.memory

import net.novaware.chip8.core.cpu.register.Registers
import spock.lang.Shared
import spock.lang.Specification

class MemoryMapSpec extends Specification {

    @Shared
    Registers registers = new Registers()

    @Shared
    MemoryMap instance = new MemoryMap(registers.getVariables())

    def "should properly size memory segments" () {
        expect:
        instance.getInterpreter().getSize() == MemoryMap.INTERPRETER_SIZE
        instance.getProgram().getSize() == MemoryMap.PROGRAM_SIZE
        instance.getStack().getSize() == MemoryMap.STACK_SIZE
        instance.getInterpreterRam().getSize() == MemoryMap.INTERPRETER_RAM_SIZE
        instance.getVariables().getSize() == MemoryMap.VARIABLES_SIZE
        instance.getDisplayIo().getSize() == MemoryMap.DISPLAY_IO_SIZE
        instance.getCpuMemory().getSize() == 4096
    }

    def "should return correct segment with given address" () {
        given:
        def memory = instance.getCpuMemory() as MappedMemory

        when:
        def segment = memory.getSegment(address).ref

        then:
        segment.is(expectedSegment)

        where:

        address                     || expectedSegment
        MemoryMap.INTERPRETER_START || instance.getInterpreter()
        (short) 0x002               || instance.getInterpreter()
        MemoryMap.INTERPRETER_END   || instance.getInterpreter()

        MemoryMap.PROGRAM_START     || instance.getProgram()
        (short) 0x203               || instance.getProgram()
        MemoryMap.PROGRAM_END       || instance.getProgram()

        //TODO: add more tests that verify the whole mapped memoryMap
    }

    def "should translate absolute address into segment space" () {
        given:
        def memory = instance.getCpuMemory() as MappedMemory
        short address = 0x202

        def segment = memory.getSegment(address)

        expect:
        (short) 0x002 == memory.translateToSegmentAddress(segment, address)

    }

    //TODO: read / write tests, add nice logging

}
