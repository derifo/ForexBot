package forexbot.core;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import forexbot.ForexBot;
import forexbot.core.containers.AvailableSymbols;
import forexbot.core.containers.Recommendation;
import forexbot.core.containers.SymbolListing;
import forexbot.core.containers.UserSettings;
import forexbot.core.dbc.DataUploader;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;
import forexbot.modules.cyclecomponents.indicators.Indicators;
import forexbot.modules.cyclecomponents.listings.Scrobbler;
import forexbot.modules.cyclecomponents.transactions.DecisionModule;
import forexbot.modules.cyclecomponents.transactions.TransactionModule;
import forexbot.modules.evolver.EvolutionaryAlgorithm;

public class CycleController implements Control{
	/*
	 * Purpose of this class is to control and maintain of main cycle loop 
	 */

	public CycleController(){
		
		work_flag = false;
		trade_flag = false;
		error_flag = false;
	}
	
	@Override
	public void InitializeCycle() {
		//initialize globals
		ForexBot.dbc.Conect();
		ForexBot.uploader = new DataUploader();
		ForexBot.transaction_module = new TransactionModule();
		
		Thread data_up = new Thread(ForexBot.uploader);
		data_up.start();//data uploader thread start
		
		Thread tr_module = new Thread(ForexBot.transaction_module);
		tr_module.start();//transaction module thread start
		
		ForexBot.EVOLVER = new EvolutionaryAlgorithm(this);
		Thread evolver_th = new Thread(ForexBot.EVOLVER);
		evolver_th.start();//evolutionary tree start
		
		//initialize cycle specifics 		
		available_symbols = new AvailableSymbols();
		cache = new LocalCache(43200, this);//cache size is equal to one day (24h)
		scrobbler = new Scrobbler();
		indicators = new Indicators(this);
		decision_module = new DecisionModule(this);
		
		//do checking
		if(PrepareDatabase()){
			CheckUserSymbols();//check for obsolete
			for(String s : ForexBot.user_settings.getUserSymbols()) cache.CreateSymbolCache(s);//create cache for user symbols
			
			
		}else{
			error_flag = true;
		}
		
		if(!error_flag){
			ForexBot.work_frame.PostLog("Variables initialized.");
			ForexBot.log.addLogINFO("Variables initialized.");
		
			StartCycle();
		}
	}


	@Override
	public void StartCycle() {
		if(!error_flag){
		
			indicators.setIndicatorsPeriods(14, 12, 26, 9, 5, 3);//replace later with evolver data!
			decision_module.CreateIndicatorCache(ForexBot.user_settings.getUserSymbols());//initiate indicator cache
		
			work_flag = true;
			
			
			
			ForexBot.work_frame.PostLog("Cycle started...");
			ForexBot.log.addLogINFO("Cycle started...");
		}else{
			JOptionPane.showMessageDialog(null,        	  
      			    "Internal error! Please restart.",
      			    "Error!",
      			    JOptionPane.WARNING_MESSAGE);  
		}
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
		Recommendation r = null;
		double RSI;
		double MACD;
		double MACD_H;
		double STOCHASTIC_D;
		double STOCHASTIC_K;
		
		try{
			do{
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					if(ForexBot.DEBUG) e1.printStackTrace();
				}//Apparently loop need wait time to check conditions below (doesn't work without)
				
				if(work_flag){
					long start_time = System.currentTimeMillis();
					//download***********************************************************************************
					SymbolListing[] temp = scrobbler.Scrobble(ForexBot.user_settings.getUserSymbols());
						for(SymbolListing l : temp){
							if(ForexBot.DEBUG) System.out.println(l.toString());
							cache.addListingToCache(l.symbol_name, l);//add downloaded listings to cache
						//calculate******************************************************************************
							try {
								indicators.LoadCache(0, l.symbol_name);
							} catch (Exception e) {
								ForexBot.log.addLogDEBUG("Too few listings in cache for ["+l.symbol_name+"]");
								continue;//if not enough listings skip this cycle for symbol
							}
							RSI = indicators.Calculate_RSI();
							MACD = indicators.Calculate_MACD();
							MACD_H = indicators.Calculate_MACD_Histogram();
							STOCHASTIC_D = indicators.Calculate_Stochastic_D();
							STOCHASTIC_K = indicators.Calculate_Stochastic_K(0);
							
							ForexBot.log.addLogDEBUG("======Indicators for ["+l.symbol_name+"]========");
							ForexBot.log.addLogDEBUG("RSI "+RSI);
							ForexBot.log.addLogDEBUG("MACD "+MACD+" / Histogram "+MACD_H);
							ForexBot.log.addLogDEBUG("Stochastic %K "+STOCHASTIC_K+ " Stochastic %D "+STOCHASTIC_D);
							ForexBot.log.addLogDEBUG("");
							
						//decide***********************************************************************************
							decision_module.LoadIndicators(RSI, MACD, MACD_H, STOCHASTIC_K, STOCHASTIC_D, l.symbol_name);
														
							r = decision_module.MakeDecision(l.symbol_name);
							
						//trade***********************************************************************************
							if(trade_flag){
								
								ForexBot.work_frame.PostLog(r.toString());
								ForexBot.log.addLogINFO(r.toString());
								ForexBot.transaction_module.addRecommendation(r);
								
							}
						}
					
					
					
					TikClock(start_time, 1999);
					if(ForexBot.DEBUG) cache.DEBUG_PRINT_CACHE();
				}			
				
				
			}while(!ForexBot.GLOBAL_EXIT);
		}catch(NullPointerException e){
			/*
			 * In case of null pointer adds log message without collapsing entire program
			 * allows for safe termination
			 */
			ForexBot.log.addLogCRITICAL("Internal error within main cycle loop [null pointer exeption]");
			ForexBot.work_frame.PostLog("Main cycle internal error - restart program!");
			error_flag = true;
			StopCycle();
		}
		
	}
	
	public void EvolverInput(){
		
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
	private boolean error_flag;

	private boolean PrepareDatabase(){
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
	}//method for regulating cycle time

	private void CheckUserSymbols(){
		ForexBot.log.addLogINFO("Checking user selected symbols..");
		ArrayList<String> symbols_checked = new ArrayList<String>();
		
		for(String s : ForexBot.user_settings.getUserSymbols()){
			for(String n : available_symbols.getSymbolsAvailableNames()){
				if(s.equals(n)){
					symbols_checked.add(s);
					break;
				}
			}
		}
		
		if(symbols_checked.size() != ForexBot.user_settings.getUserSymbols().length){
			ForexBot.log.addLogWARNING("User symbol missmatch - corecting.");
			
			String[] t = new String[symbols_checked.size()];
			for(int i = 0; i<t.length; i++){
				t[i] = symbols_checked.get(i);
			}
			
			ForexBot.log.addLogWARNING("User symbol missmatch - obsolate symbols removed.");
			
			ForexBot.user_settings.setSymbols(t);
			UserSettings.SaveSettings(ForexBot.user_settings);
			
		}//if some of user selected symbols do not match available symbols removes incorrect entries form settings file 
		
		
		ForexBot.work_frame.PostLog("User symbol checked.");
		ForexBot.log.addLogINFO("User symbol checked.");
	}//method checks for incorrect symbols in user defined list (for example outdated symbols no longer available)
	
}
