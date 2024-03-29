import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import xml.Cal;
import xml.CalSerializer;
import xml.Task;
import xml.TaskSerializer;

/**
 * This class is a combined server and JGroup rolled into one. The test client is in the main method
 * @author MacGyvers
 *
 */
public class TaskManagerGroup extends ReceiverAdapter{

	private Cal cal;
	private CalSerializer cs;
	private HashMap<String, Task> calMap;
	private JChannel channel;
	private final static String GROUPNAME = "TaskManager"; 
	private String xmlPathName;

	public TaskManagerGroup(String path) {
		xmlPathName = path;
		try {
			cs = new CalSerializer();
			cal = cs.deserialize(xmlPathName);
			calMap = new HashMap<String, Task>();
			for(Task task : cal.tasks)
				calMap.put(task.id, task);
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
	@Override
	public void receive(Message msg){
		try {
			//retrieve the command
			short command = msg.getFlags();

			//retrieve the task
			String xml = (String) msg.getObject();
			Task task = TaskSerializer.deserializeTask(xml);
			
			printTask(task, command);
			
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
			Collection<Task> tasks = calMap.values();
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
		if(calMap.containsKey(task.id)){
			updateTask(task);
			saveTasks();
		}
		else calMap.put(task.id, task);
//		System.out.println("added: " + task.id +" | "+ task.name +" | "+ task.date +" | "+ task.status);
		
	}
	
	
	private void updateTask(Task task){
		if(calMap.containsKey(task.id)){
			calMap.put(task.id, task);
			saveTasks();
//			System.out.println("update: " + task.id +" | "+ task.name +" | "+ task.date +" | "+ task.status);
		} else {
			System.out.println("Cannot update task if the task doesn't exist.");
		}
		
	}

	private void deleteTask(Task task){
		if(calMap.containsKey(task.id)){
			calMap.remove(task.id);
			saveTasks();
//			System.out.println("delete: " + task.id +" | "+ task.name +" | "+ task.date +" | "+ task.status);
		} else {
			System.out.println("Cannot delete task if the task doesn't exist.");
		}
		
	}
	// saves the hashMap values to the Cal object that can be serialized.
	private void saveTasks(){
		cal.tasks = calMap.values();
		try {
			cs.serialize(cal,xmlPathName);
		} catch (JAXBException e) {
			// Handle errors here, skipped for this exercise :-)
			e.printStackTrace();
		} catch (IOException e) {
			// Handle errors here, skipped for this exercise :-)
			e.printStackTrace();
		}
	}
	
	private void printTask(Task task, short command){
		
		switch (command){
		case 0:
			System.out.println("Task added: ");
			System.out.println(task);
			break;
		case 1:
			System.out.println("Task updated: ");
			System.out.println(task);
			break;
		case 2:
			System.out.println("Task deleted: ");
			System.out.println(task);
			break;
		case 3:
			System.out.println("All tasks has been requested from another client.");
			break;
		}
		
		
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
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Type client number: ");
		String clientNr = scanner.next();
		
		
		System.out.println("Client #" + clientNr);
		
		
		String path = "task-manager-xml";
		String fileType = ".xml";
		
		TaskManagerGroup tmg = new TaskManagerGroup(path + clientNr + fileType);
		
		
		Task task = new Task("1", "Make JGroup", "08-10-12", "not executed");
		String taskxml = TaskSerializer.serialize(task);

		// test add
		tmg.send("add", taskxml);

//		// test update
//		task.status = "executed";
//		String taskxml2 = TaskSerializer.serialize(task);
//		tmg1.send("update", taskxml2);
//
//		// test delete
//		tmg1.send("delete", taskxml);

		System.in.read();
		System.out.println("Client shut down...");
		tmg.closeChannel();
	}
	
	

}
