package xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Converts Tasks to string in xml format, and back again
 * @author Ellen
 *
 */
public class TaskSerializer {

	public static Task deserializeTask(String t) throws JAXBException {

		// create an instance context class, to serialize/deserialize.
		JAXBContext jaxbContext = JAXBContext.newInstance(Task.class);

		//Create a file input stream for the university Xml.
		StringReader stream = new StringReader(t);

		// deserialize university xml into java objects.
		if (t.isEmpty()) return new Task();			// hack to get around exception thrown by
		else return (Task) jaxbContext.createUnmarshaller().unmarshal(stream);

	}

	public static String serialize(Task task) throws JAXBException, IOException {
		String returningString = "";

		// create an instance context class, to serialize/deserialize.
		JAXBContext jaxbContext = JAXBContext.newInstance(Task.class);

		// Serialize university object into xml.
		StringWriter writer = new StringWriter();

		// We can use the same context object, as it knows how to
		//serialize or deserialize University class.
		jaxbContext.createMarshaller().marshal(task, writer);

		returningString = writer.toString();

		return returningString;
	}

}
