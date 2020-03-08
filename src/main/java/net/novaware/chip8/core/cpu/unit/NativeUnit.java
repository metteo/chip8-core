package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

@BoardScope
public class NativeUnit implements Unit {

    private static final Logger LOG = LogManager.getLogger();

    @Used
    private final Gpu gpu;

    @Inject
    public NativeUnit(
            final Gpu gpu
    ) {
        this.gpu = gpu;
    }

    /* package */ void callMls(short address) {
        LOG.warn("MLS to address: " + toHexString(address));

        int addrInt = uint(address);

        switch(addrInt) {
            case 0x000:
                gpu.scrollUp(ushort(6));
                break;
            case 0x001: //TODO: verify what this address did in Boot128
                gpu.clearScreen();
                break;
            case 0x010:
                //TODO: exit emulator with code 0
                LOG.warn("Emulator should exit with 0");
                break;
            case 0x011:
                //TODO: exit emulator with code 1
                LOG.warn("Emulator should exit with 1");
                break;
            default:
                throw new RuntimeException("Unknown MLS to address: " + toHexString(address));
        }
    }

}
