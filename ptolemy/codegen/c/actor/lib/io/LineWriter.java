/*
 * Created on Apr 5, 2005
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
import ptolemy.util.FileUtilities;

/**
 * @author Jackie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LineWriter extends CCodeGeneratorHelper {

	/**
	 * @param actor
	 */
	public LineWriter(ptolemy.actor.lib.io.LineWriter actor) {
		super(actor);
	}
    
    /**
     * @param stream
     */
    public void  generateFireCode(StringBuffer stream)
        throws IllegalActionException {
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("writeLine");
        stream.append(processCode(tmpStream.toString()));
    }

    public String generateInitializeCode()
        throws IllegalActionException {
        super.generateInitializeCode();
        ptolemy.actor.lib.io.LineWriter actor =
            (ptolemy.actor.lib.io.LineWriter)getComponent();
        CodeStream tmpStream = new CodeStream(this);

        tmpStream.appendCodeBlock("initBlock");
        if (actor.fileName.getExpression().equals("System.out")) {
            _fileOpen = false;
            tmpStream.appendCodeBlock("openForStdout");                   	
        } else {
            _fileOpen = true;

            // FIXME: how do we handle relative file path??
            String fileNameString = actor.fileName.getExpression();
            fileNameString = fileNameString.replaceFirst("file:/", "");
            fileNameString = fileNameString.replaceAll("%20", " ");

            boolean fileExist = 
                FileUtilities.nameToFile(fileNameString, null).exists();
            boolean askForOverwrite = 
                actor.confirmOverwrite.getExpression().equals("true");

            if (fileExist && askForOverwrite) {
                tmpStream.appendCodeBlock("confirmOverwrite");            
            }
          
            if (actor.append.getExpression().equals("true")) {
                tmpStream.appendCodeBlock("openForAppend");
            } else {
                tmpStream.appendCodeBlock("openForWrite");
            }
            
        }
        return processCode(tmpStream.toString());
    }

    /**
     * @param stream
     */
    public void generateWrapupCode(StringBuffer stream)
        throws IllegalActionException {
        
        if (_fileOpen) {
            CodeStream tmpStream = new CodeStream(this);
            tmpStream.appendCodeBlock("wrapUpBlock");
            stream.append(processCode(tmpStream.toString()));
        }
    }
    
    /** Get the files needed by the code generated for the
     *  LineWriter actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the LineWriter actor.
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
