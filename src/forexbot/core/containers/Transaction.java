package forexbot.core.containers;

import java.sql.Timestamp;

import pro.xstore.api.message.codes.TRADE_OPERATION_CODE;
import pro.xstore.api.message.codes.TRADE_TRANSACTION_TYPE;
import pro.xstore.api.message.records.TradeTransInfoRecord;

public class Transaction {
	/*
	 * Container class for transaction data and creation of transaction requests for API
	 * class contains fields for all properties regarding deal but not all are required every time
	 */
	
	public Transaction(){
		date = new Timestamp(System.currentTimeMillis());
	}

	public Transaction(double price, double sl, double tp, String symbol,
			double volume, String position) {
		
		this();

		this.position = position;
		this.price = price;
		this.sl = sl;
		this.tp = tp;
		this.symbol = symbol;
		this.volume = volume;
	}
	
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getSl() {
		return sl;
	}

	public void setSl(double sl) {
		this.sl = sl;
	}

	public double getTp() {
		return tp;
	}

	public void setTp(double tp) {
		this.tp = tp;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}
	
	public long getOrder2() {
		return order2;
	}

	public void setOrder2(long order2) {
		this.order2 = order2;
	}

	public String getCustomComment() {
		return customComment;
	}

	public void setCustomComment(String customComment) {
		this.customComment = customComment;
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	public TradeTransInfoRecord getTransaction_info() {
		return transaction_info;
	}

	public void setTransaction_info(TradeTransInfoRecord transaction_info) {
		this.transaction_info = transaction_info;
	}


	private double price;
	private double sl = 0.0;
	private double tp = 0.0;
	private String symbol;
	private double volume;
	private long order = 0;//number of opening deal
	private long order2 = 0;//number of closing deal
	private String customComment = null;
	private long expiration = 0;
	
	private double profit = 0.0;
	private int open = 0;// 0 - not confirmed, 1 - open, -1 - closed
	private String position;
	private Timestamp date;
	private int request_status;
	
	private boolean toClose;
	
	public boolean isToClose() {
		return toClose;
	}

	public void setToClose(boolean toClose) {
		this.toClose = toClose;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getRequest_status() {
		return request_status;
	}

	public void setRequest_status(int request_status) {
		this.request_status = request_status;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}


	private TradeTransInfoRecord transaction_info;
	
	
	public TradeTransInfoRecord generatTransactionInfo(String code){
		/*
		 * Method prepares API object from container class fields in order to make new transaction
		 * 
		 * In case of OPEN request fields like symbol and volume are essential also price
		 * In case of CLOSE request field order is required as it identifies transaction
		 * 
		 *  Other fields:
		 *  sl - stop loss
		 *  tp - take profit
		 *  customComent - user comment for transaction
		 *  expiration - it is possible to set transaction to expire in time
		 */
		
		if(customComment == null || customComment.equals("")) customComment = "bot";
		
		if(code.equals("OPEN") && position.equals("BUY")){
			
			transaction_info = new TradeTransInfoRecord(TRADE_OPERATION_CODE.BUY, TRADE_TRANSACTION_TYPE.OPEN, price, sl, tp, symbol, volume, order, customComment, expiration);
			
		}else if(code.equals("CLOSE") && position.equals("BUY")){
			
			transaction_info = new TradeTransInfoRecord(TRADE_OPERATION_CODE.BUY, TRADE_TRANSACTION_TYPE.CLOSE, price, sl, tp, symbol, volume, order, customComment, expiration);
			
		}
		
		else if(code.equals("OPEN") && position.equals("SELL")){
			
			transaction_info = new TradeTransInfoRecord(TRADE_OPERATION_CODE.SELL, TRADE_TRANSACTION_TYPE.OPEN, price, sl, tp, symbol, volume, order, customComment, expiration);
			
		}else if(code.equals("CLOSE") && position.equals("SELL")){
			
			transaction_info = new TradeTransInfoRecord(TRADE_OPERATION_CODE.SELL, TRADE_TRANSACTION_TYPE.CLOSE, price, sl, tp, symbol, volume, order, customComment, expiration);
			
		}
		
		return transaction_info;
	}
}
