package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.Registers
import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.PhysicalMemory
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.instruction.InstructionType.*

class ControlUnitIT extends Specification {

    Memory memory = new PhysicalMemory("test", 4096)

    Registers registers = new Registers()

    ArithmeticLogic alu = new ArithmeticLogic(registers, memory)

    AddressGeneration agu = new AddressGeneration(registers, memory)

    StackEngine stackEngine = new StackEngine(registers, memory)

    GraphicsProcessing gpu = new GraphicsProcessing(registers, memory)

    ControlUnit cu = new ControlUnit(registers, memory, alu, agu, stackEngine, gpu)

    def "should clear the screen"() {
        given:
        short graphicsSegment = 0x100
        int graphicsSize = 32 * 64 / 8 as int
        registers.getGraphicSegment().set(graphicsSegment)

        //fill memoryMap at addresses -1 / +1 so we can see if the op goes out of boundary
        short start = graphicsSegment - 1 as short
        short end = graphicsSegment + graphicsSize as short

        for (short addr = start; addr <= end; addr++) {
            memory.setByte(addr, 0xFF as byte)
        }

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox00E0.opcode())

        when:
        cu.execute()

        then:
        for (short addr = graphicsSegment; addr < graphicsSegment + graphicsSize; addr++) {
            assert memory.getByte(addr) == 0 as byte
        }

