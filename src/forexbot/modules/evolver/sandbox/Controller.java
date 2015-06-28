package forexbot.modules.evolver.sandbox;

import forexbot.core.containers.AvailableSymbols;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;

public class Controller implements Control{
	/*
	 * Purpose of this class is to emulate main cycle loop for evolutionary algorithm purposes 
	 * 
	 * cycle should be identical except for Scrobbler thread (emulated by database feed of historical data from SandboxController) 
	 * and TransactionModule (emulated by Evaluator)
	 */
	
	public Controller(){
		
	}	
	
	@Override
	public void InitializeCycle() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void StartCycle() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StopCycle() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public AvailableSymbols getAvailableSymbols() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalCache getCache() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
