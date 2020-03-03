module net.novaware.chip8.core {
    requires static java.compiler; // @j.a.p.Generated

    //TODO: consider transitive / static
    requires javax.inject; // automatic
    requires dagger; //automatic

    requires org.checkerframework.checker.qual;
    requires jsr305; //automatic

    requires org.apache.logging.log4j;

    exports net.novaware.chip8.core;
    exports net.novaware.chip8.core.port;
    exports net.novaware.chip8.core.clock;

    exports net.novaware.chip8.core.cpu;
    exports net.novaware.chip8.core.cpu.register; //TODO: internal, expose part as public API
    exports net.novaware.chip8.core.gpu; //TODO: temporary, screen should not need internal ViewPort
    exports net.novaware.chip8.core.memory;
    exports net.novaware.chip8.core.util; //TODO: should be part of chip8-jvm or sth?
}