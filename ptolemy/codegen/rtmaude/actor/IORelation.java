/* RTMaude Code generator helper class for the IORelation class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor;

import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// IORelation

/**
 * Generate RTMaude code for an IORelationt in DE domain.
 *
 * @see ptolemy.actor.IORelation
 * @author Kyungmin Bae
@version $Id$
@since Ptolemy II 7.1
 * @version $Id$
 * @Pt.ProposedRating Red (kquine)
 *
 */
public class IORelation extends RTMaudeAdaptor {

    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public IORelation(ptolemy.actor.IORelation component) {
        super(component);
    }

    public String generateTermCode() throws IllegalActionException {
        final ptolemy.actor.IORelation r = (ptolemy.actor.IORelation) getComponent();
        StringBuffer rec = new StringBuffer();

        for (ptolemy.actor.IOPort pi : r.linkedSourcePortList()) {
            if (rec.length() > 0) {
                rec.append("\n");
            }
            rec.append(_generateBlockCode(this.defaultTermBlock,
                    generateEPortId(r.getContainer(), pi),
                    new ListTerm<ptolemy.actor.IOPort>("noPort", " ; ", r
                            .linkedDestinationPortList()) {
                        public String item(ptolemy.actor.IOPort port)
                                throws IllegalActionException {
                            return generateEPortId(r.getContainer(), port);
                        }
                    }.generateCode()));
        }
        return rec.toString();
    }

    private String generateEPortId(NamedObj container, ptolemy.actor.IOPort port)
            throws IllegalActionException {
        return _generateBlockCode("scopeBlock", generateActorIdforPort(
                container, port), port.getName());
    }

    private String generateActorIdforPort(NamedObj container,
            ptolemy.actor.IOPort port) {
        String actorId;
        if (container.equals(port.getContainer().getContainer())) {
            actorId = "'" + port.getContainer().getName();
        } else {
            actorId = "parent";
        }
        return actorId;
    }

}
