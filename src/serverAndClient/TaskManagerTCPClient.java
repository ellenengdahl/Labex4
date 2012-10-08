package serverAndClient;

import java.io.*;
import java.net.*;

import xml.Task;

/**
 * The client to ask for requests according to the the assignment: Taskmanager
 * 
 * 17/09-2012
 * 
 * @author The MacGyvers
 * @version 1.0 beta
 */
public class TaskManagerTCPClient {
	//A predefined port to use to the requests
	int serverPort = 7890;

	//The location of the server
	InetAddress serverAddress;
    Socket socket;

    //The streams
    ObjectInputStream in;
    ObjectOutputStream out;


    /**
     * The contructor
     * Sets the address of the server
     * @param host The serveraddress (ip-address or "localhost")
     * @throws IOException
     */
    public TaskManagerTCPClient(String host) throws IOException {
        serverAddress = InetAddress.getByName(host);
	}

    /**
     * Creates the socket and streams to use
     * @throws IOException If not possible to get the input/output-streams from the socket
     */
    public void open() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        socket.setReuseAddress(true);

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // ObjectOutputStream has a block constructor. This fixes it. #java-io-api-hacks

        in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Closes all sockets and streams used in proper way
     * @throws IOException If unable to close the streams or sockets
     */
    public void close() throws IOException {
        socket.close();
        out.close();
        in.close();
    }

    /**
     * Closes and opens all the sockets and streams
     * @throws IOException If unable to close and reopen 
     */
    public void reset() throws IOException {
        close();
        open();
    }

	/**
	 * Collects the tasks from the server, that the user participates in
	 * @param userID Id of the user
	 * @return The tasks the user is participating in
	 * @throws IOException If unable to get correct response from server
	 * @throws ClassNotFoundException When unable to get the tasks from the server
	 */
	private Task[] get(String userID) throws IOException, ClassNotFoundException {

        out.writeObject("GET");
        out.flush();
        
        //May be used to see if the server gets/reads the correct request 
        @SuppressWarnings("unused")
		String responseProtocol = in.readObject().toString();
        
        out.writeObject(userID);
        out.flush();

        Task[] receivedTasks = (Task[]) in.readObject();

		return receivedTasks;
	}	

	/**
	 * Adds a new task to the task manager at the server
	 * @param task The task to add
	 * @return String telling if the post actually is posted
	 * @throws IOException If a proper connection to the server is not established 
	 * @throws ClassNotFoundException
	 */
	private String post(Task task) throws IOException, ClassNotFoundException {

        out.writeObject("POST");
        out.flush();
        
        @SuppressWarnings("unused")
		String responseProtocol = in.readObject().toString();

        out.writeObject(task);
        out.flush();

        String response = in.readObject().toString();

        return response;
	}
	
	/**
	 * Updates a task at the server
	 * 
	 * @param task The task to update
	 * @return String telling if the tasks was updated
	 * @throws IOException When unable to establish a proper connection to the server
	 * @throws ClassNotFoundException When unable to read the response string from server
	 */
	private String put(Task task) throws IOException, ClassNotFoundException {

        out.writeObject("PUT");
        out.flush();
        
        @SuppressWarnings("unused")
		String responseProtocol = in.readObject().toString();

        out.writeObject(task);
        out.flush();

        String response = in.readObject().toString();

        return response;
	}
	
	/**
	 * Deletes a task at the server
	 * @param taskID If of the task to be deleted
	 * @return A string telling if the task was deleted
	 * @throws IOException If unable to establish a proper connection to the server
	 * @throws ClassNotFoundException When unable to read the response from server
	 */
	private String delete(String taskID) throws IOException, ClassNotFoundException {

        out.writeObject("DELETE");
        out.flush();
        
        @SuppressWarnings("unused")
		String responseProtocol = in.readObject().toString();

        out.writeObject(taskID);
        out.flush();
        
        String response = in.readObject().toString();

        return response;
	}
	

	/**
	 * Main method
	 * @param args Arguments from the user
	 */
	public static void main(String[] args)  {
        try {
            TaskManagerTCPClient client = new TaskManagerTCPClient("localhost");

            client.open();

            Task task1 = new Task("1", "Do MDS Mandatory Exercise 1", "17-9-2012", "done");
            task1.attendants.add("eeng");
            task1.attendants.add("lynd");
            task1.attendants.add("mrof");
            task1.attendants.add("cstp");

            System.out.println("Response to post: "+client.post(task1));

            client.reset();

            System.out.println("Response to get: ");

            Task[] tasks = client.get("eeng");
            
            for(Task task : tasks){
                //xml.CalSerializer.PrintTaskObject(task);
            	System.out.println("Task: " + task.name);
            	System.out.println("ID: " + task.id);
            	System.out.println("status: " + task.status);
            	
            	System.out.println("Attendants:");
            	for(String s : task.attendants)
            		System.out.println(" - " + s);
            }

            client.reset();          
            
            Task task2 = new Task("2412", "Christmas", "24/12/2014", "not executed");
            task2.attendants.add("santa");
            client.post(task2);            
            client.reset();
            System.out.println("Response to delete: "+client.delete("2412"));
            
            client.reset();

            Task task3 = new Task("tch-01", "teach lect 01", "8/11/2002", "not executed");
            task3.attendants.add("hilde");
            client.post(task3);         
            client.reset();            
            Task task4 = new Task("tch-01", "teach lect 01", "8/11/2002", "executed");
            task4.attendants.add("hilde");
            System.out.println("Response to put: "+client.put(task4));

            client.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
	}
}
