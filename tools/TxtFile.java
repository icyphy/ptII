/**
 * 
 */
package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @author Tarciana Cabral de Brito Guerra
 *
 */
public class TxtFile {
    private String name;
    private File file;

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }
    
    /**
     * @param name the name to of the file
     */
    public TxtFile(String name){
	if(name.equals(null) || name.length()<3){
	    System.out.println("Choose a valid name for the txt file."); 
	}else{
	    if(!name.endsWith(".txt")){
		name = name.concat(".txt");
	    }
	    this.name = name;
	    try{
		System.out.println(this.name);
		File file = new File(this.name);
		this.file =file;
		boolean verify = false;
		if(!file.exists()){
		    verify = file.createNewFile();
		}else {
		    verify = true;
		}if (!verify){
		    throw new Exception();
		}
		
	    }catch(Exception e){
		System.out.println("Couldn't create the file.");
	    }
	}
	
    }
    /**
     * @param data the information you want to write
     */
    public boolean write(String data){
	try{
	    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
	    writer.newLine();
	    writer.write(data);
	    writer.newLine();
	    writer.flush();
	    writer.close();
	    return true;
	}catch(Exception e){
	    return false;
	}
    }
    
    
    
}
