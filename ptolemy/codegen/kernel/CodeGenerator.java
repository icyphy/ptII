/* Base class for code generators.

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

import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/** FIXME
 * 
 *  @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Rachel Zhou
 *  @version $Id$
 *  @since Ptolemy II 5.0
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {

	/** Create a new instance of the C code generator.
	 *  @param container The container.
	 *  @param name The name.
	 *  @throws IllegalActionException
	 *  @throws NameDuplicationException
	 */
	public CodeGenerator(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
        
        codeDirectory = new FileParameter(this, "codeDirectory");
        codeDirectory.setExpression("$HOME/codegen");
        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);
        
        generatorPackage = new StringParameter(this, "generatorPackage");
        generatorPackage.setExpression("ptolemy.codegen.c");
        
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");
        
        // FIXME: We may not want this GUI dependency here...
        // This attibute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
	}
    
    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////
    
    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;
    
    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to 
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Generate the body code that lies between initialize and wrapup.
     *  In this base class, nothing is generated.
     */
    public void generateBodyCode(StringBuffer code) throws IllegalActionException {
    }

    /** Generate code.  This is the main entry point.
     *  FIXME: more
     */
    public void generateCode(StringBuffer code) throws IllegalActionException {
        generateInitializeCode(code);
        generateBodyCode(code);
        generateWrapupCode(code);
        
        // FIXME: Write the code to standard out for now.
        // The code should be written to a file in the
        // codeDirectory instead.
        System.out.println(code.toString());
    }
    
    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/". Subclasses may override this
     *  produce comments that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
    	return "/* " + comment + " */\n";
    }

    /** Generate the code associated with initialization of the
     *  container composite actor. This is created by stringing
     *  together the intialization code for actors contained by
     *  the container of this attribute (in arbitrary order).
     */
    public void generateInitializeCode(StringBuffer code) 
            throws IllegalActionException {
        code.append(comment(
                "Initialize " + getContainer().getFullName()));
        // FIXME: do the work.
    }

    /** Generate into the specified code stream the code associated
     *  with wrapping up the container composite actor. This is
     *  created by stringing together the wrapup code for the actors
     *  contained by the container of this attribute (in arbitrary
     *  order).
     *  @param code The code stream into which to generate the code.
     */
    public void generateWrapupCode(StringBuffer code) {
        code.append(comment(
                "Wrapup " + getContainer().getFullName()));
        // FIXME: Do the work.
    }
    
    /** Return the associated component, which is always the container.
     *  @return The component for which this is a helper to generate code.
     */
    public NamedObj getComponent() {
    	return getContainer();
    }

    // FIXME: Override setContainer to ensure that the container is a CompositeEntity.
}
