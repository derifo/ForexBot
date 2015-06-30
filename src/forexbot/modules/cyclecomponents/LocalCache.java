package forexbot.modules.cyclecomponents;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import forexbot.ForexBot;
import forexbot.core.containers.SymbolListing;
import forexbot.interfaces.Control;

public class LocalCache {
	
	private final int CACHE_SIZE;
	private final Control CONTROLLER;

	public LocalCache(int cache_size, Control controller){
		CACHE_SIZE =  cache_size;
		CONTROLLER = controller;
		
	}
	
	public synchronized void CreateSymbolCache(String name){
		CONTROLLER.LogEntry("DEBUG", "Cache for - "+name+" created!");
		SymbolCache scache = new SymbolCache(name, CACHE_SIZE);
		cache.put(name, scache);
	}
	
	public synchronized void LoadSymbolListings(String name){
		
	}
	
	public void addListingToCache(String cache_name, SymbolListing listing){
		/*
		 * Adds new item to cache, if cache is full change last_loaded
		 * new items are sent to database
		 */
		
		if(cache.get(cache_name).isFull()) cache.get(cache_name).setLastLoadedIndex((cache.get(cache_name).getLastloadedIndex()-1));
		
		cache.get(cache_name).add(listing);
		
		CONTROLLER.LogEntry("DEBUG", "Sending to upload for: "+cache_name);
			
		String query = "INSERT INTO `"+cache_name+"`("
					+ "`ask`, `bid`, `low`, `high`, `currency`, `symbol`, `date_time`)"
					+ " VALUES ("
					+ listing.ask+","
					+ listing.bid+","
					+ listing.low+","
					+ listing.high+","
					+ "'"+listing.currency+"',"
					+ "'"+listing.symbol_name+"',"
					+ "'"+listing.date_time+"'"
					+ ")";
		
		CONTROLLER.UploaderQueue(query);
	}
	
	public SymbolListing[] getListingsFromCache(String cache_name, int number_of_listings) throws Exception{
		/*
		 * Symbols are fetched from newest (last index) to oldest (last - number_of_listings) 
		 * results are returned in reverse order - [0] index is newest
		 */
		
		SymbolListing[] l = new SymbolListing[number_of_listings];
		int last_index = cache.get(cache_name).getLastIndex();
		
		if(last_index < number_of_listings) throw new Exception("Not enaught listings in cache!");
		
		int p = 0;
		for(int i = last_index; i > (last_index-number_of_listings); i--){
			l[p] = cache.get(cache_name).get(i);
			p++;
		}
		
		return l;		
	}
	
	
	//--------------------------------------------------------------------------------------------------------
	//debug method for printing cache 
	public void DEBUG_PRINT_CACHE(){
		if(ForexBot.DEBUG){
			
			CONTROLLER.LogEntry("DEBUG", "DEBUG CACHE DUMP ---------------------------------");
			
			for(Entry<String, SymbolCache> entry : cache.entrySet()) {
			    @SuppressWarnings("unused")
				String key = entry.getKey();
			    SymbolCache value = entry.getValue();

			    CONTROLLER.LogEntry("DEBUG", value.symbol_name+" / last index: "+value.getLastIndex()+" / last loaded: "+value.getLastloadedIndex());
			}
		}
	}
	
	private HashMap<String, SymbolCache> cache;
	
	//=====================================================================================================================
	
	private class SymbolCache {
		/*
		 * Symbol Cache is a queue based storage class for storing local copy of downloaded listings for symbol
		 * it is more efficient to conduct calculations on local data rather to download it from database every time 
		 * they are needed.
		 * 
		 * Cache stores listings witch are downloaded no more then every two seconds - number is based on current settings.
		 * 
		 * Cache is based on Apache CircularFifoQueue<E>:
		 * 
		 * public class CircularFifoQueue<E>
		 * extends AbstractCollection<E>
		 * implements Queue<E>, BoundedCollection<E>, Serializable
		 * CircularFifoQueue is a first-in first-out queue with a fixed size that replaces its oldest element if full.
		 * The removal order of a CircularFifoQueue is based on the insertion order; elements are removed in the same order in which they were added.
		 * The iteration order is the same as the removal order.
		 * The add(Object), remove(), peek(), poll(), offer(Object) operations all perform in constant time. All other operations perform in linear time or worse.
		 * This queue prevents null objects from being added.
		 * Since: 4.0
		 * Version: $Id: CircularFifoQueue.html 887892 2013-11-24 13:43:45Z tn $
		 */
		private final int CACHE_SIZE;
		
		public SymbolCache(String symbol_name, int cache_size){
			CACHE_SIZE =  cache_size;
			
			this.symbol_name = symbol_name;
			cache = new CircularFifoQueue<SymbolListing>(CACHE_SIZE);
			last_loaded_index = -1;
		}
		
		public int add(SymbolListing listing){
			//ForexBot.log.addLogDEBUG("Listing added to cache - "+ listing.toString());//remove if to much logs
			cache.add(listing);
			
			return getLastIndex();
		}
		
		public int getLastIndex(){
			return cache.size() -1;
		}//size() return number of elements, index of the last element is 1 smaller
		
		public boolean isFull(){
			return cache.isFull();
		}//returns true if queue stores maximum number of elements and oldest are being replaced
		
		public SymbolListing get(int number) throws Exception{
			if(number < CACHE_SIZE) return cache.get(number);
			else throw new Exception("Index " + number + " is out of bounds!");
		}
		
		public void setLastLoadedIndex(int i){
			last_loaded_index = i;
		}
		
		public int getLastloadedIndex(){
			return last_loaded_index;
		}
		
		public final String symbol_name;
		private CircularFifoQueue<SymbolListing> cache;
		private int last_loaded_index;
	}
}
