package forexbot.modules.cyclecomponents.listings;

import forexbot.ForexBot;
import forexbot.core.containers.SymbolListing;

public class Scrobbler {
	/*
	 * Class method Scrobble() downloads new listings from xStation using API reference and then
	 * returns array of listings back to calling class (Controller) for processing
	 */
	
	public Scrobbler(String[] symbols){
		this.symbols = symbols;
	}

	
	public SymbolListing[] Scrobble(){
		SymbolListing[] out = new SymbolListing[symbols.length];
		
		for(int i = 0; i < symbols.length; i++){
			out[i] = ForexBot.api.getSymbolRecord(symbols[i]);
		}
		
		return out;
	}
	
	
	private String[] symbols;
}
