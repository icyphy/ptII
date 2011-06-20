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
 *  @version $Id$
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

    /** Gets the relative x position of the top-left corner.
     *  Special constants:
     *          MATCH_PARENT = -1
     *          WRAP_CONTENT = -2
     *  @return The relative x position.
     *  @throws IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getX() throws IllegalActionException {
        if (_layout_x < -3)
            _setSize();
        return _layout_x;
    }

    /** Gets the relative y position of the top-left corner.
     *  Special constants:
     *          MATCH_PARENT = -1
     *          WRAP_CONTENT = -2
     *  @return The relative y position.
     *  @throws IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getY() throws IllegalActionException {
        if (_layout_y < -3)
            _setSize();
        return _layout_y;
    }

    /** Gets the width of the element.
     *  @return The width of the element.
     *  @throws IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getWidth() throws IllegalActionException {
        if (_width < -3)
            _setSize();
        return _width;
    }

    /** Gets the height of the element.
     *  @return The height of the element.
     *  @throws IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    public int getHeight() throws IllegalActionException {
        if (_height < -3)
            _setSize();
        return _height;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    /** Sets all the location attributes based on the underlying token.
     *  @throws IllegalActionException If the underlying token is not
     *  an IntMatrixToken.
     */
    private void _setSize() throws IllegalActionException {
        IntMatrixToken token = (IntMatrixToken) getToken();

        if (token != null) {
            _layout_x = token.getElementAt(0, 0);
            _layout_y = token.getElementAt(0, 1);
            _width = token.getElementAt(0, 2);
            _height = token.getElementAt(0, 3);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** Relative x position of the top-left corner.
     *  Special constants:
     *          MATCH_PARENT = -1
     *          WRAP_CONTENT = -2
     */
    private int _layout_x = -5;

    /** Relative y position of the top-left corner.
     *  Special constants:
     *          MATCH_PARENT = -1
     *          WRAP_CONTENT = -2
     */
    private int _layout_y = -5;

    /** Width of the element.
     */
    private int _width = -5;

    /** Height of the element.
     */
    private int _height = -5;
}
