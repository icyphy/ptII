/* Stores location information for positionable elements.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.homer.kernel;

import ptolemy.data.IntMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// HomerLocation

/** Stores location information for positionable elements.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class HomerLocation extends Parameter {

    /** Create a new location parameter that stores x, y, width, and height.
     *
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *  acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with a
     *  parameter already in the container.
     */
    public HomerLocation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.NONE);
    }

    /** Create a new location parameter that stores x, y, width, and height.
     *
     *  @param container The container.
     *  @exception IllegalActionException If the parameter is not of an
     *  acceptable class for the container.
     *  @exception NameDuplicationException If the location already exist
     *  in that container.
     */
    public HomerLocation(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super(container, HomerConstants.POSITION_NODE);
        setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the relative x position of the top-left corner.
     *  Special constants:
     *  <pre>
     *  MATCH_PARENT = -1
     *  WRAP_CONTENT = -2
     *  </pre>
     *  @return The relative x position.
     *  @exception IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getX() throws IllegalActionException {
        validateLocation();
        return ((IntMatrixToken) getToken()).getElementAt(0, 0);
    }

    /** Get the relative y position of the top-left corner.
     *  Special constants:
     *  <pre>
     *  MATCH_PARENT = -1
     *  WRAP_CONTENT = -2
     *  </pre>
     *  @return The relative y position.
     *  @exception IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getY() throws IllegalActionException {
        validateLocation();
        return ((IntMatrixToken) getToken()).getElementAt(0, 1);
    }

    /** Get the width of the element.
     *  @return The width of the element.
     *  @exception IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getWidth() throws IllegalActionException {
        validateLocation();
        return ((IntMatrixToken) getToken()).getElementAt(0, 2);
    }

    /** Get the height of the element.
     *  @return The height of the element.
     *  @exception IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getHeight() throws IllegalActionException {
        validateLocation();
        return ((IntMatrixToken) getToken()).getElementAt(0, 3);
    }

    /** Set the underlying location based on the given values.
     *
     *  @param x The topleft points horizontal coordinate.
     *  @param y The topleft points vertical coordinate.
     *  @param width The width of the area.
     *  @param height The height of the area.
     */
    public void setLocation(int x, int y, int width, int height) {
        try {
            setToken(new IntMatrixToken(new int[] { x, y, width, height }, 1,
                    4, MatrixToken.DO_NOT_COPY));
        } catch (IllegalActionException e) {
            // matrix is non-null so this can't happen.
        }
    }

    /** Validate the location. The location should have four parameters,
     *  the top left position (x and y), the width, and the height.
     *
     *  @exception IllegalActionException If the location is not valid.
     */
    public void validateLocation() throws IllegalActionException {
        IntMatrixToken token = (IntMatrixToken) getToken();

        // Check if the token is not a valid token, if it has the 4 required
        // parameters (x, y, width, and height), and if the height and width
        // make any sense.
        if (token == null || token.getColumnCount() != 4
                || token.getElementAt(0, 2) < -2
                || token.getElementAt(0, 3) < -2) {
            throw new IllegalActionException(this,
                    "Invalid location information.");
        }
    }
}
