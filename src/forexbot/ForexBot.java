package forexbot;

import javax.swing.JOptionPane;

import forexbot.core.CycleController;
import forexbot.core.api.API;
import forexbot.core.containers.UserSettings;
import forexbot.core.dbc.DBC;
import forexbot.core.dbc.DataUploader;
import forexbot.core.gui.LoginFrame;
import forexbot.core.gui.ReportFrame;
import forexbot.core.gui.WorkFrame;
import forexbot.core.log.Log;
import forexbot.modules.evolver.EvolutionaryAlgorithm;

public class ForexBot {
	public static final String VERSION = "v1.0.0";
	public static final boolean DEBUG = true;//set application mode to debug mode, shows errors reports in console 
	public static final boolean EA_TEST = false;//set EA testing mode
	public static boolean GLOBAL_EXIT = false;//flag for terminating application cycle
	public static final String SYMBOL = "EURUSD";//name of currently analyzed symbol
	
	//settings flags
	public static boolean settings_dbc;//flag for settings processing

	public static void main(String[] args) {
		/*
		 * Starting point of Forex Bot system.
		 */
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	//Start logger
            	log = new Log(5);//for dev purpose highest level - later add option to options window
            	Thread logger = new Thread(log);
            	logger.start();
            	
            	log.addLogINFO("ForexBot version ["+VERSION+"] started.");
            	
            	//Start API
            	api = new API();
            	
            	//Look for saved settings
            	LoadUserSettings();
            	
            	//Start gui
            	login_frame = new LoginFrame();
            	login_frame.LoadFromSettings();
            	login_frame.setVisible(true);
            	
            	work_frame = new WorkFrame();
            	report_frame = new ReportFrame();            	
            	
            	//if(api.Login("400826", "ea75aa2d")) System.out.println("Loged in!");
            }
            
		});

	}
	//UserSettings
	public static UserSettings user_settings;
	//GUI
	public static LoginFrame login_frame;
	public static WorkFrame work_frame;
	public static ReportFrame report_frame;
	//xAPI
	public static API api;
	//Logger
	public static Log log;
	//DBC
	public static DBC dbc;
	public static DataUploader uploader;
	//Main Cycle controller
	public static CycleController CYCLE;
	//Evolutionary tree
	public static EvolutionaryAlgorithm EVOLVER;
	
	
	
	private static void LoadUserSettings(){
		/*
		 * Method loads previously saved user settings from file
		 */
		log.addLogINFO("Initiating settings check...");
		
		if(UserSettings.LookForSettings("user_settings.dat")){//checking for saved settings
			
			log.addLogINFO("Settings found! Loading.");
    		
    		user_settings = UserSettings.LoadSettings();//loading saved settings
    		
    		if(DBC.TestConnection(user_settings.getCredentials())){//testing loaded connection info
    			
    			log.addLogINFO("Database connection settings loaded.");
    			dbc = new DBC();
    			dbc.SetCredentials(user_settings.getCredentials()); 
    			
    			settings_dbc = true;
    			
    		}else{
    			
    			settings_dbc = false;
    			
    			 JOptionPane.showMessageDialog(null,        	  
    		  			    "User settings for database incorrect! Please input settings in options window.",
    		  			    "Error!",
    		  			    JOptionPane.WARNING_MESSAGE);  
    			  
    		}
    		
    	}else{
    		log.addLogWARNING("No settings found! Using default.");
    		
    	    JOptionPane.showMessageDialog(null,        	  
  			    "User settings not found! Please input settings in options window.",
  			    "Error!",
  			    JOptionPane.WARNING_MESSAGE);  
    		
    	    settings_dbc = false;
    		
    		user_settings = new UserSettings();
    		dbc = new DBC();
    	}
	}
	
	public static int restart_count = 0;
	public static void EvolverRestart(int base){
		/*
		 * Method is invoked by evolver thread to restart itself after it stops
		 * (no improvement) 
		 * 
		 * New thread is created and base for simulation increased.
		 */
		
		
		ForexBot.EVOLVER = new EvolutionaryAlgorithm(CYCLE, base);
		Thread evolver_th = new Thread(ForexBot.EVOLVER);
		evolver_th.start();//evolutionary tree start
		
		restart_count++;
		
		log.addLogINFO("AI module has been restarted, starting "+restart_count+" generation [AI]");
		work_frame.PostLog("AI module has been restarted, starting "+restart_count+" generation [AI]");
	}
}
