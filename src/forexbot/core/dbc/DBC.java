package forexbot.core.dbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import forexbot.ForexBot;
import forexbot.core.containers.DBCcredentials;

public class DBC {

	public DBC(){
		
	}
	
	public void SetCredentials(DBCcredentials c){
		this.credentials = c;
	}//accepts object container with db IP, login and password 
	
	public static boolean TestConnection(DBCcredentials credentials_test){
		String url = "jdbc:mysql://"+credentials_test.getIP()+":"+credentials_test.getPort()+"/";

        String driver = "com.mysql.jdbc.Driver";        
        
        try {

            Class.forName(driver).newInstance();

            Connection con = DriverManager.getConnection(url+"forexbot",credentials_test.getUser(),credentials_test.getPass());
            con.close();
            ForexBot.log.addLogDEBUG("Credentials test - OK!"); 
            
            return true;

          } catch (Exception e) {
        	  ForexBot.log.addLogDEBUG("Credentials test - FAILED! Wrong connectiona data or no database!"); 
        	 if(ForexBot.DEBUG) e.printStackTrace();
          }
		
		return false;
	}//method used for testing credentials and connection
	
	public boolean Conect(){
		/////////////////////////////////   database connection  //////////////////////
		String url = "jdbc:mysql://"+credentials.getIP()+":"+credentials.getPort()+"/";
        String driver = "com.mysql.jdbc.Driver";        
        
        try {

            Class.forName(driver).newInstance();

            databaseConnection = DriverManager.getConnection(url+"forexbot",credentials.getUser(),credentials.getPass());

            ForexBot.log.addLogINFO("Connected to the database "+credentials.getIP()); 
            
            return true;

          } catch (Exception e) {
        	  ForexBot.log.addLogCRITICAL("Database connection error! Can't connect to: "+credentials.getIP()+ ":"+ credentials.getPort());
        	    JOptionPane.showMessageDialog(null,        	  
      			    "Error connecting to database: "+credentials.getIP()+ ":"+ credentials.getPort(),
      			    "Error!",
      			    JOptionPane.WARNING_MESSAGE);  
        	  
        	  if(ForexBot.DEBUG) e.printStackTrace();
          }
		
		return false;
	}//this method establishes connection with database
	
	public synchronized ResultSet SELECT(String query){
		
		java.sql.Statement st = null;
		ResultSet result = null;
		
		try {
			st = databaseConnection.createStatement();
			result = st.executeQuery(query);
		} catch (SQLException e) {
			ForexBot.log.addLogDEBUG("Cannot exectute query: ["+query+"]");
			if(ForexBot.DEBUG) e.printStackTrace();
		}
				
		return result;
	}//this method executes SELECT queries and returns data as ResultSet to invoking class for processing, keyword synchronized keeps it threads safe
	
	public static Object[][] ResultSetToObject(String field_list[], ResultSet result){
		ForexBot.log.addLogDEBUG("Parsing ResultSet...");
		
		if(result != null){
			ArrayList<Object[]> data = new ArrayList<Object[]>();
			
			try{
				while(result.next()){
					Object[] row = new Object[field_list.length];
					for(int i = 0; i < field_list.length; i++){
						row[i] = result.getObject(field_list[i]);
					}
					data.add(row);
				}
				
				Object end_result[][] = new Object[data.size()][];
				for(int i = 0; i < data.size(); i++) end_result[i] = data.get(i);
				
				ForexBot.log.addLogDEBUG("["+data.size()+"] rows returned.");
				
				return end_result;		
			}catch (SQLException e) {
				ForexBot.log.addLogDEBUG("ResultSet parsing error!");
				if(ForexBot.DEBUG) e.printStackTrace();
			}
			
		}
		
		return null;
	}//Universal method for parsing ResultSets to Object arrays.
	
	public synchronized boolean QUERY(String query){
		
		java.sql.Statement st = null;
			
		try {
			st = databaseConnection.createStatement();
			st.executeUpdate(query);
			
			return true;
		} catch (SQLException e) {
			ForexBot.log.addLogDEBUG("Cannot exectute query: ["+query+"]");
			if(ForexBot.DEBUG) e.printStackTrace();
			
			return false;
		}			
	}//this method executes queries such as UPDATE, INSERT, CREATE and DELETE, returns true/false depending on outcome, keyword synchronized keeps it threads safe 
	
	private void TRANSACTION(){
		try {
			databaseConnection.setAutoCommit(false);
			ForexBot.log.addLogDEBUG("Opening transaction!");
		} catch (SQLException e) {
			ForexBot.log.addLogDEBUG("Opening transaction error!");
			if(ForexBot.DEBUG) e.printStackTrace();
		}
	}//method for opening new transaction on database
	
	private boolean COMMIT(boolean rollback){
		if(!rollback){
			try {
				databaseConnection.commit();
				try {
					databaseConnection.setAutoCommit(true);
					ForexBot.log.addLogDEBUG("Commit successful!");
					return true;
				} catch (SQLException e) {
					ForexBot.log.addLogDEBUG("Commit failure!");
					if(ForexBot.DEBUG) e.printStackTrace();
				}
			} catch (SQLException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
				
				try {
					databaseConnection.rollback();
					ForexBot.log.addLogDEBUG("Commit unsuccessful! Comencing rollback!");
				} catch (SQLException e) {
					ForexBot.log.addLogDEBUG("Rolback failure!");
					if(ForexBot.DEBUG) e.printStackTrace();
				}
			}
		}else{
			try {
				databaseConnection.rollback();
				ForexBot.log.addLogDEBUG("Querry unsuccessful! Comencing rollback!");
				databaseConnection.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				ForexBot.log.addLogDEBUG("Rolback failure!");
				if(ForexBot.DEBUG) e.printStackTrace();
			}
		}
		
		return false;
	}//method for closing and committing transaction on database or executing rollback in case of failure 
	
	public synchronized boolean TRANSACTION_QUERY(String[] queries){
		
		TRANSACTION();
		
		for(String q : queries){
			if(!QUERY(q)){
				COMMIT(true);
				ForexBot.log.addLogDEBUG("Transaction failure at: "+q);
				return false;
			}
		}
		
		if(COMMIT(false)) return true;
		
		return false;
	}//this method executes queries in series within a transaction for data integrity, synchronized for threads safety
	
	public boolean CrateSymbolTable(String symbol){
		
		String query_listing_table = "CREATE TABLE IF NOT EXISTS `"+symbol+"` ("
				+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
				+ " `ask` double NOT NULL,"
				+ " `bid` double NOT NULL,"
				+ " `low` double NOT NULL,"
				+ " `high` double NOT NULL,"
				+ " `currency` varchar(10) NOT NULL,"
				+ " `symbol` varchar(10) NOT NULL,"
				+ " `date_time` timestamp NOT NULL,"
				+ " PRIMARY KEY (`id`)	)"
				+ " ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;";
		
		if(QUERY(query_listing_table)){
			ForexBot.log.addLogDEBUG("Table created or already existed ["+symbol+"]");
			return true;
		}
		
		ForexBot.log.addLogCRITICAL("Table creation failed! ["+symbol+"]");
		return false;
	}//method invoked in order to create table structure for symbols defined by user
	
	private DBCcredentials credentials;
	private Connection databaseConnection = null;
}
