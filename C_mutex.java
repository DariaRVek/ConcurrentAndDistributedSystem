import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

public class C_mutex extends Thread {
    
	private C_buffer buffer;
    private Socket s;
    private int port;

    // ip address and port number of the node requesting the token.
    // They will be fetched from the buffer    
    private String n_host;
    private int n_port;
    private int priority;
    private long time;
    
	private FileWriter file_writer;
	private PrintWriter print_writer;
	private ServerSocket ss_back;
	
	/**
	 * Constructor method.
	 * @param b the buffer from where a request is removed.
	 * @param p the port the port a request is received on.
	 */
    public C_mutex (C_buffer b, int p){
		buffer = b;
		port = p;
    }//end constructor method

    /**
     * Method to override default run() method.
     * While this thread is running it will check buffer size.
     * If buffer is not empty (>0) it will read the request details,
     * then issue the token, and wait for the Node to return the token.
     */
    @Override
    public void run(){
		try{ 
		    //  >>>  Listening from the server socket on port 7001
		    // from where the TOKEN will be returned later.
		    ss_back = new ServerSocket(7001);
			
		    while (true){			
				// if the buffer is not empty
				if (buffer.size() > 0) {
					openFile(); //for access to shared resource
					getNode(); //read request details
					grantToken(); //fulfil request - issue token to node
					closeFile();
					receiveTokenBack();	//block until coordinator receives token
				}//endif				
		    }//endwhile
		}catch (Exception e) { 
			System.out.print(e); 
		}//end try catch 
   }//end method run
    
    /*================================ SERVICE NODE REQUEST METHODS ================================*/
    
    /**
     * Method to get token back from Node.
     * Timeout value set to 10 seconds.
     * If token not returned in this timeframe, coordinator abandons this Node and proceeds.
     */
    private void receiveTokenBack(){
    	//  >>>  **** Getting the token back
	    try{
			// THIS IS BLOCKING !
	    	/*
	    	 * ss_back.accept will block until it establishes a connection
	    	 * A connection will only be established when a node has returned a token
	    	 */
	    	ss_back.setSoTimeout(10000);
	    	s = ss_back.accept();
	    	System.out.println("C_mutex: Token returned by node " + n_host + ":" + n_port);
	    } catch(SocketTimeoutException ste) {
	    	System.out.println("C_mutex still waiting on: " + n_host + ":" + n_port + " returning token. Moving to next request.");
	    } catch (SocketException se) {
	    	System.out.println("C_mutex lost connection with: " + n_host + ":" + n_port + ". Moving to next request.");
	    } catch (java.io.IOException e) {
			System.out.println(e);
			System.out.println("CRASH Mutex waiting for the TOKEN back" + e);
			System.exit(1);
	    }//end try catch
    }//end method receiveTokenBack
    
    /**
     * Method to issue token to Node.
     * Successful connection through Socket s using Node 
     * host name and port number indicates token has been granted.
     */
    private void grantToken() {
    	// >>>  **** Granting the token
	    try{			
    		s = new Socket(n_host, n_port);
    		writeToFile("TOKEN ISSUED to " + n_host + ":" + n_port + ", priority: " + priority +
	    			"\nC:mutex   Buffer size is " + buffer.size());
	    	System.out.println("C_mutex: Coordinator granted token to node " + n_host + ":" + n_port);
	    	System.out.println("C:mutex   Buffer size is "+ buffer.size());
	    	s.close();   
	    } catch (java.io.IOException e) {
			System.out.println(e);
			System.out.println("CRASH Mutex connecting to the node for granting the TOKEN " + e);
			sleep(2500);
	    }//end try catch
    }//end method grantToken
    
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
    		System.out.println("C_mutex sleep failed: " + ie);
    	}//end try catch
    }//end method sleep
    
    /**
     * Method to get the highest priority node waiting for a token.
     */
    private void getNode() {
    	// >>>   Getting the first (highest priority) node that is waiting for a TOKEN form the buffer
	    //       Type conversions may be needed.
	    try{
	    		String[] request = (String[])buffer.getRequest();
	    		n_host = request[0];
	    		n_port = Integer.parseInt(request[1]);
	    		priority = Integer.parseInt(request[2]); 
	    		time = Long.parseLong(request[3]);
	    } catch (Exception e){
	        System.err.println("C_mutex couldn't convert request: " + e);
	    }//end try catch
    }//end method getNode
    
    /*================================ FILE HANDLING METHODS ================================*/
    
    /**
     * Method to open text file for file logging if it does not already exist.
     */
    private void openFile() {
    	try {
	    	file_writer = new FileWriter("2118616_log.txt", true);
	    	print_writer = new PrintWriter(file_writer, true);
    	} catch(IOException e) {
    		System.out.println("C_mutex couldn't create file: " + e);
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
    		System.out.println("C_mutex couldn't close file: " + e);
    	}//end try catch
    }//end method closeFile
    
}//end class C_mutex
