/* A class that allows to see values of some parameters of an Actor

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE 
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THE SUPELEC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

import java.util.*;
import java.awt.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.*;

import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// ActorWatcher
/**
 This class allows to track some parameters values of an actor
@author SUPELEC team
@version $Id$
@see Nameable, Watchers
@see ptolemy.vergil.debugger.ActorWatcher
*/
public class ActorWatcher implements Nameable, Watchers {

    public Vector valueList;
    /** Constructor
     * @see ptolemy.vergil.debugger#ActorWatcher(NamedObj target)
     * @param target : the actor that you care about
     */
    public ActorWatcher(NamedObj target) {
	_target = target;
	_attributes = null;
	valueList = new Vector();
	_name = target.getFullName();
	_frame = new JFrame();
	_frame.setTitle(_target.getName());
	_frame.setVisible(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    public String description() {
	return "Me";
    }

    public Nameable getContainer() {
	return null;
    }

    public String getName() {
	return _name;
    }

    public String getFullName() {
	return _name;
    }

    public void setName(String name) {
	_name = name;
    }

    /** Refresh all elements that display a value. This method is called
     * each time the user has to enter a command : he can eventually 
     * decide what to do depending on the displayed values
     * @see ptolemy.vergil.debugger#refresh()
     */
    public void refresh() {
	Enumeration att;
	att = _attributes.elements();
	valueList.removeAllElements();
	while (att.hasMoreElements()) {
	    try {
		valueList.addElement(new String(((Variable)att.nextElement()).getToken().toString())); 
	    } catch (IllegalActionException e) {}
	}
	edit(_attributes);
	_frame.setVisible(true);
    }

    /** Build a visual representation of the watcher. For each target
     * value it builds a swing component to display this values
     * @see ptolemy.vergil.debugger#edit()
     * @param attributeList : the list of attribute to display 
     */
    public void edit(NamedList attributeList) {

	_attributes = attributeList;
	Enumeration e = _attributes.elements();
	int size = _attributes.size();
	_frame.setSize(200, size*50);
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(size, 2, 2, 10));
	while (e.hasMoreElements()) {
	    // Build a swing component to display attribute
	    // and add it to the frame 
	    // ONLY for string displayable parameter
	    Variable attribute = (Variable)e.nextElement();
	    JLabel label = new JLabel(attribute.getName());
	    panel.add(label);
	    try {
		String nextValue = new String((attribute.getToken()).toString());
		JTextField text = new JTextField(nextValue);
		valueList.addElement(nextValue);
		panel.add(text);
	    } catch (IllegalActionException ex) {}
	}
	_frame.setContentPane(panel);

		
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private NamedObj _target;
    private String _name;
    private NamedList _attributes;
    private JFrame _frame;
}
