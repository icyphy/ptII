/* An object representing an arc between two states.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.actor.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import ptolemy.domains.fsm.kernel.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.URL;
import java.io.Writer;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// Arc
/**
Instances of this class represent an arc between two states in a state
machine visualization.  To see how this class is used and links are made
using this class, see ArcModel in FSMGraphModel.

@author Steve Neuendorffer
@version $Id$
@see FSMGraphModel.ArcModel
*/
public class Arc {

    /** Return the head of this link.   This will be the icon of a state.
     */
    public Object getHead() {
	return _head;
    }

    /** Return the relation that this link represents.  This should always
     *  be an instance of Transition.
     */
    public ComponentRelation getRelation() {
	return _relation;
    }

    /** Return the tail of this link.   This will be the icon of a state.
     */
    public Object getTail() {
	return _tail;
    }

    /** Set the head of this link.  This will be the icon of a state.
     */
    public void setHead(Object head) {
	_head = head;
    }

    /** Set the relation that this link represents.  This should always
     *  be an instance of Transition.
     */
    public void setRelation(ComponentRelation relation) {
	_relation = relation;
    }

    /** Set the tail of this link.  This will be the icon of a state.
     */
    public void setTail(Object tail) {
	_tail = tail;
    }

    /** Return a string representation of this link.
     */
    public String toString() {
	return "Arc("
	    + _head + ", "
                + _tail + ", "
                    + _relation + ")";
    }

    private Object _head;
    private Object _tail;
    private ComponentRelation _relation;
}
