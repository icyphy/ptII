/* A class that keeps track of the clipboard contents and the type of
data being stored.

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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

//////////////////////////////////////////////////////////////////////////
//// SimpleSelection
/**

A class that keeps track of the clipboard contents and the type of
data being stored.

@author Nick Zamora (nzamor@uclink4.berkeley.edu)
@version $Id$
@since Ptolemy II 3.0
*/
public class SimpleSelection implements Transferable, ClipboardOwner {

    public SimpleSelection (Object selection, DataFlavor flavor) {
        _selection = selection;
        _flavor = flavor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public DataFlavor[] getTransferDataFlavors () {
        return new DataFlavor[] { _flavor };
    }

    public boolean isDataFlavorSupported (DataFlavor f) {
        return f.equals(_flavor);
    }

    public Object getTransferData (DataFlavor f)
            throws UnsupportedFlavorException {
        if (f.equals (_flavor)) {
            return _selection;
        } else {
            throw new UnsupportedFlavorException (f);
        }
    }

    public void lostOwnership (Clipboard c, Transferable t) {
        _selection = null;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Object _selection;

    protected DataFlavor _flavor;
}


