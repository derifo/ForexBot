package forexbot.modules.cyclecomponents.transactions;

import java.util.ArrayList;

import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.SymbolResponse;
import forexbot.ForexBot;
import forexbot.core.containers.Balance;
import forexbot.core.containers.Recommendation;
import forexbot.core.containers.Transaction;

public class TransactionModule{
	
	private final double LOT = 100000; //1LOT = volume value 1 -> 100k 
	/*
	 * This class represents transaction mechanism which buys or sells forex equities 
	 * depending on recommendations sent by decision module.
	 * 
	 * TRADE_TRANS_INFO
	 * sl/tp - value in pips (must be greater then spread [ask - bid])! 
	 * volume - 1 equals to 100k of given symbol currency (0.1 EURUSD = 10k EUR)
	 * 
	 * BALANCE
	 * account currency (for DEMO) - EUR
	 */
	
	public TransactionModule(){
		active = new ArrayList<Transaction>();
	}

	
	public Balance getBalance(){		
		//Method returns account balance and base currency
		return ForexBot.api.getBalance();
		
	}
	
	public void addRecommendation(Recommendation r){
		//Public method for adding new recommendation
		recommendation = r;
	}
	
	
	public void Process() {
		Transaction t = null;
		balance = getBalance();
		if(balance != null) ForexBot.log.addLogINFO(getBalance().toString());		
		
		
			//get open deals*****************************************************
		if(ComapreActive()){
			
			//asses profits/losses **********************************************
			AssessOpenDeals();
			
			//close deals********************************************************
			CloseUnprofitable();			
			
		}
		//open deals*********************************************************
		t = CreateTransacions();
		if(t != null) OpenDeal(t);

	}	

	private Recommendation recommendation;
	private ArrayList<Transaction> active;//list off open deals
	private Balance balance;
	
	public void CloseAll(){
		
		ArrayList<Transaction> temp = null;
		try {
			temp = ForexBot.api.getOpenTransactions();
			
			for(Transaction t : temp){
				t.setSymbol(ForexBot.SYMBOL);
				Transaction tr = ForexBot.api.CloseTransaction(t);
				ForexBot.log.addLogINFO("Closed : "+tr.toString());
				ForexBot.work_frame.PostLog("Closed : "+tr.toString());
			}
			
		} catch (APICommandConstructionException | APIReplyParseException
				| APICommunicationException | APIErrorResponse e) {

			ForexBot.log.addLogERROR("Transactions closing error - can't close open deals!");
			ForexBot.work_frame.PostLog("Transactions closing error - close open deals manually!");
		}
		
	}
	
	private void AssessOpenDeals(){
		/*
		 * Marks transactions that needs to be closed
		 */
		if(!active.isEmpty()){
			for(int i = 0; i < active.size(); i++){
				
				double profit = active.get(i).getProfit();
				double volume = active.get(i).getVolume() * LOT;
				
				double margin = profit / volume;
				
				if(Math.abs(margin) > 0.01 && active.get(i).getVolume() <= 0.1){
					active.get(i).setToClose(true);
					ForexBot.log.addLogDEBUG("Transaction value change exceeded 1% (vlue < 0.1 LOT) "+ active.get(i).getOrder());
					
				}else if(Math.abs(margin) > 0.005 && active.get(i).getVolume() <= 0.2 ){
					active.get(i).setToClose(true);
					ForexBot.log.addLogDEBUG("Transaction value change exceeded 0,5% (vlue < 0.2 LOT) "+ active.get(i).getOrder());
					
				}else if(Math.abs(margin) > 0.0025 && active.get(i).getVolume() <= 0.5 ){
					active.get(i).setToClose(true);
					ForexBot.log.addLogDEBUG("Transaction value change exceeded 0,25% (vlue < 0.5 LOT) "+ active.get(i).getOrder());
					
				}else {
					if(active.get(i).getPosition().equals("BUY")){
						if(recommendation.getDecision().equals("SELL")){
							active.get(i).setToClose(true);
							ForexBot.log.addLogDEBUG("Trend changed - to close "+ active.get(i).getOrder());
						}
					}else if(active.get(i).getPosition().equals("SELL")){
						if(recommendation.getDecision().equals("BUY")){
							active.get(i).setToClose(true);
							ForexBot.log.addLogDEBUG("Trend changed - to close "+ active.get(i).getOrder());
						}
					}
				}
				
			}
		}
	}
	
