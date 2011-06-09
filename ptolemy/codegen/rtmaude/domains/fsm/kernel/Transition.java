/* RTMaude Code generator helper class for the Transition class.

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
package ptolemy.codegen.rtmaude.domains.fsm.kernel;

import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////Director

/**
 * Generate RTMaude code for a Transition in DE domain.
 *
 * @see ptolemy.domains.fsm.kernel.Transition
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class Transition extends RTMaudeAdaptor {
    /** Construct the code generator adapter associated
     *  with the given Transition.
     *  @param component The associated component.
     */
    public Transition(ptolemy.domains.fsm.kernel.Transition component) {
        super(component);
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor#generateTermCode()
     */
    public String generateTermCode() throws IllegalActionException {
        ptolemy.domains.fsm.kernel.Transition t = (ptolemy.domains.fsm.kernel.Transition) getComponent();
        ParseTreeCodeGenerator pcg = getParseTreeCodeGenerator();

        ASTPtRootNode pt = (new PtParser()).generateParseTree(t
                .getGuardExpression());
        pcg.evaluateParseTree(pt, null);
        String guard = pcg.generateFireCode();

        String set = ((RTMaudeAdaptor) _getHelper(t.setActions))
                .generateTermCode();
        String out = ((RTMaudeAdaptor) _getHelper(t.outputActions))
                .generateTermCode();

        return _generateBlockCode(defaultTermBlock, t.sourceState().getName(),
                t.destinationState().getName(), guard, out, set);
    }
}
