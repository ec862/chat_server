import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList; 
import java.util.Queue;
import java.util.concurrent.TimeUnit; 

/**
 * Server for clients to send to and receive messages from
 */
public class ChatGUIServer {
	
	/*GUI for the Server*/
	private UserGUI gui;

	/*boolean guard for when the threads should stop i.e when server shuts down*/
	public boolean online = true;
	
	/*The ServerSocket that clients can connect to*/
	private ServerSocket in;
	
	/*thread for connecting new clients*/
	private Thread clientThread;
	
	/*threads for receiving data from clients*/
	private Thread recieveThread;
	
	/*thread for writing to the clients*/
	private Thread sendThread;
	
	/*thread for checking if the server should shut down*/
	private Thread shutdownThread;
	
	/*Queue of connected Clients*/
	private Queue<Socket> socketQueue= new LinkedList<>();
	
	/*Queue of messages to be sent to all clients*/
	private Queue<String> messageQueue= new LinkedList<>();
	
	/**
	 * Default constructor, Server
	 * @param address : port of the server
	 */
	public ChatGUIServer() {
		gui = new UserGUI("Server");
	}
	
	/**
	 * method to be run when first constructed, calls the connection thread, output thread and shutdown checking thread.
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
			in = new ServerSocket(gui.getPort());
			in.setReuseAddress(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clientConnection();
		processOutput();
		shutdownCheck();
	}
	
	/**
	 * thread that connects a new client to the server
	 * whenever a client is connected, a new thread is made for receiving messages from that client
	 */
	private void clientConnection(){
		clientThread = new Thread() {
			public void run() {
				try {	
					while(online) {
						System.out.println("Server listening");
						gui.addText("Server listening");
						Socket s = in.accept();
						System.out.println("Server accepted connection on " + in.getLocalPort() + " ; " + s.getPort());
						gui.addText("Server accepted connection on " + in.getLocalPort() + " ; " + s.getPort());
						synchronized(socketQueue){
							socketQueue.add(s);
						}
						recieveInput(s);
					}		
				} catch (java.net.SocketException e) {
					
				}  catch (IOException e) {
					e.printStackTrace();
				} 
				finally {
					if(in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		clientThread.start();
	}
	
	/**
	 * thread that receives messages from a client and adds the message to the message queue
	 */
	private void recieveInput(Socket s) {
		recieveThread = new Thread() {
			public void run() {
				BufferedReader clientIn =  null;
				try {
					InputStreamReader r = new InputStreamReader(s.getInputStream());
					clientIn = new BufferedReader(r);
					String userInput;
					/*receiving null from a client means they have disconnected*/
					while((userInput = clientIn.readLine())!=null && online) {
						synchronized(messageQueue){
							messageQueue.add(userInput);
						}
						gui.addText("Message sent - " + userInput);
					}
				} catch (java.net.SocketException e) {
					disconnectUser(s);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						clientIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} 
			}
		};
		recieveThread.start();		
	}
	
	/**
	 * thread that checks if the message queue has a message in it and then sends that message to all clients in the socket queue
	 */
	private void processOutput() {
		sendThread = new Thread() {
			public void run() {
				while(online) {
					String message = null;
					synchronized(messageQueue){
						message = messageQueue.peek();
					}
					if(message != null) {
						/*iterate each socket and send message*/
						synchronized(socketQueue){
							int socketNo = socketQueue.size();
							for(int i = socketNo; i>0; i--) {
								Socket s = socketQueue.peek();
								try {
									PrintWriter clientOut = new PrintWriter(s.getOutputStream(), true);
									clientOut.println(message);
								/*if the socket cannot be reached then the client has disconnected*/
								} catch (java.net.SocketException e) {
									disconnectUser(s);
									continue; /* continues so that the socket is not added back to the end of the queue*/
								}catch (IOException e) {
									e.printStackTrace();
									System.out.println("Unable to send message to Client at \'" + s.getLocalPort() + " ; " + s.getPort() + "\'");
									gui.addText("Unable to send message to Client at \'" + s.getLocalPort() + " ; " + s.getPort() + "\'");
								}
								socketQueue.remove();
								socketQueue.add(s);
							}
						}
						synchronized(messageQueue){
							messageQueue.remove(message);
						}
					}
				}
			}	
		};
		sendThread.start();		
	}
	
	/**
	 * disconnects a user by removing their socket from the socket queue and closing it
	 */
	private void disconnectUser(Socket s){
		System.out.println("Client at \'" + s.getLocalPort() + " ; " + s.getPort() + "\' disconnected");
		gui.addText("Client at \'" + s.getLocalPort() + " ; " + s.getPort() + "\' disconnected");
		synchronized(socketQueue){
			socketQueue.remove(s);
		}
		try {
			s.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * thread that checks if "EXIT" has been entered into the console and then shuts down if it has. 
	 * Server shuts down by closing the server socket and removing all connected sockets from the socket queue and closing them
	 */
	private void shutdownCheck() {
		shutdownThread = new Thread() {
			public void run() {
				try {	
					while(online) {
						String message = gui.getMessage();
						if(message != null && message.equals("EXIT")) {
							online = false;
							if(in != null) {
								try {
									in.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							synchronized(socketQueue){
								int socketNo = socketQueue.size();
								for(int i = socketNo; i>0; i--) {
									Socket s = socketQueue.remove();
									s.close();
								}
							}
							System.out.println("Server Shut down");
							gui.addText("Server Shut down");
							
						}
					}
				} catch(IOException e){
					e.printStackTrace();
				}
			}
		};
		shutdownThread.start();
	}
	
	/**
	 * Method to be first run when the class is run
	 */
	public static void main(String[] args) {
		new ChatGUIServer().go();
		
	}
}