	private void CloseUnprofitable(){
		/*
		 * Closes unprofitable and capitalizes profits 
		 */
		for(int i = 0; i < active.size(); i++){
			
			if(active.get(i).isToClose()){
				try {
					
					Transaction t = ForexBot.api.CloseTransaction(active.get(i));//close transaction
					
					ForexBot.log.addLogDEBUG("Transaction closed "+ t.getOrder());
					ForexBot.work_frame.PostLog("Transaction closed "+ t.getOrder());
					active.get(i).setOpen(t.getOpen());
					
				} catch (APICommandConstructionException
						| APIReplyParseException | APICommunicationException
						| APIErrorResponse e) {

					if(ForexBot.DEBUG)e.printStackTrace();
				}
				
			}
			
		}
		
	}
	
	private Transaction CreateTransacions(){
		/*
		 * Create Transaction object based on current recommendation
		 */
		ForexBot.log.addLogDEBUG("Creating transaction...");
		
		balance = getBalance();
		
		if(active.size() >= 2){
			ForexBot.log.addLogINFO("Transaction limit reached!");
			
			return null;
		}
			
		
		Transaction t = new Transaction();
		
		SymbolResponse R = ForexBot.api.getSymbolResponse();
		
		if(recommendation.getDecision().equals("BUY") && R != null){
			
			t.setSymbol(R.getSymbol().getSymbol());
			t.setPrice(R.getSymbol().getAsk());
			
			//t.setSl(-10.0);
			//t.setTp(10.0);
			if(balance.getAmount()/LOT > 0.8) t.setVolume(0.5);
			else if(balance.getAmount()/LOT > 0.4) t.setVolume(0.2);
			else t.setVolume(0.1);
			
			t.setPosition("BUY");
			
			ForexBot.log.addLogDEBUG("Transaction created: "+t.toString());
			
			return t;
			
		}else if(recommendation.getDecision().equals("SELL") && R != null){
			
			t.setSymbol(R.getSymbol().getSymbol());
			t.setPrice(R.getSymbol().getBid());
			
			//t.setSl(-10.0);
			//t.setTp(10.0);
			if(balance.getAmount()/LOT > 0.8) t.setVolume(0.5);
			else if(balance.getAmount()/LOT > 0.4) t.setVolume(0.2);
			else t.setVolume(0.1);
			
			t.setPosition("SELL");
			
			ForexBot.log.addLogDEBUG("Transaction created: "+t.toString());
			
			return t;
			
		}
		
		if(R == null) ForexBot.log.addLogERROR("Transaction creation error!");
				
		return null;
	}
	
	private boolean OpenDeal(Transaction t){
		/*
		 * Opens new deal
		 */
		ForexBot.log.addLogDEBUG("Opening transaction...");
		
		try {
			Transaction temp = ForexBot.api.MakeTransaction(t);
			active.add(temp);
			
			ForexBot.log.addLogINFO("Transaction opened : "+t.toString());
			ForexBot.work_frame.PostLog("Transaction opened : "+t.toString());
			
			return true;
		} catch (APICommandConstructionException | APIReplyParseException
				| APICommunicationException | APIErrorResponse e) {
			// TODO Auto-generated catch block
			if(ForexBot.DEBUG) e.printStackTrace();
		}
		
		return false;
	}
	
	private void ProcessRemoved(Transaction t){
		//add info to log about mismatch 
		ForexBot.log.addLogINFO("Transaction ["+t.getOrder()+"] removed from cache due to mismatch - sl/tp or error.");
		
	}
	
	
	private boolean ComapreActive(){
		ForexBot.log.addLogDEBUG("Checking open deals...");
		if(!active.isEmpty()){
			ArrayList<Transaction> temp = null;
			//get list of open deals
			try {
				temp = ForexBot.api.getOpenTransactions();
				
				//compare with local table
				for(int i = 0; i < active.size(); i++){
					boolean match = false;
					
					for(Transaction tmp : temp){
						if(active.get(i).getOrder() == tmp.getOrder()){
							active.get(i).setProfit(tmp.getProfit());
							active.get(i).setOrder2(tmp.getOrder2());
							match = true;
							ForexBot.log.addLogINFO("Transaction ["+active.get(i).getOrder()+"] profit: "+ active.get(i).getProfit());
							break;
						}//if transaction form local table is in open deals list -> update profit
					}
					
					if(!match){
						ProcessRemoved(active.get(i));
						active.remove(i);
					}//if there is no match -> remove from local cache, process removed
				}
				
				return true;
				
			} catch (APICommandConstructionException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			} catch (APIReplyParseException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			} catch (APICommunicationException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			} catch (APIErrorResponse e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}		
		
		}else{
			ForexBot.log.addLogDEBUG("No open transactions.");
		}
		
		return false;
	}
	
}
