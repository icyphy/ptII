/* A transferable object that contains a named object. 

 Copyright (c) 2000 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;

/** 
A transferable object that contains a local JVM reference to a
named object.  To get a reference to the object, request data with the
data flavor given in the static namedObjFlavor variable.
This class will also return a MoML representation of the object, if 
data is requested with the DataFlavor.stringFlavor or 
DataFlavor.plainTextFlavor.

@author 	Steve Neuendorffer
@version	$Id$
*/
public class PtolemyTransferable implements Transferable {

    /** 
     * Create a new transferable object with a reference to the given
     * named object.
     */
    public PtolemyTransferable(NamedObj object) {
	    _object = object;
    }
    
    /** The flavor that requests a reference to the node.
     */	
    public static final DataFlavor namedObjFlavor =
	new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
		       "ptolemy.kernel.util.NamedObj", "Named Object");

    /**
     * Return the data flavors that this transferable supports.
     */
    public synchronized DataFlavor[] getTransferDataFlavors() {
	return _flavors;
    }
    
    /**
     * Return true if the given data flavor is supported.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
	int i;
	for(i = 0; i < _flavors.length; i++)
	    if(_flavors[i].equals(flavor)) return true;
	return false;
    }
    
    /**
     * Return an object that represents the data contained within this
     * transferable with the given flavor.  If the flavor is namedObjFlavor,
     * return the named obj that this transferable refers to.  If the flavor
     * is DataFlavor.plainTextFlavor, return an InputStream that contains a
     * MoML representation of the object.  If the flavor is 
     * DataFlavor.stringFlavor return a string that contains the MoML
     * representation.
     *
     * @return An object with the given data flavor. 
     * @exception UnsupportedFlavorException If the given flavor is 
     * not supported.
     */
    public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
	if (flavor.equals(DataFlavor.plainTextFlavor)) {
	    return new ByteArrayInputStream(_object.exportMoML().
					    getBytes("Unicode"));
	} else if(flavor.equals(namedObjFlavor)) {
	    return _object;
	} else if(flavor.equals(DataFlavor.stringFlavor)) {
	    return _object.exportMoML();
	}
	throw new UnsupportedFlavorException(flavor);
    }

    // The flavors that this node can return.
    private final DataFlavor[] _flavors = {
	DataFlavor.plainTextFlavor,
	DataFlavor.stringFlavor,
	namedObjFlavor,
    };

    //The object contained by this transferable.
    private NamedObj _object;
}
