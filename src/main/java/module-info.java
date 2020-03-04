module net.novaware.chip8.core {
    requires static java.compiler; // @j.a.p.Generated
    requires dagger; //TODO: filename-based
    requires javax.inject; //TODO: filename-based, should be transitive of dagger

    requires org.checkerframework.checker.qual;

    requires org.apache.logging.log4j;

    exports net.novaware.chip8.core;
    exports net.novaware.chip8.core.port;
    exports net.novaware.chip8.core.clock;
    exports net.novaware.chip8.core.util;

    uses net.novaware.chip8.core.clock.ClockGenerator;

    exports net.novaware.chip8.core.gpu; //TODO: temporary, screen should not need internal ViewPort
}