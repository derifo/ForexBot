package forexbot.core.containers;

import java.sql.Timestamp;

import forexbot.ForexBot;

public class SymbolListing {
	/*
	 * Container class for single symbol listing
	 * contains essential data for later calculations and 
	 * simplifies process of transferring data between different
	 * methods and modules 
	 */
	
	public SymbolListing(double bid, double ask, double high, double low, String currency, Timestamp date_time){
		this.bid = bid;
		this.ask = ask;
		this.high = high;
		this.low = low;
		this.currency = currency;
		this.date_time = date_time;
	}
	
	@SuppressWarnings("deprecation")
	@Override public String toString(){
		String out;
		out = ForexBot.SYMBOL+" /"+currency+"/["+date_time.toLocaleString()+"]/A "+ask+"/B "+bid+"/L "+low+"/H "+high;
		return out;
	}
	
	public final double bid;
	public final double ask;
	public final double high;
	public final double low;
	public final String currency;
	public final Timestamp date_time;
}
