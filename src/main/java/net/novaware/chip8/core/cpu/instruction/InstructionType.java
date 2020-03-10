package net.novaware.chip8.core.cpu.instruction;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static net.novaware.chip8.core.cpu.instruction.InstructionMask.*;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * MMM - address
 * KK, K - number
 * X, Y - indices of V registers (VX, VY)
 *
 * NOTE: Every instruction instance name starts with capital 'O', not '0' because of Java syntax rules
 *
 * @see <a href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM">Cowgod's Chip-8 Tech Ref v1.0</a>
 * @see <a href="http://mattmik.com/files/chip8/mastering/chip8.html">Mastering Chip-8 by mattmik</a>
 * @see <a href="https://hc-ddr.hucki.net/wiki/doku.php/homecomputer/chip8">Chip-8 at Homecomputer DDR</a>
 * @see <a href="https://github.com/trapexit/chip-8_documentation">Chip-8 docs by trapexit</a>
 * @see <a href="http://chip8.sourceforge.net/">Chip-8 at Sourceforge by Peter Miller</a>
 * @see <a href="http://www.pong-story.com/chip8/">CHIP8 by David Winter</a>
 */
public enum InstructionType {
    /**
     * 0MMM - SYS addr
     * Jump to a machine code routine at MMM.
     *
     * This instruction is only used on the old computers on which Chip-8 was originally implemented.
     * It is ignored by modern interpreters.
     */
    Ox0MMM (0x0000, OxF000.value()),

    /**
     * 00E0 - CLS
     * Clear the display.
     */
    Ox00E0 (0x00E0, OxFFFF.value()),

    /**
     * 00EE - RET
     * Return from a subroutine.
     *
     * The interpreter sets the program counter to the address popped from the stack + 2.
     */
    Ox00EE (0x00EE, OxFFFF.value()),

    /**
     * 1MMM - JP addr
     * Jump to location MMM.
     *
     * The interpreter sets the program counter to MMM. PC incremented during fetch is overridden.
     */
    Ox1MMM (0x1000, OxF000.value()),

    /**
     * 0x2MMM - CALL addr
     * Call subroutine at MMM.
     *
     * The interpreter decrements the stack pointer, then puts the current memory address on the top of the stack.
     * The PC is then set to MMM.
     */
    Ox2MMM (0x2000, OxF000.value()),

    /**
     * 3XKK - SE VX, byte
     * Skip next instruction if VX = KK.
     *
     * The interpreter compares register VX to KK, and if they are equal, increments the program counter by 2.
     */
    Ox3XKK (0x3000, OxF000.value()),

    /**
     * 4XKK - SNE VX, byte
     * Skip next instruction if Vx != kk.
     *
     * The interpreter compares register VX to KK, and if they are not equal, increments the program counter by 2.
     */
    Ox4XKK (0x4000, OxF000.value()),

    /**
     * 5XY0 - SE Vx, Vy
     * Skip next instruction if Vx = Vy.
     *
     * The interpreter compares register Vx to register Vy, and if they are equal, increments the program counter by 2.
     */
    Ox5XY0 (0x5000, OxF000.value()),

    /**
     * 6XKK - LD VX, byte
     * Set VX = KK.
     *
     * The interpreter puts the value KK into register VX.
     */
    Ox6XKK (0x6000, OxF000.value()),

    /**
     * 7XKK - ADD VX, byte
     * Set VX = VX + KK.
     *
     * Adds the value KK to the value of register VX, then stores the result in VX.
     */
    Ox7XKK (0x7000, OxF000.value()),

    /**
     * 8XY0 - LD VX, VY
     * Set Vx = Vy.
     *
     * Stores the value of register Vy in register Vx.
     */
    Ox8XY0 (0x8000, OxF00F.value()),

    /**
     * 8xy1 - OR Vx, Vy
     * Set Vx = Vx OR Vy.
     *
     * Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx. A bitwise OR compares the
     * corresponding bits from two values, and if either bit is 1, then the same bit in the result is also 1. Otherwise, it is 0.
     */
    Ox8XY1 (0x8001, OxF00F.value()),

    /**
     * 8xy2 - AND Vx, Vy
     * Set Vx = Vx AND Vy.
     *
     * Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx. A bitwise AND compares the
     * corresponding bits from two values, and if both bits are 1, then the same bit in the result is also 1. Otherwise, it is 0.
     */
    Ox8XY2 (0x8002, OxF00F.value()),

