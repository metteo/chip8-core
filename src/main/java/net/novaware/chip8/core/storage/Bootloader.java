package net.novaware.chip8.core.storage;

import net.novaware.chip8.core.memory.Memory;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.memory.MemoryModule.BOOTLOADER_ROM_START;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

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

            0x2002, // DO 002           // reset()
            0x1200  // GO 200           // goto main
    };

    public void fill(Memory memory) {
        requireNonNull(memory, "memory must not be null");

        final int codeStart = uint(BOOTLOADER_ROM_START);
        for (int i = 0; i < CODE.length; ++i) {
            final short addr = ushort(codeStart + i * BYTES_PER_INSTRUCTION);
            memory.setWord(addr, ushort(CODE[i]));
        }

        for (int i = 0; i < Boot128.CODE.length; ++i) {
            final short addr = ushort(Boot128.MEMORY_START + i * BYTES_PER_INSTRUCTION);
            memory.setWord(addr, ushort(Boot128.CODE[i]));
        }

        for (int i = 0; i < VipOs.CODE.length; ++i) {
            final short addr = ushort(VipOs.MEMORY_START + (i * 2));
            memory.setWord(addr, ushort(VipOs.CODE[i]));
        }
    }

    public short getFontAddress() {
        return ushort(VipOs.FONT_START);
    }
}
