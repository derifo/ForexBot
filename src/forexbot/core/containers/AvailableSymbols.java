package forexbot.core.containers;

import java.util.ArrayList;

import forexbot.ForexBot;
import pro.xstore.api.message.records.SymbolRecord;
import pro.xstore.api.message.response.AllSymbolsResponse;

public class AvailableSymbols {
	/*
	 * Container class for listing all symbols available for trading (essential for other operations)
	 * converts API response to String array of symbol names (example: EURUSD)
	 */
	
	public AvailableSymbols(){
		
		AllSymbolsResponse availableSymbols = ForexBot.api.getAvailableSymbols();
		ArrayList<SymbolRecord> list = new ArrayList<SymbolRecord>();
		
		// List all available symbols on console
        for(SymbolRecord symbol : availableSymbols.getSymbolRecords()) {
            list.add(symbol);
        }
		
        symbols = new SymbolRecord[list.size()];
        symbols_names = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
        	symbols[i] = list.get(i);
        	symbols_names[i] = list.get(i).getSymbol();
        }
	}
	
	public AvailableSymbols(AvailableSymbols old){
		/*
		 * This constructor is meant for sandbox 
		 */
		
		symbols = old.getSymbolsAvailable();
		symbols_names = old.getSymbolsAvailableNames();
	}
	
	public SymbolRecord[] getSymbolsAvailable(){
		return symbols;
	}
	
	public String[] getSymbolsAvailableNames(){
		return symbols_names;
	}
	
	private final SymbolRecord[] symbols;
	private final String[] symbols_names;
}
