package forexbot.modules.evolver.sandbox;

import java.util.ArrayList;

import forexbot.core.containers.Recommendation;
import forexbot.core.containers.SymbolListing;
import forexbot.modules.evolver.containers.Genom;

public class Evaluator {
	/*
	 * Purpose of this class is to evaluate member of any given generation 
	 * based on produced recommendations
	 * 
	 * Score is based on amount of profit or loss generated by evaluated member 
	 */
	
	public Evaluator(Genom genom){
		INITIAL_BALANCE = 100000.0;
		BALANCE = 100000.0;
		recommendations = new ArrayList<Recommendation>();
		listings = new ArrayList<SymbolListing>();
		this.GENOM = genom;
	}

	public void addRecommendation(Recommendation r, SymbolListing current){
		recommendations.add(r);
		listings.add(current);
	}
	
	public int Evaluate(){
		/*
		 * Evaluation is based on profit made
		 * score has no boundaries and can be positive or negative (gain or loss)
		 */
		//System.out.println("Genom "+GENOM.getID()+ " recommendations: "+recommendations.size());
		for(int i = 0; i < recommendations.size(); i++){
			
			if(current_deal != null){
				//there is open deal
				if(!recommendations.get(i).getDecision().equals("KEEP")){
					//recommendation other ten KEEP
					
					if(recommendations.get(i).getDecision().equals("BUY")){
						//current recommendation BUY
						if(current_deal.getType().equals("SELL")){
							
							//new recommendation different then open deal
							double profit = current_deal.Close(listings.get(i).bid, listings.get(i).ask);
							BALANCE = BALANCE + profit;
							current_deal = new Deal("BUY", listings.get(i).bid, listings.get(i).ask);
						}
						
					}else{
						//current recommendation SELL
						if(current_deal.getType().equals("BUY")){
							
							//new recommendation different then open deal
							double profit = current_deal.Close(listings.get(i).bid, listings.get(i).ask);
							BALANCE = BALANCE + profit;
							current_deal = new Deal("SELL", listings.get(i).bid, listings.get(i).ask);
						}
						
					}
					
				}
				
			}else{
				//no open deals
				if(recommendations.get(i).getDecision().equals("BUY")){
					//new BUY order
					current_deal = new Deal("BUY", listings.get(i).bid, listings.get(i).ask);
					
				}else if(recommendations.get(i).getDecision().equals("SELL")){
					//new SELL order
					current_deal = new Deal("SELL", listings.get(i).bid, listings.get(i).ask);
					
				}
				
			}
			
		}
		
		double dif = (BALANCE - INITIAL_BALANCE);
		System.out.println("Genom "+GENOM.getID()+ " profit: "+(BALANCE - INITIAL_BALANCE));
		int score = (int) dif;
		
		return score;
	}
	
	private Genom GENOM;
	private final double INITIAL_BALANCE;
	private double BALANCE;
	private ArrayList<Recommendation> recommendations;
	private ArrayList<SymbolListing> listings;
	
	private Deal current_deal = null;
	
	public Genom getGenom(){
		return GENOM;
	}
	
	private class Deal {
		
		private final double VOLUME = 50000.0;// 0.5 LOT
		
		public Deal(String type, double bid, double ask){
			this.type = type;
			this.ask = ask;
			this.bid = bid;
		}
		
		
		public double Close(double bid, double ask){
			//returns difference between opening price and closing price
			if(type.equals("BUY")){
				double profit  = (VOLUME*this.ask) - (VOLUME*bid);
				//System.out.println("Genom "+GENOM.getID()+ " close deal BUY "+profit);
				return profit;
			}else {
				double profit  = (VOLUME*this.bid) - (VOLUME*ask);
				//System.out.println("Genom "+GENOM.getID()+ " close deal SELL "+profit);
				return profit;
			}
		}
		
		public String getType(){
			return type;
		}
		
		private String type;
		private double bid, ask;
	}
}
