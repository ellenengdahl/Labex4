/**
 * 
 */
package xml;

import java.io.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author Yndal
 *
 */
public class CalSerializer {
	private final String SAVING_PATH = "task-manager-xml.xml";

	public Cal deserialize() throws JAXBException, FileNotFoundException {
		Cal cal = null;

        // create an instance context class, to serialize/deserialize.
        JAXBContext jaxbContext = JAXBContext.newInstance(Cal.class);

        //Create a file input stream for the university Xml.
        FileInputStream stream = new FileInputStream(SAVING_PATH);

        // deserialize university xml into java objects.
        cal = (Cal) jaxbContext.createUnmarshaller().unmarshal(stream);



        // Iterate through the collection of student object and print each student object in the form of Xml to console.
        ListIterator<Task> taskIterator = cal.tasks.listIterator();

        ListIterator<User> userIterator = cal.users.listIterator();

		
		return cal;
	}
	
	
	public String serialize(Cal cal) throws JAXBException, IOException {
		String returningString = "";

        // create an instance context class, to serialize/deserialize.
        JAXBContext jaxbContext = JAXBContext.newInstance(Cal.class);

        // Serialize university object into xml.
        StringWriter writer = new StringWriter();

        // We can use the same context object, as it knows how to
        //serialize or deserialize University class.
        jaxbContext.createMarshaller().marshal(cal, writer);


        SaveFile(writer.toString(), SAVING_PATH);

        returningString = writer.toString();
            

		return returningString;
	}
	

//    public static void main(String args[]) throws IOException, FileNotFoundException, JAXBException{
//    	
//    	Cal cal = new Cal();
//    	cal.tasks = new ArrayList<Task>();
//    	cal.users = new ArrayList<User>();
//    	
//    	Task task = new Task();
//    	task.id = "id-somehting";
//    	task.name = "name-somehting";
//    	task.date = "date-somehting";
//    	task.status = "status-somehting";
//    	task.attendants = new ArrayList<String>();
//    	task.attendants.add("Attendant1");
//    	task.attendants.add("Attendant2");
//    	task.attendants.add("Attendant3");
//    	cal.tasks.add(task);
//    	
//    	User user = new User();
//    	user.name = "name of user";
//    	user.password = "Undercover Brother";
//    	cal.users.add(user);
//    	
//    	CalSerializer calSer = new CalSerializer();
//    	System.out.println(calSer.serialize(cal));
//    	
//    	System.out.println("Tasks in Cal: " + cal.tasks.size());
//    	System.out.println("Users in Cal: " + cal.users.size());
//    	
//    	
//    	
//    	Cal calClone = calSer.deserialize();
//    	
//    	if(calClone != null) System.out.println(calClone);
//    	
//    	System.out.println(calSer.serialize(calClone));
//    	
//    }

    public static void PrintTaskObject(Task task){

        try {

            StringWriter writer = new StringWriter();

            // create a context object for Student Class
            JAXBContext jaxbContext = JAXBContext.newInstance(Task.class);

            // Call marshal method to serialize student object into Xml
            jaxbContext.createMarshaller().marshal(task, writer);

            System.out.println(writer.toString());

        } catch (JAXBException ex) {
            Logger.getLogger(CalSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public static void PrintUserObject(User user){

        try {
            StringWriter writer = new StringWriter();

            // create a context object for Student Class
            JAXBContext jaxbContext = JAXBContext.newInstance(User.class);

            // Call marshal method to serialize student object into Xml
            jaxbContext.createMarshaller().marshal(user, writer);

            System.out.println(writer.toString());

        } catch (JAXBException ex) {
            Logger.getLogger(CalSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    private static void SaveFile(String xml, String path) throws IOException {


        File file = new File(path);

        // create a bufferedwriter to write Xml
        BufferedWriter output = new BufferedWriter(new FileWriter(file));

        output.write(xml);

        output.close();
   }
}