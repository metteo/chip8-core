package net.novaware.chip8.core.config;

import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.unit.ControlUnit;

//TODO: move all configuration to this interface
public interface Config extends Cpu.Config, ControlUnit.Config {
}