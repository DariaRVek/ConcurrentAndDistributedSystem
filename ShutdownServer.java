import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ShutdownServer extends Thread {

	private ServerSocket serverSocket; //to listen for incoming connections
	private List<Socket> connectedNodes; //to track active Nodes
	
	/**
	 * Constructor method
	 * @param port the port a closing down request is received on
	 * @throws IOException
	 */
	public ShutdownServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		connectedNodes = new ArrayList<>();
	}//end constructor method
	
	/**
	 * Overriding default run() method.
	 * Continuously listens for connections and checks for closing down requests.
	 */
	@Override
	public void run() {
		try {
			while(true) {
				Socket nodeSocket = serverSocket.accept(); //accept connection
				connectedNodes.add(nodeSocket); //add node to the list
				handleShutdownRequest(nodeSocket);	
			}//end while
		} catch (IOException e) {
			e.printStackTrace();
		}//end try catch
	}//end method run
	
	/**
	 * Method to communicate closing down signal.
	 * @param s the socket from the node to connect to.
	 * @throws IOException
	 */
	private void handleShutdownRequest(Socket s) throws IOException {
		InputStream in = s.getInputStream();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		String message = bin.readLine();
		//check if closing down request received
		if("SHUTDOWN".equals(message)) {
			System.out.println("Closing down request initiated by: " + s.getInetAddress());
			s.close(); //close the socket
			broadcastShutdownRequest(); //communicate to all nodes to close down
		}//end if message equals SHUTDOWN
	}//end method handleShutdownRequest
	
	/**
	 * Closes all Nodes currently in List of connected Nodes.
	 */
	private void broadcastShutdownRequest() {
		//for every connected node
		for(Socket node : connectedNodes) {
			//send a signal to close down
			try {
				PrintWriter output = new PrintWriter(node.getOutputStream(), true);
				output.println("SHUTDOWN".getBytes()); //the signal
				node.close(); //now close connection
			} catch (IOException e) {
				e.printStackTrace();
			}//end try catch
		}//end for
	}//end method broadcastShutdown
	
	/**
	 * Method would be called after all Nodes receive closing down signal.
	 * @throws IOException
	 */
	public void closeShutdownServer() throws IOException {
		serverSocket.close();
	}//end method closeShutdownServer
	
}//end class ShutdownServer
