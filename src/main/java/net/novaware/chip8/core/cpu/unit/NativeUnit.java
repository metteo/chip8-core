package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.OUTPUT;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

@BoardScope
public class NativeUnit implements Unit {

    private static final Logger LOG = LogManager.getLogger();

    @Used
    private final WordRegister output;

    @Used
    private final PowerMgmt powerMgmt;

    @Used
    private final Gpu gpu;

    @Inject
    public NativeUnit(
            @Named(OUTPUT) final WordRegister output,
            final PowerMgmt powerMgmt,
            final Gpu gpu
    ) {
        this.output = output;
        this.powerMgmt = powerMgmt;
        this.gpu = gpu;
    }

    /* package */ void callMls(short address) {
        LOG.warn("MLS to address: " + toHexString(address));

        int addrInt = uint(address);

        switch(addrInt) {
            case 0x000:
                gpu.scrollUp(ushort(6));
                break;
            case 0x001:
                //TODO: clear some registers / memory, used by Boot-128
                break;
            case 0x010:
            case 0x011:
                //TODO: document these mls as exit 0/1 routines
                output.set(address);
                powerMgmt.sleep();
                break;
            default:
                throw new RuntimeException("Unknown MLS to address: " + toHexString(address));
        }
    }

}
