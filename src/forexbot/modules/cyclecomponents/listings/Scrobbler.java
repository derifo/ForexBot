package forexbot.modules.cyclecomponents.listings;

import forexbot.ForexBot;
import forexbot.core.containers.SymbolListing;

public class Scrobbler {
	/*
	 * Class method Scrobble() downloads new listings from xStation using API reference and then
	 * returns array of listings back to calling class (Controller) for processing
	 */
	
	public Scrobbler(){

	}

	
	public SymbolListing Scrobble(){
		//method retrieves current listing values form API
		SymbolListing out = null;		

		out = ForexBot.api.getSymbolRecord();
		
		return out;
	}
	

}
