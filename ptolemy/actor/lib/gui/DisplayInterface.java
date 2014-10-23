/* Interface for implementing platform dependent parts of the display actor.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.gui;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////DisplayInterface

/**
 Interface for implementing platform dependent parts of the display actor.

@author Ishwinder Singh
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (ishwinde)
@Pt.AcceptedRating Yellow (ishwinde)
 */
public interface DisplayInterface {

    /** Free up memory when closing. */
    public void cleanUp();

    /** Append the string value of the token to the text area
     *  on the screen.  Each value is terminated with a newline
     *  character.
     *  @param tokenValue The string to be displayed
     */
    public void display(String tokenValue);

    /** Return the object of the containing text area.
     *  @return the text area.
     */
    public Object getTextArea();

    /** Set the number of rows for the text area.
     * @param display Object of the display actor.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public void init(Display display) throws IllegalActionException,
    NameDuplicationException;

    /** Open the display window if it has not been opened.
     *  @exception IllegalActionException If there is a problem creating
     *  the effigy and tableau.
     */
    public void openWindow() throws IllegalActionException;

    /** Specify the container in which the data should be displayed.
     *  An instance of JTextArea will be added to that container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of JTextArea will be placed in its own frame.
     *  The text area is also placed in its own frame if this method
     *  is called with a null argument.
     *  The background of the text area is set equal to that of the container
     *  (unless it is null).
     *
     *  @param container The container into which to place the text area, or
     *   null to specify that there is no current container.
     */
    public void place(PortableContainer container);

    /** Remove the display from the current container, if there is one.
     */
    public void remove();

    /** Set the desired number of columns of the textArea, if there is one.
     *
     *  @param numberOfColumns The new value of the attribute.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */
    public void setColumns(int numberOfColumns) throws IllegalActionException;

    /** Set the desired number of rows of the textArea, if there is one.
     *
     *  @param numberOfRows The new value of the attribute.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */
    public void setRows(int numberOfRows) throws IllegalActionException;

    /** Set the title of the window.
     *
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of
     *  the Display window will be updated to the value of the name
     *  parameter.</p>
     *
     * @param stringValue The title to be set.
     * @exception IllegalActionException If the title cannot be set.
     */
    public void setTitle(String stringValue) throws IllegalActionException;
}
