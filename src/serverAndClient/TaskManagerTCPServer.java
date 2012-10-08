package serverAndClient;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import xml.Cal;
import xml.CalSerializer;
import xml.Task;

import javax.xml.bind.JAXBException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

/**
 * A server to handle requests from several clients regarding the Taskmanager-assignment
 * 
 * 17/09-2012
 * 
 * @author the MacGyvers
 * @version 1.0 beta
 *
 */
public class TaskManagerTCPServer extends ReceiverAdapter {
	//The predefined port to accept incoming requests
	int serverPort = 7890;

    Cal cal;
    CalSerializer cs;

    /**
     * The constructor
     * Will start be deserialize the local XML-file
     */
	public TaskManagerTCPServer() {
        cs = new CalSerializer();
        try {
            cal = cs.deserialize();
        } catch (JAXBException e) {
            System.out.println("Couldn't start server. JAXBException: ");
            e.printStackTrace();
        } catch (FileNotFoundException e) {

            System.out.println("Couldn't start server. FileNotFoundException: ");
            e.printStackTrace();
        }
        try {
            serve();
        } catch (IOException e) {
            System.out.println("Couldn't start server. IOException:");
            e.printStackTrace();
        }
    }
	
	/**
	 * Listens for incoming requests and handles these in each separate thread
	 * by sending forth the socket to a private class
	 * @throws IOException In case the server crashes
	 */
	public void serve() throws IOException{

        ServerSocket serverSocket = new ServerSocket(serverPort);
		serverSocket.setReuseAddress(true);

		System.out.println("Server is running...");
        while(true){
			Socket socket = serverSocket.accept();
		    new HandleIncomingClient(socket);
		}
	}
	
	/**
	 * Will handle a single request, which socket i defined in the constructor
	 * @author The MacGyvers
	 *
	 */
	private class HandleIncomingClient extends Thread{
		Socket socket;

		//The streams 
		ObjectInputStream in;
	    ObjectOutputStream out;
	    
	    /**
	     * The constructor
	     * Sets the streams to be used and calls the method run() hereafter 
	     * 
	     * @param sock The socket from where the request to be handled is
	     * @throws IOException If anything goes wrong
	     */
		private HandleIncomingClient(Socket sock)  throws IOException{

            socket = sock;
			socket.setReuseAddress(true);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();


            in = new ObjectInputStream(socket.getInputStream());


            this.start();
		}
		
		/**
		 * Handles the requests according what kind it is (fx. get, put, delete,...)
		 */
		public void run() {
			try{

                // Protocol is specified first
                Object obj1 = in.readObject();
                System.out.println("Object 1: "+obj1);

                String protocol = obj1.toString(); // blocking call
                out.writeObject(protocol);
                out.flush();
                
                Object obj2 = in.readObject();
                System.out.println("Object 2: "+obj2);
                // Data in bytestream
                Object data = obj2;

                // handle protocols differently
                if(protocol.equalsIgnoreCase("GET")){
            	    // In this case the data is a userid
                    // @todo check this data

                    Task[] tasks = get(data.toString());

                    out.writeObject(tasks);
                    out.flush();
                }

                if(protocol.equalsIgnoreCase("POST")){
                    // In this case data is a task

                    Task task = (Task) data;
                    String result = post(task);

                    out.writeObject(result);
                    out.flush();
                }

                if(protocol.equalsIgnoreCase("PUT")){

                    Task task = (Task) data;

                    String result = put(task);

                    out.writeObject(result);
                    out.flush();
                }

                if(protocol.equalsIgnoreCase("DELETE")){

                    // In this case the data is an id
                    String result = delete(data.toString());

                    out.writeObject(result);
                    out.flush();
                }

                out.close();
                in.close();
            } catch(EOFException e) {
                System.out.println("Client disconnected");
			} catch(IOException ioe){				
				ioe.printStackTrace();				
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
	}
	
    /**
     * Method to get all the tasks belonging to the specified user id
     * do this by iterating the tasks in the current cal object.
     * Add all the task with the user in the attendants list
     * @param userID to search for
     * @return the list of tasks (might be size 0)
     */
	private Task[] get(String userID){
		ArrayList<Task> returnTasks = new ArrayList<Task>();

        for(Task task : cal.tasks){
            CalSerializer.PrintTaskObject(task);
			if(task.attendants != null && task.attendants.contains(userID))
				returnTasks.add(task);
		}
		
		return returnTasks.toArray(new Task[0]);
		
	}

    /**
     * Simple method to append a task to the list of current cal object
     * @param task
     * @return Success message (confidence wins)
     */
	private String post(Task task){
		
		//share it round
		
        System.out.println("Posting "+task);
        System.out.println(cal);
        System.out.println(cal.tasks);
        cal.tasks.add(task);
        try {
            cs.serialize(cal);
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "Task inserted.";
	}

    /**
     * Method to update the current task list with the provided task
     * Do this by iterating the list until the task with the right id is found
     * Then simply switch the tasks
     * @param task the new task to insert (it aso specifies the id to search for)
     * @return response of how well we did
     */
	private String put(Task task){
        String response = "";

        for(int i=0; i < cal.tasks.size(); i++)
            if(cal.tasks.get(i).id == null) response = "No task by that id";
            else if(cal.tasks.get(i).id.equals(task.id)){
                /* out with the old, in with the new */
                cal.tasks.remove(i);
                cal.tasks.add(task);
                response = "Task updated!";
                try {
                    cs.serialize(cal);
                } catch (JAXBException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

        if(response.isEmpty()) response = "No task with that id found";

        return "Task updated.";
	}

    /**
     * Function to remove the task with the specified id
     * do this by iterate through the current list, if the specified id is found remove the task from list
     * @param id
     * @return Human readable response
     */
	private String delete(String id){
		String response = "";
        for(int i=0; i < cal.tasks.size(); i++)
            if(cal.tasks.get(i).id == null) response = "No task by that id";
            else if(cal.tasks.get(i).id.equals(id)){
                response = "Task deleted!";
                cal.tasks.remove(i);
                try {
                    cs.serialize(cal);
                } catch (JAXBException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
		
		if(response.isEmpty()) response = "No task with that id found";
		
        return response;
	}


    public static void main(String[] args) throws IOException{
        TaskManagerTCPServer server = new TaskManagerTCPServer();
    }
    
    public void receive(){
    
    }
    
}
