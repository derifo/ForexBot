package forexbot.modules.cyclecomponents.indicators;

import java.util.HashMap;
import java.util.Map.Entry;

import forexbot.core.containers.SymbolListing;
import forexbot.interfaces.Control;

public class Indicators {
	/*
	 * Class responsible for calculating current values of indicators
	 * 
	 * Implemented indicators are:
	 * RSI
	 * MACD
	 * Stochastic
	 */
	
	public Indicators(Control controller){
		CONTROLLER = controller;
		periods = new HashMap<String, Integer>();
		
	}
	
	public void setIndicatorsPeriods(int RSI_period, int MACDs_period, int MACDl_period, int MACDh_period, int StochasticK_period, int StochasticD_period){
		
		periods.put("RSI_period", RSI_period);
		periods.put("MACDs_period", MACDs_period);
		periods.put("MACDl_period", MACDl_period);
		periods.put("MACDh_period", MACDh_period);
		periods.put("StochasticK_period", StochasticK_period);
		periods.put("StochasticD_period", StochasticD_period);
		
		CONTROLLER.LogEntry("INFO", "Indicators periods set: RSI "+RSI_period+" / MACD  S "+MACDs_period+
				" L"+MACDl_period+ " histogram "+MACDh_period+ " / Stochastic "+StochasticK_period + "/"+StochasticD_period );
	}
	
	public void LoadCache(int offset, String name) throws Exception{
		
		int max = 0;
		
		for(Entry<String, Integer> entry : periods.entrySet()) {
			if(max < entry.getValue()) max = entry.getValue();
		}
		
		cache = new SymbolListing[max];
		SymbolListing[] p = CONTROLLER.getCache().getListingsFromCache(name, max+offset); 
		int i2 = 0; 
		for(int i = offset; i < max+offset; i++){
			cache[i2] = p[i];
			i2++;
		}
		
		CONTROLLER.LogEntry("DEBUG", "Indicator cache for ["+name+"] loaded! Needed/Loaded/Offset: "+max+"/"+p.length+"/"+offset);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Indicators
	private double EMA(int period){
		double ema = 0.0;
		double top,bot;
		double a = 1.0 - (2.0/ (period+1.0));
		
		top = cache[0].bid;//newest
		bot = 1.0;
		
		for(int i = 1; i < period; i++){
			
			top += Math.pow(a, i) * cache[i].bid;
			bot += Math.pow(a, i);
		}
		
		ema = top / bot;
		
		return ema;
	}
	
	//============================ RSI
	
	private double Partial_RS(){
		double RS = 0.0;
		double G = 0.0;
		double L = 0.0;
		
		for(int i = 0; i < periods.get("RSI_period"); i++){
			if((cache[i].bid - cache[i+1].bid) > 0){
				G += cache[i].bid;
			}else if((cache[i].bid - cache[i+1].bid) < 0){
				L += cache[i].bid;
			}
		}
		
		if(L == 0.0) return G;

		RS = ( G / periods.get("RSI_period") ) / ( L / periods.get("RSI_period") );
		
		return RS;
	}
	
	public double Calculate_RSI(){
		return 100 - (100 / (1+Partial_RS()) ); //value 0-100
	}
	
	//============================ MACD
	
	public double Calculate_MACD(){
		return (EMA(periods.get("MACDs_period")) - EMA(periods.get("MACDl_period")));
	}
	
	public double Calculate_MACD_Histogram(){
		return (Calculate_MACD() - EMA(periods.get("MACDh_period")));
	}
	
	//============================ STOCHASTIC 
	
	public double Calculate_Stochastic_K(int offset){
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
		if(max == min) return cache[0+offset].bid - min;
		K = 100 * ((cache[0+offset].bid - min) / (max - min));
		
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
