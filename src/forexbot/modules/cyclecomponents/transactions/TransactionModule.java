package forexbot.modules.cyclecomponents.transactions;

import forexbot.ForexBot;
import forexbot.core.containers.Transaction;

public class TransactionModule implements Runnable{
	
	public TransactionModule(){
		
	}

	
	public void addTransaction(Transaction t){
		
	}
	
	
	@Override
	public void run() {

		do{
			
		}while(!ForexBot.GLOBAL_EXIT);
		
		if(ForexBot.DEBUG) System.out.println("Transaction module terminated!");
		
	}
	
	

}
