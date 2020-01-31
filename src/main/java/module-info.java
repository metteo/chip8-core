module net.novaware.chip8.core {
    requires java.base;
    requires java.desktop; //TODO: should be in gui only

    requires javax.inject; //automatic
    requires dagger; //automatic

    requires org.checkerframework.checker.qual;
    requires jsr305; //automatic

    exports net.novaware.chip8.core;
}