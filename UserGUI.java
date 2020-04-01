import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList; 
import java.util.Queue; 
import javax.swing.*;

/**
 * GUI for both server and client, must specify which when called
 */
public class UserGUI implements ActionListener {
	
	/*frame of the GUI*/
	private JFrame frame;
	
	/*textbox to input messages/commands*/
	private JTextField txtMessage;
	
	/*button that sends the message*/
	private JButton butSend;
	
	/*button that starts the server/client*/
	private JButton butStart;
	
	/*textarea that shows all messages*/
	private JTextArea txtChat;
	
	/*textarea that titles the txtChat*/
	private JTextArea txtTitle;
	
	/*textarea that titles the Port textbox*/
	private JTextArea txtPortTitle;
	
	/*textarea that titles the IP address textbox*/
	private JTextArea txtIPTitle;
	
	/*textbox to input the Port*/
	private JTextField txtPort;
	
	/*textbox to input the IP address*/
	private JTextField txtIP;
	
	/*whether the GUI is being used for a server or client*/
	private String GUIType = "";
	
	/*Port inputed by the user*/
	private int givenPort = 14002;
	
	/*IP address inputed by the user*/
	private String givenIP = "localhost";
	
	/*whether the port and ip have been inputed and the user has requested to start*/
	private boolean start = false;
	
	/*Queue of messages inputed by the user*/
	private Queue<String> messageQueue= new LinkedList<>();
	
	/**
	 * constructor, UserGUI. Sets up the type of GUI and then calls the draw of the setup frame
	 * @param type : whether the user is using a server or a client program
	 */
	public UserGUI(String type){
		GUIType = type;
		drawSetup();
	}
	
	/**
	 * draws the setup frame where the user can input the port of the server or the  port/IP of the client connection
	 * 
	 */
	private void drawSetup(){
		frame = new JFrame();
		frame.setTitle(GUIType);
		frame.setSize(300,300);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		
		if(GUIType == "Client") {
			txtIPTitle = new JTextArea(" Server IP:",1,1);
			txtIPTitle.setEditable(false);
			frame.add(txtIPTitle);
			
			txtIP = new JTextField(18);
			frame.add(txtIP);
		}
		
		txtPortTitle = new JTextArea("Port:",1,1);
		txtPortTitle.setEditable(false);
		frame.add(txtPortTitle);
		
		txtPort = new JTextField(18);
		frame.add(txtPort);
		
		butStart = new JButton("Start");
		butStart.addActionListener(this);
		frame.add(butStart);
		
		frame.setVisible(true);
	}
	
	/**
	 * draws main GUI interface to show all messages and be able to send messages for the client/commands for the server
	 * 
	 */
	private void drawMain(){
		frame = new JFrame();
		frame.setTitle(GUIType);
		frame.setSize(300,600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		
		txtMessage = new JTextField(16);
		frame.add(txtMessage);
		
		butSend = new JButton("Send");
		butSend.addActionListener(this);
		frame.add(butSend);
		
		txtTitle = new JTextArea("Messages:",1,1);
		txtTitle.setEditable(false);
		frame.add(txtTitle);
		
		txtChat = new JTextArea(1,20);
		txtChat.setEditable(false);
		frame.add(txtChat);
		
		frame.setVisible(true);
	}
	
	/**
	 * method for whenever a button is pressed
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		/*empties the message textbox and adds it to the message queue when the send button is pressed */
		if(src == butSend) {
			synchronized(messageQueue){
				String message = txtMessage.getText();
				messageQueue.add(message);
			}
			txtMessage.setText("");
		} 
		/* sets the inputed port and ip address and calls the draw of the main frame when the start button is pressed */
		else if(src == butStart) {
			if(!txtPort.getText().equals("")) {
				givenPort = Integer.valueOf(txtPort.getText());
			}
			if(GUIType.equals("Client") && !txtIP.getText().equals("")) {
				givenIP = txtIP.getText();
			}
			
			frame.setVisible(false);
			start = true;
			drawMain();
		}

	}
	
	/**
	 * adds a message and a newline to the message textarea 
	 * 
	 */
	public void addText(String message) {
		txtChat.setText(txtChat.getText() + message + "\n");
	}
	
	/**
	 * gets a message from the message queue 
	 * @return a message as a string if there is a message in the queue, otherwise returns null
	 */
	public String getMessage() {
		synchronized(messageQueue){
			if(messageQueue.peek() != null) {
				return messageQueue.remove();
			} else {
				return null;
			}
		}	
	}
	
	/**
	 * @return start : whether the user has requested to start
	 */
	public boolean getStart() {
		return start;
	}
	
	/**
	 * @return givenPort : Port given by the user
	 */
	public int getPort() {
		return givenPort;
	}
	
	/**
	 * @return givenIP : IP Address given by the user
	 */
	public String getIP() {
		return givenIP;
	}
	
}
