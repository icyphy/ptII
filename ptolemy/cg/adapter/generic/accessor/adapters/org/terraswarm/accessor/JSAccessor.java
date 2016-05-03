/* An adapter class for org.terraswarm.accessor.JSAccessor

 Copyright (c) 2016 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.accessor.adapters.org.terraswarm.accessor;

import java.util.List;

import ptolemy.cg.kernel.generic.accessor.AccessorCodeGeneratorAdapter;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// JSAccessor

/**
 An adapter class for org.terraswarm.accessor.JSAccessor.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class JSAccessor
extends
AccessorCodeGeneratorAdapter {
    /**
     *  Construct the JSAccessor adapter.
     *  @param actor the associated actor
     */
    public JSAccessor(org.terraswarm.accessor.JSAccessor actor) {
        super(actor);
    }

    /** Generate Accessor code.
     *  @return The generated Accessor.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating Accessor for the director.
     */
    @Override
    public String generateAccessor() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        org.terraswarm.accessor.JSAccessor actor = (org.terraswarm.accessor.JSAccessor) getComponent();

        String name = StringUtilities.sanitizeName(actor.getName());

        code.append(_eol + _INDENT1 + "// Start: " + getComponent().getName()
                + ": ptolemy/cg/adapter/generic/accessor/adapters/org/terraswarm/accessor/JSAccessor.java" + _eol);


        code.append(_INDENT1 + "var " + name + " = this.instantiate('" + name
                + "', '"
                + actor.accessorSource.getExpression()
                // Replace both https and http.  http is used in older accessors.
                .replace("https://www.terraswarm.org/accessors/", "")
                .replace("http://www.terraswarm.org/accessors/", "")
                + "');"
                + _eol);

        List<Parameter> parameters = actor.attributeList(Parameter.class);
        for (Parameter parameter : parameters) {
            // Skip the default parameters in JSAccessor and emit code for the other parameters.
            if (!parameter.getName().equals("accessorSource")
                    && !parameter.getName().equals("checkoutOrUpdateAccessorsRepository")
                    && !parameter.getName().equals("script")) {
                code.append(_INDENT1);
                if (parameter instanceof PortParameter) {
                    code.append("// ");
                }

                code.append(actor.getName() + ".setParameter('" + parameter.getName() + "', "
                        + targetExpression(parameter)
                        + ");" + _eol);
            }
        }
        return code.toString();
    }
}
