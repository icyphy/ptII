/* A toplevel frame for debugging ptolemy models.

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
@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red
*/

package ptolemy.vergil.debugger;

import java.lang.*;
import java.util.*;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.URL;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.interactor.*;
import diva.graph.*;
import diva.graph.layout.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.RelativeBundle;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;

import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.*;
import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;

//////////////////////////////////////////////////////////////////////////
//// DebuggerFrame
/**
   This class creates the interface for the debugger.
   The frame is composed of a menubar, a toolbar, and a scrollbar
   where the messages are displayed (Through the use of the
   function displayResult). Its coded on java swing add makes
   use of actionListener to acknowledge the user command (Selection
   of a button or menu item of a menu).
   Buttons:
   - go: begin execution with XXXDebugDirector
   - resume: execute until next breakpoint
   - stop: nothing yet **** stop the execution
   - end: finish execution in normal mode.
   - step, stepin, stepout and µstep: change _cmd (command field)
   and gives control to debugController.
   - quit: Closes the debugger returning the control to Vergil.
   Menus: breakpoint and watcher
   - add: creates breakpoint and calls BreakpointEditor/Watcher.
   - delete: deletes breakpoint/watcher from displayList.
   - edit: calls BreakpointEditor/Watcher.
   GetSelectedActor is a method that gets the selected actor on
   the vergil interface.
   Function to enable and disable the use of buttons or menus are
   also implemented;
@author SUPELEC team
@version $Id$
@since Ptolemy II 1.0
*/
public class DebuggerFrame extends PtolemyFrame
    implements ActionListener, ItemListener {
    JTextArea output;
    JScrollPane scrollPane;
    String newline = "\n";
    int response;
    JMenuBar menuBar;
    JButton rsm, end, stop, step, stepIn, stepOut, mstep, quit;
    JMenu breakpointMenu, watcherMenu;
    public boolean putCmd = false;



    // a NamedObj element to keep reference on object to edit
    protected NamedObj element = null;


    /** Constructor
     */
    public DebuggerFrame(CompositeActor actor, Tableau tableau) {
	super(actor, tableau);

	_controller = new DebugController(this);
	_toolbar = new JToolBar();

	///////////////////////////////////////////////////////////////////////
        //Add regular components (buttons, scrollpane,...)
	//to the window, using the default BorderLayout.

    }

    public void _addMenus() {
	JMenuItem menuItem;
	// Create scrollpane (Text area used for output)
        output = new JTextArea(5, 30);
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);

	//Create the toolbar for general functions.
        _toolbar = new JToolBar();
        addButtons();

        //Lay out the content pane.
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(_toolbar, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
	setLocation(450,0);

	final JFrame packframe = this;
	Action packer = new AbstractAction() {
	    public void actionPerformed(ActionEvent event) {
		packframe.getContentPane().doLayout();
		packframe.repaint();
		packframe.pack();
	    }
	};
	javax.swing.Timer timer =
	    new javax.swing.Timer(200, packer);
	timer.setRepeats(false);
	timer.start();

       	//////////////////////////////////////////////////////////
            //Build the Breakpoint menu.
            /////////////////////////////////////////////////////////
            breakpointMenu = new JMenu("Breakpoint");
            breakpointMenu.setMnemonic(KeyEvent.VK_B);
            breakpointMenu.getAccessibleContext().setAccessibleDescription("Breakpoint tools");
            _menubar.add(breakpointMenu);

            //a group of JMenuItems implementing the breakpoint tools
            //add breakpoint JMenuItem
            menuItem = new JMenuItem("Add Breakpoint", KeyEvent.VK_A);
            menuItem.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    disableMenu();
		    Breakpoint brkpt = null;
		    NamedObj actor = getSelectedActor();
		    if (actor != null) {
                        try {
                            brkpt = new Breakpoint(actor, "fire");
                        } catch (NameDuplicationException ex) {
                        } catch (IllegalActionException ex) {
                        }
                        JFrame editFrame = new BreakpointEditor(brkpt);
                        displayResult("Breakpoint successfully added on "
                                + actor.getFullName() +" !");
		    } else displayResult ("An actor must be selected before " +
                            "adding a breakpoint.");
		    enableMenu();
		}
	    });
            breakpointMenu.add(menuItem);

            //edit breakpoint JMenuItem
            menuItem = new JMenuItem("Edit Breakpoint", KeyEvent.VK_E);
            menuItem.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    NamedObj actor = getSelectedActor();
		    if (actor != null) {
			DisplayList list =
			    new DisplayList((Actor)actor, DebuggerFrame.this,
                                    DisplayList.EDIT_B);
		    }
		}
	    });
            breakpointMenu.add(menuItem);

            //Delete breakpoint JMenuItem
            menuItem = new JMenuItem("Delete Breakpoint",
                    KeyEvent.VK_D);
            menuItem.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    NamedObj actor = getSelectedActor();
		    if (actor != null) {
			DisplayList list = new DisplayList((Actor)actor, DebuggerFrame.this, DisplayList.DEL);
		    }
		}
	    });
            breakpointMenu.add(menuItem);

            //////////////////////////////////////////////////////////
                //Build the Watcher menu.
                /////////////////////////////////////////////////////////
                watcherMenu = new JMenu("Watcher");
                watcherMenu.setMnemonic(KeyEvent.VK_W);  // Activates menu
                //by pressed keys
                watcherMenu.getAccessibleContext().setAccessibleDescription(
                        "Watcher tools");
                _menubar.add(watcherMenu);

                //a group of JMenuItems implementing the Watcher tools

                //add Watcher JMenuItem
                menuItem = new JMenuItem("Add Watcher",
                        KeyEvent.VK_A);
                menuItem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        displayResult("Action for Add Watcher");
                    }
                });
                watcherMenu.add(menuItem);

                //edit Watcher JMenuItem
                menuItem = new JMenuItem("Edit Watcher",
                        KeyEvent.VK_E);
                menuItem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        displayResult("Elements selected are:");
                        //JFrame editWatcher = new WatcherEditor(DebuggerFrame.this);
                        //editWatcher.pack();
                        //editWatcher.setLocation(550,300);
                        //editWatcher.setVisible(true);
                    }
                });
                watcherMenu.add(menuItem);

                //Delete Watcher JMenuItem
                menuItem = new JMenuItem("Delete Watcher",
                        KeyEvent.VK_D);
                menuItem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        displayResult("Action for delete watcher");
                    }
                });
                watcherMenu.add(menuItem);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This function displays messages on the scroll panel of
     *  the DebuggerFrame
     * @see ptolemy.vergil.debugger.DebuggerFrame#displayResult(String actionDescription)
     * @param a message string
     */
    public  void displayResult(String actionDescription) {
	output.append(actionDescription + newline);
    }

    /** Does nothing
     * @param e an action event
     */
    public void actionPerformed(ActionEvent e) {}

    /** Return the command entered
     * @return a reference on the command string
     */
    public String getUserCommand() {
	return _cmd;
    }

    /** Does nothing
     * @param e an item event
     */
    public void itemStateChanged(ItemEvent e) {}

    //////////////////////////////////////////////////////////
    //Several methods to enable and disable buttons and menus
    /////////////////////////////////////////////////////////
    /** Disable the button
     * @param index : index of the button (in the creation order)
     */
    public void disableButton(int index) {
	switch(index){
	    //	case 1: go.setEnabled(false); break;
	case 2: rsm.setEnabled(false); break;
	case 3: end.setEnabled(false); break;
	case 4: stop.setEnabled(false); break;
	case 5: step.setEnabled(false); break;
	case 6: stepIn.setEnabled(false); break;
	case 7: stepOut.setEnabled(false); break;
	case 8: mstep.setEnabled(false); break;
	case 9: quit.setEnabled(false); break;
	default : break;
	}
    }

    /** Enable a button
     * @param index : index of the button (in the creation order)
     */
    public void enableButton(int index) {
	switch(index){
	    //	case 1: go.setEnabled(true);
	    // break;
	case 2: rsm.setEnabled(true);
	    break;
	case 3: end.setEnabled(true);
	    break;
	case 4: stop.setEnabled(true);
	    break;
	case 5: step.setEnabled(true);
	    break;
	case 6: stepIn.setEnabled(true);
	    break;
	case 7: stepOut.setEnabled(true);
	    break;
	case 8: mstep.setEnabled(true);
	    break;
	case 9: quit.setEnabled(true);
	    break;
	default : break;
	}
    }

    /** Disable all buttons
     * @see ptolemy.vergil.debugger.DebuggerFrame#disableAllButtons()
     */
    public void disableAllButtons() {
	//	go.setEnabled(false);
	rsm.setEnabled(false);
	end.setEnabled(false);
	stop.setEnabled(false);
	step.setEnabled(false);
	stepIn.setEnabled(false);
	stepOut.setEnabled(false);
	mstep.setEnabled(false);
	quit.setEnabled(false);
    }

    /** Enable all buttons
     * @see ptolemy.vergil.debugger.DebuggerFrame#enableAllButtons()
     */
    public void enableAllButtons() {
	//	go.setEnabled(true);
	rsm.setEnabled(true);
	end.setEnabled(true);
	stop.setEnabled(true);
	step.setEnabled(true);
	stepIn.setEnabled(true);
	stepOut.setEnabled(true);
	mstep.setEnabled(true);
	quit.setEnabled(true);
    }

    /** Enable the menu
     * @see ptolemy.vergil.debugger.DebuggerFrame#enableMenu()
     */
    public void enableMenu() {
	breakpointMenu.setEnabled(true);
	watcherMenu.setEnabled(true);
    }

    /** Disable the menu
     * @see ptolemy.vergil.debugger.DebuggerFrame#disableMenu()
     */
    public void disableMenu() {
	breakpointMenu.setEnabled(false);
	watcherMenu.setEnabled(false);
    }

    /** Enable UI component
     * @see ptolemy.vergil.debugger.DebuggerFrame#enableAll()
     */
    public void enableAll() {
	enableMenu();
	enableAllButtons();
    }

    /** Disable all UI components
     * @see ptolemy.vergil.debugger.DebuggerFrame#disableAll()
     */
    public void disableAll() {
	disableMenu();
	disableAllButtons();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Creates the buttons that implement the functionality tools of
     * the debugger.
     * @param _toolbar : Frame's toolbar
     */
    protected void addButtons() {
	JButton button = null;

	//Resume button
	rsm = new JButton("RSM");
	rsm.setMnemonic(KeyEvent.VK_R);
	rsm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (putCmd) {
                    _cmd ="resume";
                    displayResult("Command entered = resume");
                    _controller.commandEntered();
                }
                else {
                    displayResult("Wait a minute !!!");
                }
            }
        });
	_toolbar.add(rsm);

	//End button
	end = new JButton("END");
	end.setMnemonic(KeyEvent.VK_E);
	end.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (putCmd) {
                    /////////////////////////////////////////////////////////
                    // The next line is hacking : _cmd should be set to    //
                    // "end" but this wouldn't work so we cheat the        //
                    // DbgController                                       //
                    _cmd = "microstep";
                    displayResult("Command entered = end");
                    _controller.notFinished = false;
                    _controller.commandEntered();
                }
                else {
                    displayResult("Wait a minute !!!");
                }
            }
        });
	_toolbar.add(end);

	//Stop button
	stop = new JButton("STOP");
 	stop.setMnemonic(KeyEvent.VK_T);
	stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Here we must stop the execution of the model

                //    if (putCmd) {
                //In fact, exit directly.

                //_cmd ="stop";
                //displayResult("Command entered = stop");
                //_controller.cmdNotEntered = false;
                //}
                //else {
                //displayResult("Wait a minute !!!");
                //}
            }
        });
	_toolbar.add(stop);

	_toolbar.addSeparator();

	//Step button
	step = new JButton("Step");
	step.setMnemonic(KeyEvent.VK_S);
	step.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (putCmd) {
                    _cmd ="step";
                    displayResult("Command entered = step");
                    _controller.commandEntered();
                }
                else {
                    displayResult("Wait a minute !!!");
                }

            }
        });
	_toolbar.add(step);

	//Step in button
	stepIn = new JButton("S.in");
	stepIn.setMnemonic(KeyEvent.VK_I);
	stepIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (putCmd) {
                    _cmd ="stepin";
                    displayResult("Command entered = Step in");
                    _controller.commandEntered();
                }
                else {
                    displayResult("Wait a minute !!!");
                }
            }
        });
	_toolbar.add(stepIn);

	//Step out button
	stepOut = new JButton("S.out");
	stepOut.setMnemonic(KeyEvent.VK_O);
	stepOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (putCmd) {
                    _cmd ="stepout";
                    displayResult("Command entered = Step Out");
                    _controller.commandEntered();
                }
                else {
                    displayResult("Wait a minute !!!");
                }

            }
        });
	_toolbar.add(stepOut);

	//MicroStep button
	mstep = new JButton("\u00B5Step");
	mstep.setMnemonic(KeyEvent.VK_P);
	mstep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (putCmd) {
                    _cmd ="microstep";
                    displayResult("Command entered = Micro Step");
                    _controller.commandEntered();
                }
                else {
                    displayResult("Wait a minute !!!");
                }
            }
        });
	_toolbar.add(mstep);
	_toolbar.addSeparator();

	//Quit button
	//    It generates a window dialog to make sure the user
	//really wants to quit the debugger.
	quit = new JButton("QUIT");
	quit.setMnemonic(KeyEvent.VK_Q);
	quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response;
                response = JOptionPane.showConfirmDialog(null, "Quit Pdb ?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION)
                    DebuggerFrame.this.dispose();
            }
        });
	_toolbar.add(quit);
    }

    /** Write the model to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        java.io.FileWriter fout = new java.io.FileWriter(file);
        getModel().exportMoML(fout);
        fout.close();
    }


    /////////////////////////////////////////////////////////////////////
    // Method that gets the selected actor on the vergil interface
    /////////////////////////////////////////////////////////////////////
    private NamedObj getSelectedActor() {
	NamedObj selectedActor = null;
	/* FIXME FIXME FIXME
           The below code should be rewritten to access the graph model

           try {
           View v = VergilApplication.getInstance().getCurrentView();
           PtolemyDocument d = (PtolemyDocument) v.getDocument();
           JGraph g = (JGraph) v.getComponent();
           GraphPane graphPane = g.getGraphPane();
           GraphController controller =
           (GraphController) graphPane.getGraphController();
           Object selection[] = controller.getSelectionModel().getSelectionAsArray();;
           if (selection.length == 1) {
           if (selection[0] instanceof Figure) {
           Object obj = ((Figure)selection[0]).getUserObject();
           if (obj instanceof NamedObj) {
           NamedObj userobj = (NamedObj)obj;
           if (userobj instanceof ptolemy.moml.Icon) {
           ptolemy.moml.Icon icon =
           (ptolemy.moml.Icon)userobj;
           ComponentEntity entity =
           (ComponentEntity)icon.getContainer();
           NamedObj actor = (NamedObj)entity;
           selectedActor = actor;
           } else {
           displayResult("Error !");
           }
           }  else {
           displayResult("You must select an ACTOR !");
           }
           } else {
           displayResult("You must select one and only one Actor !");
           }
           }
           } catch (NullPointerException ex) {
           displayResult("Nothing to select");
           }
	*/
	return selectedActor;
    }

    //////////////////////////////////////////////////////////
    //               Private Variables                      //

    private String _cmd; // store the user command
    private DebugController _controller;
    private JToolBar _toolbar;
}


