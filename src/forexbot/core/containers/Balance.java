package forexbot.core.containers;

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
	
	@Override
	public String toString(){
		return "Account balance ["+amount+"] currency: "+currency;
	}
}

