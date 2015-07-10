package forexbot.modules.evolver.containers;

import java.io.Serializable;
import java.util.HashMap;

public class Genom implements Serializable{
	
	/**
	 * Container class for generation variables 
	 * serializable in case evolution tree needs to be stopped before completion
	 */
	private static final long serialVersionUID = 2631109610137427744L;


	public Genom(){
		periods = new HashMap<String, Integer>();
	}
	
	public Genom(int StochasticK_period, int StochasticD_period , int Stochastic_Slow){
		this();		
		
		periods.put("StochasticK_period", StochasticK_period);
		periods.put("StochasticD_period", StochasticD_period);
		periods.put("Stochastic_Slow", Stochastic_Slow);
		
	}
	
	public int getValue(String key){
		
		return periods.get(key);
	}
	
	//-----------------------------------------------------------------

	public static Genom Cross(Genom A, Genom B){
		
		return null;
	}
	
	public static Genom Mutate(Genom A){
		
		return null;
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
	
	public static enum KEY {StochasticD_period, StochasticK_period, Stochastic_Slow};
	
}
