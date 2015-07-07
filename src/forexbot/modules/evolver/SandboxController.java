package forexbot.modules.evolver;

import forexbot.modules.evolver.sandbox.Controller;

public class SandboxController {
	
	public static int SANDBOX_CACHE_SIZE;
	public static String SANDBOX_SYMBOL_NAME;
	
	public SandboxController(EvolutionaryAlgorithm HANDLER){
		this.HANDLER = HANDLER;
	}

	public void addNewGeneration(Controller[] generation){
		/*
		 * Add new generation for evolution
		 */
		this.GENERATION = generation;
	}
	
	public void InitiateGeneration(){
		
	}
	
	public void EvaluateGeneration(){
		
	}
	
	private Controller[] GENERATION;
	private EvolutionaryAlgorithm HANDLER;
	
}
