package net.novaware.chip8.core.memory;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.memory.MemoryModule.BOOTLOADER_ROM_END;
import static net.novaware.chip8.core.memory.MemoryModule.BOOTLOADER_ROM_START;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

// https://computer.howstuffworks.com/pc3.htm
// TODO: power on self test and beep (test memory bytes by writing and then reading, see VIP manual)
// TODO: hardware specs printed: cpu Hz, memory size, (hardware spec registers)
// TODO: check the program in storage, if ok load it
public class Bootloader {

    private static final int BYTES_PER_INSTRUCTION = 2;

    private static final int[] CODE = new int[] {
            0x1040, // GO 040           // goto boot

            0x1004, // GO 004           // def reset() {
            0x00E0, // ERASE
            0x6000, // V0=00
            0x6100, // V1=00
            0x6200, // V2=00
            0x6300, // V3=00
            0x6400, // V4=00
            0x6500, // V5=00
            0x6600, // V6=00
            0x6700, // V7=00
            0x6800, // V8=00
            0x6900, // V9=00
            0x6A00, // VA=00
            0x6B00, // VB=00
            0x6C00, // VC=00
            0x6D00, // VD=00
            0x6E00, // VE=00
            0x6F00, // VF=00
            0xA000, // I=000
            0xF015, // TIME=V0
            0xF018, // TONE=V0
            0x00EE, // RET              // }

            0x1030, // GO 030           // def drawFont(x = VA, y=VB, font=VC) {
            0xFC29, // I=VC(LSDP)
            0xDAB5, // SHOW 5MI@VAVB
            0x00EE, // RET              // }

            0x8000, //                  // sprite dot (h=1)

            0x103A, // GO 03A           // def drawDot(x = VA, y=VB) {
            0xA036, // I=36
            0xDAB1, // SHOW 1MI@VAVB
            0x00EE, // RET              // }

            0x00E0, // ERASE            // boot:

            0x6A01, // VA=01            // x = 1
            0x6B02, // VB=02            // y = 2
            0x6C0C, // VC=0C            // font = 'C'
            0x202E, // DO 02E           // drawFont()

            0x6A06, // VA=06            // x = 6
            0x6C08, // VC=08            // font = '8'
            0x202E, // DO 02E           // drawFont()

            0x6D02, // VD=02            // t = 2 (~33ms)
            0xFD18, // TONE=VD          // beep(t)

            0x6A0B, // VA=0B            // x = 11
            0x6B06, // VB=06            // y = 6
            0x2038, // DO 038           // drawDot()
            0x2038, // DO 038           // drawDot()

            0x6A0C, // VA=0C            // x = 12
            0x2038, // DO 038           // drawDot()
            0x2038, // DO 038           // drawDot()

            0x6A0D, // VA=0D            // x = 13
            0x2038, // DO 038           // drawDot()
            0x2038, // DO 038           // drawDot()

            0x6A0E, // VA=0E            // x = 14
            0x2038, // DO 038           // drawDot()
            0x2038, // DO 038           // drawDot()

            0x6A0F, // VA=0F            // x = 15
            0x2038, // DO 038           // drawDot()
            0x2038, // DO 038           // drawDot()

            0x6A10, // VA=10            // x = 16
            0x2038, // DO 038           // drawDot()
            0x2038, // DO 038           // drawDot()

            0xF00A, // V0=KEY           //
            0x400F, // SKIP;V0 NE 0F    // skip next if last key != F
            0x1100, // GO 100           // goto boot128

            0x2002, // DO 002           // reset()
            0x1200  // GO 200           // goto main
    };

    //Boot-128 by David Winter (Postware license)
    private static final int BOOT_128_START = 0x100;
    private static final int[] BOOT_128 = new int[] {
            0x0001, 0x6302, 0x3500, 0x1124, 0xF329, 0x2170, 0x8040, 0x8006,
            0x8006, 0x8006, 0x8006, 0xF029, 0x2170, 0xF429, 0x2170, 0xA176,
            0x2170, 0x75FF, 0x2166, 0x8780, 0x870E, 0x870E, 0x870E, 0x870E,
            0x2166, 0x8874, 0xA000, 0xF41E, 0x8030, 0x6180, 0xF11E, 0xF11E,
            0x70FF, 0x3000, 0x113C, 0x8080, 0xF055, 0x7401, 0x4400, 0x7301,
            0x4310, 0x1200, 0x3527, 0x1104, 0x6500, 0x4618, 0x1162, 0x7606,
            0x1104, 0x0000, 0x1104, 0xA17A, 0xD566, 0xF80A, 0xD566, 0xF829,
            0xD565, 0x7505, 0x00EE, 0x0040, 0x0040, 0x0000, 0x0000, 0x00F0
    };

    private static final int[] BASIC_FONT = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    public void fill(Memory memory) {
        requireNonNull(memory, "memory must not be null");

        final int codeStart = uint(BOOTLOADER_ROM_START);
        for (int i = 0; i < CODE.length; ++i) {
            final short addr = ushort(codeStart + i * BYTES_PER_INSTRUCTION);
            memory.setWord(addr, ushort(CODE[i]));
        }

        for (int i = 0; i < BOOT_128.length; ++i) {
            final short addr = ushort(BOOT_128_START + i * BYTES_PER_INSTRUCTION);
            memory.setWord(addr, ushort(BOOT_128[i]));
        }

        final int basicFontStart = getFontAddress();
        for (int i = 0; i < BASIC_FONT.length; ++i) {
            final short addr = ushort(basicFontStart + i);
            memory.setByte(addr, ubyte(BASIC_FONT[i]));
        }
    }

    public short getFontAddress() {
        return ushort(uint(BOOTLOADER_ROM_END) - BASIC_FONT.length + 1);
    }
}
