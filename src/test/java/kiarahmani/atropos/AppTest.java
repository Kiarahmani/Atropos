package kiarahmani.atropos;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;

/**
 * Unit test for simple App.
 */
public class AppTest {
	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldAnswerWithTrue() {
		assertTrue(true);
	}

	@Test
	public void confGraphIsNotNull() {
		InputProgramGenerator input_program_generator = new InputProgramGenerator();
		Refactoring_Engine refactoring_engine = new Refactoring_Engine();
		Program bank = input_program_generator.generateBankProgram();
		Conflict_Graph conflict_graph = refactoring_engine.constructConfGraph(bank);

		assertTrue(conflict_graph != null);
	}


}
