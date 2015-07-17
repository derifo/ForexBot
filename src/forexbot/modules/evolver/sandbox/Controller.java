package forexbot.modules.evolver.sandbox;

import java.util.concurrent.Callable;

import forexbot.ForexBot;
import forexbot.core.containers.Recommendation;
import forexbot.core.containers.SymbolListing;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;
import forexbot.modules.cyclecomponents.indicators.Indicators;
import forexbot.modules.cyclecomponents.transactions.DecisionModule;
import forexbot.modules.evolver.SandboxController;
import forexbot.modules.evolver.containers.Genom;

public class Controller implements Control, Callable<Evaluator>{
	public final int ID;
	public final Genom GENOM;
	/*
	 * Purpose of this class is to emulate main cycle loop for evolutionary algorithm purposes 
	 * 
	 * cycle should be identical except for Scrobbler (emulated by database feed of historical data from SandboxController) 
	 * and TransactionModule (emulated by Evaluator)
	 */
	
	public Controller(SandboxController SANDBOX_CONTROLLER, Genom GENOM){
		this.SANDBOX_CONTROLLER = SANDBOX_CONTROLLER;
		this.GENOM = GENOM;
		this.ID = GENOM.ID;
		
		work_flag = false;
		error_flag = false;
		terminate_flag = false;

	}	
	
	@Override
	public void InitializeCycle() {
		// Initialize this instance of sandbox thread
		cache = new LocalCache(SANDBOX_CONTROLLER.SANDBOX_CACHE_SIZE, this);
		cache.CreateSymbolCache();
		indicators = new Indicators(this);
		decider = new DecisionModule(this);
		indicators.setIndicatorsPeriods(GENOM.getValue("StochasticK_period"), GENOM.getValue("StochasticD_period"), GENOM.getValue("Stochastic_Slow"));
		evaluator = new Evaluator(GENOM);
		
		SANDBOX_CONTROLLER.LogEvent("Specimen "+ID+ "created.");
	}


	@Override
	public void StartCycle() {
		// Start virtual cycle
		toProcess = SANDBOX_CONTROLLER.DataToProcess();		
		work_flag = true;
		
		SANDBOX_CONTROLLER.LogEvent("Specimen "+ID+ "starting cycle.");
	}

	@Override
	public void StopCycle() {
		// Stop virtual cycle
		
		work_flag = false;
	}

	@Override
	public LocalCache getCache() {
		// Method mask
		return cache;
	}

	@Override
	public void LogEntry(String level, String msg) {
		// Method mask
		
	}

	@Override
	public void UploaderQueue(String query) {
		// Method mask
		
	}
	
	public void Terminate(){
		//Method for terminating thread by sandbox controller
		terminate_flag = true;
	}
	
	//-----------------------------------------------------------------------
	private LocalCache cache;
	private Indicators indicators;
	private DecisionModule decider;
	private Evaluator evaluator;
	
	private SymbolListing[] toProcess;
	
	private SandboxController SANDBOX_CONTROLLER;
	
	//variables (flags)
		private boolean work_flag;
		@SuppressWarnings("unused")
		private boolean error_flag;
		private boolean terminate_flag;

	

	@Override
	public Evaluator call() throws Exception {
		int progress = 0;
		
		double STOCHASTIC_D;
		double STOCHASTIC_K;
		
		do{
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}//Apparently loop need wait time to check conditions below (doesn't work without)
			
			if(work_flag){
				
				//download new listing
				SymbolListing temp = toProcess[progress];
				progress++;
				cache.addListingToCache(temp);
				
				//process indicators
				if(indicators.LoadCache(0)){
					STOCHASTIC_D = indicators.Calculate_Stochastic_D();
					STOCHASTIC_K = indicators.Calculate_Stochastic_K(0);
					
					//create recommendation
					decider.addIndicators(STOCHASTIC_K, STOCHASTIC_D);					
					Recommendation r = decider.MakeDecision("SANDBOX");
					
					//add to Evaluator
					evaluator.addRecommendation(r, temp);
				}			
				
			}
			
				if(progress == toProcess.length) terminate_flag = true;
			try {
				Thread.sleep(9);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}
			
		}while(!terminate_flag);
		
		SANDBOX_CONTROLLER.LogEvent("Specimen "+ID+ " finished.");
		
		return evaluator;
	}

}
