/* A tableau representing a token in a text editor.

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

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TokenTableau
/**
A tableau representing one or more tokens in a top-level window with
a text editor.  Subclasses can be created to represent specific token
types in more specialized ways.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
@see TokenEffigy
*/
public class TokenTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  This constructor creates a default editor frame, which is an instance
     *  of TextEditor.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public TokenTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        createFrame(null);
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
    public TokenTableau(Effigy container, String name, TableauFrame frame)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        createFrame(frame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the specified token to the display.
     *  If the display is not a TextEditor, do nothing.
     *  Subclasses should override this with display-specific actions.
     *  @param token The token to append.
     */
    public void append(Token token) {
        if (_editor != null) {
            _editor.text.append(token.toString());
        }
    }

    /** Append the specified tokens to the display.
     *  @param list A list of tokens.
     */
    public void append(List list) {
        if (_editor != null) {
            Iterator tokens = list.iterator();
            while (tokens.hasNext()) {
                _editor.text.append(tokens.next().toString());
            }
        }
    }

    /** Return true if this tableau can display the specified token.
     *  @param token A candidate token to display.
     *  @return True, since this tableau can display any token.
     */
    public static boolean canDisplay(Token token) {
        return true;
    }

    /** Clear the display.
     */
    public void clear() {
        if (_editor != null) {
            _editor.text.setText("");
        }
    }

    /** Create a text editor frame to view the data. This can be overridden
     *  in derived classes to create more specialized viewers/editors.
     *  If the specified frame is not an instance of TextEditor, then
     *  it is replaced with a text editor.
     *  This is called in the constructor.
     *  @param frame The frame to use, or null if none is specified.
     *  @exception IllegalActionException If the frame cannot be created.
     */
    public void createFrame(TableauFrame frame) throws IllegalActionException {
        TokenEffigy effigy = (TokenEffigy)getContainer();
        if (!(frame instanceof TextEditor)) {
            frame = new TextEditor("Token display");
        }
        setFrame(frame);
        frame.setTableau(this);
        ((TextEditor)frame).text.setEditable(false);
        // Display current data.
        Iterator tokens = effigy.getTokens().iterator();
        while (tokens.hasNext()) {
            ((TextEditor)frame).text.append(tokens.next().toString());
            ((TextEditor)frame).text.append("\n");
        }
        _editor = (TextEditor)frame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The frame as a text editor.
    private TextEditor _editor;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates a token tableau.
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
         *  a new instance of TokenTableau in the specified
         *  effigy, and name it "tokenTableau".  If the specified
         *  effigy is not an instance of TokenEffigy, then do not
         *  create a tableau and return null. It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy, which is expected to be a TokenEffigy.
         *  @return An instance of TokenTableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof TokenEffigy) {
                // First see whether the effigy already contains an
                // TokenTableau.
                TokenTableau tableau =
                    (TokenTableau)effigy.getEntity("tokenTableau");
                if (tableau != null) {
                    return tableau;
                }
                // NOTE: Normally need to check effigy tokens for
                // compatibility here, but they are always compatible,
                // so we don't bother.
                return new TokenTableau(
                        (TokenEffigy)effigy, "tokenTableau");
            }
            return null;
        }
    }
}
