package net.novaware.chip8.core.memory;

import java.util.function.Supplier;

import static net.novaware.chip8.core.util.AssertUtil.assertState;

/**
 * <b>IF</b> read only mode is active, blocks writes to underlying memory
 */
public class ReadOnlyMemory extends MemoryDecorator implements Memory {

    private Supplier<Boolean> readOnly = () -> false;

    public ReadOnlyMemory(Memory memory) {
        super(memory);
    }

    public void setReadOnly(Supplier<Boolean> readOnly) {
        this.readOnly = readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = () -> readOnly;
    }

    public boolean isReadOnly() {
        return readOnly.get();
    }

    @Override
    public void setByte(short address, byte value) {
        assertState(!readOnly.get(), () -> getName() + " is in RO mode");

        super.setByte(address, value);
    }

    @Override
    public void setWord(short address, short word) {
        assertState(!readOnly.get(), () -> getName() + " is in RO mode");

        super.setWord(address, word);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        assertState(!readOnly.get(), () -> getName() + " is in RO mode");

        super.setBytes(address, source, length);
    }
}
