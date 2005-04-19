    /*
 * Created on Apr 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.actor.lib.io;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

/**
 * @author Jackie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LineReader extends CCodeGeneratorHelper {

	/**
	 * @param actor
	 */
	public LineReader(ptolemy.actor.lib.io.LineReader actor) {
		super(actor);
	}

	public void  generateFireCode(StringBuffer stream)
	    throws IllegalActionException {
		CodeStream tmpStream = new CodeStream(this);
		tmpStream.appendCodeBlock("readLine");
		stream.append(processCode(tmpStream.toString()));
	}
   
	public String generateInitializeCode()
	    throws IllegalActionException {
		
		super.generateInitializeCode();

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("initBlock");

        ptolemy.actor.lib.io.LineReader actor =
			(ptolemy.actor.lib.io.LineReader)getComponent();
        
        int skipLines = Integer.parseInt(
                actor.numberOfLinesToSkip.getExpression());
        
        String fileNameString = actor.fileOrURL.getExpression();
        fileNameString = fileNameString.replaceFirst("file:/", "");
        fileNameString = fileNameString.replaceAll("%20", " ");
    
        if (fileNameString.equals("System.in")) {
            _fileOpen = false;
            tmpStream.append("openForStdin");            
        } else {
            _fileOpen = true;
            tmpStream.appendCodeBlock("openForRead");
        	for (int i=0; i<skipLines; i++) {
        		tmpStream.appendCodeBlock("skipLine");
            }
        }
		return processCode(tmpStream.toString());
   }

   public void generateWrapupCode(StringBuffer stream)
        throws IllegalActionException {
   
   	    if (_fileOpen) {
   	    	CodeStream tmpStream = new CodeStream(this);
            tmpStream.appendCodeBlock("wrapUpBlock");
            stream.append(processCode(tmpStream.toString()));
       }
   }
   
   /** Get the files needed by the code generated for the
    *  LineReader actor.
    *  @return A set of strings that are names of the files
    *   needed by the code generated for the LineReader actor.
    */
   public Set getIncludingFiles() {
       Set files = new HashSet();
       files.add("\"stdio.h\"");
       return files;
   }

   /** 
    * indicate whether or not the user requests to open a file
    * e.g. false - write to standard (console) output
    *      true - some file name is specified 
    */
   private boolean _fileOpen = false;
    
}
