/* Query dialog.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

// FIXME: This is not the right package for this.

package ptolemy.actor.util;

import java.awt.*;
import java.util.Hashtable;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// Query
/** 
Create a query with various types of entry boxes and controls.

@author  Edward A. Lee
@version $Id$
*/
public class Query extends Panel {

    /** Construct a panel with no queries in it.
     */	
    public Query () {
        // FIXME: Setting the number of rows to a large number.
        setLayout(new GridLayout(1,2));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a single-line entry box with the specified name, label, and
     *  default value.
     */	
    public void line(String name, String label, String defvalue) {
        add(new Label(label));
        // FIXME: Fixed width.
        TextField entrybox = new TextField(defvalue, 20);
        add(entrybox);
        _lines.put(name, entrybox);
    }

    /** Get the current value in the entry with the given name.
     *  @return The value currently in the dialog box.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public String get(String name) throws NoSuchElementException {
        TextField result = (TextField)(_lines.get(name));
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the query box.");
        }
        return result.getText();
    }    

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Hashtable _lines = new Hashtable();
}
