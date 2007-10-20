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

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gt.data.Pair;
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

    public List<Pair<String, String>> getCorrespondence() {
        return _correspondence;
    }

    public CompositeActorMatcher getPattern() {
        return _pattern;
    }

    public CompositeActorMatcher getReplacement() {
        return _replacement;
    }

    public CorrespondenceAttribute correspondenceAttribute;

    public class CorrespondenceAttribute extends StringAttribute {

        public String getExpression() {
            return getExpression(_correspondence);
        }

        public String getExpression(List<Pair<String, String>> correspondence) {
            StringBuffer buffer = new StringBuffer();
            for (Pair<String, String> pair : correspondence) {
                if (buffer.length() > 0) {
                    buffer.append(SEPARATOR);
                }
                buffer.append(pair.getFirst() + SEPARATOR + pair.getSecond());
            }
            return buffer.toString();
        }

        public void setExpression(String expression)
        throws IllegalActionException {
            String[] correspondences = expression.split(SEPARATOR_PATTERN, -1);
            _correspondence.clear();
            for (int i = 0; i < correspondences.length; i += 2) {
                if (i + 1 < correspondences.length) {
                    _correspondence.add(new Pair<String, String>(
                            correspondences[i], correspondences[i + 1]));
                }
            }
            super.setExpression(expression);
        }

        public static final String SEPARATOR = "<..>";

        public static final String SEPARATOR_PATTERN = "<\\.\\.>";

        CorrespondenceAttribute(String name) throws IllegalActionException,
        NameDuplicationException {
            super(SingleRuleTransformer.this, name);
        }

        private static final long serialVersionUID = 1805180151377867487L;
    }

    protected void _init()
    throws IllegalActionException, NameDuplicationException {
        // Create the default refinement.
        _pattern = new CompositeActorMatcher(this, "Pattern");
        _replacement = new CompositeActorMatcher(this, "Replacement");
        _correspondence = new LinkedList<Pair<String, String>>();
        correspondenceAttribute =
            new CorrespondenceAttribute("correspondence");
        correspondenceAttribute.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private variables                ////

    private List<Pair<String, String>> _correspondence;

    private CompositeActorMatcher _pattern;

    private CompositeActorMatcher _replacement;

    private static final long serialVersionUID = -456353254196458127L;

}
