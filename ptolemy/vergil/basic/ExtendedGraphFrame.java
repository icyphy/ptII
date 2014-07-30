/* An extended simple graph view for Ptolemy models

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import ptolemy.actor.gui.Tableau;
import ptolemy.gui.UndeferredGraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.LibraryAttribute;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ExtendedGraphFrame

/**
 An graph view for ptolemy models extended with the capability
 to display the model in full-screen mode.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public abstract class ExtendedGraphFrame extends BasicGraphFrame {
    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public ExtendedGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public ExtendedGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
        _initExtendedGraphFrame();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cancel full screen mode. Note that this should be called
     *  in the swing event thread.
     */
    public void cancelFullScreen() {
        if (_screen == null) {
            // Already canceled.
            return;
        }

        _screen.dispose();
        _screen = null;

        // Only include the palettePane and panner if there is an actor library.
        // The ptinyViewer configuration uses this.
        if ((CompositeEntity) getConfiguration().getEntity("actor library") != null) {
            // Put the component back into the original window.
            _splitPane.setRightComponent(_getRightComponent());

            // Restore association with the graph panner.
            _graphPanner.setCanvas(getJGraph());
        } else {
            getContentPane().add(_getRightComponent());
        }
        pack();
        show();
        UndeferredGraphicalMessageHandler.setContext(_previousDefaultContext);
        toFront();
        _getRightComponent().requestFocus();
    }

    /** Go to full screen.
     */
    public void fullScreen() {
        if (_screen != null) {
            // Already in full screen mode.
            _screen.toFront();
            return;
        }

        // NOTE: Do not make the original graph frame the owner,
        // because we are going to hide it, and if it is the owner,
        // then the new frame will be hidden also.
        _screen = new JDialog();
        _screen.getContentPane().setLayout(new BorderLayout());

        // Set to full-screen size.
        Toolkit toolkit = _screen.getToolkit();
        int width = toolkit.getScreenSize().width;
        int height = toolkit.getScreenSize().height;
        _screen.setSize(width, height);

        _screen.setUndecorated(true);
        _screen.getContentPane().add(getJGraph(), BorderLayout.CENTER);

        // NOTE: Have to avoid the following, which forces the
        // dialog to resize the preferred size of _jgraph, which
        // nullifies the call to setSize() above.
        // _screen.pack();
        _screen.setVisible(true);

        // Make the new screen the default context for modal messages.
        _previousDefaultContext = UndeferredGraphicalMessageHandler
                .getContext();
        UndeferredGraphicalMessageHandler.setContext(_screen);

        // NOTE: As usual with swing, what the UI does is pretty
        // random, and doesn't correlate much with the documentation.
        // The following two lines do not work if _screen is a
        // JWindow instead of a JDialog.  There is no apparent
        // reason for this, but this is why we use JDialog.
        // Unfortunately, apparently the JDialog does not appear
        // in the Windows task bar.
        _screen.toFront();
        getJGraph().requestFocus();

        _screen.setResizable(false);

        // Bind escape key to remove full-screen mode.
        ActionMap actionMap = getJGraph().getActionMap();

        // Use the action as both a key and the action.
        actionMap.put(_fullScreenAction, _fullScreenAction);

        InputMap inputMap = getJGraph().getInputMap();
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), _fullScreenAction);

        // The ptinyViewer configuration will have a null _graphPanner.
        if (_graphPanner != null) {
            // Remove association with the graph panner.
            _graphPanner.setCanvas(null);
        }

        setVisible(false);
    }

    /** Dispose of this frame.
     *     Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the
     *  dispose() method of the superclass,
     *  {@link ptolemy.vergil.basic.BasicGraphFrame}.
     */
    @Override
    public void dispose() {
        if (_debugClosing) {
            System.out.println("ExtendedGraphFrame.dispose() : "
                    + this.getName());
        }

        _fullScreenAction = null;
        super.dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Initialize this class.
     * In this base class, a button for the full screen action
     * is added to the toolbar.
     */
    protected void _initExtendedGraphFrame() {
        GUIUtilities.addToolBarButton(_toolbar, _fullScreenAction);
    }

    /** Create the menus that are used by this frame.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        _viewMenu.addSeparator();
        GUIUtilities.addHotKey(_getRightComponent(), _fullScreenAction);
        GUIUtilities.addMenuItem(_viewMenu, _fullScreenAction);
    }

    /** Invoke the close() method of the superclass and optionally
     *  print a debugging message.
     *  If {@link ptolemy.actor.gui.Tableau#_debugClosing} is
     *  true, then a message is printed to standard out.
     *  This method is used for debugging memory leaks.
     *  @return True if the close completes, and false otherwise.
     */
    @Override
    protected boolean _close() {
        if (_debugClosing) {
            System.out.println("ExtendedGraphFrame._close() : "
                    + this.getName());
        }

        return super._close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Action for displaying in full-screen mode. */
    private Action _fullScreenAction = new FullScreenAction("Full Screen");

    /** Default context for dialogs before going to full-screen mode. */
    private Component _previousDefaultContext;

    /** If we are in full-screen mode, this will be non-null. */
    private JDialog _screen;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// FullScreenAction
    /** An action to display in full-screen mode. */
    public class FullScreenAction extends AbstractAction implements KeyListener {
        /** Construct a full screen action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public FullScreenAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/fullscreen.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/basic/img/fullscreen_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/basic/img/fullscreen_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/basic/img/fullscreen_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description);

            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        }

        /** If we are in full-screen mode, then revert; otherwise, go
         *  to full-screen mode.
         *  @param e The action event, ignored by this method.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_screen == null) {
                fullScreen();
            } else {
                cancelFullScreen();
            }
        }

        /** React to a key press by removing full-screen mode.
         *  @param e The key event, ignored by this method.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (_screen != null) {
                cancelFullScreen();
            }
        }

        /** React to a key press by removing full-screen mode.
         *  @param e The key event, ignored by this method.
         */
        @Override
        public void keyReleased(KeyEvent e) {
            if (_screen != null) {
                cancelFullScreen();
            }
        }

        /** React to a key press by removing full-screen mode.
         *  @param e The key event, ignored by this method.
         */
        @Override
        public void keyTyped(KeyEvent e) {
            if (_screen != null) {
                cancelFullScreen();
            }
        }
    }
}
