package forexbot.modules.evolver.sandbox;

import forexbot.ForexBot;
import forexbot.core.containers.AvailableSymbols;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;
import forexbot.modules.cyclecomponents.indicators.Indicators;
import forexbot.modules.cyclecomponents.transactions.DecisionModule;
import forexbot.modules.evolver.SandboxController;
import forexbot.modules.evolver.containers.Genom;

public class Controller implements Control{
	public final int ID;
	public final Genom GENOM;
	/*
	 * Purpose of this class is to emulate main cycle loop for evolutionary algorithm purposes 
	 * 
	 * cycle should be identical except for Scrobbler (emulated by database feed of historical data from SandboxController) 
	 * and TransactionModule (emulated by Evaluator)
	 */
	
	public Controller(SandboxController SANDBOX_CONTROLLER, int ID, Genom GENOM){
		this.SANDBOX_CONTROLLER = SANDBOX_CONTROLLER;
		this.GENOM = GENOM;
		this.ID = ID;
		
		work_flag = false;
		error_flag = false;
		terminate_flag = false;

	}	
	
	@Override
	public void InitializeCycle() {
		// Initialize this instance of sandbox thread
		cache = new LocalCache(SANDBOX_CONTROLLER.SANDBOX_CACHE_SIZE, this);
		cache.CreateSymbolCache(SANDBOX_CONTROLLER.SANDBOX_SYMBOL_NAME);
		indicators = new Indicators(this);
		decider = new DecisionModule(this);
		indicators.setIndicatorsPeriods(GENOM.getValue("RSI_period"), GENOM.getValue("MACDs_period"), GENOM.getValue("MACDl_period"), GENOM.getValue("MACDh_period"), GENOM.getValue("StochasticK_period"), GENOM.getValue("StochasticD_period"));
		
	}


	@Override
	public void StartCycle() {
		// Start virtual cycle
		
		work_flag = true;
		
	}

	@Override
	public void StopCycle() {
		// Stop virtual cycle
		
		work_flag = false;
	}
	

	@Override
	public AvailableSymbols getAvailableSymbols() {
		// Method mask
		return null;
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
	private String symbol;
	private LocalCache cache;
	private Indicators indicators;
	private DecisionModule decider;
	
	private SandboxController SANDBOX_CONTROLLER;
	
	//variables (flags)
		private boolean work_flag;
		private boolean error_flag;
		private boolean terminate_flag;

	@Override
	public void run() {

		do{
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}//Apparently loop need wait time to check conditions below (doesn't work without)
			
			if(work_flag){
				
				//download new listing
				
				//process indicators
				
				//create recommendation
				
			}
			
			
			try {
				Thread.sleep(9);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}
			
		}while(!terminate_flag);
		
	}

}
