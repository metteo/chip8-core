package net.novaware.chip8.core.port;

public interface KeyPort extends InputPort {

    void updateKeyState(final short state);
    void keyPressed(final byte key);
}
