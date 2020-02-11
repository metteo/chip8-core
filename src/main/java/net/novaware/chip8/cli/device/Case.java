package net.novaware.chip8.cli.device;

import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

/**
 * Computer Case a.k.a. Enclosure
 *
 * Contains:
 * Screen
 * Keyboard
 * Buzzer
 *
 * Tape is an attachment
 */
public class Case {

    private Terminal terminal;

    private TerminalScreen screen;

    public Case() throws IOException {
        terminal = new DefaultTerminalFactory().createTerminal();

        screen = new TerminalScreen(terminal);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public TerminalScreen getScreen() {
        return screen;
    }
}
