package net.novaware.chip8.cli.device;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import net.novaware.chip8.core.port.KeyPort;

import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * Key device
 */
public class Keyboard extends KeyAdapter {
    //16 keys

    private short keyState;
    private byte lastKey;

    private KeyPort keyPort;

    private Timer t = new Timer("KeyPoller", true);

    public void init(KeyPort keyPort, Terminal terminal) {
        this.keyPort = keyPort;

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    final KeyStroke keyStroke = terminal.pollInput();

                    if (keyStroke == null) {
                        keyPort.keyPressed(ubyte(0));
                        keyPort.updateKeyState(ushort(0));
                        return;
                    }
                    if (keyStroke.getKeyType() == KeyType.Escape) {
                        System.exit(0);
                    }

                    if (keyStroke.getKeyType() == KeyType.Character) {
                        switch(keyStroke.getCharacter()) {
                            case '5':
                                keyState = ushort(1 << 5);
                                lastKey = 5;
                                break;
                            case '4':
                                keyState = ushort(1 << 4);
                                lastKey = 4;
                                break;
                            case '6':
                                keyState = ushort(1 << 6);
                                lastKey = 6;
                                break;
                        }

                        keyPort.keyPressed(lastKey);
                        keyPort.updateKeyState(keyState);
                    }
                } catch (IOException e) {
                    e.printStackTrace(); //TODO: handle exception
                }
            }
        }, 100, 100);
    }
}
