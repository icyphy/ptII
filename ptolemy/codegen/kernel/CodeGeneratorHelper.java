/* Base class for code generator helper.

Copyright (c) 2005 The Regents of the University of California.
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

*/

package ptolemy.codegen.kernel;

import java.util.StringTokenizer;

import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;


//////////////////////////////////////////////////////////////////////////
//// CodeGeneratorHelper
/**
   Base class for code generator helper.

   @author Gang Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class CodeGeneratorHelper implements ActorCodeGenerator {
    
    /** FIXME
     */
    public CodeGeneratorHelper(NamedObj component) {
    	_component = component;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME
     */
    public void generateFireCode(StringBuffer stream) throws IllegalActionException {
    }

    /** FIXME
     */
    public void generateInitializeCode(StringBuffer code) throws IllegalActionException {
    }

    /** FIXME
     */
    public void generateWrapupCode(StringBuffer code) throws IllegalActionException {
    }

    /** FIXME
     */
    public NamedObj getComponent() {
        return _component;
    }
    
    /** Return the value of the specified parameter of the
     *  associated actor.
     *  @param parameterName The name of the parameter.
     *  @return The value as a string.
     *  @exception IllegalActionException If the parameter
     *   does not exist or does not have a value.
     */
    public String getParameterValue(String name) 
            throws IllegalActionException {
    	Attribute attribute = _component.getAttribute(name);
        if (attribute == null) {
        	throw new IllegalActionException(_component,
                    "No attribute named: " + name);
        }
        if (attribute instanceof Variable) {
            // FIXME: need to ensure that the returned string
            // is correct syntax for the target language.
        	return ((Variable)attribute).getToken().toString();
        } else if (attribute instanceof Settable) {
        	return ((Settable)attribute).getExpression();
        }
        // FIXME: Are there any other values that a
        // parameter might have?
        throw new IllegalActionException(_component,
                "Attribute does not have a value: " + name);
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor.
     *  @param parameterName The name of the parameter or port
     *  @return The reference to that parameter or port (a variable
     *   name, for example).
     *  @exception IllegalActionException If the parameter or port
     *   does not exist or does not have a value.
     */
    public String getReference(String name) 
            throws IllegalActionException {
    	// FIXME: Implement this. The returned name should be that
        // of a unique variable that represents the parameter
        // or the port value. First deal with ports: a
        // declaration should be created for each connection
        // and produced in the initialization code. E.g.,
        // the name fullName_inputPortName_channelNumber
        // could be used for a variable representing the connection.
        // This method needs to generate this name given
        // the input port name.
        return name;
    }

    /** Process the specified code, replacing macros with their
     *  values.
     *  @param code The code to process.
     *  @return The processed code.
     */
    public String processCode(String code) throws IllegalActionException {
        
        StringBuffer result = new StringBuffer();
        int currentPos = code.indexOf("$");
        if (currentPos < 0) {
            // No "$" in the string
            return code.toString();
        }
        
        result.append(code.substring(0, currentPos));
        while (currentPos < code.length()) {
            int nextPos = code.indexOf("$", currentPos + 1);
            if (nextPos < 0) {
                //currentPos is the last "$"
                nextPos = code.length();
            }
            String subcode = code.substring(currentPos, nextPos);
            boolean foundIt = false;
            StringTokenizer tokenizer = new StringTokenizer(subcode, "()", true);
            if (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.equals("$ref") && tokenizer.hasMoreTokens()) {
                    String openParen = tokenizer.nextToken();
                    if (openParen.equals("(") && tokenizer.hasMoreTokens()) {
                        String name = tokenizer.nextToken();
                        if (name.equals(")")) {
                            foundIt = true;
                            while (tokenizer.hasMoreTokens()) {
                                result.append(tokenizer.nextToken());  
                            }
                        } else if (tokenizer.hasMoreTokens()) {
                            String closeParen = tokenizer.nextToken();
                            if (closeParen.equals(")")) {
                                foundIt = true;
                                name = name.trim();
                                if (name != "") {
                                    result.append(getReference(name));
                                }
                                // attach the rest subcode.
                                while (tokenizer.hasMoreTokens()) {
                                    result.append(tokenizer.nextToken());
                                }
                            }
                        }
                    }
                } else if (token.equals("$val") && tokenizer.hasMoreTokens()) {
                    String openParen = tokenizer.nextToken();
                    if (openParen.equals("(") && tokenizer.hasMoreTokens()) {
                        String macroName = tokenizer.nextToken();
                        if (macroName.equals(")")) {
                            foundIt = true;
                            while (tokenizer.hasMoreTokens()) {
                                result.append(tokenizer.nextToken());  
                            }
                        }
                        else if (tokenizer.hasMoreTokens()) {
                            String closeParen = tokenizer.nextToken();
                            if (closeParen.equals(")")) {
                                foundIt = true;
                                macroName = macroName.trim();
                                if (macroName != "")  {
                                    result.append(getParameterValue(macroName));
                                }
                                // attach the rest subcode.
                                while (tokenizer.hasMoreTokens()) {
                                    result.append(tokenizer.nextToken());
                                }
                            }
                        }
                    }
                }
            }
            if (!foundIt) {
                result.append(subcode);
            }
            currentPos = nextPos;
        }
        
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The associated component. */
    private NamedObj _component;
}
