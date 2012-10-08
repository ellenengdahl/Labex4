import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import xml.Cal;
import xml.CalSerializer;
import xml.Task;
import xml.TaskSerializer;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

/**
 * This class is a combined server and JGroup rolled into one. The test client is in the main method
 * @author MacGyvers
 *
 */
public class TaskManagerGroup extends ReceiverAdapter{

	private Cal cal;
	private CalSerializer cs;
	private JChannel channel;
	private final static String GROUPNAME = "TaskManager";

	public TaskManagerGroup() {

		try {
			cs = new CalSerializer();
			cal = cs.deserialize();

			// initialize a JChannel
			channel = new JChannel();
			channel.setReceiver(this);
			channel.connect(GROUPNAME);
			channel.getState(null, 10000);

			// synch with other servers
			synch();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Probably a problem with the channel");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a request to synch with other servers in the group
	 */
	private void synch(){		
		send("synch", "");		
	}

	/**
	 * Sends a message to all servers via the group
	 * @param command
	 * @param xml
	 */
	public void send(String command, String xml){

		Message msg = new Message(null, null, xml);

		/*
		 * Flags:
		 * add = 0
		 * update = 1
		 * delete = 2
		 * synch = 3 	
		 */
		if (command.equalsIgnoreCase("add")) msg.setFlag(new Short("0"));
		else if (command.equalsIgnoreCase("update")) msg.setFlag(new Short("1"));
		else if (command.equalsIgnoreCase("delete")) msg.setFlag(new Short("2"));
		else if (command.equalsIgnoreCase("synch")) msg.setFlag(new Short("3"));

		else throw new IllegalArgumentException();		

		try {
			channel.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receives updates to the TaskManager from the other task managers via the group
	 */
	public void receive(Message msg){
		try {
			//retrieve the command
			short command = msg.getFlags();

			//retrieve the task
			String xml = (String) msg.getObject();
			Task task = TaskSerializer.deserializeTask(xml);

			// if command is "add" (or put)
			if (command == 0)
			{
				addTask(task);
			}

			// if command is "update" (or post)
			else if (command == 1)
			{
				updateTask(task);
			}

			// if command is "delete"
			else if (command == 2)
			{
				deleteTask(task);
			}

			// if command is "synch"
			else if (command == 3)
			{
				sendAllTasks();
			}

			else {
				throw new IllegalArgumentException();
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendAllTasks()
	{
		try {
			ArrayList<Task> tasks = cal.tasks;
			for (Task t : tasks){
				String taskXml = TaskSerializer.serialize(t);
				send("add", taskXml);	
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addTask(Task task){
		System.out.println("added: " + task.id +" | "+ task.name +" | "+ task.date +" | "+ task.status);
	}

	private void updateTask(Task task){
		System.out.println("update: " + task.id +" | "+ task.name +" | "+ task.date +" | "+ task.status);
	}

	private void deleteTask(Task task){
		System.out.println("delete: " + task.id +" | "+ task.name +" | "+ task.date +" | "+ task.status);
	}

	/**
	 * Closes channel.
	 */
	public void closeChannel(){
		channel.close();
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		TaskManagerGroup tmg = new TaskManagerGroup();

		Task task = new Task("1", "Make JGroup", "08-10-12", "not executed");
		String taskxml = TaskSerializer.serialize(task);

		// test add
		tmg.send("add", taskxml);

		// test update
		task.status = "executed";
		String taskxml2 = TaskSerializer.serialize(task);
		tmg.send("update", taskxml2);

		// test delete
		tmg.send("delete", taskxml);

		tmg.closeChannel();
	}

}
