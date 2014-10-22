/* An attribute to specify that multiple connected relations in the model may be
   considered as one in pattern matching.

@Copyright (c) 2007-2014 The Regents of the University of California.
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

import java.util.Collection;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

/**
 An attribute to specify that multiple connected relations in the model may be
 considered as one in pattern matching.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RelationCollapsingAttribute extends Parameter implements
        GTAttribute {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public RelationCollapsingAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        setTypeEquals(BaseType.BOOLEAN);
        setToken(BooleanToken.getInstance(!DEFAULT));

        editorFactory = new VisibleParameterEditorFactory(this, "editorFactory");
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this object being garbage collected.
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  <p>
     *  Subclasses may constrain the type of container by overriding
     *  {@link #setContainer(NamedObj)}.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            GTTools.checkContainerClass(this, container, Pattern.class, true);
            GTTools.checkUniqueness(this, container);
        }
    }

    /** If this variable is not lazy (the default) then evaluate
     *  the expression contained in this variable, and notify any
     *  value dependents. If those are not lazy, then they too will
     *  be evaluated.  Also, if the variable is not lazy, then
     *  notify its container, if there is one, by calling its
     *  attributeChanged() method.
     *  <p>
     *  If this variable is lazy, then mark this variable and any
     *  of its value dependents as needing evaluation and for any
     *  value dependents that are not lazy, evaluate them.
     *  Note that if there are no value dependents,
     *  or if they are all lazy, then this will not
     *  result in evaluation of this variable, and hence will not ensure
     *  that the expression giving its value is valid.  Call getToken()
     *  or getType() to accomplish that.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        Collection collection = super.validate();
        try {
            BooleanToken token = (BooleanToken) getToken();
            if (token == null || token.equals(BooleanToken.TRUE)) {
                GTTools.setIconDescription(this, _COLLAPSING_ICON);
            } else {
                GTTools.setIconDescription(this, _NON_COLLAPSING_ICON);
            }
        } catch (IllegalActionException e) {
            throw new KernelRuntimeException(e,
                    "Cannot get token from the attribute.");
        }
        return collection;
    }

    /** The default value of this attribute.
     */
    public static final boolean DEFAULT = false;

    /** The editor factory.
     */
    public VisibleParameterEditorFactory editorFactory;

    /** The icon used when collapsing is set to true.
     */
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

    /** The icon used when collapsing is set to false.
     */
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
