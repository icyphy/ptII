/* Represents an undo or redo action.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.kernel.undo.test;

import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoStackAttribute;

///////////////////////////////////////////////////////////////////
//// UndoActionTest

/**
 Test of UndoActin, an interface represents an undo or redo action that is
 maintained on an undo/redo stack, such as that maintained by
 UndoStackAttribute.

 @see UndoStackAttribute
 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class UndoActionTest implements UndoAction {

    /** Create a UndoActionTest.
     *  @param name The name of this UndoAction.
     */
    public UndoActionTest(String name) {
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the undo or redo action by printing the name on stdout.
     *  @exception Exception If something goes wrong.
     */
    @Override
    public void execute() throws Exception {
        if (_name.equals("throwException")) {
            throw new Exception("Name was \"throw Exception\", so we do so.");
        }
        System.out.println("UndoActionTest.execute(): " + _name);
    }

    /** Return a string representation of this object.
     *  @return The name of the class and the name of this object
     */
    @Override
    public String toString() {
        return "UndoActionTest-" + _name;
    }

    private String _name;
}
