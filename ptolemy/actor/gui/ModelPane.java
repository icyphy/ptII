/* A panel containing controls for a Ptolemy II model.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

// Java imports
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Container;
import java.awt.Event;
import java.awt.event.*;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

//////////////////////////////////////////////////////////////////////////
//// ModelPane
/**

ModelPane is a panel for interacting with an executing Ptolemy II model.
It has controls for setting top-level and director parameters, a set of
buttons for controlling the execution, and a panel for displaying
results of the execution.

@author Edward A. Lee
@version $Id$
*/
public class ModelPane extends JPanel {

    /** Construct a panel for interacting with the specified Ptolemy II model.
     *  @param model The model to control.
     */
    public ModelPane(CompositeActor model) {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Add run controls.
        _controlPanel = new JPanel();
        _controlPanel.setLayout(new BoxLayout(_controlPanel, BoxLayout.Y_AXIS));
        _controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setModel(model);

        _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.X_AXIS));
        // Padding top and bottom...
        _buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        _buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

        _goButton = new JButton("Go");
        _goButton.setToolTipText("Execute the model");
        _goButton.setAlignmentX(LEFT_ALIGNMENT);
        _buttonPanel.add(_goButton);
        _buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        _goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startRun();
            }
        });

        _stopButton = new JButton("Stop");
        _stopButton.setToolTipText("Stop executing the model");
        _buttonPanel.add(_stopButton);
        _stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopRun();
            }
        });
        _controlPanel.add(_buttonPanel);
        add(_controlPanel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the container for model displays.
     *  @return A container for graphical displays.
     */
    public Container getDisplayPane() {
        if (_displays == null) {
            _displays = new JPanel();
            _displays.setBackground(getBackground());
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

    /** Set background color.  This overrides the base class to set the
     *  background of contained objects.
     *  @param background The background color.
     */
    public void setBackground(Color background) {
        super.setBackground(background);
        // This seems to be called in a base class constructor, before
        // these variables have been set.
        if (_controlPanel != null) _controlPanel.setBackground(background);
        if (_buttonPanel != null) _buttonPanel.setBackground(background);
        if (_paramQuery != null) _paramQuery.setBackground(background);
        if (_directorQuery != null) _directorQuery.setBackground(background);
        if (_displays != null) _displays.setBackground(background);
        if (_paramQuery != null) _paramQuery.setBackground(background);
        if (_directorQuery != null) _directorQuery.setBackground(background);
    }

    /** Make the Go button the default button for the root pane.
     *  You should call this after placing this pane in a container with
     *  a root pane.
     */
    public void setDefaultButton() {
        JRootPane root = getRootPane();
        if (root != null) {
            root.setDefaultButton(_goButton);
            _goButton.setMnemonic(KeyEvent.VK_G);
            _stopButton.setMnemonic(KeyEvent.VK_S);
        }
    }

    /** Set the container for model displays.  This method sets the
     *  background of the specified pane to match that of this panel.
     *  @return A container for graphical displays.
     */
    public void setDisplayPane(Container pane) {
        if (_displays != null) {
            remove(_displays);
        }
        _displays = pane;
        _displays.setBackground(getBackground());
        add(_displays);
    }

    /** Set the associated model and add a query box with its top-level
     *  parameters, and those of its director, if it has one.
     *  @param model The associated model.
     */
    public void setModel(CompositeActor model) {
        _model = model;
        if (_paramQuery != null) {
            _controlPanel.remove(_paramQuery);
            _paramQuery = null;
        }
        if (_directorQuery != null) {
            _controlPanel.remove(_directorQuery);
            _directorQuery = null;
        }
        if (model != null) {
            _manager = _model.getManager();

            List paramList = _model.attributeList(Parameter.class);
            if (paramList.size() > 0) {
                JLabel pTitle = new JLabel("Model parameters:");
                // Use a dark blue for the text color.
                pTitle.setForeground(new Color(0, 0, 128));
                _controlPanel.add(pTitle);
                _controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                _paramQuery = new PtolemyQuery();
                _paramQuery.setAlignmentX(LEFT_ALIGNMENT);
                _paramQuery.setBackground(getBackground());
                Iterator params = paramList.iterator();
                while (params.hasNext()) {
                    Parameter param = (Parameter)params.next();
                    String name = param.getName();
                    _paramQuery.addLine(name, name,
                            param.stringRepresentation());
                    _paramQuery.attachParameter(param, name);
                }
                _controlPanel.add(_paramQuery);
                _controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            // Director parameters.
            Director director = _model.getDirector();
            if (director != null) {
                List dirParamList = director.attributeList(Parameter.class);
                if (dirParamList.size() > 0) {
                    JLabel pTitle = new JLabel("Director parameters:");
                    // Use a dark blue for the text color.
                    pTitle.setForeground(new Color(0, 0, 128));
                    _controlPanel.add(pTitle);
                    _controlPanel.add(
                            Box.createRigidArea(new Dimension(0, 8)));
                    _directorQuery = new PtolemyQuery();
                    _directorQuery.setAlignmentX(LEFT_ALIGNMENT);
                    _directorQuery.setBackground(getBackground());
                    Iterator params = dirParamList.iterator();
                    while (params.hasNext()) {
                        Parameter param = (Parameter)params.next();
                        String name = param.getName();
                        _directorQuery.addLine(name, name,
                                param.stringRepresentation());
                        _directorQuery.attachParameter(param, name);
                    }
                    _controlPanel.add(_directorQuery);

                    // If there are two queries, make them the same width.
                    if (_paramQuery != null) {
                        Dimension modelSize
                            = _paramQuery.getPreferredSize();
                        Dimension directorSize
                            = _directorQuery.getPreferredSize();
                        if (directorSize.width > modelSize.width) {
                            _paramQuery.setPreferredSize(new Dimension(
                                    directorSize.width,
                                    modelSize.height));
                        } else {
                            _directorQuery.setPreferredSize(new Dimension(
                                    modelSize.width,
                                    directorSize.height));
                        }
                    }
                }
            }

            // Why they call this glue is beyond me, but what it does
            // is make extra space to fill in the bottom.
            _controlPanel.add(Box.createVerticalGlue());
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
     *  stop execution by calling the finish() method of the manager.
     *  If there is no manager, do nothing.
     */
    public void stopRun() {
        if(_manager != null) {
            _manager.finish();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The panel for the control buttons.
    private JPanel _buttonPanel;

    // The control panel on the left.
    private JPanel _controlPanel;

    // The query box for the director parameters.
    private PtolemyQuery _directorQuery;

    // The go button.
    private JButton _goButton;

    // The manager of the associated model.
    private Manager _manager;

    // The associated model.
    private CompositeActor _model;

    // The query box for the top-level parameters.
    private PtolemyQuery _paramQuery;

    // The stop button.
    private JButton _stopButton;

    // A panel into which to place model displays.
    private Container _displays;
}
