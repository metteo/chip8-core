package net.novaware.chip8.cli.device;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.TerminalScreen;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.util.ViewPort;

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

    private ViewPort viewPort = new ViewPort();

    private ViewPort.Bit bit = new ViewPort.Bit();

    private ViewPort.Index idx = new ViewPort.Index();

    private int lowerBlockPos = 0;

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

        for (int y = 0; y < Math.min(32 - 1, terminalSize.getRows() * 2); y+=2) {
            for (int x = 0; x < Math.min(64, terminalSize.getColumns()); ++x) {
                boolean upperPixel = model[y][x];
                boolean lowerPixel = model[y+1][x];

                String s = "░";
                if (upperPixel && lowerPixel) {
                    s = "█";
                } else if (upperPixel) {
                    s = "▀";
                } else if (lowerPixel) {
                    s = "▄";
                } else {
                    s = " ";
                }

                tg.putString(x, y/2, s);
            }
        }

        //visualize redraw
        tg.putString(lowerBlockPos, 16, "░");
        lowerBlockPos = (lowerBlockPos + 1) % 64;
        tg.putString(lowerBlockPos, 16, "█");
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
        for (int yCoord  = 0; yCoord < 32; ++yCoord) {
            for (int xCoord  = 0; xCoord < 64; ++xCoord) {
                bit.x = xCoord;
                bit.y = yCoord;

                viewPort.toIndex(bit, idx, false);

                byte frame = data[idx.arrayByte];
                int mask = 0x1 << 7 - idx.byteBit;
                int pixel = (uint(frame) & mask) >>> 7 - idx.byteBit;

                setModelValue(xCoord, yCoord, pixel != 0);
            }
        }
    }
}
