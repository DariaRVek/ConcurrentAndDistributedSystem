import java.net.*;
import java.io.*;
import java.util.*;

public class Node {

    private Random ra;
    private Socket s;
    private PrintWriter pout = null;
    private ServerSocket n_ss;
    private Socket n_token;
    private String c_host = "127.0.0.1";
    private int  c_request_port = 7000;
    private int c_return_port = 7001;
    private String n_host = "127.0.0.1";
    private String n_host_name;
    private int n_port;
    private Date timestmp;
    private String timestamp;
	private int reconnAttempts = 0;
	private static final int MAX_RECONN_ATTEMPTS = 5;	
	private FileWriter file_writer;
	private PrintWriter print_writer;
	//private String shutdown_host = "127.0.0.1";
	//private int shutdown_port = 7002;
	//private Socket closingDownRequestSocket; //closing down request would be communicated via this Socket
    //private ServerSocket closeDownSignal_ss;
	
	/**
	 * Constructor method.
	 * @param nam the Node's host name.
	 * @param por the port number a Node is running on.
	 * @param sec upper bound (exclusive) of wait time prior to sending request.
	 * @throws InterruptedException
	 */
    public Node(String nam, int por, int sec) throws InterruptedException {	
		ra = new Random();
		n_host_name = nam;
		n_port = por;
    	System.out.println("Node " +n_host_name+ ":" +n_port+ " of DME is active ....");
    	
    	//>>> NODE sends n_host and n_port  through a socket s to the coordinator
    	//>>> c_host:c_req_port and immediately opens a server socket through which will receive 
    	//>>> a TOKEN (actually just a synchronization).
    
    	try {
			n_ss = new ServerSocket(n_port);//token received through this ServerSocket
		} catch (IOException e) {
			System.out.println("Node "+n_host_name+ ":" +n_port+" couldn't open socket: " + e);
		}//end try catch
    	
    	/*
    	 * The ServerSocket through which a Node would receive a signal to close down.
    	 */
    	/*try {
    		closeDownSignal_ss = new ServerSocket(n_port);
    	} catch (IOException e) {
			System.out.println("Node "+n_host_name+ ":" +n_port+" couldn't open socket: " + e);
		}  */
    	
    	while(true){
    
    		// >>>  sleep a random number of seconds linked to the initialisation sec value
    		sleep(ra.nextInt(sec));
    		
    		try {
    			openFile(); 			

    			s = new Socket(c_host, c_request_port); //send request through socket s
    			sendRequestDetails();
	    		s.close();
	    		reconnAttempts = 0;//connection established now, so reset reconnection attempts
	    		
	    		getToken();
	    		
	    		if(n_token.isConnected()) {
	    			System.out.println(n_host_name + ":" + n_port + " is connected ok.");
	    			executeCriticalSection(sec);
	    		} //endif

	    		returnToken();
	    		
	    		if(n_token.isClosed()) System.out.println(n_host_name + ":" + n_port + " has returned token successfully. Socket now closed.");
	    		else System.out.println(n_host_name + ":" + n_port + " still has the token! Socket still open!");
	    		
    		}
    		catch(ConnectException ce){
    			reconnAttempts++;
    			System.out.println("ERROR: " + ce);
    			System.out.println("COORDINATOR DOWN. ATTEMPTING TO RECONNECT...");
			    sleep(2000); //wait before trying to re-establish connection
			    if(reconnAttempts >= MAX_RECONN_ATTEMPTS) {
		    		System.out.println("ATTEMPTED RECONNECTION 5 TIMES. TAKING OVER COORDINATOR ROLE NOW.");	
		    		startNewCoordinator();
					break;
			    }//endif
    		} 
    		catch (java.io.IOException e) {
    			System.out.println("ERROR: " + e);
			    sleep(5000); //wait before trying to re-establish connection
			    //System.exit(1);	
    		}//end try catch
    	}//end while
    }//end constructor method
    
