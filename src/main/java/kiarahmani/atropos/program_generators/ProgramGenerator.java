package kiarahmani.atropos.program_generators;

import kiarahmani.atropos.program.Program;

public abstract interface ProgramGenerator {

	abstract public Program generate(String... args);

}
