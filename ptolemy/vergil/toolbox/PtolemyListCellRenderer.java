/* A list cell renderer for ptolemy objects.

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
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
A list cell renderer for Ptolemy objects.  
 
@author Steve Neuendorffer
@version $Id$
*/
public class PtolemyListCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(
	JList list, Object value, int index,
	boolean isSelected, boolean cellHasFocus) {
	
	DefaultListCellRenderer component = (DefaultListCellRenderer)
	    super.getListCellRendererComponent(list, value, 
		index, isSelected, cellHasFocus);	
	if(value instanceof NamedObj) {
	    NamedObj object = (NamedObj) value;
	    component.setText(object.getName());
	}	    
	return component;
    }
}
