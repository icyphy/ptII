/*
Created on 01 sept. 2003

Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (jerome.blanc@thalesgroup.com)
@AcceptedRating
*/
package thales.vergil.navigable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import thales.actor.gui.NavigableEffigy;
import thales.vergil.SingleWindowApplication;

//////////////////////////////////////////////////////////////////////////
//// NavigableActorGraphFrame
/**
<p>Titre : NavigableActorGraphFrame</p>
<p>Description : This is a simple copy of the actuel ActorGraphFrame
with additional functionalities for the navigation.</p>
<p>Société : Thales Research and technology</p>

@author Jérôme Blanc & Benoit Masson
01 sept. 2003
@since Ptolemy II 3.1
*/
public class NavigableActorGraphFrame extends ActorGraphFrame {

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
    public NavigableActorGraphFrame(CompositeEntity entity, Tableau tableau) {
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
    public NavigableActorGraphFrame(
            CompositeEntity entity,
            Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        diva.gui.GUIUtilities.addToolBarButton(_toolbar, _upAction);

        URL img =
            getClass().getResource(
                    "/thales/vergil/navigable/img/fullScreen.gif");
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            ((JButton) _toolbar.getComponent(4)).setIcon(icon);
        }

        // FIXME: Use the strategy pattern instead of removing and
        // replacing these.
        _palettePane.remove(_libraryScrollPane);
        _palettePane.remove(_graphPanner);

        JScrollPane _treeVergil = new JScrollPane();
        NavigationTreeModel treeModel = null;
        Effigy effigy = getEffigy();
        if (effigy instanceof NavigableEffigy) {
            treeModel = ((NavigableEffigy) effigy).getNavigationModel();
        }

        _tree = new NavigationPTree(treeModel);
        _treeVergil.getViewport().add(_tree);
        _tabbedPalette.add("Navigation", _treeVergil);

        _tabbedPalette.add("Palette", _libraryScrollPane);

        _palettePane.add(_tabbedPalette, BorderLayout.CENTER);
        _palettePane.add(_graphPanner, BorderLayout.SOUTH);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the navigation tree.
     *  @return The navigation tree.
     */
    public NavigationPTree getTree() {
        return _tree;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected JTabbedPane _tabbedPalette = new JTabbedPane(JTabbedPane.BOTTOM);

    protected UpAction _upAction = new UpAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    protected ActorEditorGraphController _controller;

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;
    
    // The navigation tree.
    private NavigationPTree _tree;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Action to move up in the hierarchy in the tree view.
     */
    public class UpAction extends AbstractAction {
        public UpAction() {
            super("Up");
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            URL img =
                getClass().getResource("/thales/vergil/navigable/img/Up.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", "Up");
            putValue(
                    diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_EQUALS,
                            java.awt.Event.CTRL_MASK));
            putValue(
                    diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_M));
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj toOpen = (NamedObj) getModel().getContainer();
            if (toOpen != null) {
                Configuration configuration =
                    SingleWindowApplication._mainFrame.getConfiguration();
                try {
                    configuration.openModel(toOpen);
                } catch (IllegalActionException e1) {
                    e1.printStackTrace();
                } catch (NameDuplicationException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
