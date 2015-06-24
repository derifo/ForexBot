package forexbot.core.api;

import java.io.IOException;
import java.sql.Timestamp;

import forexbot.core.containers.SymbolListing;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.AllSymbolsResponse;
import pro.xstore.api.message.response.LoginResponse;
import pro.xstore.api.message.response.SymbolResponse;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData.ServerEnum;
import pro.xstore.api.sync.SyncAPIConnector;

public class API {
	
	public API(){
		try {
			// Create new connector
			connector = new SyncAPIConnector(ServerEnum.DEMO);
			
		} catch (IOException e) {
			
		}	
	}
	
	public boolean Login(String user, String password){

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
				
				return true;
			}
			
		} catch (APICommandConstructionException | APICommunicationException
				| APIReplyParseException | APIErrorResponse | IOException e) {
			
		}
		
		return false;
	}
	
	public boolean Logout(){
		
		try {
			connector.close();

			return true;
		} catch (APICommunicationException e) {
			
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
			
		}
		
		return null;
	}

	
	private SyncAPIConnector connector;
	private Credentials credentials;
	private LoginResponse loginResponse;
	
	public static String api_login = null;
}
