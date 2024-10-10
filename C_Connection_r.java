import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
// Reacts to a node request.
// Receives and records the node request in the buffer.
//
public class C_Connection_r extends Thread {

    // class variables
	private C_buffer buffer;
	private Socket s;
	private InputStream in;
	private BufferedReader bin;
	private FileWriter file_writer;
	private PrintWriter print_writer;
	private String[] request;
    final int NODE = 0; //node ip at position 0 in request String array
	final int PORT = 1; //port number at position 1 in request String array
	final int PRIORITY = 2; //priority at position 2 in request String array
	final int TIME = 3; //timestamp at position 3 in request String array
       	
	/**
	 * Constructor method
	 * @param s the socket to access input stream from Node
	 * @param b the buffer to add requests to
	 */
    public C_Connection_r(Socket s, C_buffer b){
    	this.s = s;
    	this.buffer = b;
    }//end constructor method
    
    /**
     * Method to override default run() method.
     * While this thread is running it will read in a Node's request.
     * Then, it will add the request to the buffer and log relevant events to file.
     */
    @Override
    public void run() {	
		
		request = new String[4]; //modified to size 4 to include priority and time	
		
		System.out.println("C:connection IN  dealing with request from socket "+ s);
		try {	
			openFile();
			
		    // >>> read the request, i.e. node ip and port from the socket s
		    // >>> save it in a request object and save the object in the buffer (see C_buffer's methods).
		    createInputStream();

		    readRequest();
		    
			// Creating date format - improves readability of output statements
	        SimpleDateFormat simple = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy");
	        Date result = new Date(Long.parseLong(request[TIME]));

	        recordRequest(); //Save the request object in buffer

	        writeToFile("REQUEST from node on port " + request[PORT] + ", PRIORITY " + request[PRIORITY] + ", TIME: " + simple.format(result) +
        			"\nC_Connection_r: Buffer size: " + buffer.size());
	        				
		    s.close();
		    System.out.println("Request details: " + request[NODE] + " " + request[PORT] + " " + request[PRIORITY] + " " + simple.format(result));
		    System.out.println("C:connection OUT    received and recorded request from " + request[NODE]+":"+request[PORT]+ 
		    		", priority " + request[PRIORITY]+ ", time: " + simple.format(result) + "  (socket closed)");		    
		    
		    closeFile();
		} catch (java.io.IOException e){
				System.out.println(e);
				System.exit(1);
		}//end try catch
 	}//end method run
    
    /**
     * Method to add request into the buffer.
     */
    private void recordRequest() {
    	buffer.saveRequest(request, Integer.parseInt(request[PRIORITY]), Long.parseLong(request[TIME]));
    }//end method recordRequest
    
    /**
     * Method to read from input stream from node.
     */
    private void readRequest() {
    	try {
		    //Read the node IP
		    request[NODE] = bin.readLine();
		    
		    //Read the node port number
			request[PORT] = bin.readLine();
			
			//Read the node priority
			request[PRIORITY] = bin.readLine();
			
			//Read the node timestamp
			request[TIME] = bin.readLine();
			
			bin.close();
    	} catch (IOException ioe) {
    		System.out.println("C_Connection_r couldn't read request details: " + ioe);
    		System.exit(1);
    	}//end try catch
    }//end method readRequest
    
    /**
     * Method to create input stream from node accessed through socket s.
     * InputStreamReader(in) will convert stream of bytes from socket s to characters
	 * BufferedReader more efficient and provides methods to read text from stream
     */
    private void createInputStream() {
    	try {
	    	in = s.getInputStream(); //input stream from node accessed through socket s
		    bin = new BufferedReader(new InputStreamReader(in));
    	} catch(IOException e) {
    		System.out.println("C_Connection_r: Error accessing request: " + e);
    	}//end try catch
    }//end method createInputStream
    
    /*================================ FILE HANDLING METHODS ================================*/
    
    /**
     * Method to open text file if it does not already exist.
     */
    private void openFile() {
    	try {
	    	file_writer = new FileWriter("2118616_log.txt", true);
			print_writer = new PrintWriter(file_writer, true);
    	} catch(IOException e) {
    		System.out.println("C_Connection_r couldn't open file: " + e);
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
    		System.out.println("C_Connection_r couldn't close file: " + e);
    	}//end try catch
    }//end method closeFile
    
}//end class C_Connection_r
