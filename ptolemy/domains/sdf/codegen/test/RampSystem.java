/* Really simple Ramp and printer demo for use with Codegen

Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.domains.sdf.codegen.test;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

/** A very simple demo that connects an integer Ramp source to
 *  a Printer.
 */
public class RampSystem extends TypedCompositeActor {

    public RampSystem(Workspace w) throws IllegalActionException {
        super(w);

        try {
            setDirector(new SDFDirector(this, "director"));
            //Const ramp = new Const(this, "ramp");
            Ramp ramp = new Ramp(this, "ramp");
            FileWriter fileWriter = new FileWriter(this, "fileWriter");
            connect(ramp.output, fileWriter.input);

            // A hack to get code generation to work
            fileWriter.input.setTypeEquals(BaseType.INT);

        } catch (NameDuplicationException e) {
            throw new RuntimeException(e.toString());
        }
    }
}

If we don't set the type, then 
ptolemy/codegen/SpecializeTokenVisitor.specializeTokens() prints
a warning message:

            if ((value == PtolemyTypeIdentifier.DUMMY_LOWER_BOUND) ||
                    (value == PtolemyTypeIdentifier.TOKEN_DECL) ||
                    (value == PtolemyTypeIdentifier.SCALAR_TOKEN_DECL) ||
                    (value == PtolemyTypeIdentifier.MATRIX_TOKEN_DECL)) {
                System.err.println("Warning: SpecializeTokenVisitor" 
				   + ".specializeTokens(): "
				   + "could not solve for specific "
                                   + "token type for declaration '"
				   + typedDecl.getName()
				   + "' in "
				   + actorInfo.actor.getName()
				   + ".\n term = " + value 
				   + " which is unsupported. \n" 
				   + "Try setting the type with something like"
				   + "\n'fileWriter.input.setTypeEquals"
				   + "(BaseType.INT);'");

                // Replace the declaration type with "Token" as an indication
                // for later passes.
                //declToTokenTypeMap.put(typedDecl,
                //        PtolemyTypeIdentifier.TOKEN_TYPE.clone());

		// FIXME: This is totally wrong
                System.err.println("Warning: SpecializeTokenVisitor" 
				   + ".specializeTokens(): defaulting to " 
				   + "integer");
                declToTokenTypeMap.put(typedDecl,
                        PtolemyTypeIdentifier.INT_TOKEN_TYPE.clone());


I hacked in setting the type to INT_TOKEN_TYPE.
Without this hack

public boolean postfire() {
try {
int width = 1;
for (int i = 0; i < width; i++) {
    if (i > 0) this._writer.write("\t");

    if (true) {
Object token = null;
this._writer.write("bad token");
    }

}

this._writer.write("\n");
return super.postfire();
    }
