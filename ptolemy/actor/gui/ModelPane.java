/* A panel containing controls for a Ptolemy II model.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.CloseListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ModelPane
/**

ModelPane is a panel for interacting with an executing Ptolemy II model.
It has optional controls for setting top-level and director parameters,
a set of buttons for controlling the execution, and a panel for displaying
results of the execution.  Any entity in the model that implements
the Placeable interface is placed in the display region.

@see Placeable
@author Edward A. Lee, Elaine Cheong
@version $Id$
@since Ptolemy II 0.4
*/
public class ModelPane extends JPanel implements CloseListener {

    /** Construct a panel for interacting with the specified Ptolemy II model.
     *  This uses the default layout, which is horizontal, and shows
     *  control buttons, top-level parameters, and director parameters.
     *  @param model The model to control.
     */
    public ModelPane(CompositeActor model) {
        this(model, HORIZONTAL, BUTTONS | TOP_PARAMETERS | DIRECTOR_PARAMETERS);
    }

    /** Construct a panel for interacting with the specified Ptolemy II model.
     *  The layout argument should be one of HORIZONTAL, VERTICAL, or
     *  CONTROLS_ONLY; it determines whether the controls are put to
     *  the left of, or above the placeable displays. If CONTROLS_ONLY
     *  is given, then no displays are created for placeable objects.
     *  <p>
     *  The show argument is a bitwise
     *  or of any of BUTTONS, TOP_PARAMETERS, or DIRECTOR_PARAMETERS.
     *  Or it can be 0, in which case, no controls are shown.
     *  If BUTTONS is included, then a panel of buttons, go, pause,
     *  resume, and stop, are shown.  If TOP_PARAMETERS is included,
     *  then the top-level parameters of the model are included.
     *  If DIRECTOR_PARAMETERS is included, then the parameters of
     *  the director are included.
     *  @param model The model to control.
     *  @param layout HORIZONTAL or VERTICAL layout.
     *  @param show Indicator of which controls to show.
     */
    public ModelPane(final CompositeActor model, int layout, int show) {

        if (layout == HORIZONTAL) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        } else {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        _layout = layout;

        if (show != 0) {
            // Add run controls.
            _controlPanel = new JPanel();
            _controlPanel.setLayout(new BoxLayout(
                    _controlPanel, BoxLayout.Y_AXIS));
            _controlPanel.setBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5));
            // Add a listener that requests the focus when we click
            // in the pane. This allows keyboard bindings to work.
            ClickListener clickListener = new ClickListener();
            _controlPanel.addMouseListener(clickListener);
            _controlPanel.addKeyListener(new CommandListener());

