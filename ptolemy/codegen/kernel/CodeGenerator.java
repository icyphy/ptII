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
public class CodeGenerator extends Attribute {

	/** Create a new instance of the C code generator.
	 *  @param container The container.
	 *  @param name The name.
	 *  @throws IllegalActionException
	 *  @throws NameDuplicationException
	 */
	public CodeGenerator(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
        
        generatorPackage = new StringParameter(this, "generatorPackage");
        generatorPackage.setExpression("ptolemy.codegen.c");
        
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");
	}
    
    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////
    
    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to 
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    // FIXME: Override setContainer to ensure that the container is a CompositeEntity.
}
