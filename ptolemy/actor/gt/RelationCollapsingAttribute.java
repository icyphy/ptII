/*

@Copyright (c) 2007-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RelationCollapsingAttribute extends GTAttribute {

    public RelationCollapsingAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        parameter = new Parameter(this, "relationCollapsing");
        parameter.setTypeEquals(BaseType.BOOLEAN);
        parameter.setToken(BooleanToken.getInstance(!DEFAULT));
    }

    public void attributeChanged(Attribute attribute) {
        if (getContainer() instanceof EntityLibrary) {
            return;
        }

        if (attribute == parameter) {
            try {
                if (((BooleanToken) parameter.getToken())
                        .equals(BooleanToken.TRUE)) {
                    _setIconDescription(_COLLAPSING_ICON);
                } else {
                    _setIconDescription(_NON_COLLAPSING_ICON);
                }
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e,
                        "Cannot get token from the attribute.");
            }
        }
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            _checkContainerClass(container, Pattern.class, true);
            _checkUniqueness(container);
        }
    }

    public static final boolean DEFAULT = false;

    public Parameter parameter;

    private static final String _COLLAPSING_ICON = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"94\" height=\"32\""
            + "  style=\"fill:#00FFFF\"/>"
            + "<polygon points=\"14,5 8,11 14,17 20,11\" style=\"fill:#000000\"/>"
            + "<line x1=\"21\" y1=\"11\" x2=\"24\" y2=\"11\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"24\" y1=\"11\" x2=\"24\" y2=\"21\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"24\" y1=\"21\" x2=\"27\" y2=\"21\""
            + "  style=\"stroke:#000000\"/>"
            + "<polygon points=\"34,15 28,21 34,27 40,21\" style=\"fill:#000000\"/>"
            + "<line x1=\"47\" y1=\"14\" x2=\"60\" y2=\"14\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"47\" y1=\"18\" x2=\"60\" y2=\"18\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"58\" y1=\"11\" x2=\"63\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"58\" y1=\"21\" x2=\"63\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<polygon points=\"77,10 71,16 77,22 83,16\" style=\"fill:#000000\"/>"
            + "</svg>";

    private static final String _NON_COLLAPSING_ICON = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"94\" height=\"32\""
            + "  style=\"fill:#00FFFF\"/>"
            + "<polygon points=\"14,5 8,11 14,17 20,11\" style=\"fill:#000000\"/>"
            + "<line x1=\"21\" y1=\"11\" x2=\"24\" y2=\"11\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"24\" y1=\"11\" x2=\"24\" y2=\"21\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"24\" y1=\"21\" x2=\"27\" y2=\"21\""
            + "  style=\"stroke:#000000\"/>"
            + "<polygon points=\"34,15 28,21 34,27 40,21\" style=\"fill:#000000\"/>"
            + "<line x1=\"47\" y1=\"14\" x2=\"60\" y2=\"14\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"47\" y1=\"18\" x2=\"60\" y2=\"18\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"58\" y1=\"11\" x2=\"63\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"58\" y1=\"21\" x2=\"63\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<polygon points=\"77,10 71,16 77,22 83,16\" style=\"fill:#000000\"/>"
            + "<line x1=\"49\" y1=\"9\" x2=\"57\" y2=\"23\""
            + "  style=\"stroke:#C00000; stroke-width:3\"/>" + "</svg>";

}
