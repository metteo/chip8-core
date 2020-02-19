package net.novaware.chip8.core.memory;

import static net.novaware.chip8.core.util.AssertUtil.assertState;

/**
 * <b>IF</b> read only mode is active, blocks writes to underlying memory
 */
public class ReadOnlyMemory extends MemoryDecorator implements Memory {

    private boolean readOnly = false;

    public ReadOnlyMemory(Memory memory) {
        super(memory);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setByte(short address, byte value) {
        assertState(readOnly, getName() + " is in RO mode");

        super.setByte(address, value);
    }

    @Override
    public void setWord(short address, short word) {
        assertState(readOnly, getName() + " is in RO mode");

        super.setWord(address, word);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        assertState(readOnly, getName() + " is in RO mode");

        super.setBytes(address, source, length);
    }
}
