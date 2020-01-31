package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryMap;

/**
 * Processing Unit (PU)
 */
@Deprecated(forRemoval = true)
public class ProcessingUnit {

    public static final boolean DUMP_KEYS = false;

    // Contains ---------------------------------

    private final ArithmeticLogic alu;

    private final AddressGeneration agu;

    private final StackEngine stackEngine;

    private final GraphicsProcessing gpu;

    // Accessible -------------------------------

    private final Registers registers;

    private final Memory memory;

    public ProcessingUnit(Registers registers, Memory memory) {
        this.alu = new ArithmeticLogic(registers, memory);
        this.agu = new AddressGeneration(registers, memory);
        this.stackEngine = new StackEngine(registers, memory);
        this.gpu = new GraphicsProcessing(registers, memory);

        this.registers = registers;
        this.memory = memory;
    }

    public void execute() {
        final WordRegister[] di = registers.getDecodedInstruction();
        final TribbleRegister pc = registers.getProgramCounter();

        short addr;
        int x;

        final InstructionType instructionType = InstructionType.valueOf(di[0].get());

        int increment = 2; // instructions are always 2 bytes long

        switch (instructionType) {
            case Ox00E0:
                gpu.clearScreen();
                break;

            case OxAMMM:
                agu.loadAddressIntoIndex(di[1].get());
                break;

            case Ox6XKK:
                alu.loadValueIntoRegister(di[1].get(), di[2].get());
                break;

            case OxDXYK:
                gpu.drawSprite(di[1].getAsInt(), di[2].getAsInt(), di[3].get());
                break;

            case Ox7XKK:
                alu.addValueToRegister(di[1].get(), di[2].get());
                break;

            case Ox1MMM:
                stackEngine.jump(di[1].get());
                increment = 0;
                break;

            case OxFX1E:
                agu.addRegisterIntoIndex(di[1].get());
                break;

            case Ox3XKK:
                increment = alu.compareValueWithRegister(di[1].get(), di[2].get()) ? 4 : 2;
                break;

            case Ox2MMM:
                stackEngine.call(di[1].get());
                increment = 0;
                break;

            case Ox0MMM:
                throw new RuntimeException("system call is unsupported, yet");

            case Ox8XY0:
                alu.copyRegisterIntoRegister(di[1].get(), di[2].get());
                break;

            case OxEXA1:
                x = di[1].getAsInt();

                short keys = registers.getKeyState().get();

                byte key = registers.getData(x).get();

                if (DUMP_KEYS) System.out.printf("Key: %01X\n", key);

                int keyMask = 1 << key;

                if ((keys & keyMask) == 0) { //NOT pressed
                    increment = 4;
                }
                break;

            case OxFX65:
                loadMemoryIntoRegisters(di[1].get());
                break;

            case Ox00EE:
                stackEngine.returnFromSubroutine();
                break;

            case OxFX15:
                x = di[1].getAsInt();

                byte delay = registers.getData(x).get();
                registers.getDelay().set(delay);
                break;

            case OxFX07:
                x = di[1].getAsInt();

                byte currentDelay = registers.getDelay().get();
                registers.getData(x).set(currentDelay);
                break;

            case Ox8XY2:
                alu.andRegisterToRegister(di[1].get(), di[2].get());
                break;

            case Ox8XY6:
                alu.shiftRightRegisterIntoRegister(di[1].get(), di[1].get());
                break;

            case OxEX9E:
                x = di[1].getAsInt();

                short keyss = registers.getKeyState().get();

                byte keyy = registers.getData(x).get();

                if (DUMP_KEYS) System.out.printf("Key 9E: %01X\n", key);

                int keyMaskk = 1 << keyy;

                if ((keyss & keyMaskk) != 0) { //pressed
                    increment = 4;
                }
                break;

            case Ox8XY4:
                alu.addRegisterToRegister(di[1].get(), di[2].get());
                break;

            case Ox8XY5:
                alu.subtractRegisterFromRegister(di[1].get(), di[1].get(), di[2].get());
                break;

            case Ox4XKK:
                increment = alu.compareValueWithRegister(di[1].get(), di[2].get()) ? increment : 4;
                break;

            case Ox5XY0:
                increment = alu.compareRegisterWithRegister(di[1].get(), di[2].get()) ? 4 : increment;
                break;

            case Ox8XY3:
                alu.xorRegisterToRegister(di[1].get(), di[2].get());
                break;

            case OxFX18:
                x = di[1].getAsInt();

                byte sound = registers.getData(x).get();
                registers.getSound().set(sound);
                break;

            case OxFX0A:
                x = di[1].getAsInt();

                if (registers.getKeyState().get() > 0) { //some keys are pressed
                    registers.getData(x).set(registers.getKeyValue().get());
                } else {
                    registers.getKeyWait().set((byte) 0x1);
                    increment = 0;
                }
                break;

            case OxFX33:
                alu.bcdRegisterToMemory(di[1].get());
                break;

            case OxFX29:
                x = di[1].getAsInt();

                addr = (short)(Short.toUnsignedInt(MemoryMap.INTERPRETER_START) + registers.getData(x).getAsInt() * 5);
                registers.getIndex().set(addr);
                break;

            case OxCXKK:
                alu.andRandomToRegister(di[1].get(), di[2].get());
                break;

            case Ox8XY1:
                alu.orRegisterToRegister(di[1].get(), di[2].get());
                break;

            case OxFX55:
                x = di[1].getAsInt();
                addr = registers.getIndex().get();

                for (int i = 0; i <= x; i++, addr++) {
                    final byte data = registers.getData(i).get();
                    memory.setByte(addr, data);
                }
                break;

            case Ox8XY7:
                alu.subtractRegisterFromRegister(di[1].get(), di[2].get(), di[1].get());
                break;

            case Ox9XY0:
                increment = alu.compareRegisterWithRegister(di[1].get(), di[2].get()) ? increment : 4;
                break;

            case OxBMMM:
                stackEngine.jump(di[1].get(), (short) 0x0);
                increment = 0;
                break;

            case Ox8XYE:
                alu.shiftLeftRegisterIntoRegister(di[1].get(), di[1].get());
                break;

            default:
                System.out.println("Unknown instr: " + instructionType);
                System.exit(1);
        }

        pc.increment(increment);
    }

    private void loadMemoryIntoRegisters(final short x) {
        int xIndex = Short.toUnsignedInt(x);
        int iValue = registers.getIndex().getAsInt();


        for (int i = 0; i <= xIndex; ++i, ++iValue) {
            final byte data = memory.getByte((short) iValue);
            registers.getData(i).set(data);
        }

        //registers.getIndex().set(iValue);
    }
}
