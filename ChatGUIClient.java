import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Client to connect to the server, can send and receive messages
 */
public class ChatGUIClient {
	
	/*GUI for the Client*/
	private UserGUI gui;
	
	/*socket of the server the client is connected to*/
	private Socket server;
	
	/*thread for writing to the server*/
	private Thread writeThread;
	
	/*thread for reading from the server*/
	private Thread readThread;
	
	/*boolean guard for when the threads should stop i.e when disconnected from the server*/
	private boolean connected = false;
	
	/**
	 * Default constructor, Client
	 * @param address : ip address of the server
	 * @param address : port of the server
	 */
	public ChatGUIClient() {
		gui = new UserGUI("Client");
	}
	
	/**
	 * method to be run when first constructed, sets up the required readers and writers and then calls the reading and writing threads if connection is made
	 */
	public void go() {
		
		while(!gui.getStart()) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		
		try {
			server = new Socket(gui.getIP(),gui.getPort());
			connected = true;
		} catch (java.net.ConnectException e) {
			gui.addText("Could Not Connect to Server");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(connected) {
			try {
				PrintWriter serverOut = new PrintWriter(server.getOutputStream(), true);
				BufferedReader serverIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
				
				clientRead(serverIn);
				clientWrite(serverOut);
				
			} catch (IOException e) {
				e.printStackTrace();
				lostConnection();
			}
		}
	}
	
	/**
	 * method that is call when connection is lost or needs to be lost from the server, 
	 * sets the thread guard to false and closes the server socket connection
	 */
	private void lostConnection(){
		System.out.println("Lost connection to server");
		gui.addText("Lost connection to server");
		try {
			server.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		connected = false;
	}
	
	/**
	 * thread that reads data from the server using the given bufferedreader and prints it to the console when it is received
	 * 
	 * @param serverIn : a BufferedReader receiving data from the server
	 */
	private void clientRead(BufferedReader serverIn) {
		readThread = new Thread() {
			public void run() {
				try {
					while(connected) {
						String serverRes = serverIn.readLine();
						/*if the client reads null it means that the server connection is lost so the client should be disconnected*/
						if(serverRes == null) {
							throw new java.net.SocketException("Lost Connection to Server");
						}
						System.out.println(serverRes);
						gui.addText(serverRes);
					}
				} catch (java.net.SocketException e) {
					lostConnection();
				} catch (IOException e) {
					lostConnection();
					e.printStackTrace();
				}
			}
		};
		readThread.start();
	}
	
	/**
	 * Reads data from the console and prints it to the server when the user enters in the console
	 * 
	 * @param serverOut : the PrintWriter sending data to the server
	 * @param userIn : a BufferedReader receiving data from the console
	 */
	private void clientWrite(PrintWriter serverOut) {
		writeThread = new Thread() {
			public void run() {
				while(connected) {
					String message = gui.getMessage();
					if(message != null) {
						serverOut.println(message);
					}
				}
			}
		};
		writeThread.start();		
	}
	
	/**
	 * Method to be first run when the class is run.
	 */
	public static void main(String[] args) {
		new ChatGUIClient().go();
	}
}
