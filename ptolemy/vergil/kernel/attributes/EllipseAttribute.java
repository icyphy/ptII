/* An attribute with a reference to an ellipse.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel.attributes;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// EllipseAttribute
/**
This is an attribute that is rendered as an ellipse.
Unlike the base class, by default, an ellipse is centered on its origin.
<p>
@author Edward A. Lee
@version $Id$
*/
public class EllipseAttribute extends FilledShapeAttribute {

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
    public EllipseAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // NOTE: This used to be calling setExpression(), but the change
        // does not take effect when the icon is created.
        centered.setToken("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Return a circle.
     *  @return A Circle.
     */
    protected Shape _getDefaultShape() {
        return new Ellipse2D.Double(0.0, 0.0, 20.0, 20.0);
    }

    /** Return the a new ellipse given a new width and height.
     *  @param width The new width.
     *  @param height The new height.
     *  @return A new shape. 
     */
    protected Shape _newShape() {
        if (_centeredValue) {
            double halfWidth = _widthValue * 0.5;
            double halfHeight = _heightValue * 0.5;
            return new Ellipse2D.Double(
                -halfWidth,
                -halfHeight,
                _widthValue,
                _heightValue);
        } else {
            return new Ellipse2D.Double(0.0, 0.0, _widthValue, _heightValue);
        }
    }
}
