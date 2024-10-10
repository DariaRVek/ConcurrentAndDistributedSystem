import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class C_buffer {
	
    private Vector<Object> data;
    private FileWriter file_writer;
    private PrintWriter print_writer;
    private long aging = TimeUnit.SECONDS.toMillis(1); //used to avoid starvation; after aging value elapsed, low priority processes have priority increased
    
    /**
     * Constructor method.
     * Initialises data structure (Vector) that stores Node requests.
     * Opens text file if not already open.
     */
    public C_buffer (){
    	data = new Vector<Object>(); 
		try {
			file_writer = new FileWriter("2118616_log.txt", true);
			print_writer = new PrintWriter(file_writer, true);
		} catch (IOException e) {
			System.out.println("C_buffer: Error opening file: " + e);
		}//end try catch
    }//end constructor method

    /**
     * Method to get number of elements in the buffer.
     * @return the size of the Vector data.
     */
    public int size() {
    	return data.size();
    }//end method size

    /**
     * Method to add a request to the buffer.
     * Synchronized so only one thread at a time can manipulate buffer.
     * @param r the Node's request to be added.
     */
    public synchronized void saveRequest (String[] r){
    	data.add(r[0]);
    	data.add(r[1]);
    }//end method saveRequest

    /**
     * Method to display contents of buffer.
     */
    public void show(){
		for (int i=0; i<data.size();i++)
		    System.out.print(" "+data.get(i)+" ");
		System.out.println(" ");
    }//end method show
    
    /**
     * Method to add an Object to the buffer.
     * Synchronized as it manipulates buffer contents.
     * @param o the Object to be added.
     */
    public synchronized void add(Object o){
    	data.add(o);
    }//end method add
    
    /**
     * Method to retrieve and remove an item from the buffer.
     * @return the item at index 0 in the buffer.
     */
    public synchronized Object get(){
    	Object o = null; 
		if (data.size() > 0){
		    o = data.get(0);
		    data.remove(0);
		}
		return o;
    }//end method get
    
    /**
     * Overloaded method to save a request with a priority and timestamp.
     * Synchronized as only one thread at a time should manipulate buffer contents.
     * @param r the request to save.
     * @param priority the priority of the request.
     * @param time the time the request was made.
     */
    public synchronized void saveRequest(String[] r, int priority, long time) {
    	r[2] = Integer.toString(priority); //insert priority to String array	
    	r[3] = Long.toString(time); //save time with the request
    	data.add(r); //add request r to buffer
    }//end method saveRequest
    
    /**
     * Retrieves and removes a request from the queue.
     * Synchronized as only one thread at a time should manipulate buffer contents.
     * @return the request at index 0 after sorting performed; otherwise null.
     */
    public synchronized Object getRequest() {
    	if(!data.isEmpty()) {
    		//first sort the data according to priority (ascending because smaller integers = higher priority)
    		sortByPriority();
	    	String[] request = (String[]) data.remove(0); //remove the first request
	    	if(request != null) {    		
	    		long currentTime = System.currentTimeMillis(); //get the current time    		
	    		long requestTime = Long.parseLong(request[3]); //get the request timestamp	    		
	    		long waitingTime = currentTime - requestTime; //calculate how long request has been waiting		
	    		if(waitingTime > aging) { //is waiting time more than default time (aging)?  			
	    			int priority = Integer.parseInt(request[2]); //then extract priority of the request
	    			int updatedPriority = priority - (int) (waitingTime / aging); //boost its priority.    			
	    			request[2] = Integer.toString(updatedPriority); //now insert updated priority into String request array
	    			print_writer.println("****************I'VE BEEN WAITING " + aging + " MILLISECONDS! PRIORITY BOOSTED TO " 
	    			+ request[2]+ "!" + " " + request[0]+":"+request[1]);
	    			System.out.println("****************I'VE BEEN WAITING " + aging + " MILLISECONDS! PRIORITY BOOSTED TO " 
	    			+ request[2]+ "!" + " " + request[0]+":"+request[1]);
	    		}//end if waitingTime > aging
	    	}//end if request not null
	    	return request;
    	}//end if data not empty
    	return null;
    }//end method getRequest
    
    /**
     * Sorts data into ascending order.
     */
    private void sortByPriority() {
    	for(int i = 0; i < data.size()-1; i++) {
    		for(int j = i + 1; j < data.size(); j++) {
    			String[] r1 = (String[]) data.get(i);
    			String[] r2 = (String[]) data.get(j);
    			int p1 = Integer.parseInt(r1[2]);
    			int p2 = Integer.parseInt(r2[2]);
    			//if priority of process 2 is smaller than priority of process 1
    			if(p2 < p1) Collections.swap(data, i, j); //make the swap		
    		}//end inner for
    	}//end outer for
    }//end method sortByPriority
    
}//end class C_buffer
