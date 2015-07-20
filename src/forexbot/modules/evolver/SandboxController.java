package forexbot.modules.evolver;

import java.util.ArrayList;

import forexbot.ForexBot;
import forexbot.core.containers.SymbolListing;
import forexbot.modules.cyclecomponents.LocalCache;
import forexbot.modules.evolver.containers.Genom;
import forexbot.modules.evolver.sandbox.Controller;
import forexbot.modules.evolver.sandbox.Evaluator;

public class SandboxController {
	/*
	 * Purpose of this class is to manage operation of EA
	 */
	
	public final int SANDBOX_CACHE_SIZE;
	
	public SandboxController(EvolutionaryAlgorithm HANDLER, int SANDBOX_CACHE_SIZE){
		this.HANDLER = HANDLER;
		this.SANDBOX_CACHE_SIZE = SANDBOX_CACHE_SIZE;
		
		listing_cache = null;
	}

	public void addNewGeneration(Genom[] generation){
		/*
		 * Creates sets of controllers for current generation
		 */
		GENERATION = new Controller[generation.length];
		outcome = new ArrayList<Evaluator>();
		
		for(int i = 0; i < generation.length; i++){
			ForexBot.log.addLogDEBUG(generation[i].toString());
			GENERATION[i] = new Controller(this, generation[i]);
			ForexBot.log.addLogDEBUG("Controller "+i +" created");
		}
		
		ForexBot.log.addLogINFO("[AI] Generation "+HANDLER.getGenerationNUmber()+ " added to sandbox.");
	}
	//--------------------------------------------------------------
	public void LogEvent(String log){
		ForexBot.log.addLogDEBUG("[AI] "+log);
	}
	
	public void LogEventError(String log){
		ForexBot.log.addLogWARNING("[AI Error] "+log);
	}
	//--------------------------------------------------------------
	public void InitiateGeneration(){
		//Set variables
		for(Controller c : GENERATION){
			c.InitializeCycle();
		}
		
		ForexBot.log.addLogINFO("[AI] Generation "+HANDLER.getGenerationNUmber()+ " initiated.");
	}
	
	public int progress;
	
	public boolean ProcessGeneration(){
		/*
		 * Key method of EA, here entire generation is processed and passed for evaluation,
		 * every specimen works in separate thread within thread pool,
		 * method returns true when all have finished work
		 */
		ForexBot.log.addLogINFO("[AI] Generation "+HANDLER.getGenerationNUmber()+ " started processing simulations.");
		
		boolean error = false;
		
		for(int i = 0; i < GENERATION.length; i++){
			
			Evaluator ev = GENERATION[i].Simulate();
			outcome.add(ev);
		}
				
		
		
		if(!error){
			ForexBot.log.addLogINFO("[AI] Generation "+HANDLER.getGenerationNUmber()+ " finished processing simulations.");
			return true;
		}
		
		ForexBot.log.addLogERROR("[AI] Generation "+HANDLER.getGenerationNUmber()+ " error in processing simulations.");
		
		return false;
	}
	
	public Genom[] EvaluateGeneration(){
		//Calculate evaluations
		ArrayList<Genom> temp = new ArrayList<Genom>();
		
		for(int i = 0; i < outcome.size(); i++){
			Genom g = outcome.get(i).getGenom();
			
			int score = outcome.get(i).Evaluate();
			g.setEvaluation(score);
			
			temp.add(g);			
		}
		
		Genom[] result = new Genom[temp.size()];
		for(int i2 = 0; i2 < result.length; i2++){
			result[i2] = temp.get(i2);
		}
		
		ForexBot.log.addLogINFO("[AI] Generation "+HANDLER.getGenerationNUmber()+ " successfully evaluated.");
		
		return result;
	}
	
	public void LoadDataFromDBC() throws Exception{
		/*
		 * Loads historic data from database for future processing by EA
		 */
		ForexBot.log.addLogDEBUG("Loading form dbc...");
		if(listing_cache == null){
			SymbolListing[] temporary = LocalCache.LoadSymbolListings(SANDBOX_CACHE_SIZE);
			
			if(temporary == null){
				ForexBot.log.addLogDEBUG("No listings!");
				throw new Exception("No listings!");
			}else if(temporary.length < SANDBOX_CACHE_SIZE){
				ForexBot.log.addLogDEBUG("Not enough listings in database!");
				throw new Exception("Not enough listings in database!");
			}
			
			ForexBot.log.addLogDEBUG("Loading complete.");
			listing_cache = temporary;
		}
	}
	
	public SymbolListing[] DataToProcess(){	
		/*
		 * Provides listings for processing
		 */
		return listing_cache;
	}
	
	private Controller[] GENERATION;
	private EvolutionaryAlgorithm HANDLER;
	
	private SymbolListing[] listing_cache;
	
	private ArrayList<Evaluator> outcome;
}
