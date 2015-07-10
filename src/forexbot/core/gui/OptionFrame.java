package forexbot.core.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import forexbot.ForexBot;
import forexbot.core.containers.DBCcredentials;
import forexbot.core.containers.UserSettings;
import forexbot.core.dbc.DBC;


public class OptionFrame extends JDialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OptionFrame(){
		setBounds(0, 0, 300, 300);
		
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
				ForexBot.work_frame.PostLog("In order for changes to take effect - restart application.");
				ForexBot.work_frame.LockButtons(false, true, true, true);
			}
		}
		
	}
	
	
	private JTextField ip_field, port_field, user_field;
	private JPasswordField pass_field;
	private JButton db_save;

	
}
