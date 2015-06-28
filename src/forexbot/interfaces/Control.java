package forexbot.interfaces;

import forexbot.core.containers.AvailableSymbols;
import forexbot.modules.cyclecomponents.LocalCache;

public interface Control extends Runnable{
	/*
	 * Interface for control modules
	 * 
	 * purpose of this interface is to enable transition between work environment and sandbox 
	 * for evolutionary algorithm 
	 */
	
	public void InitializeCycle();
	public void StartCycle();
	public void StopCycle();
	
	public AvailableSymbols getAvailableSymbols();
	public LocalCache getCache();
	

}
