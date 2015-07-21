package forexbot.core.gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import forexbot.ForexBot;
import forexbot.modules.cyclecomponents.transactions.TransactionModule;

public class ReportFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2295473594589082727L;

	public ReportFrame(){
		setBounds(0, 0, 400, 400);
		
		setLayout(null);
		
		setResizable(false);
		
		JLabel l1 = new JLabel("Activity report:");
		l1.setBounds(50, 30, 150, 30);
		add(l1);
		
		JLabel l2 = new JLabel("[AI]");
		l2.setBounds(50, 60, 100, 30);
		add(l2);
		
		JLabel l3 = new JLabel("Generation: ");
		l3.setBounds(60, 90, 100, 30);
		add(l3);
		
		ai_gen = new JTextField();
		ai_gen.setEditable(false);
		ai_gen.setBounds(160, 90, 150, 30);
		add(ai_gen);
		
		JLabel l4 = new JLabel("[TRANSACTIONS]");
		l4.setBounds(50, 150, 100, 30);
		add(l4);
		
		JLabel l5 = new JLabel("Balance: ");
		l5.setBounds(60, 180, 100, 30);
		add(l5);
		
		balance = new JTextField();
		balance.setEditable(false);
		balance.setBounds(160, 180, 150, 30);
		add(balance);
		
		JLabel l6 = new JLabel("Profit:");
		l6.setBounds(60, 210, 100, 30);
		add(l6);
		
		profit = new JTextField();
		profit.setEditable(false);
		profit.setBounds(160, 210, 150, 30);
		add(profit);
		
		JLabel l7 = new JLabel("Transactions:");
		l7.setBounds(60, 240, 100, 30);
		add(l7);
		
		transactions = new JTextField();
		transactions.setEditable(false);
		transactions.setBounds(160, 240, 150, 30);
		add(transactions);
		
	}
	
	public void Refresh(){
		String bal = ""+ TransactionModule.current_balance;
		balance.setText(bal);
		
		String pro = ""+ (TransactionModule.current_balance - TransactionModule.initial_balance);
		profit.setText(pro);
		
		String tra = ""+ TransactionModule.transaction_count;
		transactions.setText(tra);
		
		String ai = ""+ForexBot.restart_count;
		ai_gen.setText(ai);
	}

	
	
	private JTextField profit, ai_gen, transactions, balance;
}