            if ((show & BUTTONS) != 0) {
                _buttonPanel = new JPanel();
                _buttonPanel.setLayout(new BoxLayout(
                        _buttonPanel, BoxLayout.X_AXIS));
                _buttonPanel.addMouseListener(clickListener);

                // Padding top and bottom...
                _buttonPanel.setBorder(
                        BorderFactory.createEmptyBorder(10, 0, 10, 0));
                _buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

                _goButton = new JButton("Go");
                _goButton.setToolTipText("Execute the model");
                _goButton.setAlignmentX(LEFT_ALIGNMENT);
                _buttonPanel.add(_goButton);
                _buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                _goButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            startRun();
                        }
                    });

                _pauseButton = new JButton("Pause");
                _pauseButton.setToolTipText("Pause execution of the model");
                _buttonPanel.add(_pauseButton);
                _buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                _pauseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            pauseRun();
                        }
                    });

                _resumeButton = new JButton("Resume");
                _resumeButton.setToolTipText("Resume executing the model");
                _buttonPanel.add(_resumeButton);
                _buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                _resumeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            resumeRun();
                        }
                    });

                _stopButton = new JButton("Stop");
                _stopButton.setToolTipText("Stop executing the model");
                _buttonPanel.add(_stopButton);
                _stopButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            stopRun();
                        }
                    });
                _controlPanel.add(_buttonPanel);
                _buttonPanel.setBackground(null);
            }
            add(_controlPanel);
            _controlPanel.setBackground(null);
        }

        _show = show;

        // Do this last so that the display pane for placeable objects
        // goes on the right or below.
        setModel(model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the container for model displays.
     *  @return A container for graphical displays.
     */
    public Container getDisplayPane() {
        if (_displays == null) {
            _displays = new JPanel();
            _displays.setBackground(null);
            add(_displays);
        }
        return _displays;
    }

    /** Get the associated model.
     *  @return The associated model.
     */
    public CompositeActor getModel() {
        return _model;
    }

    /** If the model has a manager and is executing, then
     *  pause execution by calling the pause() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void pauseRun() {
        if (_manager != null) {
            _manager.pause();
        }
    }

    /** If the model has a manager and is executing, then
     *  resume execution by calling the resume() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void resumeRun() {
        if (_manager != null) {
            _manager.resume();
        }
    }

    /** Make the Go button the default button for the root pane.
     *  You should call this after placing this pane in a container with
     *  a root pane.
     */
    public void setDefaultButton() {
        JRootPane root = getRootPane();
        if (root != null && (_show & BUTTONS) != 0) {
            root.setDefaultButton(_goButton);
            _goButton.setMnemonic(KeyEvent.VK_G);
            _pauseButton.setMnemonic(KeyEvent.VK_P);
            _resumeButton.setMnemonic(KeyEvent.VK_R);
            _stopButton.setMnemonic(KeyEvent.VK_S);
        }
    }

    /** Set the container for model displays.  This method sets the
     *  background of the specified pane to match that of this panel.
     *  @deprecated It is no longer necessary to specify a display pane.
     *   The displays are handled by setModel().
     */
    public void setDisplayPane(Container pane) {
        if (_displays != null) {
            remove(_displays);
        }
        _displays = pane;
        add(_displays);
        _displays.setBackground(null);
    }

    /** Set the associated model and add a query box with its top-level
     *  parameters, and those of its director, if it has one.
     *  All placeable objects in the hierarchy will get placed.
     *  @param model The associated model.
     */
    public void setModel(CompositeActor model) {
        // If there was a previous model, close its displays.
        _closeDisplays();
        _model = model;
        // For the new model, in case displays are open, close them.
        _closeDisplays();
        if (_parameterQuery != null) {
            _controlPanel.remove(_parameterQuery);
            _parameterQuery = null;
        }
        if (_directorQuery != null) {
            _controlPanel.remove(_directorQuery);
            _directorQuery = null;
        }
        if (model != null) {
            _manager = _model.getManager();

            if ((_show & TOP_PARAMETERS) != 0) {
                List parameterList = _model.attributeList(Parameter.class);
                if (parameterList.size() > 0) {
                    JLabel pTitle = new JLabel("Model parameters:");
                    // Use a dark blue for the text color.
                    pTitle.setForeground(new Color(0, 0, 128));
                    _controlPanel.add(pTitle);
                    _controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                    _parameterQuery = new Configurer(model);
                    if (_layout == HORIZONTAL) {
                        _parameterQuery.setAlignmentX(LEFT_ALIGNMENT);
                    } else {
                        // If we don't align this with CENTER, then
                        // the scrollbars in the Query get messed up when
                        // the orientation of an applet is vertical
                        // See SDF/Eye for an applet that displays
                        // toplevel parameters, but not director parameters.
                        _parameterQuery.setAlignmentX(CENTER_ALIGNMENT);
                    }
                    _parameterQuery.setBackground(null);
                    _controlPanel.add(_parameterQuery);
                    if ((_show & DIRECTOR_PARAMETERS) != 0) {
                        _controlPanel.add(Box.createRigidArea(
                                new Dimension(0, 15)));
                    }
                }
            }

            if ((_show & DIRECTOR_PARAMETERS) != 0) {
                // Director parameters.
                Director director = _model.getDirector();
                if (director != null) {
                    List dirParameterList =
                        director.attributeList(Parameter.class);
                    if (dirParameterList.size() > 0) {
                        JLabel pTitle = new JLabel("Director parameters:");
                        // Use a dark blue for the text color.
                        pTitle.setForeground(new Color(0, 0, 128));
                        _controlPanel.add(pTitle);
                        _controlPanel.add(
                                Box.createRigidArea(new Dimension(0, 8)));
                        _directorQuery = new Configurer(director);
                        if (_layout == HORIZONTAL) {
                            _directorQuery.setAlignmentX(LEFT_ALIGNMENT);
                        } else {
                            // If we don't align this with CENTER, then
                            // the scrollbars in the Query get messed up when
                            // the orientation of an applet is vertical
                            _directorQuery.setAlignmentX(CENTER_ALIGNMENT);
                        }
                        _directorQuery.setBackground(null);
                        _controlPanel.add(_directorQuery);
                    }
                }
            }

            if (_controlPanel != null && _layout == HORIZONTAL) {
                // Why they call this glue is beyond me, but what it does
                // is make extra space to fill in the bottom.
                _controlPanel.add(Box.createVerticalGlue());
            }

            // If there are two queries, make them the same width.
            if (_parameterQuery != null && _directorQuery != null) {
                Dimension modelSize = _parameterQuery.getPreferredSize();
                Dimension directorSize = _directorQuery.getPreferredSize();
                if (directorSize.width > modelSize.width) {
                    _parameterQuery.setPreferredSize(new Dimension(
                            directorSize.width,
                            modelSize.height));
                } else {
                    _directorQuery.setPreferredSize(new Dimension(
                            modelSize.width,
                            directorSize.height));
                }
            }

            if (_layout != CONTROLS_ONLY) {
                _createPlaceable(model);
            }
        }
    }

    /** If the model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     */
    public void startRun() {
        if (_manager != null) {
            try {
                _manager.startRun();
            } catch (IllegalActionException ex) {
                // Model is already running.  Ignore.
            }
        }
    }

    /** If the model has a manager and is executing, then
     *  stop execution by calling the stop() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void stopRun() {
        if (_manager != null) {
            _manager.stop();
        }
    }

    /** Notify the contained instances of PtolemyQuery that the window
     *  has been closed, and remove all Placeable displays by calling
     *  place(null).  This method is called if this pane is contained
     *  within a container that supports such notification.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    public void windowClosed(Window window, String button) {
        if (_directorQuery != null) {
            _directorQuery.windowClosed(window, button);
        }
        if (_parameterQuery != null) {
            _parameterQuery.windowClosed(window, button);
        }
        if (_model != null) {
            _closeDisplays();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator to use a horizontal layout. */
    public static int HORIZONTAL = 0;

    /** Indicator to use a vertical layout. */
    public static int VERTICAL = 1;

    /** Indicator to create only buttons. */
    public static int CONTROLS_ONLY = 2;

    /** Indicator to include control buttons. */
    public static int BUTTONS = 1;

    /** Indicator to include top-level parameters in the controls. */
    public static int TOP_PARAMETERS = 2;

    /** Indicator to include director parameters in the controls. */
    public static int DIRECTOR_PARAMETERS = 4;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Place the placeable objects in the model in the display pane.
     *  This method places all placeables vertically. Derived classes
     *  may override this method if the placeable objects are to be
     *  placed differently.
     *  @param model The model that contains the placeable objects.
     */
    protected void _createPlaceable(CompositeActor model) {
        if (_displays != null) {
            remove(_displays);
            _displays = null;
        }
        // place the placeable objects in the model
        _displays = new JPanel();

        add(_displays);
        _displays.setLayout(new BoxLayout(_displays, BoxLayout.Y_AXIS));
        _displays.setBackground(null);

        // Put placeable objects in a reasonable place.
        Iterator atomicEntities = model
            .allAtomicEntityList().iterator();
        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();
            if (object instanceof Placeable) {
                ((Placeable) object).place(_displays);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A panel into which to place model displays. */
    protected Container _displays;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The panel for the control buttons.
    private JPanel _buttonPanel;

    // The control panel on the left.
    private JPanel _controlPanel;

    // The query box for the director parameters.
    private Configurer _directorQuery;

    // The go button.
    private JButton _goButton;

    // The layout specified in the constructor.
    private int _layout;

    // The manager of the associated model.
    private Manager _manager;

    // The associated model.
    private CompositeActor _model;

    // The query box for the top-level parameters.
    private Configurer _parameterQuery;

    // The stop button.
    private JButton _stopButton;

    // The pause button.
    private JButton _pauseButton;

    // The resume button.
    private JButton _resumeButton;

    // Indicator given to the constructor of how much to show.
    private int _show;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Close any open displays by calling place(null).
     */
    private void _closeDisplays() {
        if (_model != null) {
            Iterator atomicEntities = _model.allAtomicEntityList().iterator();
            while (atomicEntities.hasNext()) {
                Object object = atomicEntities.next();
                if (object instanceof Placeable) {
                    ((Placeable) object).place(null);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class ClickListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            _controlPanel.requestFocus();
        }
    }

    private class CommandListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();
            switch(keycode) {
            case KeyEvent.VK_CONTROL:
                _control = true;
                break;
            case KeyEvent.VK_SHIFT:
                _shift = true;
                break;
            case KeyEvent.VK_ENTER:
                startRun();
                break;
            case KeyEvent.VK_G:
                if (_control) {
                    startRun();
                }
                break;
            case KeyEvent.VK_H:
                if (_control) {
                    MessageHandler.message(_helpString);
                }
                break;
            case KeyEvent.VK_M:
                if (_control && _model != null) {
                    System.out.println(_model.exportMoML());
                    MessageHandler.message("Exported MoML to standard out.");
                }
                break;
            case KeyEvent.VK_P:
                if (_control) {
                    pauseRun();
                }
                break;
            case KeyEvent.VK_R:
                if (_control) {
                    resumeRun();
                }
                break;
            case KeyEvent.VK_S:
                if (_control) {
                    stopRun();
                }
                break;
            case KeyEvent.VK_SLASH:
                if (_shift) {
                    // Question mark is SHIFT-SLASH
                    MessageHandler.message(_helpString);
                }
                break;
            default:
                // None
            }
        }

        public void keyReleased(KeyEvent e) {
            int keycode = e.getKeyCode();
            switch(keycode) {
            case KeyEvent.VK_CONTROL:
                _control = false;
                break;
            case KeyEvent.VK_SHIFT:
                _shift = false;
                break;
            default:
                // None
            }
        }

        private boolean _control = false;
        private boolean _shift = false;
        private String _helpString = "Key bindings in button panel:\n"
        + "  Control-G: Start a run.\n"
        + "  Control-H: Display help.\n"
        + "  Control-M: Export MoML to standard out.\n"
        + "  Control-P: Pause a run.\n"
        + "  Control-R: Resume a run.\n"
        + "  Control-S: Stop a run.\n"
        + "  Control-?: Display help.\n";
    }
}
