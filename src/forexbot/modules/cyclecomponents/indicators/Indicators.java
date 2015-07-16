package forexbot.modules.cyclecomponents.indicators;

import java.util.HashMap;

import forexbot.core.containers.SymbolListing;
import forexbot.interfaces.Control;

public class Indicators {
	/*
	 * Class responsible for calculating current values of indicators
	 * 
	 * Implemented indicators are:
	 * 
	 * Stochastic
	 */
	
	public Indicators(Control controller){
		CONTROLLER = controller;
		periods = new HashMap<String, Integer>();
		
	}
	
	public void setIndicatorsPeriods(int StochasticK_period, int StochasticD_period, int Stochastic_Slow){
		
		periods.put("StochasticK_period", StochasticK_period);
		periods.put("StochasticD_period", StochasticD_period);
		periods.put("Stochastic_Slow", Stochastic_Slow);
		
		CONTROLLER.LogEntry("INFO", "Indicators periods set:  Stochastic "+StochasticK_period + "/"+StochasticD_period + "/"+Stochastic_Slow);
	}
	
	public boolean LoadCache(int offset){
		
		int max = 1 + periods.get("StochasticK_period") + periods.get("StochasticD_period") + periods.get("Stochastic_Slow");
		
		cache = new SymbolListing[max];
		SymbolListing[] p;
		try {
			p = CONTROLLER.getCache().getListingsFromCache(max+offset);
			
			int i2 = 0; 
			for(int i = offset; i < max+offset; i++){
				cache[i2] = p[i];
				i2++;
			}
			
			CONTROLLER.LogEntry("DEBUG", "Indicator cache loaded! Needed/Loaded/Offset: "+max+"/"+p.length+"/"+offset);
			
			return true;
		} catch (Exception e) {
			CONTROLLER.LogEntry("DEBUG", "Too few listings in cache for calculating indicators!");
		} 		
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------	
	//============================ STOCHASTIC 
	
	private double fastK(int offset){
		double K = 0.0;
		double min = 0, max = 0;
		
		for(int i = 0; i < periods.get("StochasticK_period"); i++){
			if(i == 0){
				min = cache[i+offset].bid;
				max = cache[i+offset].bid;
			}else{			
				if(cache[i+offset].bid > max) max = cache[i+offset].bid;
				if(cache[i+offset].bid < min) min = cache[i+offset].bid;
			}
		}
		if(max == min) return 100 * (cache[0+offset].bid - min);
		K = 100 * ((cache[0+offset].bid - min) / (max - min));
		
		return K;
	}
	
	public double Calculate_Stochastic_K(int offset){
		double K = 0.0;
		
		for(int s = 0; s < periods.get("Stochastic_Slow"); s++){
			
			K += fastK(s + offset);
			
		}
		
		K = K / periods.get("Stochastic_Slow");
		
		return K;
	}
	
	public double Calculate_Stochastic_D(){
		double D = 0.0;
		
		for(int i = 0; i < periods.get("StochasticD_period"); i++){
			D += Calculate_Stochastic_K(i);
		}
		
		D = D / periods.get("StochasticD_period");
		
		return D;
	}

	
	//variables
	private Control CONTROLLER;
	private SymbolListing[] cache;
	private HashMap<String, Integer> periods;
}
