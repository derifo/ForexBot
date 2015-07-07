package forexbot.modules.cyclecomponents.transactions;

import java.util.HashMap;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import forexbot.interfaces.Control;
import forexbot.core.containers.SymbolListing;
import forexbot.core.containers.Transaction;


public class DecisionModule {
	
	private static final int CACHE_SIZE = 20;
	private static final int ASSESSMENT_PERIOD = 5;
	
	public DecisionModule(Control controller){
		CONTROLLER = controller;		
	}
	
	public void CreateIndicatorCache(String[] symbols){
		/*
		 * Create cache for all user symbols
		 */
		indicators = new HashMap<String, IndicatorCache>();
		
		for(String s : symbols){
			indicators.put(s, new IndicatorCache(s));
		}
	}
	
	public void LoadIndicators(double RSI, double MACDs, double MACD_histogram, double StochasticK, double StochasticD , String name){
		/*
		 * Load calculated values to respective symbol cache for further processing
		 */		
		indicators.get(name).addInstance(RSI, MACDs, MACD_histogram, StochasticK, StochasticD);		
	}
	
	public void MakeDecision(String name){
		/*
		 * Each recommendation should return value between 1 and -1 where
		 * 	1  - BUY
		 * 	-1 - SELL
		 * 
		 * There are four recommendations so value above  
		 */
		
		double r_rsi, r_macd, r_stochastic, r_trend;
		r_rsi = recomendation_RSI(name);
		r_macd = recomendation_MACD(name);
		r_stochastic = recomendation_STOCHASTIC(name);
		r_trend = TrendAssessment(name);
		
		double rr = r_rsi + r_macd + r_stochastic + r_trend;
		
		if(rr >= 2) recomendation = "BUY";
		else if(rr <= -2) recomendation = "SELL";
		else recomendation = "KEEP";
		
		CONTROLLER.LogEntry("DEBUG", "Partial: RSI "+r_rsi +" MACD " + r_macd + " Stochastic " + r_stochastic + " Trend " + r_trend + " || DECISION : "+ recomendation);
	}
	
	public Transaction Transaction(String name){
		if(recomendation.equals("BUY")){
			
		}else if(recomendation.equals("SELL")){
			
		}else{
			
		}
		
		return null;
	}
	
	private double TrendAssessment(String name){
		/*
		 * Purpose of this method is to assess current trend 
		 * there are two options:
		 * 		rising (prices are rising)
		 * 		falling (prices are falling)
		 * 
		 * In each case method gives adequate recommendation
		 */
		double partial = 0.0;
		try {
			SymbolListing[] listings = CONTROLLER.getCache().getListingsFromCache(name, ASSESSMENT_PERIOD);
			
			//for(int i = 0; i < ASSESSMENT_PERIOD; i++ ){
				//
			//}
			
			partial = listings[0].bid - listings[ASSESSMENT_PERIOD-1].bid; 
			if(partial > 0) return 1;
			else if(partial < 0) return -1;
			
		} catch (Exception e) {
			CONTROLLER.LogEntry("DEBUG", "Not enough listings in cache for assessment ["+name+"] !");
		}
		
		
		return 0.0;
	}
	
	private double recomendation_RSI(String name){
		
		return 0.0;
	}
	
	private double recomendation_MACD(String name){
		
		return 0.0;
	}
	
	private double recomendation_STOCHASTIC(String name){
		
		return 0.0;
	}

	private HashMap<String, IndicatorCache> indicators;
	private Control CONTROLLER;
	private String recomendation;
	
	private class IndicatorCache {
		
		public IndicatorCache(String name){
			this.name = name;
			values_RSI = new CircularFifoQueue<Double>(CACHE_SIZE);
			values_MACD = new CircularFifoQueue<Double>(CACHE_SIZE);
			values_Histogram = new CircularFifoQueue<Double>(CACHE_SIZE);
			values_K = new CircularFifoQueue<Double>(CACHE_SIZE);
			values_D = new CircularFifoQueue<Double>(CACHE_SIZE);
		}
		
		public void addInstance(double RSI, double MACDs, double MACD_histogram, double StochasticK, double StochasticD){
			values_RSI.add(RSI);
			values_MACD.add(MACDs);
			values_Histogram.add(MACD_histogram);
			values_K.add(StochasticK);
			values_D.add(StochasticD);
		}
		
		public final String name;
		
		public CircularFifoQueue<Double> values_RSI;
		public CircularFifoQueue<Double> values_MACD;
		public CircularFifoQueue<Double> values_Histogram;
		public CircularFifoQueue<Double> values_K;
		public CircularFifoQueue<Double> values_D;
	}
	
}
