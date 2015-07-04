package forexbot.modules.cyclecomponents.transactions;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import forexbot.interfaces.Control;
import forexbot.core.containers.Transaction;


public class DecisionModule {
	
	private static final int CACHE_SIZE = 20;
	
	public DecisionModule(Control controller){
		CONTROLLER = controller;
		indicators = new IndicatorCache[5];
		
		indicators[0] = new IndicatorCache("RSI");
		indicators[1] = new IndicatorCache("MACD");
		indicators[2] = new IndicatorCache("Histogram");
		indicators[3] = new IndicatorCache("%K");
		indicators[4] = new IndicatorCache("%D");
	}
	
	public void LoadIndicators(double RSI, double MACDs, double MACD_histogram, double StochasticK, double StochasticD){
		indicators[0].values.add(RSI);
		indicators[1].values.add(MACDs);
		indicators[2].values.add(MACD_histogram);
		indicators[3].values.add(StochasticK);
		indicators[4].values.add(StochasticD);
	}
	
	public void CalculatePredictions(){
		
	}
	
	public void MakeDecision(){
		
	}
	
	public Transaction Transaction(){
		
		
		return null;
	}

	private IndicatorCache[] indicators;
	private Control CONTROLLER;
	
	private class IndicatorCache {
		
		public IndicatorCache(String name){
			this.name = name;
			values = new CircularFifoQueue<Double>(CACHE_SIZE);
		}
		
		public final String name;
		public CircularFifoQueue<Double> values;
	}
	
}
