package forexbot.modules.cyclecomponents;

import java.sql.Timestamp;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import forexbot.ForexBot;
import forexbot.core.containers.SymbolListing;
import forexbot.core.dbc.DBC;
import forexbot.interfaces.Control;

public class LocalCache {
	
	private final int CACHE_SIZE;
	private final Control CONTROLLER;

	public LocalCache(int cache_size, Control controller){
		CACHE_SIZE =  cache_size;
		CONTROLLER = controller;
	}
	
	public synchronized void CreateSymbolCache(){
		
		cache = new SymbolCache(ForexBot.SYMBOL, CACHE_SIZE);

		CONTROLLER.LogEntry("DEBUG", "Symbol cache created!");
		
	}
	
	
	
	public void addListingToCache(SymbolListing listing){
		/*
		 * Adds new item to cache, if cache is full change last_loaded
		 * new items are sent to database
		 */
		
		if(cache.isFull()) cache.setLastLoadedIndex((cache.getLastloadedIndex()-1));
		
			cache.add(listing);
			
			CONTROLLER.LogEntry("DEBUG", "Sending to upload for: "+ForexBot.SYMBOL);
				
			String query = "INSERT INTO `"+ForexBot.SYMBOL+"`("
						+ "`ask`, `bid`, `low`, `high`, `currency`, `symbol`, `date_time`)"
						+ " VALUES ("
						+ listing.ask+","
						+ listing.bid+","
						+ listing.low+","
						+ listing.high+","
						+ "'"+listing.currency+"',"
						+ "'"+ForexBot.SYMBOL+"',"
						+ "'"+listing.date_time+"'"
						+ ")";
			
			CONTROLLER.UploaderQueue(query);
	}
	
	public SymbolListing[] getListingsFromCache(int number_of_listings) throws Exception{
		/*
		 * Symbols are fetched from newest (last index) to oldest (last - number_of_listings) 
		 * results are returned in reverse order - [0] index is newest
		 */
		
		SymbolListing[] l = new SymbolListing[number_of_listings];
		int last_index = cache.getLastIndex();
		
		if(last_index < number_of_listings) throw new Exception("Not enaught listings in cache!");
		
		int p = 0;
		for(int i = last_index; i > (last_index-number_of_listings); i--){
			l[p] = cache.get(i);
			p++;
		}
		
		return l;		
	}
	
	
	//--------------------------------------------------------------------------------------------------------
	//debug method for printing cache 
	public void DEBUG_PRINT_CACHE(){
		if(ForexBot.DEBUG){
			CONTROLLER.LogEntry("DEBUG", "===================================================");
			CONTROLLER.LogEntry("DEBUG", "-----------------DEBUG CACHE DUMP ----------------");
			
			

			CONTROLLER.LogEntry("DEBUG", cache.symbol_name+" / last index: "+cache.getLastIndex()+" / last loaded: "+cache.getLastloadedIndex());
			
			
			CONTROLLER.LogEntry("DEBUG", "===================================================");
		}
	}
	
	private SymbolCache cache;
	
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
		
		public int getMaxIndex(){
			return cache.maxSize()-1;
		}//maxSize() returns maximum number of elements that can be stored
		
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
	
	//-------------------------------------------------------------
	
	public static synchronized SymbolListing[] LoadSymbolListings(int listings_to_load){
		/*
		 * Downloading records for processing from database
		 * 
		 * if there is not enough download all
		 * 
		 * Symbol listings are returned in table in order form oldest to newest
		 */
		SymbolListing[] result = null;
		
				long total_records = 0;
				String[] s = {"total"};
				
			
				Object[][] r = DBC.ResultSetToObject(s , ForexBot.dbc.SELECT("SELECT COUNT(ID) AS total FROM "+ForexBot.SYMBOL));
					
				if(r != null){
					total_records = (long) r[0][0];
				}
				
				//if there are symbol records commencing download
				if(total_records > 0){
					int records_to_download;
					if(total_records > listings_to_load){						
						records_to_download = listings_to_load;		
					}else{
						records_to_download = (int) total_records;				
					}
					
					result = new SymbolListing[records_to_download];
					
					String query = "SELECT * FROM `"+ForexBot.SYMBOL+"` WHERE id > ((SELECT COUNT(id) FROM `"+ForexBot.SYMBOL+"`) - "+records_to_download+") ORDER BY id ASC";
							
					s = new String[]{"id", "ask", "bid", "low", "high", "currency", "date_time"};
					r = DBC.ResultSetToObject(s , ForexBot.dbc.SELECT(query));
								
					if(r != null){
						int pom = 0;
						for(Object[] row : r){
								double a,b,l,h;
								String cu;
								Timestamp dt;
								
								a = Double.parseDouble(row[1].toString());
								b = Double.parseDouble(row[2].toString());
								l = Double.parseDouble(row[3].toString());
								h = Double.parseDouble(row[4].toString());
								cu = row[5].toString();
								Object o = row[7];
								dt = (Timestamp) o;
								
								SymbolListing listing = new SymbolListing(b, a, h, l, cu, dt);
							
								result[pom] = listing;
								pom++;
						}
					}
							
				}	
				
			return result;
	}
	
}
