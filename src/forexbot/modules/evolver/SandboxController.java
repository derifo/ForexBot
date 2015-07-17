package forexbot.modules.evolver;

import java.util.ArrayList;

import forexbot.ForexBot;
import forexbot.core.containers.SymbolListing;
import forexbot.modules.evolver.containers.Genom;
import forexbot.modules.evolver.sandbox.Controller;
import forexbot.modules.evolver.sandbox.Evaluator;

public class SandboxController {
	
	public final int SANDBOX_CACHE_SIZE;
	
	public SandboxController(EvolutionaryAlgorithm HANDLER, int SANDBOX_CACHE_SIZE){
		this.HANDLER = HANDLER;
		this.SANDBOX_CACHE_SIZE = SANDBOX_CACHE_SIZE;
		
		listing_cache = new SymbolListing[this.SANDBOX_CACHE_SIZE];
	}

	public void addNewGeneration(Genom[] generation){
		/*
		 * Creates sets of controllers for current generation
		 */
		GENERATION = new Controller[generation.length];
		
		for(int i = 0; i < generation.length; i++){
			GENERATION[i] = new Controller(this, generation[i]);
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
	
	public boolean ProcessGeneration(){
		//Process
				
		ForexBot.log.addLogINFO("[AI] Generation "+HANDLER.getGenerationNUmber()+ " finished processing simulations.");
		
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
	
	public SymbolListing[] DataToProcess(){
		
		return null;
	}
	
	private Controller[] GENERATION;
	private EvolutionaryAlgorithm HANDLER;
	
	private SymbolListing[] listing_cache;
	
	private ArrayList<Evaluator> outcome;
}
