/*
 * Created on Feb 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.actor.lib;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import ptolemy.util.FileUtilities;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;

/**
 * @author Jackie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CodeStream {

    public CodeStream (CCodeGeneratorHelper ccgh) {
        _cgHelper = ccgh;
    }

    public String toString() {
        return _stream.toString();
    }
    
    public void append(String codeBlock) {
    	_stream.append(codeBlock);
    }
    
    public void append(StringBuffer codeBlock) {
        _stream.append(codeBlock);
    }
   
    public void append(CodeStream codeBlock) {
        _stream.append(codeBlock.toString());
    }
   
    public void appendCodeBlock(String name) throws IOException {
    	// read from .c file
        URL file = FileUtilities.nameToURL(_cgHelper.getClass().toString(), null, null);
        
        // fetch the code within the file
        StringBuffer codeBlock = _fetchCodeBlock(file, name);

        // append to stream
        append(codeBlock);
    }

    /**
	 * @param file
	 * @param name
     * Given the name of the CodeBlock,
	 * @return the string representation of the code within the file
     * @throws IOException
	 */
	private StringBuffer _fetchCodeBlock(URL file, String name) throws IOException {
        
        StringBuffer codeInFile = new StringBuffer((String) file.getContent());
        int startIndex = codeInFile.lastIndexOf(_startCodeBlock1 + name + _startCodeBlock2) + 1;
        int endIndex = codeInFile.indexOf(_endCodeBlock1 + name + _endCodeBlock2) - 1;
	
        return new StringBuffer(codeInFile.substring(startIndex, endIndex));
	}


    /**
     * private variables declaration
     */
    private StringBuffer _stream = new StringBuffer();
    private CCodeGeneratorHelper _cgHelper;
    

    /**
     * The symbol style that indicates the start of a code block
     * (ex. _startCodeBlock1 + codeBlockName + _startCodeBlock2) 
     */
    private String _startCodeBlock1 = "//****";
    private String _startCodeBlock2 = "****";
    
        
    /**
     * The symbol style that indicates the end of a code block
     * (ex. _endCodeBlock1 + codeBlockName + _endCodeBlock2) 
     */
    private String _endCodeBlock1 = "****";
    private String _endCodeBlock2 = "****//";
    
}
