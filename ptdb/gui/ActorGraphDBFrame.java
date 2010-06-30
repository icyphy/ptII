package ptdb.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ptdb.kernel.bl.load.LoadManager;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.MessageHandler;
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

        _loadModelFromDBAction = new LoadModelFromDBAction();
        _openSearchFrameAction = new OpenSearchFrameAction(this.getTableau());
        _saveModelToDBAction = new SaveModelToDBAction();
        _openDatabaseSetupAction = new DatabaseSetupAction();
        _simpleSearchAction = new SimpleSearchAction
                                (getModel(), this, getConfiguration());

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

            // LoadModelFromDBAction is a new internal class.
            GUIUtilities
                    .addHotKey(_getRightComponent(), _loadModelFromDBAction);
            GUIUtilities.addMenuItem(_dbMenu, _loadModelFromDBAction);

            GUIUtilities
                    .addHotKey(_getRightComponent(), _openSearchFrameAction);
            GUIUtilities.addMenuItem(_dbMenu, _openSearchFrameAction);
            
            GUIUtilities
                    .addHotKey(_getRightComponent(), _simpleSearchAction);
            GUIUtilities.addMenuItem(_dbMenu, _simpleSearchAction);

            GUIUtilities
                .addHotKey(_getRightComponent(), _saveModelToDBAction);
            GUIUtilities.addMenuItem(_dbMenu, _saveModelToDBAction);
            
            GUIUtilities
            .addHotKey(_getRightComponent(), _openDatabaseSetupAction);
            GUIUtilities.addMenuItem(_dbMenu, _openDatabaseSetupAction);
            
            // TODO: }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The database menu. */
    protected JMenu _dbMenu;

    /** The action for loading a model from the database. */
    protected Action _loadModelFromDBAction;

    /** The action for opening the search frame. */
    protected Action _openSearchFrameAction;

    /** The action for saving a model to the database. */
    protected Action _saveModelToDBAction;
    
    /** The action for performing a simple database search. */
    protected Action _simpleSearchAction;

    /** The action for saving a model to the database. */
    protected Action _openDatabaseSetupAction;

    ///////////////////////////////////////////////////////////////////
    ////         public inner classes                              ////

    /** Search dialog. */
    public class ModelSearchResults extends JDialog implements QueryListener {

        private JButton _Load_Button;
        private JButton _Cancel_Button;

        String _selectedModel = null;

        /** Construct the dialog box with all components and listeners. */
        public ModelSearchResults() {

            super();

            JPanel contentPane = new JPanel();

            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            _query = new Query();
            contentPane.add(_query);

            _Load_Button = new JButton("Load");
            _Load_Button.setMnemonic(KeyEvent.VK_ENTER);
            _Load_Button.setActionCommand("Load");

            // For this requirement, we are
            // just temporarily going to use a text box and pretend that the
            // content of the text box contains the model you wanted to load
            _query.addTextArea("searchResults", "Search Results", null,
                    Color.white, Color.black, 1, 8);

            contentPane.add(_Load_Button);
            _Load_Button.setHorizontalTextPosition(SwingConstants.CENTER);

            _Load_Button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {

                    _loadModel();
                    setVisible(false);

                }
            });

            _Cancel_Button = new JButton("Cancel");
            _Cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);
            _Cancel_Button.setActionCommand("Cancel");

            contentPane.add(_Cancel_Button);
            _Cancel_Button.setHorizontalTextPosition(SwingConstants.CENTER);

            _Cancel_Button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {

                    setVisible(false);

                }
            });

            _query.addQueryListener(this);
            _query.setBackground(getBackground());
            setContentPane(contentPane);

        }

        ///////////////////////////////////////////////////////////////////
        ////                    public methods                          ////

        /**
         * Called to notify that one of the entries has changed. The name of the
         * entry is passed as an argument.
         *
         * @param name
         *            The name of the entry.
         */
        public void changed(String name) {

            if (name.equals("searchResults")) {

                _selectedModel = _query.getStringValue(name);

            }

        }

        /** Load selected models into new frames **/
        private void _loadModel() {

            // For now we are only passing one model name string.
            // This will need to change when we allow loading of
            // Multiple models.

            try {

                String modelToFetch = _selectedModel;

                PtolemyEffigy effigy = LoadManager.loadModel(modelToFetch,
                        getConfiguration());

                if(effigy != null){
                    
                    effigy.showTableaux();
                    
                } else {
                    
                    JOptionPane
                    .showMessageDialog((Component) this,
                            "The specified model could " +
                            "not be found in the database.",
                            "Load Error",
                            JOptionPane.INFORMATION_MESSAGE, null);
                    
                }

            } catch (Exception e) {

                MessageHandler.error("Cannot load the specified model. ", e);

            }

            setVisible(false);

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                private inner classes                      ////

    ///////////////////////////////////////////////////////////////////
    //// LoadModelFromDB

    /**
     * Action to load a model from the model database.
     */
    private class LoadModelFromDBAction extends AbstractAction {
        /**
         * Create a new action to load a model from the database.
         */
        public LoadModelFromDBAction() {

            super("Load Model from Database");
            putValue("tooltip", "Load Model from Database");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));

        }

        public void actionPerformed(ActionEvent e) {

            // When search is implemented, it will open the search dialog.
            // For now, we are directly opening a temporary form
            // with a prefilled model result.
            // We're using JDialog in case we later decide we want a Wizard
            // based
            // search.

            JDialog dialog = new ModelSearchResults();
            dialog.pack();
            dialog.setVisible(true);

        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenSearchFrameAction

    private class OpenSearchFrameAction extends AbstractAction {

        public OpenSearchFrameAction(Tableau tableau) {
            super("Pattern Database Search");

            putValue("tooltip", "Pattern Database Search");
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
                        new Tableau(effigy, "DBSearchframe"));

                frame.setBackground(BACKGROUND_COLOR);
                frame.pack();
                frame.centerOnScreen();
                frame.setVisible(true);

            } catch (Exception e2) {

                e2.printStackTrace();
            }

        }
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

            super("Save Model to Database");
            
            putValue("tooltip", "Save Model to Database");
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
        public SimpleSearchAction(NamedObj model, 
                JFrame frame, Configuration configuration) {

            super("Simple Database Search");
            
            putValue("tooltip", "Simple Database Search");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));

            _containerModel = model;
            _sourceFrame = frame;
            _configuration = configuration;
            
        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new SimpleSearchFrame(_containerModel, 
                    _sourceFrame, _configuration);
            frame.pack();
            frame.setVisible(true);

        }
        
        private NamedObj _containerModel;
        private JFrame _sourceFrame;
        private Configuration _configuration;
        
    }
}
