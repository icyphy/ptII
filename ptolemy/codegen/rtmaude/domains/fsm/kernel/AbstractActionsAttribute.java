/* RTMaude Code generator helper class for the AbstractActionsAttribute class.

 Copyright (c) 2009-2011 The Regents of the University of California.
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
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AbstractActionsAttribute

/**
 * Generate RTMaude code for an AbstractActionsAttribute in DE domain.
 *
 * @see ptolemy.domains.fsm.kernel.AbstractActionsAttribute
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class AbstractActionsAttribute extends RTMaudeAdaptor {
    /** Construct the code generator adapter associated
     * with the given AbstractActionsAttribute.
     *  @param component The associated component.
     */
    public AbstractActionsAttribute(
            ptolemy.domains.fsm.kernel.AbstractActionsAttribute component) {
        super(component);
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor#generateTermCode()
     */
    public String generateTermCode() throws IllegalActionException {
        final ptolemy.domains.fsm.kernel.AbstractActionsAttribute aa = (ptolemy.domains.fsm.kernel.AbstractActionsAttribute) getComponent();
        final ParseTreeCodeGenerator pcg = getParseTreeCodeGenerator();

        return new ListTerm<String>("emptyMap", " ;" + _eol,
                aa.getDestinationNameList()) {
            public String item(String aan) throws IllegalActionException {
                pcg.evaluateParseTree(aa.getParseTree(aan), null);
                return _generateBlockCode("mapBlock", aan,
                        pcg.generateFireCode());
            }
        }.generateCode();
    }
}
