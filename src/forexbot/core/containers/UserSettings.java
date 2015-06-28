package forexbot.core.containers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import forexbot.ForexBot;

public class UserSettings implements Serializable{

	/**
	 * Class container for user defined settings
	 */
	private static final long serialVersionUID = -2847809523558757309L;
	
	public UserSettings(){
		credentials = null;
		SYMBOLS = null;
		api_login = null;
		api_mode = null;
	}
	
	public UserSettings(DBCcredentials credentials, String[] symbols){
		this.credentials = credentials;
		this.SYMBOLS = symbols;		
	}
	
	public UserSettings(DBCcredentials credentials, String[] symbols, String api_login, String api_mode){
		this(credentials, symbols);
		this.api_login = api_login;
		this.api_mode = api_mode;
	}
	
	public DBCcredentials getCredentials(){
		return credentials;
	}	
	public void setCredentials(DBCcredentials credentials){
		this.credentials = credentials;
	}
	
	public String[] getUserSymbols(){
		return SYMBOLS;
	}
	public void setSymbols(String[] SYMBOLS){
		this.SYMBOLS = SYMBOLS;
	}
	
	public String getApiLogin(){
		return api_login;
	}
	public void setApiLogin(String login){
		api_login = login;
	}
	
	public String getApiMode(){
		return api_mode;
	}
	public void setApiMode(String mode){
		api_mode = mode;
	}

	//DB settings
	private DBCcredentials credentials;
	//Trade settings - selected by user
	private String[] SYMBOLS;
	//API settings
	private String api_login;
	private String api_mode;
	
	//--------------------------------------------------------------------
	//methods for both saving and loading settings from file
	public static boolean SaveSettings(UserSettings settings){
		
		try {
			ObjectOutputStream save = new ObjectOutputStream(new FileOutputStream("user_settings.dat"));
			save.writeObject(settings);
			save.close();
		} catch (IOException e) {
			ForexBot.log.addLogERROR("Can't save settings to file!");
			if(ForexBot.DEBUG) e.printStackTrace();
		}
		
		return false;
	}
	
	public static UserSettings LoadSettings(){
		UserSettings settings;
		
		try {
			ObjectInputStream load = new ObjectInputStream(new FileInputStream("user_settings.dat"));
			settings = (UserSettings) load.readObject();
			load.close();
			return settings;
		} catch (IOException e) {
			ForexBot.log.addLogERROR("Can't load settings from file! [user_settings.dat]");
			if(ForexBot.DEBUG) e.printStackTrace();
		} catch (ClassNotFoundException e) {
			ForexBot.log.addLogERROR("Can't load settings!");
			if(ForexBot.DEBUG) e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean LookForSettings(String path){
		boolean exists = new File(path).isFile();
		return exists;
	}
	
}