    /**
     * Method to send token back to coordinator.
     * on completion of critical section.
     */
    private void returnToken() {
		// **** Return the token
	    // Print suitable messages - also considering communication failures
    	try {
			timestmp = new Date();
			timestamp = timestmp.toString();
			writeToFile("**NODE " + n_host_name + ":" + n_port + " RETURNED TOKEN " + timestamp + "**");
			closeFile();
			n_token = new Socket(c_host, c_return_port);
			System.out.println("Node " + n_host_name + ":" + n_port + " returning token to coordinator.");
			n_token.close();
    	} catch (IOException e) {
    		System.out.println("NODE " + n_host_name + ":" + n_port + " COULDN'T RETURN TOKEN." + e);
    		sleep(5000);
    	}//end try catch
    }//end method returnToken
    
    /**
     * Method to perform critical section code.
     * This is simulated by a sleep here.
     * @param sec used to generate a random sleep (integer) value.
     */
    private void executeCriticalSection(int sec) {
		// **** Sleep for a while
	    // This is the critical session
		int sleep = ra.nextInt(sec);
		timestmp = new Date();
		timestamp = timestmp.toString();
		writeToFile("**NODE " + n_host_name + ":" + n_port + " STARTING CRITICAL SECTION " + timestamp + " for " + sleep + " milliseconds**");
		System.out.println("Node " + n_host_name + ":" + n_port + " starting critical section");
		sleep(sleep);
		System.out.println("Node " + n_host_name + ":" + n_port + " exiting critical section");
    }//end method executeCriticalSection
    
    /**
     * Method to receive the token from coordinator.
     * Token gives Node permission to perform critical section.
     */
    private void getToken() {
	    // **** Then Wait for the token
	    // Print suitable messages
    	try {
	    	n_ss.setSoTimeout(10000); //wait max 10s for a token
			n_token = n_ss.accept();
    	} catch (SocketTimeoutException se) {
    		System.out.println("NODE " + n_host_name + ":" + n_port + " REQUEST UNSUCCESSFUL - CONNECTION TIMED OUT." + se);
			sleep(5000); //wait before trying to re-establish connection
    	} catch (IOException ioe) {
    		System.out.println("ERROR: " + ioe);
    		sleep(5000);
    	}//end try catch
    }//end method getToken
    
    /**
     * Method to make the thread sleep.
     * A useful method to enhance readability.
     * Reduces need for repeating try catch statements.
     * @param duration the duration (integer) of the sleep.
     */
    private void sleep(int duration) {
    	try {
    		Thread.sleep(duration);
    	} catch (InterruptedException ie) {
    		System.out.println("Thread sleep error: " + n_host_name + ": " + ie);
    	}//end try catch
    }//end method sleep
    
    /**
     * Method to send request details.
     */
    private void sendRequestDetails() {
	    // **** Send to the coordinator a token request.
	    // send your ip address and port number
    	try {
	    	pout = new PrintWriter(s.getOutputStream(), true);
			pout.println(n_host); //ip
			pout.println(n_port); //port
			pout.println(getPriority());
			pout.println(System.currentTimeMillis());//the timestamp of request
			System.out.println("Node " + n_host_name + ":" + n_port + " requested token from coordinator.");
			pout.close();
    	} catch (IOException e) {
    		System.out.println("Error sending request: " + e);
    		sleep(5000);
    	}//end try catch
    }//end method sendRequestDetails   
    
    /**
     * Method to generate a priority value for a Node.
     * @return a random integer between 1 and 15 (inclusive).
     */
    private int getPriority() {
    	return ra.nextInt(15)+1;
    }//end method getPriority
    
