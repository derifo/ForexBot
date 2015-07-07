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
			try {
				if(toMake.isEmpty())Thread.sleep(200);
				else Thread.sleep(50);
			} catch (InterruptedException e) {
				if(ForexBot.DEBUG) e.printStackTrace();
			}

		}while(!ForexBot.GLOBAL_EXIT);
		
		if(ForexBot.DEBUG) System.out.println("Transaction module terminated!");
		
	}	
	
	//---------------------------------------------------------------------------------

	private ArrayList<Recommendation> recommendations;
	private ConcurrentLinkedQueue<Transaction> toMake;//queue for pending transaction orders
	private ArrayList<Transaction> active;//list off open deals
	private Balance balance;
	
	private void AssessOpenDeals(){
		/*
		 * Marks transactions that needs to be closed
		 */
		
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
		
		return false;
	}
	
}
