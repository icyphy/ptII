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
   
    /**
     * To append an specific code block
     * @throws IllegalActionException
     */
    public void appendCodeBlock(String blockName) throws IllegalActionException {
        _appendCodeBlock(_cgHelper.getClass().toString(), blockName);
    }

    /**
     * To append an nameless code block
     * e.g. the file contains only one code block 
     * @throws IllegalActionException
     */
    public void appendCodeBlock() throws IllegalActionException {
        _appendCodeBlock(_cgHelper.getClass().toString(), ""); 
    }

    private void _appendCodeBlock(String className, String blockName) throws IllegalActionException {
        try {
            BufferedReader br = FileUtilities.openForReading(_filePath + className+"txt", null, null);

            StringBuffer codeBlock = _fetchCodeBlock(br, blockName);     // fetch the code within the file            
            append(codeBlock);                                              // append to stream            

        } catch (IOException e) {
            throw new IllegalActionException ("Cannot open file: " + className + ".c");
        } 
    }
    
    /**
     * @param file
     * @param name
     * Given the name of the CodeBlock,
	 * @return the string representation of the code within the file
     * @throws IOException
     * @throws IllegalActionException
	 */
	private static StringBuffer _fetchCodeBlock(BufferedReader br, String blockName) throws IOException, IllegalActionException {        
        StringBuffer codeInFile = new StringBuffer();
        String s = br.readLine();
        while (s != null) {
            codeInFile.append(s);
            s = br.readLine();
        }
        
        String startHeader = _startCodeBlock1 + blockName + _startCodeBlock2;
        int startIndex = codeInFile.indexOf(startHeader) + startHeader.length();
        int endIndex = codeInFile.indexOf(_endCodeBlock1 + blockName + _endCodeBlock2);
        
        if (endIndex <= startIndex)
        	throw new IllegalActionException ("Cannot find codeBlock (" + blockName + ")");
        
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
    private static String _startCodeBlock1 = "/****";
    private static String _startCodeBlock2 = "****";
    
        
    /**
     * The symbol style that indicates the end of a code block
     * (ex. _endCodeBlock1 + codeBlockName + _endCodeBlock2)
     */
    private static String _endCodeBlock1 = "****";
    private static String _endCodeBlock2 = "****/";
    
    
    
    // FIXME: where's the file path for the .c files??
    private static String _filePath = "C:\\Program Files\\eclipse\\workspace\\ptII\\ptolemy\\codegen\\c\\actor\\lib\\";
            // "xxxxxxCLASSPATHxxxxxx/" + /ptII/ptolemy/codegen/c/actor/lib/"; //"xxxxxxCLASSPATHxxxxxx/";

    private static String _appendCodeBlock_testing(String className, String blockName) throws IllegalActionException, IOException {
        //try {
            BufferedReader br = FileUtilities.openForReading(_filePath + className+".c", null, null);
            StringBuffer codeBlock = _fetchCodeBlock(br, blockName);     // fetch the code within the file            
            return codeBlock.toString();
        //} catch (IOException e) {
        //    throw new IllegalActionException ("Cannot open file: " + className + ".c");
        //} 
    }

    public static void main(String[] arg) throws IOException, IllegalActionException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    	String className, blockName;
        System.out.println("testing-----------------");
        System.out.println("please input class name: ");
        //className = in.readLine();
        System.out.println("please input code block name: ");
        //blockName = in.readLine();
        
        System.out.println(CodeStream._appendCodeBlock_testing("Accumulator", "codeBlock"));
    }
}