    /**
     * 8xy3 - XOR Vx, Vy
     * Set Vx = Vx XOR Vy.
     *
     * Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result in Vx. An exclusive OR compares
     * the corresponding bits from two values, and if the bits are not both the same, then the corresponding bit in the
     * result is set to 1. Otherwise, it is 0.
     */
    Ox8XY3 (0x8003, OxF00F.value()),

    /**
     * 8xy4 - ADD Vx, Vy
     * Set Vx = Vx + Vy, set VF = carry.
     *
     * The values of Vx and Vy are added together. If the result is greater than 8 bits (i.e., > 255,) VF is set to 1,
     * otherwise 0. Only the lowest 8 bits of the result are kept, and stored in Vx.
     */
    Ox8XY4 (0x8004, OxF00F.value()),

    /**
     * 8xy5 - SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     *
     * If Vx > Vy, then VF is set to 1, otherwise 0. Then Vy is subtracted from Vx, and the results stored in Vx.
     */
    Ox8XY5 (0x8005, OxF00F.value()),

    /**
     * 8xy6 - SHR Vx {, Vy}
     * <br>Set Vx = Vx SHR 1. (legacy e.g. INVADERS)
     * <br>Set Vx = Vy SHR 1. (modern e.g. TANK)
     * <br>
     * <br>If the least-significant bit of Vx {Vy} is 1, then VF is set to 1, otherwise 0. Then Vx {Vy} is divided by 2.
     *
     * @see <a href="https://www.reddit.com/r/EmuDev/comments/8cbvz6/chip8_8xy6/" >Reddit - EmuDev - chip8 8xy6</a>
     * @see <a href="https://github.com/JohnEarnest/Octo/blob/gh-pages/docs/SuperChip.md#compatibility" >SCHIP - compatibility</a>
     */
    Ox8XY6 (0x8006, OxF00F.value()),

    /**
     * 8xy7 - SUBN Vx, Vy
     * Set Vx = Vy - Vx, set VF = NOT borrow.
     *
     * If Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy, and the results stored in Vx.
     */
    Ox8XY7 (0x8007, OxF00F.value()),

    /**
     * 8xyE - SHL Vx {, Vy}
     * <br>Set Vx = Vx SHL 1. (legacy)
     * <br>Set Vx = Vy SHL 1. (modern)
     * <br>
     * <br>If the most-significant bit of Vx {Vy} is 1, then VF is set to 1, otherwise to 0. Then Vx {Vy} is multiplied by 2.
     *
     * @see InstructionType#Ox8XY6
     */
    Ox8XYE (0x800E, OxF00F.value()),

    /**
     * 9xy0 - SNE Vx, Vy
     * Skip next instruction if Vx != Vy.
     *
     * The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
     */
    Ox9XY0 (0x9000, OxF00F.value()),

    /**
     * AMMM - LD I, addr
     * Set I = MMM.
     *
     * The value of register I is set to MMM.
     */
    OxAMMM (0xA000, OxF000.value()),

    /**
     * BMMM - JP V0, addr
     * Jump to location MMM + V0.
     *
     * The program counter is set to MMM plus the value of V0.
     */
    OxBMMM (0xB000, OxF000.value()),

    /**
     * Cxkk - RND Vx, byte
     * Set Vx = random byte AND kk.
     *
     * The interpreter generates a random number from 0 to 255, which is then ANDed with the value kk. The results are
     * stored in Vx. See instruction 8xy2 for more information on AND.
     */
    OxCXKK (0xC000, OxF000.value()),

    /**
     * DXYK - DRW VX, VY, nibble
     * Display K-byte sprite starting at memory location I at (VX, VY), set VF = collision.
     */
    OxDXYK (0xD000, OxF000.value()),

    /**
     * Ex9E - SKP Vx
     * Skip next instruction if [Vx]-th input bit is set to 1.
     *
     * Checks the input register, and if the given bit is currently set to 1, PC is increased by 2.
     */
    OxEX9E (0xE09E, OxF0FF.value()),

    /**
     * ExA1 - SKNP Vx
     * Skip next instruction if [Vx]-th input bit is set to 0.
     *
     * Checks the input register, and if the given bit is currently set to 0, PC is increased by 2.
     */
    OxEXA1 (0xE0A1, OxF0FF.value()),

    /**
     * Fx07 - LD Vx, DT
     * Set Vx = delay timer value.
     *
     * The value of DT is placed into Vx.
     */
    OxFX07 (0xF007, OxF0FF.value()),

