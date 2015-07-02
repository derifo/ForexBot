package forexbot.core;

import forexbot.ForexBot;
import forexbot.core.containers.AvailableSymbols;
import forexbot.core.containers.SymbolListing;
import forexbot.core.dbc.DataUploader;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;
import forexbot.modules.cyclecomponents.indicators.Indicators;
import forexbot.modules.cyclecomponents.listings.Scrobbler;
import forexbot.modules.cyclecomponents.transactions.DecisionModule;

public class CycleController implements Control{
	/*
	 * Purpose of this class is to control and maintain of main cycle loop 
	 */

	public CycleController(){
		
		work_flag = false;
		trade_flag = false;
		
	}
	
	@Override
	public void InitializeCycle() {
		//initialize globals
		ForexBot.dbc.Conect();
		ForexBot.uploader = new DataUploader();
		
		//initialize cycle specifics 		
		available_symbols = new AvailableSymbols();
		cache = new LocalCache(100, this);
		scrobbler = new Scrobbler(available_symbols.getSymbolsAvailableNames());
		indicators = new Indicators(this);
		decision_module = new DecisionModule();
		
		//do checking
		if(PrepareDatabase()){
			
			StartCycle();
		}
		
	}


	@Override
	public void StartCycle() {
		
		
		
		work_flag = true;
		ForexBot.work_frame.PostLog("Cycle started...");
		ForexBot.log.addLogINFO("Cycle started...");
	}

	@Override
	public void StopCycle() {
		
		
		work_flag = false;
		ForexBot.work_frame.PostLog("Cycle stopped.");
		ForexBot.log.addLogINFO("Cycle stopped.");
	}

	public void EnableTrade(){
		trade_flag = true;
	}
	
	@Override
	public void LogEntry(String level, String msg) {
		
		if(level.equals("DEBUG")) ForexBot.log.addLogDEBUG(msg);
		if(level.equals("INFO")) ForexBot.log.addLogINFO(msg);
		if(level.equals("WARNING")) ForexBot.log.addLogWARNING(msg);
		if(level.equals("ERROR")) ForexBot.log.addLogERROR(msg);
		if(level.equals("CRIRICAL")) ForexBot.log.addLogCRITICAL(msg);
		
	}

	@Override
	public void UploaderQueue(String query) {
		
		ForexBot.uploader.addUploadQuery(query);
		
	}
	
	
	@Override
	public AvailableSymbols getAvailableSymbols() {

		return available_symbols;
	}

	@Override
	public LocalCache getCache() {

		return cache;
	}

	//Thread loop --------------------------------------------------
	@Override
	public void run() {

		do{
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}//Apparently loop need wait time to check conditions below (doesn't work without)
			
			if(work_flag){
				long start_time = System.currentTimeMillis();
				
				SymbolListing[] temp = scrobbler.Scrobble();
				for(SymbolListing l : temp) System.out.println(l.toString());
				
				
				if(trade_flag){
					
					
					
				}
				
				TikClock(start_time, 1999);
			}
			
			
			
			
		}while(!ForexBot.GLOBAL_EXIT);
		
	}
	
	
	//variables (handlers)
	private AvailableSymbols available_symbols;
	private LocalCache cache;
	private Scrobbler scrobbler;
	private Indicators indicators;
	private DecisionModule decision_module;
	
	//variables (flags)
	private boolean work_flag;
	private boolean trade_flag;
	

	public boolean PrepareDatabase(){
		ForexBot.log.addLogDEBUG("Preparing database..");
		boolean clear = true;
		
		for(String s : available_symbols.getSymbolsAvailableNames()){
			if(!ForexBot.dbc.CrateSymbolTable(s)){
				clear = false;
			}
		}
		
		if(clear){
			ForexBot.work_frame.PostLog("Database integrity - OK");
			ForexBot.log.addLogINFO("Database integrity - OK");
		}
		else{
			ForexBot.work_frame.PostLog("Database integrity - Failed");
			ForexBot.log.addLogCRITICAL("Database integrity - Failed");
		}
		return clear;
	}//method prepares database tables sets for all user defined symbols to work with
	
	private long TikClock(long start_time, long sleep){
		long time_elapsed = System.currentTimeMillis() - start_time;
		
		ForexBot.log.addLogDEBUG("cycle - time elapsed ["+time_elapsed+"]");
		if(time_elapsed < sleep) {
			try {
			    Thread.sleep(sleep - time_elapsed);                 //waits for remaining time
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
		
		return time_elapsed;
	}

	
}
