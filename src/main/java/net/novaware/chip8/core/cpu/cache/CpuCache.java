package net.novaware.chip8.core.cpu.cache;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.List;

//XXX: WIP, annotates memory locations to speed up execution and display sprites
public class CpuCache {

    private class Entry {
        @Unsigned short address;
        @Unsigned int size;
        List<EntryType> types;
        String label;
        Runnable exec;
    }

    private enum EntryType {
        INSTRUCTION,
        BRANCH,
        JUMP_SOURCE,
        JUMP_TARGET,
        SUBROUTINE_CALL,
        SUBROUTINE_START,
        SUBROUTINE_END,
        KEY_CHECK,
        KEY_WAIT,

        DATA,
        SPRITE,
        VARIABLES,
        BCD,

        UNKNOWN,
    }
}
