/**
 * 
 */
package xml;

import java.io.Serializable;

import javax.xml.bind.annotation.*;

/**
 * @author Yndal
 *
 */
@XmlRootElement(name = "user")
public class User implements Serializable {
	
	@XmlElement
	public String name;
	
	@XmlElement
	public String password;

    public User(){}

	public User(String name, String password){
        this.name = name;
        this.password = password;
    }

}
