/* Interface for implementing platform dependent parts of the display actor.

 @Copyright (c) 1998-2010 The Regents of the University of California.
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

import ptolemy.actor.gui.PortableContainer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
Interface for implementing platform dependent parts of the display actor.

@author Ishwinder Singh
@version $Id$
@since Ptolemy II 8.1
@Pt.ProposedRating Yellow (ishwinde)
@Pt.AcceptedRating Yellow (ishwinde)
*/

///////////////////////////////////////////////////////////////////
////DisplayInterface

public interface DisplayInterface {

    /** Set the number of rows for the text area. 
     * @param display Object of the display actor.
     */
    public void init(Display display) throws IllegalActionException,
            NameDuplicationException;

    /** Set the number of rows for the text area. 
     */
    public void setRows(int numRows) throws IllegalActionException;

    /** Set the number of columns for the text area. 
    */
    public void setColumns(int numColumns) throws IllegalActionException;

    /** Return the object of the containing text area. 
    */
    public Object getTextArea();

    /** Place the Actor in the provided container. 
     *
     *  @param portableContainer The container into which to place the actor, or
     *   null to specify that there is no current container.
     */
    public void place(PortableContainer container);

    /** Display the string token in the text area. 
     */
    public void display(String tokenValue);

    /** Free up memory when closing. */
    public void cleanUp();

    /** Set the title of the display.
     * 
     * @param stringValue String containing the title to be set
     * @throws IllegalActionException
     */
    public void setTitle(String stringValue) throws IllegalActionException;

    /** Creates a window for showing the display actor
     * 
     * @throws IllegalActionException
     */
    public void openWindow() throws IllegalActionException;

    /** Remove the display from the current container, if there is one.
     */
    public void remove();

}
