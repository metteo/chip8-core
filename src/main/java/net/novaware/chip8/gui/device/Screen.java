package net.novaware.chip8.gui.device;

import javax.swing.*;
import java.awt.*;

/**
 * Display device
 */
public class Screen extends JComponent {

    private boolean[][] model = new boolean[64][32];

    private static final boolean REDRAW_HEURISTIC = !false;
    private boolean drawing = true; //TODO: improve and use timers which repaint even earlier if next erase happens long after last draw (like game over screen)

    private long lastPaint;
    private int fps; //calculate average from 3 frames?

    public Screen() {
        setPreferredSize(new Dimension(640, 320));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for(int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (model[x][y]) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(x * 10, y * 10, 10, 10);
            }
        }

        char[] s = ("" + fps).toCharArray(); //TODO: very inefficient

        g.setColor(Color.RED);
        g.drawChars(s, 0, s.length, 4, 12);
    }

    private void calculateFps() {
        long now = System.nanoTime();
        fps = (int)(1e9 / (now - lastPaint));
        lastPaint = now;
    }

    public void setModelValue(int x, int y, boolean value) {
        model[x][y] = value;
    }

    public void draw(boolean erasing, byte[] data) { //TODO: make dumps of images with annotation if something was drawn /erased etc to figure out the sequence and effects
        if (REDRAW_HEURISTIC) {
            if (/*was*/drawing && /*now*/ erasing) {
                calculateFps();
                SwingUtilities.invokeLater(this::repaint); //FIXME deferred in the future so even though called before updateModel may execute after...
            } else {
                updateModel(data);
            }


            drawing = !erasing;
        } else {
            updateModel(data);
            calculateFps();
            SwingUtilities.invokeLater(this::repaint);
        }
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
                int mask = 0x1 << localBit;
                int pixel = (Byte.toUnsignedInt(frame) & mask) >>> localBit;

                setModelValue(xCoord, yCoord, pixel != 0);
            }
        }
    }
}