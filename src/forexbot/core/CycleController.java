package forexbot.core;

import forexbot.core.containers.AvailableSymbols;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;

public class CycleController implements Control{
	/*
	 * Purpose of this class is to control and maintain of main cycle loop 
	 */

	public CycleController(){
		
	}
	
	@Override
	public void InitializeCycle() {
		
	}


	@Override
	public void StartCycle() {
		
	}

	@Override
	public void StopCycle() {
		
	}

	
	
	
	@Override
	public AvailableSymbols getAvailableSymbols() {

		return null;
	}

	@Override
	public LocalCache getCache() {

		return null;
	}

	//Thread loop --------------------------------------------------
	@Override
	public void run() {

		
	}
	
	
	//variables
	private AvailableSymbols available_symbols;
	private LocalCache cache;

	}
