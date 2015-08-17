package forexbot.core.containers;

import java.io.Serializable;

public class DBCcredentials implements Serializable{
	
	/**
	 * Container for database credentials info
	 */
	private static final long serialVersionUID = 1L;
	
	public DBCcredentials(){
		
	}
	
	public DBCcredentials(String IP, String port, String user, String pass){
		this.IP = IP;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}

	public String getIP(){
		return IP;
	}
	
	public void setIP(String IP){
		this.IP = IP;
	}
	
	public String getPort(){
		return port;
	}
	
	public void setPort(String port){
		this.port = port;
	}
	
	public String getUser(){
		return user;
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public String getPass(){
		return pass;
	}
	
	public void setPass(String pass){
		this.pass = pass;
	}
	
	
	private String IP;
	private String port;
	private String user;
	private String pass;
}
