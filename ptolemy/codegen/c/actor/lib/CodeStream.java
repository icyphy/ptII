/*
  @Copyright (c) 2005 The Regents of the University of California.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the
  above copyright notice and the following two paragraphs appear in all
  copies of this software.

  IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
  FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
  ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
  THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.

  THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
  PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY


*/
/*
 * Created on Feb 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.actor.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.util.FileUtilities;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Jackie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CodeStream {

    // FIXME: put Javadoc comments

    /**
     * 
     */
	public CodeStream (CCodeGeneratorHelper helper) {
        _actorHelper = helper;
    }

	/** To append the given string to this code stream
	 * @param codeBlock
	 */
    public void append(String codeBlock) {
        _stream.append(codeBlock);
    }

    /** To append the content of the StringBuffer to this code stream
     * @param codeBlock
     */
    public void append(StringBuffer codeBlock) {
        _stream.append(codeBlock);
    }

    /** To append the content of the CodeStream to this code stream
     * @param codeBlock
     */
    public void append(CodeStream codeBlock) {
        _stream.append(codeBlock.toString());
    }
   
    /** To append an specific code block. 
     * First, it checks if the code file is parsed already.
     * If so, it gets the code block from the well-constructed code block table.
     * If not, it has to construct the table. 
     * @param blockName
     * @throws IllegalActionException
     */
    public void appendCodeBlock(String blockName) throws IllegalActionException {
        if (_codeBlockTable == null) {
        	_constructCodeBlockTable();
        }
        StringBuffer codeBlock = (StringBuffer) _codeBlockTable.get(blockName);
        if (codeBlock == null) 
            throw new IllegalActionException ("Cannot find code block: " + blockName);        
       	
        _stream.append(codeBlock);
    }

    /** 
     * Return the string representation of the code stream
     */
    public String toString() {
        return _stream.toString();
    }

    
    /**
     * This method reads the .c file associate with the particular actor 
     * identified by the className and constructs the code block table. 
     * @param className
     * @throws IllegalActionException
     */
    private void _constructCodeBlockTable() throws IllegalActionException {
        String className = (_actorHelper == null) ? _testingClassName : _actorHelper.getClass().toString();
        
        _codeBlockTable = new Hashtable();
        BufferedReader reader = null;
        try {
            // open the .c file for reading
            reader = FileUtilities.openForReading(_filePath + className+".c", null, null);
            StringBuffer codeInFile = new StringBuffer();

            // create a string of all code in the file
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                codeInFile.append(line);
            }
            
            // recursively parse the file
            while (_parseCodeBlock(codeInFile) != null); 
            
        } catch (IOException e) {
            throw new IllegalActionException ("Cannot open file: " + className + ".c");
        }         
    }
 
    /** This method parse code from the given StringBuffer. It is
     * responsible for putting the first single (nested or non-nested) 
     * code block along with the block name (key) into the code block table.
     * It calls sub-parsing functions parseHeader() and parseBody().
     * It returns the name of the code block (key). If null is returned, it means
     * there is no more code block to be parsed in the .c file.
	 * 
     * @param codeInFile
	 * @return
     * @throws IllegalActionException
	 */
	private String _parseCodeBlock(StringBuffer codeInFile) throws IllegalActionException {
        String name = _parseHeader(codeInFile);
        if (name != null) {
            StringBuffer body = _parseBody(codeInFile);
            _codeBlockTable.put(name, body);
        }
		return name;
	}

	/** 
     * This is a sub-parsing method which is responsible for
     * returning a well-formed string representing the code block name
	 * @param codeInFile
	 * @return
	 * @throws IllegalActionException
	 */
	private String _parseHeader(StringBuffer codeInFile) throws IllegalActionException {
        
        _parseIndex = codeInFile.indexOf(_BLOCKSTART, _parseIndex);
        
        if (_parseIndex == -1) // find no more code block start headers
            return null;
        
        _parseIndex += _BLOCKSTART.length();        
        int endIndex = codeInFile.indexOf(_HEADEREND, _parseIndex);
        
        if (endIndex == -1) 
            throw new IllegalActionException("Missing code block close header");
        
        String name = _checkCodeHeader(codeInFile.substring(_parseIndex, endIndex));
        _parseIndex = _HEADEREND.length() + endIndex; 
		return name;
	}

    /**
     * This method type-checks the format of a proper code block name.
     * Assume the code block name is short (that is the reason why String
     * is used instead of StringBuffer).
     * This method returns a well-formed code block name,
     * otherwise, it throws exception
	 * @param string
	 */
	private static String _checkCodeHeader(String name) throws IllegalActionException{
        // FIXME: type checking
		return name;
	}

	/**
     * This is a sub-parsing method which is responsible for
     * returning a well-formed string representing the code block body.
     * It recursively parses within the code body for nested code blocks.
     * @param codeInFile
     * @return
	 * @throws IllegalActionException
     */
    private StringBuffer _parseBody(StringBuffer codeInFile) throws IllegalActionException {
        int openBlock = 1;
        int scanIndex = _parseIndex;

        int startIndex, endIndex = -1;        
        while (openBlock > 0) {
            endIndex = codeInFile.indexOf(_BLOCKEND, scanIndex);
        	startIndex = codeInFile.indexOf(_BLOCKSTART, scanIndex);
            if (startIndex < endIndex && startIndex != -1) {
            	openBlock++;
                scanIndex = startIndex+1;
            }
            else {
                openBlock--;                
                scanIndex = endIndex+1;
            }
        }
        
        if (endIndex == -1) 
            throw new IllegalActionException("Missing close block");

        StringBuffer body = new StringBuffer(codeInFile.substring(_parseIndex, endIndex));

        // recursively parse for nested code blocks
        
        for (String subBlockKey = _parseCodeBlock(codeInFile); subBlockKey != null; ) {       
            // FIXME: do we include the nested code block into the current block??
            //body.append((StringBuffer) _codeBlockTable.get(subBlockKey));
            subBlockKey = _parseCodeBlock(codeInFile);
        }
        _parseIndex = _BLOCKEND.length() + endIndex; 
        return body;
    }

    ///////////////////////////////////////////////////////////////////////
    // private variables declarations
    ///////////////////////////////////////////////////////////////////////

    
    /**
     * private index pointer that indicates the location
     * within the .c file to be parsed.
     */
    private int _parseIndex = 0;
    
	/**
     * stream buffer that stores the content of this CodeStream.
     */
    private StringBuffer _stream = new StringBuffer();
   
    /**
     * code block table, which stores the code blocks using the 
     * code block names (String) as keys.
     */
    private Hashtable _codeBlockTable = null;
    
    /**
     * private variable that keeps the pointer to the associated actor helper object
     */
    private CCodeGeneratorHelper _actorHelper;

    /** 
     * file path to the .c files
     */
    private static String _filePath = "$CLASSPATH" + "/ptolemy/codegen/c/actor/lib/";

    /**
     * string pattern which represents the start of a code block.
     */
    private static String _BLOCKSTART = "/***";
    
    /**
     * string pattern which represents the end of a code block header.
     */
    private static String _HEADEREND = "*/";

    /**
     * string pattern which represents the end of a code block.
     */
    private static String _BLOCKEND = "/**/";
    
    
    
    
    
    //============================================================================
 
    private static String _testingClassName;
    /**
     * This is a private test method. Given the name of an actor class,
     * It returns a StringBuffer that contains all the code block names and bodies
     * @throws IllegalActionException
     */
    private static StringBuffer _testing() throws IllegalActionException {
        StringBuffer buffer = new StringBuffer();
    	CodeStream stream = new CodeStream(null);
        if (stream._codeBlockTable == null) {
            stream._constructCodeBlockTable();
        }
        
        for (Iterator keys = stream._codeBlockTable.keySet().iterator(); keys.hasNext();) {
        	String key = (String) keys.next();
            buffer.append(key + ": \n");
            buffer.append((StringBuffer) stream._codeBlockTable.get(key));
            buffer.append("\n-------------------------------\n\n");
        }
        
        return buffer;
    }
    public static void main(String[] arg) throws IOException, IllegalActionException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("----------Testing--------------------------------");
        System.out.print("please input class name: ");
        _testingClassName = in.readLine();
        System.out.println("\n----------Result----------------------------------");

        System.out.println(CodeStream._testing());
    }
}
