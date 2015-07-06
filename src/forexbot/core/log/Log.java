package forexbot.core.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import forexbot.ForexBot;

public class Log implements Runnable{
	/*
	 * This is logging class with simple purpose of gathering activity data from other methods and threads
	 * in order to enable future insight to internal processes and calculations all data (according to detail 
	 * level specified by user) will be stored and saved in txt file (possible to redirect copy to database) 
	 */
	
	private static String DIR = "logs";

	public Log(int LOG_LEVEL){
		this.LOG_LEVEL = LOG_LEVEL;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		Date date = new Date();
		
		File theDir = new File(DIR);
		if (!theDir.exists()) {
		   if(ForexBot.DEBUG)  System.out.println("creating directory: " + DIR);
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		    	if(ForexBot.DEBUG) System.out.println("DIR creation failed!");  
		    }        
		    if(result) {    
		    	if(ForexBot.DEBUG) System.out.println("DIR created");  
		    }
		}
		
		DIR =  DIR + "\\";
		
		FILE_NAME = DIR + dateFormat.format(date) + "_log.txt";
		log = new ConcurrentLinkedQueue<LogInput>();
		
		if(ForexBot.DEBUG){
			FILE_NAME_DEBUG = DIR + dateFormat.format(date) + "_log_DEBUG.txt";
			debug_log = new ConcurrentLinkedQueue<LogInput>();
		}else{
			FILE_NAME_DEBUG = null;
			debug_log = null;
		}
		
	}
	
	public void addLogCRITICAL(String msg){
		LogInput i = new LogInput("## CRITICAL ERROR ##", msg);
		log.add(i);
		addLogDEBUG(msg);
	}
	
	public void addLogERROR(String msg){
		LogInput i = new LogInput("# ERROR #", msg);
		log.add(i);
		addLogDEBUG(msg);
	}
	
	public void addLogWARNING(String msg){
		LogInput i = new LogInput("! WARNING !", msg);
		if(LOG_LEVEL >= 3) log.add(i);
		addLogDEBUG(msg);
	}
	
	public void addLogINFO(String msg){
		LogInput i = new LogInput("INFO", msg);
		if(LOG_LEVEL >= 4) log.add(i);
		addLogDEBUG(msg);
	}
	
	public void addLogDEBUG(String msg){
		LogInput i = null;
		if(ForexBot.DEBUG){
			i = new LogInput("DEBUG", msg);
			debug_log.add(i);
			
			System.out.println(msg);
		}
		
	}

	@Override
	public void run() {
		if(ForexBot.DEBUG) System.out.println("Logger thread - started.");
		
		//normal log
		File fout = new File(FILE_NAME);
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			fos = new FileOutputStream(fout);
			 bw = new BufferedWriter(new OutputStreamWriter(fos));
		} catch (FileNotFoundException e) {
			if(ForexBot.DEBUG) e.printStackTrace();
		}
		//debug log
		BufferedWriter Dbw = null;
		if(ForexBot.DEBUG){
			File Dfout = new File(FILE_NAME_DEBUG);
			FileOutputStream Dfos = null;
			try {
				Dfos = new FileOutputStream(Dfout);
				 Dbw = new BufferedWriter(new OutputStreamWriter(Dfos));
			} catch (FileNotFoundException e) {
				if(ForexBot.DEBUG) e.printStackTrace();
			}
		}
		
		if(bw != null){
					
			do{
				//normal log
				if(!log.isEmpty()){
					try {
						bw.write(log.poll().toString());
						bw.newLine();
					} catch (IOException e) {
						if(ForexBot.DEBUG) e.printStackTrace();
					}
				
				}
				//debug log
				if(!debug_log.isEmpty() && Dbw != null){
					try {
						Dbw.write(debug_log.poll().toString());
						Dbw.newLine();
					} catch (IOException e) {
						if(ForexBot.DEBUG) e.printStackTrace();
					}
				}else{
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						if(ForexBot.DEBUG) e.printStackTrace();
					}
				}
				
			}while(!ForexBot.GLOBAL_EXIT);
			
			
			try {
				bw.close();
				Dbw.close();
				if(ForexBot.DEBUG)	System.out.println("Log thread terminated - global exit flag!");
			} catch (IOException e) {
				if(ForexBot.DEBUG) e.printStackTrace();
			}
		}else{
			if(ForexBot.DEBUG)	System.out.println("Log file error!");
		}
		
	}
	
	private ConcurrentLinkedQueue<LogInput> log;
	private ConcurrentLinkedQueue<LogInput> debug_log;
	private final String FILE_NAME;
	private final String FILE_NAME_DEBUG;
	private final int LOG_LEVEL;
	
	private class LogInput {
		
		public LogInput(String level, String message){
			this.level = level;
			msg = message;
			time = new Timestamp(System.currentTimeMillis());			
		}
		
		@SuppressWarnings("unused")
		public LogInput(String level, String message, Timestamp time){
			this.level = level;
			msg = message;
			this.time = time;
		}
		
		@Override public String toString(){
			return "["+ level +"] "+ time.toString()+" \t "+ msg;
		}
		
		public final String msg;
		public final String level;
		public final Timestamp time;
	}

	
}
