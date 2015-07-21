package forexbot.core.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import java.awt.Dialog;

import forexbot.ForexBot;
import forexbot.core.CycleController;

public class WorkFrame extends JFrame {
	
	/**
	 * Main application window.
	 */
	private static final long serialVersionUID = 1L;

	public WorkFrame(){
		setBounds(0, 0, 400, 600);
		
		setLayout(null);
		
		setResizable(false);
		
		setTitle("ForexBot "+ForexBot.VERSION+" by £ukasz Wójcik");
		//-------------------------------------------	
		
		status = new JLabel("Connected to xStation");
		status.setBounds(10, 10, 150, 30);
		add(status);
		
		start = new JButton("Start");
		start.setBounds(10, 50, 100, 30);
		start.addActionListener(new StartAction());
		add(start);
		
		stop = new JButton("Stop");
		stop.setBounds(120, 50, 100, 30);
		stop.addActionListener(new StopAction());
		stop.setEnabled(false);
		add(stop);
		
		options = new JButton("Options");
		options.setBounds(230, 10, 100, 30);
		options.addActionListener(new OptionsAction());
		add(options);
		
		report = new JButton("Statistics");
		report.setBounds(230, 50, 100, 30);
		report.addActionListener(new ReportAction());
		report.setEnabled(false);
		add(report);
		
		log = new JTextArea();
		log.setEditable(false);
		
		logcontainer = new JScrollPane(log);
		logcontainer.setBounds(10, 90, 365, 450);
		add(logcontainer);
		
		this.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	ForexBot.GLOBAL_EXIT = true;
	        }

	    });
		
		if(ForexBot.settings_dbc == false) LockButtons(false, true, true, true);
		
		ForexBot.log.addLogDEBUG("Main frame created.");		
		
	}
	
	public void LockButtons(boolean options, boolean start, boolean stop, boolean report){
		if(options) this.options.setEnabled(false);
		if(start) this.start.setEnabled(false);
		if(stop) this.stop.setEnabled(false);
		if(report) this.report.setEnabled(false);
	}
	
	public void UnlockButtons(boolean options, boolean start, boolean stop, boolean report){
		if(options) this.options.setEnabled(true);
		if(start) this.start.setEnabled(true);
		if(stop) this.stop.setEnabled(true);
		if(report) this.report.setEnabled(true);
	}

	
	public void setStatusLabel(String status_text){
		status.setText(status_text);
	}
	
	@SuppressWarnings("deprecation")
	public synchronized void PostLog(String msg){
		ForexBot.log.addLogDEBUG("Main window log posted. ["+msg+"]");

		Timestamp time = new Timestamp(System.currentTimeMillis());
		
		try {
			  log.getDocument().insertString(0, time.toLocaleString() +" -> "+ msg + "\n", null);
		} catch (BadLocationException e) {
			 if(ForexBot.DEBUG) e.printStackTrace();
		}
	}
	
	private JButton start, stop, options, report;
	private JLabel status;
	private JTextArea log;
	private JScrollPane logcontainer;
	
	
	private class StartAction implements ActionListener {
		//After start is pressed 
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ForexBot.log.addLogDEBUG("Start button pressed.");		
			
			if(ForexBot.CYCLE == null){
				ForexBot.CYCLE = new CycleController();
				Thread cycle_thread = new Thread(ForexBot.CYCLE);
				cycle_thread.start();
				
				ForexBot.CYCLE.InitializeCycle();
			}else{
				ForexBot.CYCLE.StartCycle();
			}
			
			LockButtons(true, true, false, false);//lock start and options
			UnlockButtons(false, false, true, true);//unlock stop and reports
		}
		
	}
	
	private class StopAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ForexBot.log.addLogDEBUG("Stop button pressed.");
			
			ForexBot.CYCLE.StopCycle();
			
			LockButtons(false, false, true, false);//lock stop
			UnlockButtons(true, true, false, true);//unlock start and options
		}
		
	}
	
	private class OptionsAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ForexBot.log.addLogDEBUG("Options button pressed.");
			
			JDialog option = new OptionFrame();
			option.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);//locks all other windows 
			option.setVisible(true);
		}
		
	}
	
	private class ReportAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ForexBot.log.addLogDEBUG("Report button pressed.");
			
			ForexBot.report_frame.Refresh();
			ForexBot.report_frame.setVisible(true);
			
		}
		
	}
}
