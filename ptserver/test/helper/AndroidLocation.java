/* Stores location information for Android specific elements.
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.test.helper;

import ptolemy.data.IntMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// AndroidLocation

/** Stores location information for Android specific elements.
 * 
 *  @author Peter Foldes
 *  @version $Id: AndroidLocation.java 61296 2011-06-23 04:26:22Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class AndroidLocation extends Parameter {

    public AndroidLocation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Get the relative x position of the top-left corner. <br/>
     *  Special constants: <br/>
     *          MATCH_PARENT = -1 <br/>
     *          WRAP_CONTENT = -2
     *  @return The relative x position.
     *  @exception IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getX() throws IllegalActionException {
        validateLocation();
        return ((IntMatrixToken) getToken()).getElementAt(0, 0);
    }

    /** Get the relative y position of the top-left corner. <br/>
     *  Special constants: <br/>
     *          MATCH_PARENT = -1 <br/>
     *          WRAP_CONTENT = -2
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

    /** Validate the location. The location should have four parameters,
     *  the top left position (x and y), the width, and the height.
     *  
     *  @exception IllegalActionException If the location is not valid.
     */
    public void validateLocation() throws IllegalActionException {
        IntMatrixToken token = (IntMatrixToken) getToken();

        // Check if the token is not a valid token, if it has the 4 required
        // parameters (x, y, height, and width), and if the height and width
        // make any sense.
        if (token == null || token.getColumnCount() != 4
                || token.getElementAt(0, 2) < -2
                || token.getElementAt(0, 3) < -2) {
            throw new IllegalActionException(this,
                    "Invalid location information.");
        }
    }
}
