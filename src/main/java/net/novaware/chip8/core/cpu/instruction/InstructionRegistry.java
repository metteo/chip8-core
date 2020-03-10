package net.novaware.chip8.core.cpu.instruction;

import net.novaware.chip8.core.cpu.instruction.definition.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import javax.inject.Inject;
import java.util.*;

import static net.novaware.chip8.core.cpu.instruction.InstructionType.*;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class InstructionRegistry {

    private final Map<Short, Map<Short, InstructionDefinition>> defsIndex;

    @Inject
    public InstructionRegistry() {
        List<InstructionDefinition> defs = new ArrayList<>(Arrays.asList(
                new OpCodeOnlyDef(Ox00E0),
                new OpCodeOnlyDef(Ox00EE),
                new SystemJumpDef(Ox0MMM),
                new AddressOnlyDef(Ox1MMM),
                new AddressOnlyDef(Ox2MMM),
                new RegisterValueDef(Ox3XKK),
                new RegisterValueDef(Ox4XKK),
                new TwoRegistersDef(Ox5XY0),
                new RegisterValueDef(Ox6XKK),
                new RegisterValueDef(Ox7XKK),
                new TwoRegistersDef(Ox8XY0),
                new TwoRegistersDef(Ox8XY1),
                new TwoRegistersDef(Ox8XY2),
                new TwoRegistersDef(Ox8XY3),
                new TwoRegistersDef(Ox8XY4),
                new TwoRegistersDef(Ox8XY5),
                new TwoRegistersDef(Ox8XY6),
                new TwoRegistersDef(Ox8XY7),
                new TwoRegistersDef(Ox8XYE),
                new TwoRegistersDef(Ox9XY0),
                new AddressOnlyDef(OxAMMM),
                new AddressOnlyDef(OxBMMM),
                new RegisterValueDef(OxCXKK),
                new DrawSpriteDef(OxDXYK),
                new OneRegisterDef(OxEXA1),
                new OneRegisterDef(OxEX9E),
                new OneRegisterDef(OxFX07),
                new OneRegisterDef(OxFX0A),
                new OneRegisterDef(OxFX15),
                new OneRegisterDef(OxFX18),
                new OneRegisterDef(OxFX1E),
                new OneRegisterDef(OxFX29),
                new OneRegisterDef(OxFX33),
                new OneRegisterDef(OxFX55),
                new OneRegisterDef(OxFX65)
        ));

        defsIndex = new HashMap<>();

        for (InstructionDefinition def : defs) {
            final InstructionType instructionType = def.getInstructionType();
            final short mask = instructionType.mask();

            final Map<Short, InstructionDefinition> values = defsIndex.computeIfAbsent(mask, k -> new HashMap<>());

            values.put(instructionType.opcode(), def);
        }
    }

    public @Nullable InstructionDefinition getDefinition(final @Unsigned short instruction) {
        final List<InstructionMask> masks = InstructionMask.getInstances();
        int masksSize = masks.size();

        for (int m = 0; m < masksSize; ++m) {

            @Unsigned
            short mask = masks.get(m).value();

            @Unsigned
            short opcode = ushort(uint(mask) & uint(instruction));

            var instrPerMask = defsIndex.get(mask);
            if (instrPerMask == null) {
                throw new AssertionError("impossible"); // we iterate over masks, checker...
            }

            final InstructionDefinition instructionDefinition = instrPerMask.get(opcode);

            if (instructionDefinition != null) {
                boolean recognized = instructionDefinition.isRecognized(instruction);

                if (recognized) {
                    return instructionDefinition;
                }
            }
        }

        return null;
    }
}
