package forexbot.core.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import forexbot.ForexBot;
import forexbot.core.containers.Balance;
import forexbot.core.containers.SymbolListing;
import forexbot.core.containers.Transaction;
import pro.xstore.api.message.codes.REQUEST_STATUS;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.records.TradeRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.AllSymbolsResponse;
import pro.xstore.api.message.response.LoginResponse;
import pro.xstore.api.message.response.MarginLevelResponse;
import pro.xstore.api.message.response.SymbolResponse;
import pro.xstore.api.message.response.TradeTransactionResponse;
import pro.xstore.api.message.response.TradeTransactionStatusResponse;
import pro.xstore.api.message.response.TradesResponse;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData.ServerEnum;
import pro.xstore.api.sync.SyncAPIConnector;

public class API {
	
	public API(){
	
	}
	
	
	public boolean Login(String user, String password, String mode){
		
		try {
			// Create new connector
			if(mode.equals("DEMO"))		connector = new SyncAPIConnector(ServerEnum.DEMO); 
			else if(mode.equals("REAL")) connector = new SyncAPIConnector(ServerEnum.REAL); 
			
		} catch (IOException e) {
			ForexBot.log.addLogCRITICAL("API connector error!"); 
			if(ForexBot.DEBUG) e.printStackTrace();
		}	

		try {
			// Create new credentials
            // TODO: Insert your credentials
			credentials = new Credentials(user, password);
			 // Create and execute new login command
			loginResponse = APICommandFactory.executeLoginCommand(
			        connector,         // APIConnector
			        credentials        // Credentials
			);
			
			
			
			if(loginResponse.getStatus() == true){
				
				api_login = user;
				ForexBot.log.addLogINFO("xStation user ["+user+"] logged in."); 
				return true;
			}
			
		} catch (APICommandConstructionException | APICommunicationException
				| APIReplyParseException | APIErrorResponse | IOException e) {
			
			ForexBot.log.addLogCRITICAL("API login error!"); 
			if(ForexBot.DEBUG) e.printStackTrace();
			
		}
		
		return false;
	}
	
	public boolean Logout(){
		
		try {
			connector.close();

			return true;
		} catch (APICommunicationException e) {
			ForexBot.log.addLogERROR("API logout error!"); 
			if(ForexBot.DEBUG) e.printStackTrace();
		}
		
		return false;
	}
	
	public AllSymbolsResponse getAvailableSymbols(){
		// Create and execute all symbols command (which gets list of all symbols available for the user)
        try {
			AllSymbolsResponse availableSymbols = APICommandFactory.executeAllSymbolsCommand(connector);

			return availableSymbols;
		} catch (APICommandConstructionException | APIReplyParseException
				| APICommunicationException | APIErrorResponse e) {
			
			ForexBot.log.addLogERROR("API can't load available symbols error!"); 
			if(ForexBot.DEBUG) e.printStackTrace();
			
		}
        
        return null;
	}
	
	public SymbolListing getSymbolRecord(String symbol){
		try {
			SymbolResponse record = APICommandFactory.executeSymbolCommand(connector, symbol);
			SymbolRecord sr = record.getSymbol();
			SymbolListing listing = new SymbolListing(sr.getBid(), sr.getAsk(), sr.getHigh(), sr.getLow(), sr.getCurrency(), sr.getSymbol(), new Timestamp(System.currentTimeMillis()));
			
			
			return listing;
		} catch (APICommandConstructionException | APIReplyParseException
				| APIErrorResponse | APICommunicationException e) {
			
			ForexBot.log.addLogERROR("API can't get symbol listing error! ["+symbol+"]"); 
			if(ForexBot.DEBUG) e.printStackTrace();
			
		}
		
		return null;
	}
	
	//Transaction section
	
	public Balance getBalance(){
		
		MarginLevelResponse marginLevelResponse;
		try {
			marginLevelResponse = APICommandFactory.executeMarginLevelCommand(connector);
			double balance = marginLevelResponse.getBalance();
			String currency = marginLevelResponse.getCurrency();
			return new Balance(balance, currency);
			
		} catch (APICommandConstructionException e) {
			if(ForexBot.DEBUG) e.printStackTrace();
		} catch (APIReplyParseException e) {
			if(ForexBot.DEBUG) e.printStackTrace();
		} catch (APICommunicationException e) {
			if(ForexBot.DEBUG) e.printStackTrace();
		} catch (APIErrorResponse e) {
			if(ForexBot.DEBUG) e.printStackTrace();
		}
		
		ForexBot.log.addLogERROR("API can't get account balance! Error!"); 
		return null;
	}//method returns actual account balance
	
	public Transaction MakeTransaction(Transaction transaction) throws APICommandConstructionException, APIReplyParseException, APICommunicationException, APIErrorResponse{
		/*
		 * Method executes transaction order returning transaction request with order number from API
		 * this number is essential for closing deals
		 */
		
		TradeTransactionResponse response = APICommandFactory.executeTradeTransactionCommand(connector, transaction.generatTransactionInfo("OPEN"));
		
		transaction.setOrder(response.getOrder());
		transaction.setOpen(1);
				
		return transaction;
	}
	
	public int getTransactionStatus(long order) throws APICommandConstructionException, APIReplyParseException, APICommunicationException, APIErrorResponse{
		/*
		 * Method for checking transaction status
		 * 
		 * if transaction was accepted by server site it should return code 3 in case of code 1 transaction is still pending 
		 * any other code means that transaction request failed
		 */
		
		TradeTransactionStatusResponse response = APICommandFactory.executeTradeTransactionStatusCommand(connector, order);
		
		int status = 0;// Error
		
		if(response.getRequestStatus().equals(REQUEST_STATUS.ACCEPTED) ) status = 3;//The transaction has been executed successfully
		else if(response.getRequestStatus().equals(REQUEST_STATUS.PENDING) ) status = 1;//Pending
		else if(response.getRequestStatus().equals(REQUEST_STATUS.REJECTED) ) status = 4;//The transaction has been rejected
		
		return 	status;
	}
	
	public ArrayList<Transaction> getOpenTransactions() throws APICommandConstructionException, APIReplyParseException, APICommunicationException, APIErrorResponse{
		/*
		 * Method returns a list of open deals with theirs numbers
		 * 
		 * Transaction objects from this method does not contain all the data!
		 * only necessary for assessing profit or loses and closing deal  
		 */
		
		TradesResponse tradeResponse = APICommandFactory.executeTradesCommand(connector, true);
		ArrayList<Transaction> open_deals = new ArrayList<Transaction>();
		
		for(TradeRecord t : tradeResponse.getTradeRecords()){
			Transaction tr = new Transaction();
			
			tr.setOrder(t.getOrder2());	 //transaction actual number
			tr.setProfit(t.getProfit());
			
			open_deals.add(tr);
		}
		
		return open_deals;
	}
	
	public Transaction CloseTransaction(Transaction transaction) throws APICommandConstructionException, APIReplyParseException, APICommunicationException, APIErrorResponse{
		/*
		 * Method closes transaction with specific order number
		 * 
		 * !Important not to switch BUY with SELL orders
		 */
		
		TradeTransactionResponse response = APICommandFactory.executeTradeTransactionCommand(connector, transaction.generatTransactionInfo("CLOSE"));
		
		transaction.setOrder2(response.getOrder());
		transaction.setOpen(-1);
				
		return transaction;
	}


	private SyncAPIConnector connector;
	private Credentials credentials;
	private LoginResponse loginResponse;
	
	public static String api_login = null;

	
}
