package forexbot.modules.evolver;

import forexbot.core.containers.SymbolListing;
import forexbot.modules.evolver.containers.Genom;
import forexbot.modules.evolver.sandbox.Controller;

public class SandboxController {
	
	public final int SANDBOX_CACHE_SIZE;
	
	public SandboxController(EvolutionaryAlgorithm HANDLER, int SANDBOX_CACHE_SIZE){
		this.HANDLER = HANDLER;
		this.SANDBOX_CACHE_SIZE = SANDBOX_CACHE_SIZE;
		
		listing_cache = new SymbolListing[this.SANDBOX_CACHE_SIZE];
	}

	public void addNewGeneration(Genom[] generation){
		/*
		 * Creates sets of controllers for current generation
		 */
		GENERATION = new Controller[generation.length];
		
		for(int i = 0; i < generation.length; i++){
			GENERATION[i] = new Controller(this, getID(), generation[i]);
		}
		
		ID += 1000000;//designation for next generation (0-gen 1,2,3.. 1-gen 1000001,1000002... )
	}
	
	public void InitiateGeneration(){
		
	}
	
	public void ProcessGeneration(){
		
	}
	
	public void EvaluateGeneration(){
		
	}
	
	public SymbolListing[] DataToProcess(){
		
		return null;
	}
	
	private Controller[] GENERATION;
	private EvolutionaryAlgorithm HANDLER;
	
	private SymbolListing[] listing_cache;
	
	private static int ID = 0;	
	private static int getID(){
		ID++;
		return ID;
	}
}
