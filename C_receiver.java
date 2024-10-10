import java.io.IOException;
import java.net.*;

public class C_receiver extends Thread {
	
    private C_buffer buffer; 
    private int port;
    private ServerSocket s_socket; 
    private Socket socketFromNode;
    private C_Connection_r connect;
    
    /**
     * Constructor method.
     * @param b the buffer (the shared resource).
     * @param p the port the port a request is made on.
     */
    public C_receiver (C_buffer b, int p){
		buffer = b;
		port = p;
    }//end constructor method
    
    /**
     * Method to override default run() method.
     * While this thread is running, it listens for incoming connections.
     * On receiving a connection, it starts a new thread that will save the request.
     */
    @Override
    public void run () {
    	// >>> create the socket the server will listen to
    	createServerSocket();
		while (true) {
			// >>> get a new connection
			getNewConnection();
			// >>> create a separate thread to service the request, a C_Connection_r thread.
			serviceRequest();
		}//end while
    }//end run
    
    /**
     * Method to create the ServerSocket that will listen for incoming connections.
     */
    private void createServerSocket() {
    	try {
			s_socket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("C_receiver: Incoming connection failed: " + e.getMessage());
		}//end try catch
    }//end method createServerSocket
    
    /**
     * Method to establish a connection from a Node.
     * Connection is established through socket that Node sends the request on.
     */
    private void getNewConnection() { 	
    	try {
	    	socketFromNode = s_socket.accept();
			System.out.println ("C:receiver    Coordinator has received a request from " + socketFromNode);
    	} catch(IOException ioe) {
    		System.out.println("C_receiver couldn't connect to " + socketFromNode + ioe);
    		System.exit(1);
    	}//end try catch
    }//end method getNewConnection
    
    /**
     * Method to create a separate thread that will service the request.
     * Request will be saved to buffer through this connect thread.
     */
    private void serviceRequest() {  	
		connect = new C_Connection_r(socketFromNode, buffer);
		connect.start();	
    }//end method serviceRequest

}//end class C_receiver
