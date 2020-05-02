@SuppressWarnings("module") // 8 in chip8 is not a version
module net.novaware.chip8.core {
    requires static java.compiler; // @j.a.p.Generated
    requires auto.value.annotations; //FIXME: filename-based
    requires dagger; //FIXME: filename-based
    requires javax.inject; //FIXME: filename-based, should be transitive of dagger

    requires org.checkerframework.checker.qual;

    requires org.apache.logging.log4j;

    exports net.novaware.chip8.core;
    exports net.novaware.chip8.core.clock;
    exports net.novaware.chip8.core.config;
    exports net.novaware.chip8.core.port;
    exports net.novaware.chip8.core.util;

    uses net.novaware.chip8.core.clock.ClockGenerator;
}