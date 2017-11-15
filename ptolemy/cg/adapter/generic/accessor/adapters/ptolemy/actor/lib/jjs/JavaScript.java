/* An adapter class for ptolemy.actor.lib.jjs.JavaScript

 Copyright (c) 2016-2017 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.accessor.adapters.ptolemy.actor.lib.jjs;

import java.util.List;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.cg.kernel.generic.accessor.AccessorCodeGeneratorAdapter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// JavaScript

/**
 An adapter class for ptolemy.actor.lib.jjs.JavaScript.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class JavaScript extends AccessorCodeGeneratorAdapter {

    /**
     *  Construct the JavaScript adapter.
     *  @param actor the associated actor
     */
    public JavaScript(ptolemy.actor.lib.jjs.JavaScript actor) {
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

        ptolemy.actor.lib.jjs.JavaScript actor = (ptolemy.actor.lib.jjs.JavaScript) getComponent();

        String name = StringUtilities.sanitizeName(actor.getName());

        code.append(_eol + _INDENT1 + "// Start: " + getComponent().getName()
                + ": ptolemy/cg/adapter/generic/accessor/adapters/ptolemy/actor/lib/jjs/JavaScript.java" + _eol);

        // See org/terraswarm/accessor/accessors/web/hosts/common/commonHost.js
        code.append(
                _INDENT1 + "// FIXME: See instantiate() in accessors/web/hosts/common/commonHost.js" + _eol
                + _INDENT1 + "// We probably need to do something with the bindings." + _eol
                + _INDENT1 + "var " + name + " = this.instantiateFromCode('"+ name
                + "', unescape('"
                 + actor.escapeForJavaScript(actor.script.getExpression())
                //+ actor.script.getExpression().replace("\"", "\\\"").replace("'", "\\\'").replace("\n", "\\n")
                + "'));"
                + _eol);

        code.append(_generateJavaScriptParameters(actor));
        return code.toString();
    }

    /** Given a NamedObj, generate JavaScript for any parameters.
     *  @param namedObj The object
     *  @return The JavaScript definitions for the parameters.
     *  @exception IllegalActionException If there is a problem getting the parameters.
     */
    protected StringBuffer _generateJavaScriptParameters(NamedObj namedObj)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        List<Parameter> parameters = namedObj.attributeList(Parameter.class);
        for (Parameter parameter : parameters) {
            // Skip the default parameters in JSAccessor and emit code for the other parameters.
            if (!parameter.getName().equals("accessorSource")
                    && !parameter.getName().equals("checkoutOrUpdateAccessorsRepository")
                    && !parameter.getName().equals("script")) {
                code.append(_INDENT1);
                String setter = "setParameter";

                // For PortParameters, use setDefault(), not setParameter().  See
                // https://accessors.org/wiki/VersionCurrent/Input#SettingDefaultInput
                if (parameter instanceof PortParameter) {
                    setter = "setDefault";
                }

                code.append(StringUtilities.sanitizeName(namedObj.getName()) + "." + setter + "('" + parameter.getName() + "', "
                        + targetExpression(parameter)
                        + ");" + _eol);
            }
        }
        return code;
    }

}