    /**
     * Method to launch a new coordinator.
     * Used in the event of original coordinator failing.
     */
    private void startNewCoordinator() {
		Coordinator c = new Coordinator ();		
		getAddress();
		clearFile();
		// Create and run a C_receiver and a C_mutex object sharing a C_buffer object		
		C_buffer buffer = new C_buffer();// Shared buffer		
		C_receiver c_receiver = new C_receiver(buffer, c_request_port); // C_receiver which accesses buffer (the shared resource)	
		C_mutex c_mutex = new C_mutex(buffer, c_request_port); // C_mutex which accesses buffer (the shared resource)	
		// Run C_receiver and C_mutex
		c_receiver.start();
		c_mutex.start();
    }//end method startNewCoordinator
    
    /**
     * Method to get this Node's address.
     * Used if this Node takes over coordinator role.
     */
    private void getAddress() {
    	try {    
		    InetAddress c_addr = InetAddress.getLocalHost();
		    String c_name = c_addr.getHostName();
		    System.out.println ("Coordinator address is "+c_addr);
		    System.out.println ("Coordinator host name is "+c_name+"\n\n");    
		}
		catch (Exception e) {
		    System.err.println(e);
		    System.err.println("Error in corrdinator");
		}//end try catch
    }//end method getAddress
    
    /*
    private void sendClosingDownRequest() {
    	try {
    		closingDownRequestSocket = new Socket(shutdown_host, shutdown_port);
    		PrintWriter out = new PrintWriter(closingDownRequestSocket.getOutputStream(), true);
    		out.println("SHUTDOWN");
    	} catch (IOException e) {
    		System.out.println("Node " + n_host_name + ":" + n_port + " failed to send closing down request: " + e);
    	}//end try catch
    }//end method sendClosingDownRequest
    */
    
    /*================================ FILE HANDLING METHODS ================================*/
    
    /**
     * Method to clear contents of text file if not already clear.
     */
    private void clearFile() {
		//Clear the text file
		try { 
            // create fileWriter - false = new file so clear contents
            FileWriter file_writer_id = new FileWriter("2118616_log.txt", false);
            file_writer_id.close();
        } catch (IOException e) {
            System.err.println("Exception in clearing file: main: " + e);
        }// end try-catch
    }//end method clearFile
    
    /**
     * Method to open text file for file logging if it does not already exist.
     */
    private void openFile() {
    	try {
	    	file_writer = new FileWriter("2118616_log.txt", true);
	    	print_writer = new PrintWriter(file_writer, true);
    	} catch(IOException e) {
    		System.out.println("Error creating file for node " + n_host_name + ":" + n_port + " " + e);
    	}//end try catch
    }//end method createFile
    
    /**
     * Method to log a message to file.
     * @param entry the message to log to file.
     */
    private void writeToFile(String entry) {
    	print_writer.println(entry);
    }//end method writeToFile
    
    /**
     * Method to close the file.
     */
    private void closeFile() {
    	try {
	    	print_writer.close();
			file_writer.close();
    	} catch (IOException e) {
    		System.out.println("Error closing file for node " + n_host_name + ":" + n_port + " " + e);
    	}//end try catch
    }//end method closeFile
    
    /*============================== END FILE HANDLING METHODS ==============================*/
    
    /**
     * Main method that constructs a Node.
     * @param args command line argument
     * @throws InterruptedException
     */
    public static void main (String args[]) throws InterruptedException{
		String n_host_name = ""; 
		int n_port;

		// port and millisec (average waiting time) are specific of a node
		if ((args.length < 1) || (args.length > 2)){
			System.out.print("Usage: Node [port number] [millisecs]");
			System.exit(1);
		}//endif
		
		// get the IP address and the port number of the node
	 	try{ 
		    InetAddress n_inet_address =  InetAddress.getLocalHost() ;
		    n_host_name = n_inet_address.getHostName();
		    System.out.println ("node hostname is " +n_host_name+":"+n_inet_address);
	    }
    	catch (java.net.UnknownHostException e){
		    System.out.println(e);
		    System.exit(1);
	    }//end try catch
		
		n_port = Integer.parseInt(args[0]);
		System.out.println ("node port is "+n_port);
	    Node n = new Node(n_host_name, n_port, Integer.parseInt(args[1]));

    }//end method main
    
}//end class Node
