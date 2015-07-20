package forexbot.modules.evolver.containers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import forexbot.modules.evolver.EvolutionaryAlgorithm;

public class Genom implements Serializable{
	
	/**
	 * Container class for generation specimen variables 
	 * serializable in case evolution tree needs to be stopped before completion
	 */
	private static final long serialVersionUID = 2631109610137427744L;
	


	public Genom(int ID){
		periods = new HashMap<String, Integer>();
		this.ID = ID;
		evaluation = 0;
	}
	
	public Genom(int ID , int StochasticK_period, int StochasticD_period , int Stochastic_Slow){
		this(ID);		
		
		periods.put("StochasticK_period", StochasticK_period);
		periods.put("StochasticD_period", StochasticD_period);
		periods.put("Stochastic_Slow", Stochastic_Slow);
		
	}
	
	public int getValue(String key){
		
		return periods.get(key);
	}
	
	//-----------------------------------------------------------------

	public static Genom Cross(Genom A, Genom B){
		/*
		 * New genome will inherit all traits of A with one random
		 * trait form B, this will ensure more random changes in
		 * new populations  
		 */
		
		int rnd = randInt(1,3);
		
		switch (rnd){
		case 1 :
			A.periods.remove("StochasticK_period");
			int new_k = B.periods.get("StochasticK_period");
			A.periods.put("StochasticK_period", new_k);				
		break;
		
		case 2 :
			A.periods.remove("StochasticD_period");
			int new_d = B.periods.get("StochasticD_period");
			A.periods.put("StochasticD_period", new_d);		
		break;
			
		case 3 :
			A.periods.remove("Stochastic_Slow");
			int new_s = B.periods.get("Stochastic_Slow");
			A.periods.put("Stochastic_Slow", new_s);	
		break;
	}
		
		
		Genom result = new Genom(0, A.getValue("StochasticK_period"), A.getValue("StochasticD_period"), A.getValue("Stochastic_Slow"));
		
		return result;
	}
	
	public static Genom Mutate(Genom A){
		/*
		 * Random change to one of three values
		 */
		
		int rnd = randInt(1,3);
		
		switch (rnd){
			case 1 :
				A.periods.remove("StochasticK_period");
				int new_k = randInt(EvolutionaryAlgorithm.k_min, EvolutionaryAlgorithm.k_max);
				A.periods.put("StochasticK_period", new_k);				
			break;
			
			case 2 :
				A.periods.remove("StochasticD_period");
				int new_d = randInt(EvolutionaryAlgorithm.d_min, EvolutionaryAlgorithm.d_max);
				A.periods.put("StochasticD_period", new_d);		
			break;
				
			case 3 :
				A.periods.remove("Stochastic_Slow");
				int new_s = randInt(EvolutionaryAlgorithm.slow_min, EvolutionaryAlgorithm.slow_max);
				A.periods.put("Stochastic_Slow", new_s);	
			break;
		}
		
		Genom result = new Genom(0, A.getValue("StochasticK_period"), A.getValue("StochasticD_period"), A.getValue("Stochastic_Slow"));
		
		return result;
	}
	
	
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	//-----------------------------------------------------------------
	
	public int getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(int evaluation) {
		this.evaluation = evaluation;
	}
	
	private HashMap<String, Integer> periods;
	private int evaluation;
	
	private int ID;
	
	public int getID() {
		return ID;
	}

	public void FormatGenom(int newID) {
		ID = newID;
		evaluation = 0;
	}
	
	@Override
	public String toString(){
		return "ID ["+ID+"] evaluation = "+evaluation+ " values: "+ this.getValue("StochasticK_period")+"/"+ this.getValue("StochasticD_period")+"/"+ this.getValue("Stochastic_Slow");
	}

	//public static enum KEY {StochasticD_period, StochasticK_period, Stochastic_Slow};
	
}
