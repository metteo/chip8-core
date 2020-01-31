package net.novaware.chip8.core.cpu.instruction

import spock.lang.Specification
import spock.lang.Unroll

import static InstructionType.*

class InstructionRegistryIT extends Specification {

    InstructionRegistry registry = new InstructionRegistry()

    def "should return empty optional on unknown instruction"() {
        expect:
        null == registry.getDefinition(0xFEEE as short)
    }

    @Unroll
    def "should decode #type into opcode and params" () {
        given: "input from the table"
        short instruction = input as short

        when: "asking for instance of definition"
        def instance = registry.getDefinition(instruction)

        then: "returned instance is correct"
        instance.isRecognized(instruction)

        and: "behaves as it should"
        type.opcode() == instance.getOpCode()
        count == instance.getParamCount()
        (short) param1 == instance.getParam(0, instruction)
        (short) param2 ==  instance.getParam(1, instruction)
        (short) param3 == instance.getParam(2, instruction)

        where:

        input  || type   | count | param1 | param2 | param3
        0x00E0 || Ox00E0 | 0     | 0x0    | 0x0    | 0x0
        0x00EE || Ox00EE | 0     | 0x0    | 0x0    | 0x0
        0x0123 || Ox0MMM | 1     | 0x0123 | 0x0    | 0x0

        0x1234 || Ox1MMM | 1     | 0x0234 | 0x0    | 0x0
        0x2345 || Ox2MMM | 1     | 0x0345 | 0x0    | 0x0
        0x3456 || Ox3XKK | 2     | 0x0004 | 0x0056 | 0x0
        0x4567 || Ox4XKK | 2     | 0x0005 | 0x0067 | 0x0
        0x5670 || Ox5XY0 | 2     | 0x0006 | 0x0007 | 0x0
        0x6789 || Ox6XKK | 2     | 0x0007 | 0x0089 | 0x0
        0x789A || Ox7XKK | 2     | 0x0008 | 0x009A | 0x0

        0x8AB0 || Ox8XY0 | 2     | 0x000A | 0x000B | 0x0
        0x89A1 || Ox8XY1 | 2     | 0x0009 | 0x000A | 0x0
        0x8122 || Ox8XY2 | 2     | 0x0001 | 0x0002 | 0x0
        0x8673 || Ox8XY3 | 2     | 0x0006 | 0x0007 | 0x0
        0x8354 || Ox8XY4 | 2     | 0x0003 | 0x0005 | 0x0
        0x8345 || Ox8XY5 | 2     | 0x0003 | 0x0004 | 0x0
        0x8456 || Ox8XY6 | 2     | 0x0004 | 0x0005 | 0x0
        0x8697 || Ox8XY7 | 2     | 0x0006 | 0x0009 | 0x0
        0x845E || Ox8XYE | 2     | 0x0004 | 0x0005 | 0x0

        0x9670 || Ox9XY0 | 2     | 0x0006 | 0x0007 | 0x0

        0xABCD || OxAMMM | 1     | 0x0BCD | 0x0    | 0x0
        0xBCDE || OxBMMM | 1     | 0x0CDE | 0x0    | 0x0
        0xCDEF || OxCXKK | 2     | 0x000D | 0x00EF | 0x0
        0xD123 || OxDXYK | 3     | 0x0001 | 0x0002 | 0x0003

        0xE5A1 || OxEXA1 | 1     | 0x0005 | 0x0    | 0x0
        0xE49E || OxEX9E | 1     | 0x0004 | 0x0    | 0x0

        0xF507 || OxFX07 | 1     | 0x0005 | 0x0    | 0x0
        0xF30A || OxFX0A | 1     | 0x0003 | 0x0    | 0x0
        0xF515 || OxFX15 | 1     | 0x0005 | 0x0    | 0x0
        0xF218 || OxFX18 | 1     | 0x0002 | 0x0    | 0x0
        0xF41E || OxFX1E | 1     | 0x0004 | 0x0    | 0x0
        0xF729 || OxFX29 | 1     | 0x0007 | 0x0    | 0x0
        0xFA33 || OxFX33 | 1     | 0x000A | 0x0    | 0x0
        0xFB55 || OxFX55 | 1     | 0x000B | 0x0    | 0x0
        0xFE65 || OxFX65 | 1     | 0x000E | 0x0    | 0x0
    }
}
