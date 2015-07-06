package forexbot.modules.cyclecomponents.transactions;

import forexbot.ForexBot;
import forexbot.core.containers.Transaction;

public class TransactionModule implements Runnable{
	
	public TransactionModule(){
		
	}

	
	public Balance getBalance(){
		
		return null;
	}
	
	public void addTransaction(Transaction t){
		
	}
	
	
	@Override
	public void run() {

		ForexBot.log.addLogINFO("Transaction module started!");
		
		ForexBot.log.addLogINFO("Curent balance ["+ForexBot.api.getBalance()+"]");
		
		do{
			
			
		}while(!ForexBot.GLOBAL_EXIT);
		
		if(ForexBot.DEBUG) System.out.println("Transaction module terminated!");
		
	}
	
	
	
	//---------------------------------------------------------------------------------

	public class Balance {
		
		public Balance(){
			
		}		
		
		public Balance(double amount, String currency) {
			this.amount = amount;
			this.currency = currency;
		}


		public double getAmount() {
			return amount;
		}
		public void setAmount(double amount) {
			this.amount = amount;
		}
		public String getCurrency() {
			return currency;
		}
		public void setCurrency(String currency) {
			this.currency = currency;
		}
		
		private double amount;
		private String currency;
	}


}
