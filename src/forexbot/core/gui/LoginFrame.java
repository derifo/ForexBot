package forexbot.core.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import forexbot.ForexBot;
import forexbot.core.containers.UserSettings;

public class LoginFrame extends JFrame {

	/**
	 * Login window 
	 */
	private static final long serialVersionUID = 1L;

	public LoginFrame(){
		setBounds(0, 0, 300, 300);
		
		setLayout(null);
		
		setTitle("Login window");
		//-------------------------------------------
		JLabel l1 = new JLabel("Select type:");
		l1.setBounds(10, 10, 100, 30);
		add(l1);
		
		type = new JComboBox<String>();
			type.addItem("DEMO");
			type.addItem("REAL");
		type.setEditable(false);
		type.setBounds(120, 10, 100, 30);
		add(type);
		
		JLabel l2 = new JLabel("xStation login:");
		l2.setBounds(10, 50, 100, 30);
		add(l2);
		
		user = new JTextField();
		user.setBounds(120, 50, 100, 30);
		add(user);
		
		JLabel l3 = new JLabel("Password:");
		l3.setBounds(10, 90, 100, 30);
		add(l3);
		
		pass = new JPasswordField();
		pass.setBounds(120, 90, 100, 30);
		add(pass);
		
		login = new JButton("Login");
		login.addActionListener(new LoginAction(this));
		login.setBounds(100, 150, 100, 30);
		add(login);
		
		
	}

	private JComboBox<String> type;
	private JTextField user;
	private JPasswordField pass;
	private JButton login;
	
	public void LoadFromSettings(){
		if(ForexBot.user_settings.getApiMode() != null) type.setSelectedItem(ForexBot.user_settings.getApiMode());
		if(ForexBot.user_settings.getApiLogin() != null) user.setText(ForexBot.user_settings.getApiLogin());
	}
	
	private class LoginAction implements ActionListener {
		
		/*
		 * Login action invokes login method from API and upon successful connection with xStation 
		 * brings up WorkFrame for further interaction with user
		 */
		
		public LoginAction(JFrame d){
			this.d = d;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String l,p;
			l = user.getText();
			p = pass.getText();
			
			if(!l.equals("") && !p.equals("")){
				
					if(ForexBot.api.Login(l, p, type.getSelectedItem().toString())){
						
						d.setVisible(false);
						
						ForexBot.user_settings.setApiLogin(l);
						ForexBot.user_settings.setApiMode(type.getSelectedItem().toString());
						UserSettings.SaveSettings(ForexBot.user_settings);
						
						ForexBot.work_frame.setVisible(true);
						
					}else{
						JOptionPane.showMessageDialog(null,        	  
		          			    "Login or Password incorrect!",
		          			    "Error!",
		          			    JOptionPane.WARNING_MESSAGE);  
					}					
				
			}else{
				
				JOptionPane.showMessageDialog(null,        	  
          			    "Login or Password field empty!",
          			    "Error!",
          			    JOptionPane.WARNING_MESSAGE);     
			}

		}
		
		private JFrame d;
	}
}
