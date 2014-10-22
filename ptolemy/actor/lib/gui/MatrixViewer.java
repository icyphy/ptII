/* An actor that displays matrix inputs.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
import java.awt.Container;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.AbstractPlaceableActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MatrixPane;
import ptolemy.actor.gui.MatrixTokenTableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TokenEffigy;
import ptolemy.actor.gui.TokenTableau;
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
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
public class MatrixViewer extends AbstractPlaceableActor {
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
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.MATRIX);

        width = new Parameter(this, "width", new IntToken(500));
        width.setTypeEquals(BaseType.INT);
        height = new Parameter(this, "height", new IntToken(300));
        height.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     */
    public TypedIOPort input;

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
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the expression of the
     *   attribute cannot be parsed or cannot be evaluated.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
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
    @Override
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
    @Override
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
                    _effigy = new TokenEffigy(containerEffigy,
                            containerEffigy.uniqueName("tokenEffigy"));

                    // The default identifier is "Unnamed", which is
                    // no good for two reasons: Wrong title bar label,
                    // and it causes a save-as to destroy the original window.
                    _effigy.identifier.setExpression(getFullName());

                    // The second argument prevents a status bar.
                    _frame = new TableauFrame(null, null, this);
                    _tableau = new MatrixTokenTableau(_effigy, "tokenTableau",
                            (TableauFrame) _frame);
                    ((TableauFrame) _frame).setTableau(_tableau);
                    setFrame(_frame);
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
    @Override
    public void place(Container container) {
        // If there was a previous container that doesn't match this one,
        // remove the pane from it.
        if (_container != null && _pane != null) {
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
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token in = input.get(0);

            if (_frame != null) {
                List<Token> tokens = new LinkedList<Token>();
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
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container == null) {
            _remove();
        }
    }

    /** Set a name to present to the user.
     *  <p>If the MatrixViewer window has been rendered, then the title of the
     *  MatrixViewer window will be updated to the value of the name parameter.</p>
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    @Override
    public void setDisplayName(String name) {
        super.setDisplayName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        if (_tableau != null) {
            _tableau.setTitle(name);
        }
    }

    /** Specify the associated frame and set its properties (size, etc.)
     *  to match those stored in the _windowProperties attribute. In this
     *  class, if the frame is null, close the tableau and set it to null.
     *  Once closed, the matrix window will not reopen unless _tableau is
     *  null.
     *  @param frame The associated frame.
     */
    @Override
    public void setFrame(JFrame frame) {

        super.setFrame(frame);

        if (frame == null && _tableau != null) {
            try {
                _tableau.setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException(ex);
            }
            _tableau = null;
            _effigy = null;
        }
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  <p>If the MatrixViewer window has been rendered, then the title of the
     *  MatrixViewer window will be updated to the value of the name parameter.</p>
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period
     *   or if the object is a derived object and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #getName()
     *  @see #getName(NamedObj)
     */
    @Override
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        super.setName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        if (_tableau != null) {
            _tableau.setTitle(name);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (_container != null && _pane != null) {
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

    /** Height of the matrix viewer in pixels. */
    private int _height;

    /** Pane with the matrix display. */
    private MatrixPane _pane = null;

    /** The tableau with the display, if any. */
    private TokenTableau _tableau;

    /** Width of the matrix viewer in pixels. */
    private int _width;
}
