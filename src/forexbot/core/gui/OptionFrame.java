package forexbot.core.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;

import pro.xstore.api.message.records.SymbolRecord;
import forexbot.ForexBot;
import forexbot.core.containers.AvailableSymbols;
import forexbot.core.containers.DBCcredentials;
import forexbot.core.containers.UserSettings;
import forexbot.core.dbc.DBC;
import forexbot.core.gui.components.Table;
import forexbot.core.gui.components.TableModel;


public class OptionFrame extends JDialog implements Table{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OptionFrame(){
		setBounds(0, 0, 600, 700);
		
		setLayout(null);
		
		setTitle("Options window");
		//-------------------------------------------
		JLabel l1 = new JLabel("Database connection:");
		l1.setBounds(10, 10, 150, 30);
		add(l1);
		
		ip_field = new JTextField();
		ip_field.setText("127.0.0.1");
		ip_field.setBounds(10, 40, 100, 30);
		add(ip_field);
		
		port_field = new JTextField();
		port_field.setText("3306");
		port_field.setBounds(120, 40, 40, 30);
		add(port_field);
		
		JLabel l2 = new JLabel("Database user/pass:");
		l2.setBounds(10, 80, 150, 30);
		add(l2);
		
		user_field = new JTextField();
		user_field.setText("forexbot");
		user_field.setBounds(10, 120, 100, 30);
		add(user_field);
		
		pass_field = new JPasswordField();
		pass_field.setText("forexbot");
		pass_field.setBounds(10, 160, 100, 30);
		add(pass_field);
		
		db_save = new JButton("Save Connection");
		db_save.addActionListener(new db_save_action());
		db_save.setBounds(10, 200, 150, 30);
		add(db_save);
		
		JSeparator s1 = new JSeparator(SwingConstants.VERTICAL);
		s1.setBounds(180, 0, 1, 740);
		add(s1);
		
		//------------------------------------------------------
		PopulateTable();
		
		
		UpdateTableData(data);
		UpdateTable();
		
		symbols_save = new JButton("Save symbols for trade");
		symbols_save.addActionListener(new symbols_save_action());
		symbols_save.setBounds(290, 620, 200, 30);
		add(symbols_save);
		
		LoadData();
		
		symbols_changed = false;
		
		this.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	if(symbols_changed){

	        		//new symbols settings require restart (in this version)
	        		ForexBot.work_frame.LockButtons(false, true, true, false);
	        	}
	        }

	    });
		
		ForexBot.log.addLogINFO("Options frame created.");
	}

	private class db_save_action implements ActionListener {

		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			DBCcredentials credentials = new DBCcredentials();
			credentials.setIP(ip_field.getText());
			credentials.setPort(port_field.getText());
			credentials.setUser(user_field.getText());
			credentials.setPass(pass_field.getText());
			
			if(DBC.TestConnection(credentials)){
				ForexBot.user_settings.setCredentials(credentials);
				UserSettings.SaveSettings(ForexBot.user_settings);
				ForexBot.log.addLogINFO("Connected data saved!"); 
			}
		}
		
	}
	
	private class symbols_save_action implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			ArrayList<String> selected_symbols = new ArrayList<String>();
			ForexBot.log.addLogINFO("Selected symbols:");
			for(int i = 0; i < symbols_table.getRowCount(); i++){
				if((boolean)symbols_table.getValueAt(i, 2) == true){
					String s = (String)symbols_table.getValueAt(i, 0);
					selected_symbols.add(s);
					ForexBot.log.addLogINFO(s);
					symbols_changed = true;
				}
			}
			
			String[] selected = new String[selected_symbols.size()];
			for(int i = 0; i < selected_symbols.size(); i++){
				selected[i] = selected_symbols.get(i);
			}
			
			ForexBot.user_settings.setSymbols(selected);
			UserSettings.SaveSettings(ForexBot.user_settings);
			ForexBot.log.addLogINFO("Symbols data saved! - restart required.");
			ForexBot.work_frame.PostLog("Symbols data saved! - restart required.");
		}
		
	}
	
	private void LoadData(){
		DBCcredentials c = ForexBot.user_settings.getCredentials();
		if(c != null){
			ip_field.setText(c.getIP());
			port_field.setText(c.getPort());
			user_field.setText(c.getUser());
			pass_field.setText(c.getPass());
			
			ForexBot.log.addLogINFO("Database connection data loaded to option pane.");
		}
	}
	
	private JTextField ip_field, port_field, user_field;
	private JPasswordField pass_field;
	private JButton db_save, symbols_save;
	private JTable symbols_table;
	private String[] columns = {"Symbol", "Waluta", "Wybrany"};
	private Object[][] data;
	private JScrollPane table_contener;
	private boolean symbols_changed;

	@Override
	public String[] ColumnNames() {

		return columns;
	}
	@Override
	public Object[][] TableData() {

		return data;
	}
	@Override
	public void UpdateTableData(Object[][] data) {

		this.data = data;
	}
	@Override
	public int EditableRange() {

		return 2;
	}
	@Override
	public void UpdateTable() {
		symbols_table = new JTable(new TableModel(this));
		table_contener = new JScrollPane(symbols_table);
		table_contener.setBounds(190, 10, 390, 600);
		
		TableColumn column = null;
		column = symbols_table.getColumnModel().getColumn(2);
		column.setPreferredWidth(20);
		
		add(table_contener);
		
	}
	
	private void PopulateTable(){
		AvailableSymbols list = new AvailableSymbols();
		int n = list.getSymbolsAvailable().length;
		Object[][] names = new Object[n][3];
		int i = 0;
		for(SymbolRecord r : list.getSymbolsAvailable()){
			names[i] = new Object[]{r.getSymbol(), r.getCurrency(), CheckIfSelected(r.getSymbol())};
			i++;
		}
		
		data = names;
		
	}
	
	private Boolean CheckIfSelected(String name){
		if(ForexBot.user_settings.getUserSymbols() != null){
			for(String s : ForexBot.user_settings.getUserSymbols()){
				if(s.equals(name)){
					ForexBot.log.addLogDEBUG("Loaded - "+s+" to symbol table.");
					return new Boolean(true);
				}
			}
		}
		
		return new Boolean(false);
	}
}
