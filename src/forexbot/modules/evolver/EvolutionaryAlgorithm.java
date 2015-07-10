package forexbot.modules.evolver;

import forexbot.ForexBot;
import forexbot.core.CycleController;
import forexbot.modules.evolver.sandbox.Evaluator;

public class EvolutionaryAlgorithm implements Runnable{
	/*
	 * This class is based on principles of artificial intelligence evolutionary algorithm (EA)
	 * by creating initial population using random indicator parameters it's goal is to find
	 * best possible combination of this parameters witch generates most profits
	 * 
	 * Work cycle is as follows:
	 * 	1 create population
	 *  2 evaluate
	 *  3 choose best instances 
	 *  4 generate new population and go back to 2
	 *  
	 *  This works until achieving satisfactory results or proving that this set of indicators can't generate sufficient 
	 *  profit margin.
	 */
	
	public EvolutionaryAlgorithm(CycleController cycle_handle){
		CYCLE_HANDLE = cycle_handle;
		sandbox = new SandboxController(this);
		generation_number = 0;
	}

	
	
	//------------------------------------------------------
	public void CreateInitialPopulation(){
		ForexBot.work_frame.PostLog("[AI] Creating initial generation.");
		ForexBot.log.addLogINFO("[AI] Creating initial generation.");
		
		//Set of min and max values for indicator periods
		//parameters where chosen arbitrarily but with regard to indicator formulas  
		int k_min = 2, k_max = 20;
		int d_min = 2, d_max = 20;
		int slow_min = 1, slow_max = 10;
		
	}
	
	public void CreateNewPopulation(){
		ForexBot.work_frame.PostLog("[AI] Creating new generation.");
		ForexBot.log.addLogINFO("[AI] Creating new generation.");
		
	}

	public void Evolve(){
		
	}

	@Override
	public void run() {
		// Main loop 
		
		ForexBot.log.addLogINFO("[AI] Machine learning module started - initializing evolutionary tree...");
		ForexBot.work_frame.PostLog("[AI] Machine learning module started.");
		
		CreateInitialPopulation();
		Evolve();
		
		do{
			//
			
		}while(!ForexBot.GLOBAL_EXIT);
		
		if(ForexBot.DEBUG) System.out.println("Evolver terminated - global exit!");
		
	}
	
	
	private CycleController CYCLE_HANDLE;
	private Evaluator evaluator;
	private SandboxController sandbox;
	private int generation_number;
	
}
