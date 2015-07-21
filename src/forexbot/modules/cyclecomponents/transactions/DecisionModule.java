package forexbot.modules.cyclecomponents.transactions;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import forexbot.ForexBot;
import forexbot.interfaces.Control;
import forexbot.core.containers.Recommendation;
import forexbot.core.containers.SymbolListing;

public class DecisionModule {
	
	private final int CACHE_SIZE = 20;
	private final int ASSESSMENT_PERIOD = 10;
	
	public DecisionModule(Control controller){
		CONTROLLER = controller;		
	}
	
	public void CreateIndicatorCache(){
		/*
		 * Create cache for all user symbols
		 */
		indicators = new IndicatorCache();
	}
	
	public void addIndicators(double StochasticK, double StochasticD){
		/*
		 * Load calculated values to respective symbol cache for further processing
		 */		
		indicators.addInstance(StochasticK, StochasticD);		
	}
	
	public Recommendation MakeDecision(String mode){
		/*
		 * Each recommendation should return value between 1 and -1 where
		 * 	1  - BUY
		 * 	-1 - SELL
		 * 
		 * There are four recommendations so value above  
		 */
		Recommendation R = new Recommendation();
		
		double r_stochastic, r_trend;

		r_stochastic = recomendation_STOCHASTIC();
		r_trend = TrendAssessment();
		
		double rr = r_stochastic + r_trend;
		
		if(mode.equals("WORK")){
			 if(rr >= 1){
			
				R.setDecision("BUY");
				R.setCertainty(50);
				if(rr >= 1.5){
					R.setCertainty(90);
				}
			}
			else if(rr <= -1){
				R.setDecision("SELL");
				R.setCertainty(50);
				if(rr <= -1.5){
					R.setCertainty(90);
				}
			}
			else{
				R.setDecision("KEEP");
				if(rr <= 0.5 && rr >= -0.5) R.setCertainty(90);
				else R.setCertainty(50);
			}
			 
			CONTROLLER.LogEntry("DEBUG", " Stochastic " + r_stochastic + " Trend " + r_trend + " || DECISION : "+ R.getDecision());
			 
		}else if(mode.equals("SANDBOX")){
			if(r_stochastic > 0)R.setDecision("BUY");
			else if (r_stochastic < 0) R.setDecision("SELL");
			else R.setDecision("KEEP");
		}		
		
		
		return R;
	}
	
	private double TrendAssessment(){
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
			SymbolListing[] listings = CONTROLLER.getCache().getListingsFromCache(ASSESSMENT_PERIOD);
			
			//for(int i = 0; i < ASSESSMENT_PERIOD; i++ ){
				//
			//}
			
			partial = listings[0].bid - listings[ASSESSMENT_PERIOD-1].bid; 
			if(partial > 0) return 0.8;
			else if(partial < 0) return -0.8;
			
		} catch (Exception e) {
			CONTROLLER.LogEntry("DEBUG", "Not enough listings in cache for assessment ["+ForexBot.SYMBOL+"] !");
		}
		
		
		return 0.0;
	}
	
	
	private double recomendation_STOCHASTIC(){
		if(indicators.values_K.size() >= 2){
		
			double k[], d[];
			
			k = indicators.getK(2);
			d = indicators.getD(2);
			
			double power_p, power_n;
			
			if(k[0] >= 90) return -1;
			if(k[0] >= 80 ) power_n = -1; //above 80 - bigger changes
			else power_n = -0.8;
			
			if(k[0] <= 10) return 1;
			if(k[0] <=20) power_p = 1; //below 20 - bigger changes
			else power_p = 0.8;
			
			if(k[1] > d[1]){
				
				if(k[0] < d[0]){
					return power_n; // K line crosses D line form above - price will fall
				}
				
			}else if(k[1] < d[1]){
				
				if(k[0] > d[0]){
					return power_p; // K line crosses D line from below - price will rise
				}
				
			}
		
		}else{
			CONTROLLER.LogEntry("DEBUG", "Not enough indicators in cache for STOCHASTIC!");
		}
		
		return 0.0;		
	}

	private IndicatorCache indicators;
	private Control CONTROLLER;
	
	private class IndicatorCache {
		
		public IndicatorCache(){
			
			values_K = new CircularFifoQueue<Double>(CACHE_SIZE);
			values_D = new CircularFifoQueue<Double>(CACHE_SIZE);
		}
		
		public void addInstance(double StochasticK, double StochasticD){
			
			values_K.add(StochasticK);
			values_D.add(StochasticD);
		}
		
		public double[] getK(int x){
			double array[] = new double[x];
			
			int p  = 0;
			int last = (values_K.size()-1);
			for(int i = last; i > (last - x) ; i-- ){
				array[p] = values_K.get(i);
				p++;
			}
			
			return array;
		}
		
		public double[] getD(int x){
			double array[] = new double[x];
			
			int p  = 0;
			int last = (values_D.size()-1);
			for(int i = last; i > (last - x) ; i-- ){
				array[p] = values_D.get(i);
				p++;
			}
			
			return array;
		}		
		
		public CircularFifoQueue<Double> values_K;
		public CircularFifoQueue<Double> values_D;
	}
	
}
