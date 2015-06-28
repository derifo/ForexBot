package forexbot;

import forexbot.core.CycleController;
import forexbot.core.api.API;
import forexbot.core.dbc.DBC;
import forexbot.core.gui.LoginFrame;
import forexbot.core.gui.OptionFrame;
import forexbot.core.gui.WorkFrame;
import forexbot.core.log.Log;

public class ForexBot {
	public static final String VERSION = "v0.1.00";
	public static final boolean DEBUG = true;//set application mode to debug mode, shows errors reports in console 
	public static boolean GLOBAL_EXIT = false;

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
            	
            	//Start gui
            	login_frame = new LoginFrame();
            	login_frame.setVisible(true);
            	
            	option_frame = new OptionFrame();
            	work_frame = new WorkFrame();
            	
            	
            	//if(api.Login("400826", "ea75aa2d")) System.out.println("Loged in!");
            }
            
		});

	}

	//GUI
	public static LoginFrame login_frame;
	public static OptionFrame option_frame;
	public static WorkFrame work_frame;
	//xAPI
	public static API api;
	//Logger
	public static Log log;
	//DBC
	public static DBC dbc;
	//Main Cycle controller
	public static CycleController CYCLE;
}
