module net.novaware.chip8.core {
    requires java.base;

    requires javax.inject; //automatic
    requires dagger; //automatic

    requires org.checkerframework.checker.qual;
    requires jsr305; //automatic

    requires org.apache.logging.log4j;

    exports net.novaware.chip8.core;
    exports net.novaware.chip8.core.port;

    exports net.novaware.chip8.core.util; //TODO: should be part of chip8-jvm or sth?
    exports net.novaware.chip8.core.cpu.register; //TODO: internal, expose part as public API
}