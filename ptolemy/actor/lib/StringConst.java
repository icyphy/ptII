/* A constant source with a string value.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// StringConst

/**
   Produce a constant output of type string. This is similar to the base
   class Const, which can also produce a string output, but this has the
   added convenience that the string can be specified without the enclosing
   double quotes. Moreover, the string can include references to parameters
   within scope using the $name syntax. The value of the
   output is that of the token contained by the <i>value</i> parameter,
   which by default is an empty string.

   @see ptolemy.data.expr.Variable
   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (eal)
   @Pt.AcceptedRating Red (bilung)
*/
public class StringConst extends Const {
    /** Construct a constant source with the given container and name.
     *  Create the <i>value</i> parameter, initialize its value.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringConst(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        value.setExpression("");
        value.setStringMode(true);

        // Set the type constraint.
        output.setTypeEquals(BaseType.STRING);

        _attachText("_iconDescription",
            "<svg>\n" + "<rect x=\"0\" y=\"0\" "
            + "width=\"60\" height=\"20\" " + "style=\"fill:lightBlue\"/>\n"
            + "</svg>\n");
    }
}
