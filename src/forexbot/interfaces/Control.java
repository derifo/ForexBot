package forexbot.interfaces;

import forexbot.modules.cyclecomponents.LocalCache;

public interface Control{
	/*
	 * Interface for control modules
	 * 
	 * purpose of this interface is to enable transition between work environment and sandbox 
	 * for evolutionary algorithm 
	 */
	
	//cycle control
	public void InitializeCycle();
	public void StartCycle();
	public void StopCycle();
	
	//listing cache access
	public LocalCache getCache();
	
	//methods masks
	public void LogEntry(String level, String msg);
	public void UploaderQueue(String query);

}
