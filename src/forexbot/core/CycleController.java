package forexbot.core;

import javax.swing.JOptionPane;

import forexbot.ForexBot;
import forexbot.core.containers.Recommendation;
import forexbot.core.containers.SymbolListing;
import forexbot.core.dbc.DataUploader;
import forexbot.interfaces.Control;
import forexbot.modules.cyclecomponents.LocalCache;
import forexbot.modules.cyclecomponents.indicators.Indicators;
import forexbot.modules.cyclecomponents.listings.Scrobbler;
import forexbot.modules.cyclecomponents.transactions.DecisionModule;
import forexbot.modules.cyclecomponents.transactions.TransactionModule;
import forexbot.modules.evolver.EvolutionaryAlgorithm;
import forexbot.modules.evolver.containers.Genom;

public class CycleController implements Control, Runnable{
	/*
	 * Purpose of this class is to control and maintain of main cycle loop 
	 */
	private Genom GENOM;

	public CycleController(){
		
		work_flag = false;
		trade_flag = false;
		error_flag = false;
		
		GENOM = new Genom(0,5,3,3);
	}
	
	@Override
	public void InitializeCycle() {
		//initialize globals
		ForexBot.dbc.Conect();
		ForexBot.uploader = new DataUploader();
		transaction_module = new TransactionModule();
		
		Thread data_up = new Thread(ForexBot.uploader);
		data_up.start();//data uploader thread start
		
		ForexBot.EVOLVER = new EvolutionaryAlgorithm(this);
		Thread evolver_th = new Thread(ForexBot.EVOLVER);
		evolver_th.start();//evolutionary tree start
		
		//initialize cycle specifics 		
		cache = new LocalCache(100, this);//cache size
		scrobbler = new Scrobbler();
		indicators = new Indicators(this);
		decision_module = new DecisionModule(this);
		
		//do checking
		if(PrepareDatabase()){

			 cache.CreateSymbolCache();//create cache for user symbols
				
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
		
			indicators.setIndicatorsPeriods(GENOM.getValue("StochasticK_period"), GENOM.getValue("StochasticD_period"), GENOM.getValue("Stochastic_Slow"));
			decision_module.CreateIndicatorCache();//initiate indicator cache
		
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
		
		transaction_module.CloseAll();//in case of stopping cycle all open deal have to be closed
		
		ForexBot.work_frame.PostLog("Cycle stopped.");
		ForexBot.log.addLogINFO("Cycle stopped.");
	}

	public void EnableTrade(){
		trade_flag = true;
		
		ForexBot.work_frame.PostLog("Sufficient level of AI reached - starting trade!");
		ForexBot.log.addLogINFO("Sufficient level of AI reached - starting trade!");
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
	public LocalCache getCache() {

		return cache;
	}

	//Thread loop --------------------------------------------------
	@Override
	public void run() {
		Recommendation r = null;

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
					SymbolListing temp = scrobbler.Scrobble();

					if(ForexBot.DEBUG) System.out.println(temp.toString());
					cache.addListingToCache(temp);//add downloaded listing to cache
					
					//calculate******************************************************************************
					if(indicators.LoadCache(0)){
							
							STOCHASTIC_D = indicators.Calculate_Stochastic_D();
							STOCHASTIC_K = indicators.Calculate_Stochastic_K(0);
								

							ForexBot.log.addLogDEBUG("Stochastic %K "+STOCHASTIC_K+ " Stochastic %D "+STOCHASTIC_D);
							ForexBot.log.addLogDEBUG("");
								
							//decide***********************************************************************************
							decision_module.addIndicators(STOCHASTIC_K, STOCHASTIC_D);
														
							r = decision_module.MakeDecision("WORK");
								
							//trade***********************************************************************************
							if(trade_flag){
									
									ForexBot.work_frame.PostLog(r.toString());
									ForexBot.log.addLogINFO(r.toString());
									
									transaction_module.addRecommendation(r);
									
									//transaction cycle processing									
									transaction_module.Process();	
							}
					}					
					
					TikClock(start_time, 5999);
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
	
	public void EvolverInput(Genom g){
		ForexBot.log.addLogINFO("New settings from AI, loading...");
		
		StopCycle();
		
		GENOM = g;
		
		StartCycle();
		
		ForexBot.log.addLogINFO("New settings from AI, loaded!");
		ForexBot.work_frame.PostLog("New settings from AI, loaded!");
	}
	
	
	//variables (handlers)
	private TransactionModule transaction_module;
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
		

		if(!ForexBot.dbc.CrateSymbolTable("EURUSD")){
				clear = false;
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

}
