package net.novaware.chip8.cli;

import net.novaware.chip8.cli.device.*;
import net.novaware.chip8.core.Board;

import java.nio.file.Path;

import static java.lang.System.err;
import static java.lang.System.exit;
import static net.novaware.chip8.core.BoardFactory.newBoardFactory;

public class Chip8Cli {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            err.println("usage: chip8cli <pathToRom>");
            exit(1);
        }

        final Path romPath = Path.of(args[0]);
        Tape tape = new Tape(romPath);
        tape.load(); //TODO: temporary preload to verify paths early until lanterna clears the screen

        Case aCase = new Case();

        Screen screen = new Screen(aCase.getScreen());

        Buzzer buzzer = new Buzzer(aCase.getTerminal());
        buzzer.init();

        Board board = newBoardFactory().newBoard();
        board.init();

        board.getDisplayPort().attach(screen::draw);
        board.getAudioPort().attach(buzzer);
        board.getStoragePort().load(tape.load());

        Keyboard k = new Keyboard();
        k.init(board.getKeyPort(), aCase.getTerminal());

        board.run(Integer.MAX_VALUE);

        /*
        Signal.handle(new Signal("WINCH"), sig -> {
            System.out.println("WINCH");
        });
        */
    }
}