    /**
     * Fx0A - LD Vx, K
     * Wait for an input, store the index of the non-0 bit in Vx. (Highest if multiple are set*)
     *
     * All execution stops until an input is provided, then the index of the bit which is set to 1 is stored in Vx.
     */
    OxFX0A (0xF00A, OxF0FF.value()),

    /**
     * FX15 - LD DT, VX
     * Set delay timer = VX.
     *
     * DT is set equal to the value of VX.
     */
    OxFX15 (0xF015, OxF0FF.value()),

    /**
     * Fx18 - LD ST, Vx
     * Set sound timer = Vx.
     *
     * ST is set equal to the value of Vx.
     */
    OxFX18 (0xF018, OxF0FF.value()),

    /**
     * FX1E - ADD I, VX
     * Set I = I + VX.
     *
     * The values of I and Vx are added, and the results are stored in I.
     *
     * NOTE: If the operation results in an overflow carry=1 otherwise 0
     *      (quirk for Spacefight 2091!, legacy applications didn't use it)
     *
     */
    OxFX1E (0xF01E, OxF0FF.value()),

    /**
     * Fx29 - LD F, Vx
     * Set I = location of sprite for digit Vx.
     *
     * The value of I is set to the location for the hexadecimal sprite corresponding to the value of Vx. See section 2.4,
     * Display, for more information on the Chip-8 hexadecimal font.
     */
    OxFX29 (0xF029, OxF0FF.value()),

    /**
     * Fx33 - LD B, Vx
     * Store BCD representation of Vx in memory locations I, I+1, and I+2.
     *
     * The interpreter takes the decimal value of Vx, and places the hundreds digit in memory at location in I, the tens
     * digit at location I+1, and the ones digit at location I+2.
     */
    OxFX33 (0xF033, OxF0FF.value()),

    /**
     * Fx55 - LD [I], Vx
     * Store registers V0 through Vx in memory starting at location I.
     *
     * The interpreter copies the values of registers V0 through Vx into memory, starting at the address in I.
     *
     * NOTE: legacy systems assume I is untouched, modern games expect I to be incremented.
     *
     * @see <a href="https://www.reddit.com/r/EmuDev/comments/8cbvz6/chip8_8xy6/" >Reddit - EmuDev - chip8 8xy6</a>
     * @see <a href="https://github.com/JohnEarnest/Octo/blob/gh-pages/docs/SuperChip.md#compatibility" >SCHIP - compatibility</a>
     */
    OxFX55 (0xF055, OxF0FF.value()),

    /**
     * FX65 - LD Vx, [I]
     * Fill registers V0 through Vx from memory starting at location I.
     *
     * The interpreter reads values from memory starting at location I into registers V0 through Vx.
     *
     * NOTE: legacy systems assume I is untouched, modern games expect I to be incremented.
     *
     * @see InstructionType#OxFX55
     */
    OxFX65 (0xF065, OxF0FF.value()),
    ;
    // TODO: maybe provide OxUNKN instruction to get rid of nullable

    private static final List<InstructionType> instances = List.of(values());

    private static final Map<Short, InstructionType> byOpCode = getInstances().stream()
            .collect(toUnmodifiableMap(InstructionType::opcode, identity()));

    private final @Unsigned short opcode;

    private final @Unsigned short mask;

    InstructionType(final int opcode, final short mask) {
        this.opcode = ushort(opcode);
        this.mask = mask;
    }

    public @Unsigned short opcode() {
        return opcode;
    }

    public @Unsigned short mask() {
        return mask;
    }

    /**
     * Not using {@link java.util.Optional} on purpose here.
     *
     * @return null if opcode is unrecognized
     */
    public static @Nullable InstructionType valueOf(int opcode) {
        return byOpCode.get(ushort(opcode));
    }

    /**
     * Not using {@link java.util.Optional} on purpose here.
     *
     * @return null if opcode is unrecognized
     */
    public static @Nullable InstructionType valueOf(short opcode) {
        return byOpCode.get(opcode);
    }

    /**
     * Prevents allocation of array holding the values() (done only once at enum loading)
     *
     * @see <a href="https://www.javacodegeeks.com/2018/08/memory-hogging-enum-values-method.html">Memory-Hogging Enum.values()</a>
     */
    public static List<InstructionType> getInstances() {
        return instances;
    }
}
