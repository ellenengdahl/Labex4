import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import xml.Cal;
import xml.CalSerializer;

public class TaskGroup extends ReceiverAdapter {

	JChannel channel;
    String user_name=System.getProperty("user.name", "n/a");
    final List<String> state=new LinkedList<String>();
    Cal cal;
    CalSerializer cs;

    private void start() throws Exception {
        channel=new JChannel();
        channel.setReceiver(this);
        channel.connect("TaskCluster");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
    	BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
    	while(true) {
    		try {
    			System.out.print("> "); System.out.flush();
    			String line=in.readLine().toLowerCase();
    			if(line.startsWith("quit") || line.startsWith("exit")) {
    				break;
    			}
    			line="[" + user_name + "] " + line;
    			Message msg=new Message(null, null, line);
    			channel.send(msg);
    		}
    		catch(Exception e) {
    		}
    	}		
	}
    
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
    	String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
        synchronized(state) {
        	state.add(line);
        }
    }
	
	public byte[] getState() {
        synchronized(state) {
            try {
                return Util.objectToByteBuffer(state);
            }
            catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
	
	 public void setState(byte[] new_state) {
	        try {
	            @SuppressWarnings("unchecked")
				List<String> list=(List<String>)Util.objectFromByteBuffer(new_state);
	            synchronized(state) {
	                state.clear();
	                state.addAll(list);
	            }
	            System.out.println("received state (" + list.size() + " messages in chat history):");
	            for(String str: list) {
	                System.out.println(str);
	            }
	        }
	        catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
        
	 
	 public static void main(String[] args) throws Exception {
	        new TaskGroup().start();
	    }

}

