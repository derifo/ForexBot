package forexbot.modules.cyclecomponents.transactions;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import forexbot.ForexBot;
import forexbot.core.containers.Balance;
import forexbot.core.containers.Recommendation;
import forexbot.core.containers.Transaction;

public class TransactionModule implements Runnable{
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
		toMake = new ConcurrentLinkedQueue<Transaction>();
		active = new ArrayList<Transaction>();
		recommendations = new ArrayList<Recommendation>();
	}

	
	public Balance getBalance(){		
		//Method returns account balance and base currency
		return ForexBot.api.getBalance();
		
	}
	
	public void addRecommendation(Recommendation r){
		//Public method for adding new transaction orders to queue by DecisionModule
		recommendations.add(r);
	}
	
	
	@Override
	public void run() {

		ForexBot.log.addLogINFO("Transaction module started!");
		
		balance = getBalance();
		if(balance != null) ForexBot.log.addLogINFO(getBalance().toString());		
		
		do{
		//Main loop for processing transactions
			long start_time = System.currentTimeMillis();
			
			try {
				Thread.sleep(1);				
			} catch (InterruptedException e) {
				if(ForexBot.DEBUG) e.printStackTrace();
			}
			
			//get open deals*****************************************************
			if(ComapreActive()){
			
			//asses profits/losses **********************************************
				AssessOpenDeals();
			//close deals********************************************************
				CloseUnprofitable();
			//open deals*********************************************************
				CreateTransacions();
				OpenDeal();
			}
			
			TikClock(start_time, 1999);

		}while(!ForexBot.GLOBAL_EXIT);
		
		if(ForexBot.DEBUG) System.out.println("Transaction module terminated!");
		
	}	
	
	//---------------------------------------------------------------------------------
	private long TikClock(long start_time, long sleep){
		long time_elapsed = System.currentTimeMillis() - start_time;
		
		ForexBot.log.addLogDEBUG("cycle - time elapsed ["+time_elapsed+"]");
		if(time_elapsed < sleep) {
			try {
			    Thread.sleep(sleep - time_elapsed);                 //waits for remaining time
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
		
		return time_elapsed;
	}//method for regulating cycle time

	private ArrayList<Recommendation> recommendations;
	private ConcurrentLinkedQueue<Transaction> toMake;//queue for pending transaction orders
	private ArrayList<Transaction> active;//list off open deals
	private Balance balance;
	
	private void AssessOpenDeals(){
		/*
		 * Marks transactions that needs to be closed
		 */
		if(!active.isEmpty()){
			for(Transaction t : active){
				
				
				
			}
		}
	}
	
	private void CloseUnprofitable(){
		/*
		 * Closes unprofitable and capitalizes profits 
		 */
		
	}
	
	private void CreateTransacions(){
		/*
		 * Creates Transaction objects based on current recommendations
		 */
		balance = getBalance();
		
	}
	
	private void OpenDeal(){
		/*
		 * Opens new deals based on pending queue
		 */
		
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
