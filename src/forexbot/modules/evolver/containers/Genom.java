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
	
	public Genom(int RSI_period, int MACDs_period, int MACDl_period, int MACDh_period, int StochasticK_period, int StochasticD_period){
		this();		
		
		periods.put("RSI_period", RSI_period);
		periods.put("MACDs_period", MACDs_period);
		periods.put("MACDl_period", MACDl_period);
		periods.put("MACDh_period", MACDh_period);
		periods.put("StochasticK_period", StochasticK_period);
		periods.put("StochasticD_period", StochasticD_period);
		
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
	
}