        memory.getByte(start) == 0xFF as byte
        memory.getByte(end) == 0xFF as byte

        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should populate I register with address from instruction"() {
        given:

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxAMMM.opcode())
        instruction[1].set(0x0123)

        when:
        cu.execute()

        then:
        registers.getIndex().getAsInt() == 0x0123

        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should populate register V with number from instruction"() {
        given:

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox6XKK.opcode())
        instruction[1].set(0x0003)
        instruction[2].set(0x0045)

        when:
        cu.execute()

        then:
        registers.getVariable(3).get() == 0x45 as byte

        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should increment register V with number from instruction"() {
        given:

        registers.getVariable(4).set(0xAA as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox7XKK.opcode())
        instruction[1].set(0x0004)
        instruction[2].set(0x0054)

        when:
        cu.execute()

        then:
        registers.getVariable(4).get() == 0xFE as byte

        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should jump to location pointed to by the instruction"() {
        given:

        registers.getProgramCounter().set(0x0123)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox1MMM.opcode())
        instruction[1].set(0x0345)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x345 as short
    }

    def "should increment register I with value of register X"() {
        given:

        registers.getIndex().set(0x0123)
        registers.getVariable(4).set(0xEE as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX1E.opcode())
        instruction[1].set(0x4)

        when:
        cu.execute()

        then:
        registers.getIndex().get() == 0x0211 as short

        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should skip next instruction if V is equal to number in instruction"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox3XKK.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0x45)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x204 as short
    }

    def "should NOT skip instruction if V is equal to number in instruction"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox3XKK.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0x46)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short
    }

    def "should call instruction with given address"() {
        given:

        registers.getProgramCounter().set(0x322)
        registers.getStackPointer().set(0xFE0)
        registers.getStackSegment().set(0xFE0)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox2MMM.opcode())
        instruction[1].set(0x400)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x400 as short
        registers.getStackPointer().get() == 0xFE2 as short
        memory.getByte(0xFE2 as short) == 0x3 as short
        memory.getByte(0xFE3 as short) == 0x22 as short
    }

    def "should populate register Vx with number from register Vy"() {
        given:

        registers.getVariable(3).set(0x12 as byte)
        registers.getVariable(4).set(0x56 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY0.opcode())
        instruction[1].set(0x0003)
        instruction[2].set(0x0004)

        when:
        cu.execute()

        then:
        registers.getVariable(3).get() == 0x56 as byte
        registers.getVariable(4).get() == 0x56 as byte
    }

    def "should skip next instruction if key with value of Vx is NOT pressed"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x8 as byte)

        registers.getKeyState().set(0x7)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxEXA1.opcode())
        instruction[1].set(0xA)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x204 as short
    }

    def "should NOT skip instruction if key with value of Vx is pressed"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x8 as byte)

        registers.getKeyState().set(0x101)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxEXA1.opcode())
        instruction[1].set(0xA)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short
    }

    def "should fill registers V0 - VX with memory starting at I"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0x0).set(0 as byte)
        registers.getVariable(0x1).set(0 as byte)
        registers.getVariable(0x2).set(0 as byte)
        registers.getVariable(0x3).set(0x11 as byte)

        registers.getIndex().set(0xFFD)

        byte[] data = [ 0x98 as byte, 0x76 as byte, 0x54 as byte ]

        memory.setBytes(0xFFD as short, data, data.length)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX65.opcode())
        instruction[1].set(0x2)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short

        registers.getVariable(0x0).get() == 0x98 as byte
        registers.getVariable(0x1).get() == 0x76 as byte
        registers.getVariable(0x2).get() == 0x54 as byte
        registers.getVariable(0x3).get() == 0x11 as byte

        registers.getIndex().getAsInt() == 0xFFD //no change
    }

    def "should return from instruction call"() {
        given:
        registers.getStackSegment().set(0xFE0)

        registers.getProgramCounter().set(0x400)
        registers.getStackPointer().set(0xFE2)

        memory.setByte(0xFE2 as short, 0x3 as byte)
        memory.setByte(0xFE3 as short, 0x22 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox00EE.opcode())

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short //pc should be incremented here to prevent infinite loop
        registers.getStackPointer().get() == 0xFE0 as short
    }

    def "should set delay timer to given value" () {
        given:
        registers.getVariable(6).set(0xAB as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX15.opcode())
        instruction[1].set(0x6)

        when:
        cu.execute()

        then:
        registers.getDelay().get() == 0xAB as byte
        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should read delay timer into data register" () {
        given:
        registers.getDelay().set(0x12 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX07.opcode())
        instruction[1].set(0x6)

        when:
        cu.execute()

        then:
        registers.getVariable(0x6).get() == 0x12 as byte
        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should properly AND value of Vy into Vx"() {
        given:
        registers.getProgramCounter().set(0x400)

        registers.getVariable(0xA).set(0x34 as byte)
        registers.getVariable(0xB).set(0x56 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY2.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x402 as short

        registers.getVariable(0xA).getAsInt() == 0x14
        registers.getVariable(0xB).getAsInt() == 0x56
    }

    def "should properly shift Vx right (legacy mode) with overflow"() {
        given:
        registers.getProgramCounter().set(0x500)

        registers.getVariable(0xA).set(0x35 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY6.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x502 as short

        registers.getVariable(0xA).getAsInt() == 0x1A
        registers.getVariable(0xF).getAsInt() == 0x01
    }

    def "should properly shift Vx right (legacy mode) no overflow"() {
        given:
        registers.getProgramCounter().set(0x500)

        registers.getVariable(0xA).set(0x36 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY6.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x502 as short

        registers.getVariable(0xA).getAsInt() == 0x1B
        registers.getVariable(0xF).getAsInt() == 0x00
    }

    def "should skip next instruction if key with value of Vx is pressed"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x3 as byte)

        registers.getKeyState().set(0x8)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxEX9E.opcode())
        instruction[1].set(0xA)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x204 as short
    }

    def "should NOT skip instruction if key with value of Vx is NOT pressed"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x10 as byte)

        registers.getKeyState().set(0x8)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxEX9E.opcode())
        instruction[1].set(0xA)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short
    }

    def "should properly ADD (with carry) value of Vy into Vx (no overflow)"() {
        given:
        registers.getProgramCounter().set(0x322)

        registers.getVariable(0xC).set(0x34 as byte)
        registers.getVariable(0xD).set(0x56 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY4.opcode())
        instruction[1].set(0xC)
        instruction[2].set(0xD)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short

        registers.getVariable(0xC).getAsInt() == 0x8A
        registers.getVariable(0xD).getAsInt() == 0x56
        registers.getVariable(0xF).getAsInt() == 0x00
    }

    def "should properly ADD (with carry) value of Vy into Vx (overflow)"() {
        given:
        registers.getProgramCounter().set(0x322)

        registers.getVariable(0xC).set(0xAA as byte)
        registers.getVariable(0xD).set(0xBB as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY4.opcode())
        instruction[1].set(0xC)
        instruction[2].set(0xD)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short

        registers.getVariable(0xC).getAsInt() == 0x65
        registers.getVariable(0xD).getAsInt() == 0xBB
        registers.getVariable(0xF).getAsInt() == 0x01
    }

    def "should properly SUB (with borrow) value of Vy into Vx (no borrow)"() {
        given:
        registers.getProgramCounter().set(0x322)

        registers.getVariable(0xC).set(0x56 as byte)
        registers.getVariable(0xD).set(0x34 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY5.opcode())
        instruction[1].set(0xC)
        instruction[2].set(0xD)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short

        registers.getVariable(0xC).getAsInt() == 0x22
        registers.getVariable(0xD).getAsInt() == 0x34
        registers.getVariable(0xF).getAsInt() == 0x01
    }

    def "should properly SUB (with borrow) value of Vy into Vx (borrow)"() {
        given:
        registers.getProgramCounter().set(0x322)

        registers.getVariable(0xC).set(0xAA as byte)
        registers.getVariable(0xD).set(0xBB as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY5.opcode())
        instruction[1].set(0xC)
        instruction[2].set(0xD)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short

        registers.getVariable(0xC).getAsInt() == 0xEF
        registers.getVariable(0xD).getAsInt() == 0xBB
        registers.getVariable(0xF).getAsInt() == 0x00
    }

    def "should skip next instruction if V is NOT equal to number in instruction (SNE)"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox4XKK.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0x46)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x204 as short
    }

    def "should NOT skip instruction if V is equal to number in instruction (SNE)"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox4XKK.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0x45)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short
    }

    def "should properly XOR value of Vy into Vx"() {
        given:
        registers.getProgramCounter().set(0x400)

        registers.getVariable(0xA).set(0x34 as byte)
        registers.getVariable(0xB).set(0x56 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY3.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x402 as short

        registers.getVariable(0xA).getAsInt() == 0x62
        registers.getVariable(0xB).getAsInt() == 0x56
    }

    def "should set sound timer to given value" () {
        given:
        registers.getVariable(6).set(0xAB as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX18.opcode())
        instruction[1].set(0x6)

        when:
        cu.execute()

        then:
        registers.getSound().get() == 0xAB as byte
        registers.getProgramCounter().get() == 0x2 as short
    }

    def "should wait for key press, when none is pressed"() {
        given:
        registers.getProgramCounter().set(0x204)
        registers.getVariable(7).set(0x0 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX0A.opcode())
        instruction[1].set(0x7)

        when:
        cu.execute()

        then:
        registers.getKeyWait().get() != 0x0 as byte
        registers.getKeyState().getAsInt() == 0x0
        registers.getProgramCounter().get() == 0x204 as short // no change, we wait
    }

    def "should NOT wait for key press, when one is already pressed"() {
        given:
        registers.getProgramCounter().set(0x204)
        registers.getVariable(7).set(0x0 as byte)
        registers.getKeyState().set(0x3) //keys 0 and 1 pressed together
        registers.getKeyValue().set((byte)0x2) // 1 was registered last

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX0A.opcode())
        instruction[1].set(0x7)

        when:
        cu.execute()

        then:
        registers.getKeyWait().get() == 0x0 as byte
        registers.getVariable(0x7).getAsInt() == 0x2
        registers.getProgramCounter().get() == 0x206 as short // move forward
    }

    def "should store BCD representation of a number"() {
        given:
        registers.getProgramCounter().set(0x206)
        registers.getVariable(0xB).set((byte)123)
        registers.getIndex().set(0xE90)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX33.opcode())
        instruction[1].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x208 as short

        byte[] bcd = new byte[3]
        memory.getBytes((short)0xE90, bcd, 3)

        bcd == (byte[])[0x1, 0x2, 0x3]
    }

    def "should load address of the font sprite"() {
        given:
        registers.getProgramCounter().set(0x208)
        registers.getVariable(0xC).set((byte)9)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX29.opcode())
        instruction[1].set(0xC)

        def fontStartAddress = 0x0
        def spriteSize = 5 //bytes

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x20A as short
        registers.getIndex().getAsInt() == fontStartAddress + 9 * spriteSize
    }

    def "should generate random number from 0 - 255"() { //TODO: provide random abstraction in this test
        registers.getProgramCounter().set(0x20A)
        registers.getVariable(0xD).set((byte)123)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxCXKK.opcode())
        instruction[1].set(0xD)
        instruction[2].set(0x56)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x20C as short
        byte random = registers.getVariable(0xD).get()
        random >= (byte)(0x56 & 0x0)
        random <= (byte)(0x56 & 0xFF)
    }

    def "should call system instruction with given address"() {
        given:
        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox0MMM.opcode())
        instruction[1].set(0x400)

        when:
        cu.execute()

        then:
        thrown(RuntimeException)
    }

    def "should skip next instruction if Vx is equal to Vy (SE)"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)
        registers.getVariable(0xB).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox5XY0.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x204 as short
    }

    def "should NOT skip instruction if Vx is NOT equal to Vy (SE)"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)
        registers.getVariable(0xB).set(0x46 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox5XY0.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short
    }

    def "should properly OR value of Vy into Vx"() {
        given:
        registers.getProgramCounter().set(0x400)

        registers.getVariable(0xA).set(0x34 as byte)
        registers.getVariable(0xB).set(0x56 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY1.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x402 as short

        registers.getVariable(0xA).getAsInt() == 0x76
        registers.getVariable(0xB).getAsInt() == 0x56
    }

    def "should store registers V0 - VX with data from memory starting at I"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0x0).set(0x12 as byte)
        registers.getVariable(0x1).set(0x34 as byte)
        registers.getVariable(0x2).set(0x56 as byte)
        registers.getVariable(0x3).set(0x11 as byte)

        registers.getIndex().set(0x400)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxFX55.opcode())
        instruction[1].set(0x2)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short

        byte[] data = new byte[4]
        memory.getBytes(0x400 as short, data, 4)

        data[0] == 0x12 as byte
        data[1] == 0x34 as byte
        data[2] == 0x56 as byte
        data[3] == 0x00 as byte //not 0x11
    }

    def "should properly SUB (with borrow) Vx = Vy - Vx (borrow)"() {
        given:
        registers.getProgramCounter().set(0x322)

        registers.getVariable(0xC).set(0x56 as byte)
        registers.getVariable(0xD).set(0x34 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY7.opcode())
        instruction[1].set(0xC)
        instruction[2].set(0xD)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short

        registers.getVariable(0xC).getAsInt() == 0xDE
        registers.getVariable(0xD).getAsInt() == 0x34
        registers.getVariable(0xF).getAsInt() == 0x00
    }

    def "should properly SUB (with borrow) Vx = Vy - Vx (no borrow)"() {
        given:
        registers.getProgramCounter().set(0x322)

        registers.getVariable(0xC).set(0xAA as byte)
        registers.getVariable(0xD).set(0xBB as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XY7.opcode())
        instruction[1].set(0xC)
        instruction[2].set(0xD)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x324 as short

        registers.getVariable(0xC).getAsInt() == 0x11
        registers.getVariable(0xD).getAsInt() == 0xBB
        registers.getVariable(0xF).getAsInt() == 0x01
    }

    def "should skip next instruction if Vx is NOT equal to Vy (SNE)"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)
        registers.getVariable(0xB).set(0x46 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox9XY0.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x204 as short
    }

    def "should NOT skip instruction if Vx is equal to Vy (SNE)"() {
        given:

        registers.getProgramCounter().set(0x200)
        registers.getVariable(0xA).set(0x45 as byte)
        registers.getVariable(0xB).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox9XY0.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x202 as short
    }

    def "should jump to location pointed to by the instruction and register"() {
        given:

        registers.getProgramCounter().set(0x0123)
        registers.getVariable(0x0).set(0x67 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(OxBMMM.opcode())
        instruction[1].set(0x0345)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x3AC as short
    }

    def "should properly shift Vx left (legacy mode) with overflow"() {
        given:
        registers.getProgramCounter().set(0x500)

        registers.getVariable(0xA).set(0x85 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XYE.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x502 as short

        registers.getVariable(0xA).getAsInt() == 0x0A
        registers.getVariable(0xF).getAsInt() == 0x01
    }

    def "should properly shift Vx left (legacy mode) no overflow"() {
        given:
        registers.getProgramCounter().set(0x500)

        registers.getVariable(0xA).set(0x45 as byte)

        def instruction = registers.getDecodedInstruction()
        instruction[0].set(Ox8XYE.opcode())
        instruction[1].set(0xA)
        instruction[2].set(0xB)

        when:
        cu.execute()

        then:
        registers.getProgramCounter().get() == 0x502 as short

        registers.getVariable(0xA).getAsInt() == 0x8A
        registers.getVariable(0xF).getAsInt() == 0x00
    }

}