/* RTMaude Code generator helper class for the Variable class.

 Copyright (c) 2009-2010 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.data.expr;

import java.util.List;

import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////PropertyParameter

/**
 * Generate RTMaude code for a Variable in DE domain.
 *
 * @see ptolemy.data.expr.Variable
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class Variable extends RTMaudeAdaptor {

    /**
     * Constructs the code generator adapter associated
     * with the Variable.
     * @param component the associated Variable object
     */
    public Variable(ptolemy.data.expr.Variable component) {
        super(component);
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.Entity#getInfo(java.lang.String, java.util.List)
     */
    protected String getInfo(String name, List<String> parameters)
            throws IllegalActionException {
        if (name.equals("evaluatedValue")) {
            return this.getTranslatedExpression(
                    ((ptolemy.data.expr.Variable) getComponent()).getValueAsString());
        }

        return super.getInfo(name, parameters);
    }

}
