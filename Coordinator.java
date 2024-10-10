import java.io.FileWriter;
import java.io.IOException;
import java.net.*;

public class Coordinator {
	
	/**
	 * Starting point of the coordinator.
	 * @param args command line arguments
	 */
    public static void main (String args[]){
		launch(args);		
    }//end method main
    
    /**
     * Method to instantiate coordinator.
     * @param args command line arguments
     */
    private static void launch(String[] args) {
    	int port = 7000;
    	Coordinator c = new Coordinator ();
    	
    	getAddress();
    	clearFile();
    	
		// allows defining port at launch time
		if (args.length == 1) port = Integer.parseInt(args[0]);	
		
		//ShutdownServer continuously listens for closing down requests
		//intended to track List of nodes with pending requests
		//closing down signal would be issued to all nodes in the List
		/*try {
			ShutdownServer shutdownListener = new ShutdownServer(port);
			shutdownListener.start();
		} catch (IOException e) {
			System.out.println("Error in shutdown server: " + e);
		}*/
			
		// Create and run a C_receiver and a C_mutex object sharing a C_buffer object		
		// Shared buffer
		C_buffer buffer = new C_buffer();
		
		// C_receiver which accesses buffer (the shared resource)
		C_receiver c_receiver = new C_receiver(buffer, port);
		
		// C_mutex which accesses buffer (the shared resource)
		C_mutex c_mutex = new C_mutex(buffer, port);
		
		// Run C_receiver and C_mutex
		c_receiver.start();
		c_mutex.start();
    }//end method launch
    
    /**
     * Method to get the InetAddress of coordinator.
     */
    private static void getAddress() {
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
    
    /**
     * Method to clear contents of text file if not already clear.
     */
    private static void clearFile() {
		//Clear the text file
		try { 
            // create fileWriter - false = new file so clear contents
            FileWriter file_writer_id = new FileWriter("2118616_log.txt", false);
            file_writer_id.close();
        } catch (IOException e) {
            System.err.println("Exception in clearing file: main: " + e);
        }// end try-catch
    }//end method clearFile
    
}//end class Coordinator
