/*
 * Copyright (c) 1998-2000 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package ptolemy.vergil.icon;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * SimpleSelection.java
 * @author Nick Zamora (nzamor@uclink4.berkeley.edu) 
 *
 */


// SimpleSelection.  The class used to keep track of the clipboard 
// contents and the type of data being stored.  

public class SimpleSelection implements Transferable, ClipboardOwner {
  
    protected Object _selection;
  
    protected DataFlavor _flavor;
  
    public SimpleSelection (Object selection, DataFlavor flavor) {
      
        _selection = selection;
	
	_flavor = flavor;

    }

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
	    
	}
	
	else throw new UnsupportedFlavorException (f);
	
    }
  
    public void lostOwnership (Clipboard c, Transferable t) {
      
        _selection = null;
      
    }

}


