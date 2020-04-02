package net.novaware.chip8.core.config;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.unit.ControlUnit;

public interface CoreConfig extends Cpu.Config, ControlUnit.Config, Board.Config {
}
