/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.lib.hoc.MultiCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SingleRuleTransformer

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class SingleRuleTransformer extends MultiCompositeActor {

    public SingleRuleTransformer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public SingleRuleTransformer(Workspace workspace)
    throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    public Map<String, String> getCorrespondence() {
        return _correspondence;
    }

    public CompositeActorMatcher getPattern() {
        return _pattern;
    }

    public CompositeActorMatcher getReplacement() {
        return _replacement;
    }

    public StringAttribute _correspondenceAttribute;
    
    ///////////////////////////////////////////////////////////////////
    ////                          private variables                ////

    protected void _init()
    throws IllegalActionException, NameDuplicationException {
        // Create the default refinement.
        _pattern = new CompositeActorMatcher(this, "Pattern");
        _replacement = new CompositeActorMatcher(this, "Replacement");
        _correspondence = new HashMap<String, String>();
        _correspondenceAttribute =
            new CorrespondenceAttribute("correspondence");
        _correspondenceAttribute.setExpression("");
    }

    private Map<String, String> _correspondence;

    private CompositeActorMatcher _pattern;

    private CompositeActorMatcher _replacement;

    private static final long serialVersionUID = -456353254196458127L;

    private class CorrespondenceAttribute extends StringAttribute {

        public String getExpression() {
            StringBuffer buffer = new StringBuffer();
            for (String patternObject : _correspondence.keySet()) {
                String replacementObject = _correspondence.get(patternObject);
                String correspondence =
                    patternObject + "<..>" + replacementObject;
                if (buffer.length() > 0) {
                    buffer.append("<..>");
                }
                buffer.append(correspondence);
            }
            return buffer.toString();
        }

        public void setExpression(String expression)
        throws IllegalActionException {
            String[] correspondences = expression.split("<\\.\\.>");
            _correspondence.clear();
            for (int i = 0; i < correspondences.length; i += 2) {
                if (i + 1 < correspondences.length) {
                    _correspondence.put(correspondences[i],
                            correspondences[i + 1]);
                }
            }
            super.setExpression(expression);
        }

        CorrespondenceAttribute(String name) throws IllegalActionException,
        NameDuplicationException {
            super(SingleRuleTransformer.this, name);
        }

        private static final long serialVersionUID = 1805180151377867487L;
    }

}
