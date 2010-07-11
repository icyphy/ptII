/*
@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;

import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ActorGraphDBFrame

/**
 * An extended graph editor frame containing the ability to interface with a
 * model database via the Database menu.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class ActorGraphDBFrame extends ActorGraphFrame implements
        ActionListener {

    /**
     * Construct a frame associated with the specified Ptolemy II model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one) or the default library
     * defined in the configuration.
     *
     * @see Tableau#show()
     * @param entity
     *            The model to put in this frame.
     * @param tableau
     *            The tableau responsible for this frame.
     */
    public ActorGraphDBFrame(CompositeEntity entity, Tableau tableau) {

        this(entity, tableau, null);

    }

    /**
     * Construct a frame associated with the specified Ptolemy II model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one), or the
     * <i>defaultLibrary</i> argument (if it is non-null), or the default
     * library defined in the configuration.
     *
     * @see Tableau#show()
     * @param entity
     *            The model to put in this frame.
     * @param tableau
     *            The tableau responsible for this frame.
     * @param defaultLibrary
     *            An attribute specifying the default library to use if the
     *            model does not have a library.
     */
    public ActorGraphDBFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {

        super(entity, tableau, defaultLibrary);
        _initActorGraphDBFrame();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Initialize this class. Various actions are instantiated.
     */
    protected void _initActorGraphDBFrame() {

        _openSearchFrameAction = new OpenSearchFrameAction(this.getTableau());
        _saveModelToDBAction = new SaveModelToDBAction();
        _openDatabaseSetupAction = new DatabaseSetupAction();
        _simpleSearchAction = new SimpleSearchAction(getModel(), this,
                getConfiguration());
        _configureAttributesAction = new ConfigureAttributesAction();

    }

    /**
     * Create the menus that are used by this frame. It is essential that
     * _createGraphPane() be called before this.
     */
    protected void _addMenus() {

        super._addMenus();

        // Until some useful functionality is complete:
        if (true) {

            // Create database menu.
            _dbMenu = new JMenu("Database");
            _dbMenu.setMnemonic(KeyEvent.VK_B);
            _menubar.add(_dbMenu);

            // Add menu items if database connection has been established.
            // TODO: if (DB IS CONNECTED) {

            GUIUtilities
                .addHotKey(_getRightComponent(), _saveModelToDBAction);
            GUIUtilities.addMenuItem(_dbMenu, _saveModelToDBAction);
           
            
            // Create search menu.
            JMenu searchMenu = new JMenu("Search");
            searchMenu.setMnemonic(KeyEvent.VK_C);
            _dbMenu.add(searchMenu);

            GUIUtilities
            .addHotKey(_getRightComponent(), _simpleSearchAction);
            GUIUtilities.addMenuItem(searchMenu, _simpleSearchAction);
            
            GUIUtilities
                    .addHotKey(_getRightComponent(), _openSearchFrameAction);
            GUIUtilities.addMenuItem(searchMenu, _openSearchFrameAction);
            

            GUIUtilities.addHotKey(_getRightComponent(),
                    _openDatabaseSetupAction);
            GUIUtilities.addMenuItem(_dbMenu, _openDatabaseSetupAction);

            GUIUtilities.addHotKey(_getRightComponent(),
                    _configureAttributesAction);
            GUIUtilities.addMenuItem(_dbMenu, _configureAttributesAction);

            // TODO: }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The action for opening the attributes configuration frame. 
     */
    protected Action _configureAttributesAction;

    /** The database menu. */
    protected JMenu _dbMenu;

    /** The action for opening the search frame. */
    protected Action _openSearchFrameAction;

    /** The action for saving a model to the database. */
    protected Action _saveModelToDBAction;

    /** The action for performing a simple database search. */
    protected Action _simpleSearchAction;

    /** The action for saving a model to the database. */
    protected Action _openDatabaseSetupAction;

    ///////////////////////////////////////////////////////////////////
    ////                private inner classes                      ////

    ///////////////////////////////////////////////////////////////////
    //// ConfigureAttributesAction

    private class ConfigureAttributesAction extends AbstractAction {

        public ConfigureAttributesAction() {
            super("Configure Attributes");

            putValue("tooltip", "Configure Attributes");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        public void actionPerformed(ActionEvent e) {

            new ConfigureAttributesFrame().setVisible(true);

        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenSearchFrameAction

    private class OpenSearchFrameAction extends AbstractAction {

        public OpenSearchFrameAction(Tableau tableau) {
            super("Pattern Search");
            _tableau = tableau;
            putValue("tooltip", "Pattern Search");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        public void actionPerformed(ActionEvent e) {

            URL toRead = getClass().getClassLoader().getResource(
                    "ptolemy/actor/gt/controller/ModelBasedTransformation.xml");

            try {

                EffigyFactory effigyFactory = new EffigyFactory(
                        ActorGraphDBFrame.this.getConfiguration().workspace());

                PtolemyEffigy.Factory ptolemyEffigyFactory = new PtolemyEffigy.Factory(
                        effigyFactory, "new effigy factory");

                Effigy effigy = ptolemyEffigyFactory.createEffigy(
                        ActorGraphDBFrame.this.getConfiguration()
                                .getDirectory(), null, toRead);

                CompositeEntity compositeEntity = new TransformationRule(
                        effigy, "transformation rule");

                ExtendedGraphFrame frame = new DbSearchFrame(compositeEntity,
                        new Tableau(effigy, "DBSearchframe"),
                        ((ActorGraphDBTableau) _tableau).getGtLibrary(),
                        getModel(), ActorGraphDBFrame.this);

                frame.setBackground(BACKGROUND_COLOR);
                frame.pack();
                frame.centerOnScreen();
                frame.setVisible(true);

            } catch (Exception e2) {

                e2.printStackTrace();
            }

        }

        private Tableau _tableau;
    }

    ///////////////////////////////////////////////////////////////////
    //// SaveModelToDBAction

    /**
     * Save the model to database.
     */
    private class SaveModelToDBAction extends AbstractAction {
        /**
         * Create a new action to save a model to the database.
         * 
         */
        public SaveModelToDBAction() {

            super("Save to Database");

            putValue("tooltip", "Save to Database");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));

        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new SaveModelToDBFrame(getModel());
            frame.pack();
            frame.setVisible(true);

        }

    }

    ///////////////////////////////////////////////////////////////////
    //// DatabaseSetupAction

    /**
     * Setup database connection.
     */
    private class DatabaseSetupAction extends AbstractAction {
        /**
         * Create a new action to setup database connection.
         * 
         */
        public DatabaseSetupAction() {

            super("Setup Database Connection");

            putValue("tooltip", "Setup Database Connection");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));

        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new DatabaseSetupFrame();
            frame.pack();
            frame.setVisible(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SimpleSearchAction

    /**
     * Perform a simple database search.
     */
    private class SimpleSearchAction extends AbstractAction {
        /**
         * Create a new action to save a model to the database.
         * @param model 
         *      The model into which search results would be imported.
         * @param frame 
         *      The editing frame from which the simple search window will
         *      open.
         * @param configuration
         *      The configuration under which models from the database will
         *      be loaded. 
         * 
         */
        public SimpleSearchAction(NamedObj model, JFrame frame,
                Configuration configuration) {

            super("Quick Search");

            putValue("tooltip", "Quick Search");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));

            _containerModel = model;
            _sourceFrame = frame;
            _configuration = configuration;

        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new SimpleSearchFrame(_containerModel, _sourceFrame,
                    _configuration);
            frame.pack();
            frame.setVisible(true);

        }

        private NamedObj _containerModel;
        private JFrame _sourceFrame;
        private Configuration _configuration;

    }
}
