/* A dialog window to edit breakpoint parameters

 Copyright (c) 1999-2002 SUPELEC and The Regents of the University of
 California.  All rights reserved.  Permission is hereby granted,
 without written agreement and without license or royalty fees, to
 use, copy, modify, and distribute this software and its documentation
 for any purpose, provided that the above copyright notice and the
 following two paragraphs appear in all copies of this software.

 IN NO EVENT SHALL SUPELEC OR THE UNIVERSITY OF CALIFORNIA BE LIABLE
 TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 DOCUMENTATION, EVEN IF SUPELEC OR THE UNIVERSITY OF CALIFORNIA HAVE
 BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND SUPELEC SPECIFICALLY DISCLAIM ANY
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA AND SUPELEC HAVE NO OBLIGATION TO PROVIDE MAINTENANCE,
 SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (frederic.boulanger@supelec.fr)
@AcceptedRating Red
*/

package ptolemy.vergil.debugger;

import java.util.List;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.debugger.*;

//////////////////////////////////////////////////////////////////////////
//// BreakpointEditor
/**
A user interface for editing a breakpoint.  The condition is
displayed in a text box and can be edited.  The method that the
breakpoint is active on can also be edited.
The changes are validated when return is pressed after
editing the condition or the Ok button is activated. The
cancel button will close the window without changing at all
the breakpoint

@author B. Desoutter, P. Domecq & G. Vibert and Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
public class BreakpointEditor extends JFrame implements ActionListener {

    /** Construct a new frame that will configure the given breakpoint.
     *  @param breakpoint The breakpoint to edit.
     */
    public BreakpointEditor(Breakpoint breakpoint) {

	super("EditBreakpoint");
	_breakpoint = breakpoint;
	_actor = (Actor)_breakpoint.getContainer();

	JLabel label = new JLabel("Set a condition :");

	// Condition Panel
	JPanel textPanel = new JPanel();
	textPanel.setBorder(new TitledBorder("Condition ="));

	//Text field
	textField = new JTextField(_breakpoint.getExpression(), 20);
	textField.addActionListener(this);

	textPanel.add(textField);

	// Button panel
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BorderLayout());

	//Cancel  button
	JButton button = new JButton("Cancel");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BreakpointEditor.this.dispose();
            }
        });
	buttonPanel.add(button, BorderLayout.EAST);

	// Ok button
	button = new JButton("Ok");
	button.addActionListener(this);
	buttonPanel.add(button, BorderLayout.WEST);

	// Radiobuttons panel
	JPanel radioPanel = new JPanel();
	radioPanel.setBorder(new TitledBorder("Methods :"));

	Box buttonBox = new Box(BoxLayout.Y_AXIS);
	group = new ButtonGroup();

	JRadioButton radioButton = new JRadioButton("prefire");
	if (_breakpoint.getName().equals("prefire")) {
	    radioButton.setSelected(true);
	    _selectedMethod = "prefire";
	}
	radioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _selectedMethod = new String("prefire");
            }
        });
	group.add(radioButton);
	buttonBox.add(radioButton);

	radioButton = new JRadioButton("fire");
	if (_breakpoint.getName().equals("fire") || _breakpoint.getName().equals("default")) {
	    radioButton.setSelected(true);
	    _selectedMethod = "fire";
	}
	radioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _selectedMethod = new String("fire");
            }
        });
	group.add(radioButton);
	buttonBox.add(radioButton);

        radioButton = new JRadioButton("postfire");
	if (_breakpoint.getName().equals("postfire")) {
	    radioButton.setSelected(true);
	    _selectedMethod = "postfire";
	}
	radioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _selectedMethod = new String("postfire");
            }
        });
	group.add(radioButton);
	buttonBox.add(radioButton);

	radioButton = new JRadioButton("postpostfire");
	if (_breakpoint.getName().equals("postpostfire")) {
	    radioButton.setSelected(true);
	    _selectedMethod = "postpostfire";
	}
	radioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _selectedMethod = new String("postpostfire");
            }
        });
	group.add(radioButton);
	buttonBox.add(radioButton);

	radioPanel.add(buttonBox);

	//Lay out the content pane.
	JPanel contentPane = new JPanel();
	contentPane.setLayout(new BorderLayout());
	contentPane.add(label, BorderLayout.NORTH);
	contentPane.add(buttonPanel, BorderLayout.SOUTH);
	contentPane.add(radioPanel, BorderLayout.WEST);
	contentPane.add(textPanel, BorderLayout.EAST);
	setContentPane(contentPane);
	pack();
	setLocation(500, 300);
	setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the condition from the text field and make the necessery changes
     * to the value of the breakpoint.
     */
    public void actionPerformed(ActionEvent e) {
	String condition = textField.getText();
	try {
	    _breakpoint.setName(_selectedMethod);
	    _breakpoint.setExpression(condition);
	} catch (NullPointerException ex) {
	    System.out.println(ex.getMessage());
	} catch (IllegalActionException ex) {
	    System.out.println(ex.getMessage());
	} catch (NameDuplicationException ex) {
	    System.out.println(ex.getMessage());
	}
	BreakpointEditor.this.dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Default method */
    protected String _selectedMethod = "fire";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ButtonGroup group;
    private JTextField textField;
    private Actor _actor;
    private Breakpoint _breakpoint;

}
