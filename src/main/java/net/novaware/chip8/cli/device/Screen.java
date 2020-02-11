package net.novaware.chip8.cli.device;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.TerminalScreen;
import net.novaware.chip8.core.cpu.register.Registers;

import java.io.IOException;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Display device
 */
public class Screen {

    private boolean[][] model = new boolean[32][64]; // [y][x]

    private static final boolean REDRAW_HEURISTIC = !false;
    private Integer lastChange = uint(Registers.GC_DRAW);

    private TerminalScreen terminalScreen;

    private TextGraphics tg;

    public Screen(TerminalScreen terminalScreen) throws IOException {
        this.terminalScreen = terminalScreen;
        terminalScreen.startScreen(); //TODO: stop screen on exit

        tg = terminalScreen.newTextGraphics();
        tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
        tg.setForegroundColor(TextColor.ANSI.WHITE);
    }

    public void setModelValue(int x, int y, boolean value) {
        model[y][x] = value;
    }

    public void draw(Integer currentChange, byte[] data) {
        if (REDRAW_HEURISTIC) {
            if ((lastChange != uint(Registers.GC_ERASE) && currentChange == uint(Registers.GC_ERASE)) || currentChange == uint(Registers.GC_MIX)) { //this works for games, above works nice for invaders menu screen
                refresh();
            } else {
                updateModel(data);
            }

            lastChange = currentChange;
        } else {
            updateModel(data);
            refresh();
        }
    }

    public void refresh() {
        TerminalSize terminalSize = terminalScreen.getTerminalSize();
        final TerminalSize terminalReSize = terminalScreen.doResizeIfNecessary();
        terminalSize = terminalReSize == null ? terminalSize : terminalReSize;

        for (int y = 0; y < Math.min(32, terminalSize.getRows()); ++y) {
            for (int x = 0; x < Math.min(128, terminalSize.getColumns()); x+=2) {
                tg.putString(x, y, model[y][x/2] ? "█" : " ");//"░");
                tg.putString(x+1, y, model[y][x/2] ? "█" : " ");//"░");
            }
        }

        try {
            terminalScreen.refresh();
        } catch (IOException e) {
            e.printStackTrace(); //TODO: handle exception
        }
    }


    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    private void updateModel(byte[] data) {
        final int gfxStart = 0x0;

        for (int yCoord  = 0; yCoord < 32; yCoord++) {
            for (int xCoord  = 0; xCoord < 64; xCoord++) {
                int globalBit = yCoord * 64 + xCoord;
                int gfxOffset = globalBit / 8;
                int localBit = globalBit % 8;

                short gfxIndex = (short) (gfxStart + gfxOffset);

                byte frame = data[gfxIndex];
                int mask = 0x1 << 7 - localBit;
                int pixel = (Byte.toUnsignedInt(frame) & mask) >>> 7 - localBit;

                setModelValue(xCoord, yCoord, pixel != 0);
            }
        }
    }
}
