/* An attribute representing the location of a component.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.gui;

import ptolemy.data.IntMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

import java.awt.Rectangle;
import java.awt.Window;


//////////////////////////////////////////////////////////////////////////
//// LocationAttribute

/**
   This attribute stores the width and height of a graphical component.
   The token in this attribute is an IntMatrixToken containing a matrix
   of dimension 1x2, containing the width and the height, in that order.
   By default, this attribute has visibility NONE, so the user will not
   see it in parameter editing dialogs.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/
public class LocationAttribute extends Parameter {
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
    public LocationAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the value of the attribute to match the location
     *  of the specified component.
     *  @param component The component whose location is to be recorded.
     */
    public void recordLocation(Window component) {
        try {
            Rectangle location = component.getBounds();
            int[][] locationMatrix = new int[1][2];
            locationMatrix[0][0] = location.x;
            locationMatrix[0][1] = location.y;

            IntMatrixToken token = new IntMatrixToken(locationMatrix);
            setToken(token);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Can't set bounds value!");
        }
    }

    /** Set the location of the specified component to match the
     *  current value of the attribute.  If the value of the attribute
     *  has not been set, then do nothing.
     *  @param component The component whose location is to be set.
     *  @return True if successful.
     */
    public boolean setLocation(Window component) {
        try {
            IntMatrixToken token = (IntMatrixToken) getToken();

            if (token != null) {
                int x = token.getElementAt(0, 0);
                int y = token.getElementAt(0, 1);

                // NOTE: As usual with swing, it's not obvious what the
                // right way to do this is. The following seems to work,
                // found by trial and error.  Even then, the layout
                // manager feels free to override it.
                component.setLocation(x, y);
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
