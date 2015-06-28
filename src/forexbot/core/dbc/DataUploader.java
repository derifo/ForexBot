package forexbot.core.dbc;

import java.util.concurrent.ConcurrentLinkedQueue;

import forexbot.ForexBot;

public class DataUploader implements Runnable{
	/*
	 * Purpose of this class is to store and upload to database data send by other threads,
	 * as uploading data may be time consuming task other threads send query statements ready to be executed
	 * witch are stored in thread safe linked queue for processing
	 */
	
	public DataUploader(){
		upload_queue = new ConcurrentLinkedQueue<String>();
	}
	
	public void addUploadQuery(String q){
		upload_queue.add(q);
	}
	
	private boolean ExecuteUpload(){
		
		if(!upload_queue.isEmpty()){
			String query = upload_queue.peek();//copy first query
			if(ForexBot.dbc.QUERY(query)) {//attempts to execute 
				upload_queue.poll();//if success removes from queue
				return true;
			}
		}
		
		return false;//false is queue is empty
	}

	@Override
	public void run() {
		ForexBot.log.addLogDEBUG("Uploader thread crated.");
		int sleep;
		do{
			
			if(!ExecuteUpload()){
				sleep = 2000;//operational cycle has 2s interval, so if empty next data will show in 2s
			}else{
				sleep = 100;//0.1s wait to not lock dbc connection
				ForexBot.log.addLogDEBUG("Data uploaded!");
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				if(ForexBot.DEBUG) e.printStackTrace();
			}
			
		
			if(ForexBot.GLOBAL_EXIT && upload_queue.isEmpty()){
				ForexBot.log.addLogDEBUG("Uploader terminated - global_exit flag!");
				break;
			}
		}while(true);
		
	}

	private ConcurrentLinkedQueue<String> upload_queue;
}
