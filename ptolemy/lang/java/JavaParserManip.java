/*          
Methods to aid in parsing Java source code.

Copyright (c) 1998-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/


package ptolemy.lang.java;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ptolemy.lang.StringManip;
import ptolemy.lang.ApplicationUtility;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

/**
 *  Methods to aid in parsing of Java source code.
 *
 *  @author Jeff Tsay
 */
public class JavaParserManip implements JavaStaticSemanticConstants {

    // private constructor prevent instantiation of this class
    private JavaParserManip() {}

    /** Parse the file, doing no static resolution whatsoever. The filename may be
     *  relative or absolute. If a source file with the same canonical filename 
     *  has already been parsed, return the previous node. 
     */
    public static CompileUnitNode parse(String filename, boolean debug) {    
        return parse(new File(filename), debug);
    }

    /** Parse the file, doing no static resolution whatsoever. If a source file 
     *  with the same canonical filename has already been parsed, return the 
     *  previous node. 
     */
    public static CompileUnitNode parse(File file, boolean debug) {        
        try {
          return parseCanonical(file.getCanonicalPath(), debug);
        } catch (IOException ioe) {
          ApplicationUtility.error(ioe.toString());
        } 
        return null; 
    }

    /** Parse the file with the given canonical filename, doing no static 
     *  resolution whatsoever. If a source file with the same canonical 
     *  filename has already been parsed, return the previous node. 
     */
    public static CompileUnitNode parseCanonical(String filename, boolean debug) {
        CompileUnitNode loadedAST = null;
    
        loadedAST = (CompileUnitNode) allParsedMap.get(filename);           
                                  
        if (loadedAST != null) {
           return loadedAST;
        }
        
        JavaParser p = new JavaParser();

        try {
          p.init(filename);
        } catch (Exception e) {
          ApplicationUtility.error("error opening " + filename + " : " + e);
        }

        p.yydebug = debug;
        p.yyparse();
        
        loadedAST = p.getAST();
        
        // get the part of the filename before the last '.'
        filename = StringManip.partBeforeLast(filename, '.');
        
        loadedAST.setProperty(IDENT_KEY, filename); 
        
        allParsedMap.put(filename, loadedAST);        

        return loadedAST;
    }
    
    /** A Map containing values of CompileUnitNodes that have been parsed,
     *  including nodes that have undergone later stages of static resolution,
     *  indexed by the canonical filename of the source file.
     */
    public static final Map allParsedMap = new HashMap();    
}
