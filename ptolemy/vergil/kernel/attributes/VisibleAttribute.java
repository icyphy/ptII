/* A base class for visible attributes.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.vergil.kernel.attributes;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// VisibleAttribute

/**
 Base class for attributes that are shown in vergil with their own icons.
 This base class contains an attribute that results in its name being
 hidden, and also handles commands to send it to the back or the front.
 <p>
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class VisibleAttribute extends Attribute {
    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public VisibleAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Hide the name.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

        // No need to display any parameters when the "_showParameters"
        // preference asks for such display because presumably all the
        // parameters are reflected in the visual display already.
        Parameter hideAllParameters = new Parameter(this, "_hideAllParameters");
        hideAllParameters.setVisibility(Settable.EXPERT);
        hideAllParameters.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Move this object to the first position in the list
     *  of attributes of the container. This overrides the base
     *  class to create  an attribute named "_renderFirst" and to
     *  remove an attribute named "_renderLast", if it is present.
     *  This attribute is recognized by vergil, which then renders this
     *  attribute before entities, connections, and other attributes.
     *  This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        try {
            new SingletonAttribute(this, "_renderFirst");
            Attribute renderLast = getAttribute("_renderLast");

            if (renderLast != null) {
                renderLast.setContainer(null);
            }
        } catch (NameDuplicationException e) {
            // Ignore.  This will result in a rendering error,
            // but that is better than trashing user data.
        }

        return super.moveToFirst();
    }

    /** Move this object to the last position in the list
     *  of attributes of the container. This overrides the base
     *  class to create  an attribute named "_renderLast" and to
     *  remove an attribute named "_renderFirst" if it is present.
     *  This attribute is recognized by vergil, which then renders this
     *  attribute after entities, connections, and other attributes.
     *  This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        try {
            new SingletonAttribute(this, "_renderLast");
            Attribute renderFirst = getAttribute("_renderFirst");

            if (renderFirst != null) {
                renderFirst.setContainer(null);
            }
        } catch (NameDuplicationException e) {
            // Ignore.  This will result in a rendering error,
            // but that is better than trashing user data.
        }

        return super.moveToLast();
    }
}
