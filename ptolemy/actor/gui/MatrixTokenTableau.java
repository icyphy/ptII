/* A tableau representing a matrix token in a table.

 Copyright (c) 2000-2003 The Regents of the University of California.
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

package ptolemy.actor.gui;

import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// MatrixTokenTableau
/**
A tableau representing matrix tokens in a top-level window with a table.
This should be constructed using the provided factory, to ensure that
the matrix pane is created.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
@see TokenEffigy
*/
public class MatrixTokenTableau extends TokenTableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  Use setFrame() to specify the plot frame after construction.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public MatrixTokenTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a new tableau for the model represented by the given effigy,
     *  using the specified frame.
     *  @param container The container.
     *  @param name The name.
     *  @param frame The frame to use.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public MatrixTokenTableau(Effigy container, String name, TableauFrame frame)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, frame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display the specified token.
     *  If the display is not a MatrixPane, or the token is not
     *  a MatrixToken, do nothing.
     *  @param token The token to append.
     */
    public void append(Token token) {
        if (_pane != null && token instanceof MatrixToken) {
            _pane.display((MatrixToken)token);
        }
    }

    /** Display the specified tokens.
     *  If the display is not a MatrixPane, or the tokens are not
     *  instances of MatrixToken, do nothing.
     */
    public void append(List list) {
        if (_pane != null) {
            Iterator tokens = list.iterator();
            while (tokens.hasNext()) {
                Object token = tokens.next();
                if (token instanceof MatrixToken) {
                    _pane.display((MatrixToken)token);
                }
            }
        }
    }

    /** Return true if this tableau can display the specified token.
     *  @param token A candidate token to display.
     *  @return True if the argument is a MatrixToken.
     */
    public static boolean canDisplay(Token token) {
        if (token instanceof MatrixToken) {
            return true;
        } else {
            return false;
        }
    }

    /** Clear the display.
     */
    public void clear() {
        if (_pane != null) {
            _pane.clear();
        }
    }

    /** Create a matrix frame to view the data.  If the argument is null,
     *  then a new TableauFrame is created.
     *  This is called in the constructor.
     *  @see MatrixPane
     *  @exception IllegalActionException If the frame cannot be created.
     */
    public void createFrame(TableauFrame frame) throws IllegalActionException {
        TokenEffigy effigy = (TokenEffigy)getContainer();
        if (frame == null) {
            // The second argument prevents a status bar.
            frame = new TableauFrame(this, null);
        }
        setFrame(frame);
        _pane = new MatrixPane();
        frame.getContentPane().add(_pane);
        // Display current data.
        Iterator tokens = effigy.getTokens().iterator();
        while (tokens.hasNext()) {
            Object token = tokens.next();
            _pane.display((MatrixToken)token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The pane to display matrices.
    private MatrixPane _pane;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates a matrix token tableau.
     */
    public static class Factory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "tokenTableau", then return that tableau; otherwise, create
         *  a new instance of MatrixTokenTableau in the specified
         *  effigy, and name it "tokenTableau".  If the specified
         *  effigy is not an instance of TokenEffigy, then do not
         *  create a tableau and return null.  This method will also
         *  create a frame for viewing the token data.  Which frame
         *  is created depends on the type of the data in the effigy.
         *  The fallback is a TextEditor. It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy, which is expected to be a TokenEffigy.
         *  @return An instance of MatrixTokenTableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof TokenEffigy) {
                // Indicate to the effigy that this factory contains effigies
                // offering multiple views of the effigy data.
                effigy.setTableauFactory(this);

                // First see whether the effigy already contains a
                // TokenTableau.
                TokenTableau tableau =
                    (TokenTableau)effigy.getEntity("tokenTableau");
                if (tableau != null) {
                    return tableau;
                }

                // Next, check compatibility of the data.
                Iterator tokens = ((TokenEffigy)effigy).getTokens().iterator();
                while (tokens.hasNext()) {
                    Object token = tokens.next();
                    if (!MatrixTokenTableau.canDisplay((Token)token)) {
                        return null;
                    }
                }
                return new MatrixTokenTableau(
                        (TokenEffigy)effigy, "tokenTableau");
            }
            return null;
        }
    }
}
