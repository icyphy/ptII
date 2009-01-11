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
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class HierarchyFlatteningAttribute extends ParameterAttribute {

    /**
     * @param container
     * @param name
     * @exception NameDuplicationException
     * @exception IllegalActionException
     */
    public HierarchyFlatteningAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void attributeChanged(Attribute attribute) {
        if (getContainer() instanceof EntityLibrary) {
            return;
        }

        if (attribute == parameter) {
            try {
                if (((BooleanToken) parameter.getToken())
                        .equals(BooleanToken.TRUE)) {
                    _setIconDescription(_FLATTENING_ICON);
                } else {
                    _setIconDescription(_NOT_FLATTENING_ICON);
                }
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e,
                        "Cannot get token from the attribute.");
            }
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HierarchyFlatteningAttribute newObject =
            (HierarchyFlatteningAttribute) super.clone(workspace);
        newObject.parameter = (Parameter) newObject.getAttribute(
                "hierarchyFlattening");
        return newObject;
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

    protected void _initParameter() throws IllegalActionException,
            NameDuplicationException {
        parameter = new Parameter(this, "hierarchyFlattening");
        parameter.setTypeEquals(BaseType.BOOLEAN);
        parameter.setToken(BooleanToken.getInstance(!DEFAULT));
    }

    private static final String _FLATTENING_ICON = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"94\" height=\"32\""
            + "  style=\"fill:#00FFFF\"/>"
            + "<rect x=\"5\" y=\"5\" width=\"30\" height=\"22\""
            + "  style=\"fill:#FF0000\"/>"
            + "<rect x=\"7\" y=\"7\" width=\"26\" height=\"18\""
            + "  style=\"fill:#FFFFFF\"/>"
            + "<text x=\"8\" y=\"17\" style=\"font-size:16; " +
                    "font-family:SansSerif; fill:#A00000\">..</text>"
            + "<rect x=\"18\" y=\"11\" width=\"11\" height=\"10\""
            + "  style=\"fill:#FF0000\"/>"
            + "<rect x=\"20\" y=\"13\" width=\"7\" height=\"6\""
            + "  style=\"fill:#FFFFFF\"/>"
            + "<line x1=\"39\" y1=\"14\" x2=\"52\" y2=\"14\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"39\" y1=\"18\" x2=\"52\" y2=\"18\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"11\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"21\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<rect x=\"59\" y=\"5\" width=\"30\" height=\"22\""
            + "  style=\"fill:#FF0000\"/>"
            + "<rect x=\"61\" y=\"7\" width=\"26\" height=\"18\""
            + "  style=\"fill:#FFFFFF\"/>" + "</svg>";

    private static final String _NOT_FLATTENING_ICON = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"94\" height=\"32\""
            + "  style=\"fill:#00FFFF\"/>"
            + "<rect x=\"5\" y=\"5\" width=\"30\" height=\"22\""
            + "  style=\"fill:#FF0000\"/>"
            + "<rect x=\"7\" y=\"7\" width=\"26\" height=\"18\""
            + "  style=\"fill:#FFFFFF\"/>"
            + "<text x=\"8\" y=\"17\" style=\"font-size:16; " +
                    "font-family:SansSerif; fill:#A00000\">..</text>"
            + "<rect x=\"18\" y=\"11\" width=\"11\" height=\"10\""
            + "  style=\"fill:#FF0000\"/>"
            + "<rect x=\"20\" y=\"13\" width=\"7\" height=\"6\""
            + "  style=\"fill:#FFFFFF\"/>"
            + "<line x1=\"39\" y1=\"14\" x2=\"52\" y2=\"14\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"39\" y1=\"18\" x2=\"52\" y2=\"18\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"11\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"21\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<rect x=\"59\" y=\"5\" width=\"30\" height=\"22\""
            + "  style=\"fill:#FF0000\"/>"
            + "<rect x=\"61\" y=\"7\" width=\"26\" height=\"18\""
            + "  style=\"fill:#FFFFFF\"/>"
            + "<line x1=\"41\" y1=\"9\" x2=\"49\" y2=\"23\""
            + "  style=\"stroke:#C00000; stroke-width:3\"/>" + "</svg>";

}
