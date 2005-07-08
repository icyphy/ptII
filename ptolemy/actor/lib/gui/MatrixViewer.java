/* An actor that displays matrix inputs.

 Copyright (c) 1997-2005 The Regents of the University of California.
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


 */
package ptolemy.actor.lib.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MatrixPane;
import ptolemy.actor.gui.MatrixTokenTableau;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TokenEffigy;
import ptolemy.actor.gui.TokenTableau;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// MatrixViewer

/**
 An actor that displays the contents of a matrix input. This
 actor has a single input port, which only accepts MatrixTokens. One
 token is consumed per firing (in postfire()).  The data in the MatrixToken is
 displayed in a table format with scrollbars, using the swing JTable
 class.

 @author Bart Kienhuis and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (kienhuis)
 @Pt.AcceptedRating Red (kienhuis)
 */
public class MatrixViewer extends Sink implements Placeable {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixViewer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(false);
        input.setTypeEquals(BaseType.MATRIX);

        width = new Parameter(this, "width", new IntToken(500));
        width.setTypeEquals(BaseType.INT);
        height = new Parameter(this, "height", new IntToken(300));
        height.setTypeEquals(BaseType.INT);

        _windowProperties = new WindowPropertiesAttribute(this,
                "_windowProperties");

        _paneSize = new SizeAttribute(this, "_paneSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the table in pixels. This must contain an
     *  integer.  If the table is larger than this specified width,
     *  then scrollbars will appear, or the column width is adjusted.
     *  The default value is 500.
     */
    public Parameter width;

    /** The height of the table in pixels. This must contain an
     *  integer.  If the table is larger than this specified width,
     *  then scrollbars will appear.  The default value is 300.
     */
    public Parameter height;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.  If the attribute is
     *  width or height then read the value of the attribute.
     *  @exception IllegalActionException If the expression of the
     *   attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Do not react to changes in _windowProperties.
        // Those properties are only used when originally opening a window.
        if (attribute == width) {
            _width = ((IntToken) width.getToken()).intValue();
        } else if (attribute == height) {
            _height = ((IntToken) height.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then removes association with graphical objects
     *  belonging to the original class.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MatrixViewer newObject = (MatrixViewer) super.clone(workspace);

        newObject._container = null;
        newObject._effigy = null;
        newObject._frame = null;
        newObject._pane = null;
        newObject._tableau = null;

        return newObject;
    }

    /** Initialize this matrix viewer. If place() has not been called
     *  with a container into which to place the display, then create a
     *  new frame into which to put it.
     *  @exception IllegalActionException If the parent class
     *   throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (_container == null) {
            // No current container for the pane.
            // Need an effigy and a tableau so that menu ops work properly.
            if (_tableau == null) {
                Effigy containerEffigy = Configuration.findEffigy(toplevel());

                if (containerEffigy == null) {
                    throw new IllegalActionException(this,
                            "Cannot find effigy for top level: "
                                    + toplevel().getFullName());
                }

                try {
                    _effigy = new TokenEffigy(containerEffigy, containerEffigy
                            .uniqueName("tokenEffigy"));

                    // The default identifier is "Unnamed", which is
                    // no good for two reasons: Wrong title bar label,
                    // and it causes a save-as to destroy the original window.
                    _effigy.identifier.setExpression(getFullName());

                    _frame = new DisplayWindow();
                    _tableau = new MatrixTokenTableau(_effigy, "tokenTableau",
                            _frame);
                    _frame.setTableau(_tableau);
                    _windowProperties.setProperties(_frame);

                    // Regrettably, since setSize() in swing doesn't actually
                    // set the size of the frame, we have to also set the
                    // size of the internal component.
                    Component[] components = _frame.getContentPane()
                            .getComponents();

                    if (components.length > 0) {
                        _paneSize.setSize(components[0]);
                    }

                    _tableau.show();
                } catch (Exception ex) {
                    throw new IllegalActionException(this, null, ex,
                            "Error creating effigy and tableau");
                }
            } else {
                // Erase previous text.
                _effigy.clear();

                if (_frame != null) {
                    // Do not use show() as it overrides manual placement.
                    // FIXME: So does setVisible()... But with neither one used,
                    // then if the user dismisses the window, it does not reappear
                    // on re-running!
                    // _frame.setVisible(true);
                    _frame.toFront();
                }
            }
        }
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of JTable will be added to that container. The
     *  table is configured such that a user cannot reorder the
     *  columns of the table. Also, the table maintains a fixed
     *  preferred size, and will employ scrollbars if the table is
     *  larger than the preferred size. If this method is not called,
     *  the JTable will be placed in its own frame. The table is also
     *  placed in its own frame if this method is called with a null
     *  argument. The background of the table is set equal to that of
     *  the container (unless it is null).
     *
     *  @param container The container into which to place the table.
     */
    public void place(Container container) {
        // If there was a previous container that doesn't match this one,
        // remove the pane from it.
        if ((_container != null) && (_pane != null)) {
            _container.remove(_pane);
            _container = null;
        }

        if (_frame != null) {
            _frame.dispose();
            _frame = null;
        }

        _container = container;

        if (container == null) {
            // Reset everything.
            if (_tableau != null) {
                // This will have the side effect of removing the effigy
                // from the directory if there are no more tableaux in it.
                try {
                    _tableau.setContainer(null);
                } catch (KernelException ex) {
                    throw new InternalErrorException(ex);
                }
            }

            _tableau = null;
            _effigy = null;
            _pane = null;

            return;
        }

        if (_pane == null) {
            // Create the pane.
            _pane = new MatrixPane();

            // FIXME: The following, as usual with Swing, doesn't work.
            Dimension size = new Dimension(_width, _height);
            _pane.setPreferredSize(size);
            _pane.setSize(size);
        }

        // Place the pane in supplied container.
        _container.add(_pane, BorderLayout.CENTER);
    }

    /** Consume a matrix token from the <i>input</i> port
     *  and display the token in a table.  If a token is not available,
     *  do nothing.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token in = input.get(0);

            if (_frame != null) {
                List tokens = new LinkedList();
                tokens.add(in);
                _effigy.setTokens(tokens);
            } else if (_pane != null) {
                _pane.display((MatrixToken) in);
            }
        }

        return super.postfire();
    }

    /** Override the base class to remove the display from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container == null) {
            _remove();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object. This
     *  overrides the base class to make sure that the current frame
     *  properties, if there is a frame, are recorded.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // Make sure that the current position of the frame, if any,
        // is up to date.
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);

            // Regrettably, have to also record the size of the contents
            // because in Swing, setSize() methods do not set the size.
            // Only the first component size is recorded.
            Component[] components = _frame.getContentPane().getComponents();

            if (components.length > 0) {
                _paneSize.recordSize(components[0]);
            }
        }

        super._exportMoMLContents(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if ((_container != null) && (_pane != null)) {
                    _container.remove(_pane);
                    _container = null;
                }

                if (_frame != null) {
                    _frame.dispose();
                    _frame = null;
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Container with the display, if any. */
    private Container _container = null;

    /** The effigy for the token data. */
    private TokenEffigy _effigy;

    /** The frame, if one is used. */
    private DisplayWindow _frame = null;

    /** Height of the matrix viewer in pixels. */
    private int _height;

    /** Pane with the matrix display. */
    private MatrixPane _pane = null;

    /** A specification of the size of the pane if it's in its own window. */
    private SizeAttribute _paneSize;

    /** The tableau with the display, if any. */
    private TokenTableau _tableau;

    /** Width of the matrix viewer in pixels. */
    private int _width;

    /** A specification for the window properties of the frame. */
    private WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of TableauFrame that removes its association with the
     *  MatrixViewer upon closing, and also records the size of the display.
     */
    private class DisplayWindow extends TableauFrame {
        /** Construct an empty window.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear
         *  and setTableau() to associate it with a tableau.
         */
        public DisplayWindow() {
            // The null second argument prevents a status bar.
            super(null, null);
        }

        /** Close the window.  This overrides the base class to remove
         *  the association with the MatrixViewer and to record window
         *  properties.
         *  @return True.
         */
        protected boolean _close() {
            // Record the window properties before closing.
            _windowProperties.recordProperties(this);

            // Regrettably, have to also record the size of the contents
            // because in Swing, setSize() methods do not set the size.
            // Only the first component size is recorded.
            Component[] components = getContentPane().getComponents();

            if (components.length > 0) {
                _paneSize.recordSize(components[0]);
            }

            super._close();
            place(null);
            return true;
        }
    }
}
