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

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.LinkController;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.gt.TransformationEditor;

import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.FigureAction;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// GraphPatternSearchEditor

/**
 * The UI frame for the advanced DB search window. It has the specific 
 * searching features for PTDB.
 *
 * @author Alek Wang
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class GraphPatternSearchEditor extends TransformationEditor {

    //    /**
    //     * Construct the GraphPatternSearchEditor.
    //     *
    //     * @param entity  The model to put in this frame.
    //     * @param tableau The tableau responsible for this frame.
    //     * @param containerModel The model that will import the searched results. 
    //     * @param sourceFrame The frame that contains the model to import the 
    //     * searched results. 
    //     */
    //    public GraphPatternSearchEditor(CompositeEntity entity, Tableau tableau,
    //            NamedObj containerModel, JFrame sourceFrame) {
    //        
    //        this(entity,tableau, null, containerModel,sourceFrame);
    //
    //    }

    //    /**
    //     * Construct the GraphPatternSearchEditor.
    //     *
    //     * @param entity The model to put in this frame.
    //     * @param tableau The tableau responsible for this frame.
    //     * @param defaultLibrary An attribute specifying the default library to
    //     *   use if the model does not have a library.
    //     * @param containerModel The model that will import the searched results. 
    //     * @param sourceFrame The frame that contains the model to import the 
    //     * searched results. 
    //     */
    //    public GraphPatternSearchEditor(CompositeEntity entity, Tableau tableau,
    //            LibraryAttribute defaultLibrary, NamedObj containerModel,
    //            JFrame sourceFrame) {
    //        super(entity, tableau, defaultLibrary);
    //
    //        setTitle("Database Pattern Search");
    //
    //        _containerModel = containerModel;
    //        _sourceFrame = sourceFrame;
    //        
    //        setDefaultCloseOperation(HIDE_ON_CLOSE);
    //        
    //    }

    /**
     * Construct the GraphPatternSearchEditor.
     *
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     * @param defaultLibrary An attribute specifying the default library to
     *   use if the model does not have a library.
     * @param containerModel The model that will import the searched results. 
     * @param sourceFrame The frame that contains the model to import the 
     * searched results. 
     * @param simpleSearchFrame The Simple Search Frame instance that opens 
     * this frame. 
     */
    public GraphPatternSearchEditor(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary, NamedObj containerModel,
            JFrame sourceFrame, SimpleSearchFrame simpleSearchFrame) {

        //        this(entity,tableau,defaultLibrary,containerModel,sourceFrame);

        super(entity, tableau, defaultLibrary);

        setTitle("Database Pattern Search");

        _containerModel = containerModel;
        _sourceFrame = sourceFrame;

        setDefaultCloseOperation(HIDE_ON_CLOSE);

        _simpleSearchFrame = simpleSearchFrame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Fetch the search criteria of graph pattern specified in this frame. 
     * 
     * @param searchCriteria The search criteria object that to be set with 
     * the graph pattern criteria information. 
     */
    public void fetchSearchCriteria(SearchCriteria searchCriteria) {

        // Get the pattern specified by the user.
        TransformationRule rule = getFrameController().getTransformationRule();
        Pattern pattern = rule.getPattern();

        // Get the attributes list.
        ArrayList<Attribute> attributesList;
        if (searchCriteria.getAttributes() == null) {
            attributesList = new ArrayList<Attribute>();
        } else {
            attributesList = searchCriteria.getAttributes();
        }

        // Get the attributes from the pattern and add to the list. 
        List<NamedObj> attributes = pattern.attributeList();

        for (NamedObj attribute : attributes) {

            if (attribute instanceof Variable) {
                attributesList.add((Variable) attribute);
            }
        }

        // Set the attributes to the search criteria accordingly.
        searchCriteria.setAttributes(attributesList);

        // Create the graph pattern search criteria.
        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        // Get the ports specified by the user.
        List<Port> portsList = pattern.portList();
        ArrayList<Port> ports = new ArrayList<Port>();

        for (Iterator iterator = portsList.iterator(); iterator.hasNext();) {
            Port port = (Port) iterator.next();
            ports.add(port);
        }

        dbGraphSearchCriteria.setPortsList(ports);

        // Get the relations specified by the user.
        List<Relation> relationsList = pattern.relationList();
        ArrayList<Relation> relations = new ArrayList<Relation>();

        for (Iterator iterator = relationsList.iterator(); iterator.hasNext();) {
            Relation relation = (Relation) iterator.next();
            relations.add(relation);
        }

        dbGraphSearchCriteria.setRelationsList(relations);

        // Get the component and composite entities specified by the user.
        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();

        ArrayList<CompositeEntity> compositeEntities = new ArrayList<CompositeEntity>();

        for (Iterator iterator = pattern.entityList().iterator(); iterator
                .hasNext();) {
            Entity entity = (Entity) iterator.next();

            if (entity instanceof CompositeEntity) {
                compositeEntities.add((CompositeEntity) entity);
            } else if (entity instanceof ComponentEntity) {
                componentEntities.add((ComponentEntity) entity);
            }

        }

        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);
        dbGraphSearchCriteria.setCompositeEntities(compositeEntities);
        dbGraphSearchCriteria.setPattern(pattern);

        // Set the DBGraph search criteria to the whole search criteria.
        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _addMenus() {

        super._addMenus();

        // Remove the unused match actions from the rule menu. 
        _ruleMenu.remove(0);
        _ruleMenu.remove(0);
        _ruleMenu.remove(0);

        // Remove the full screen from the view menu.
        _viewMenu.remove(4);
        _viewMenu.remove(4);

        // Remove the fullscreen and open container tool bar icon. 
        _toolbar.remove(6);
        _toolbar.remove(6);

        // Remove the match actions icons from the tool bar. 
        _toolbar.remove(_toolbar.getComponentCount() - 1);
        _toolbar.remove(_toolbar.getComponentCount() - 1);

        // Add the menu of searching in the database.
        DBMatchAction dbMatchAction = new DBMatchAction();
        GUIUtilities.addToolBarButton(_toolbar, dbMatchAction);

        //        // Add the menu of opening simple search window. 
        //        SimpleSearchAction simpleSearchAction = new SimpleSearchAction();
        //        GUIUtilities.addToolBarButton(_toolbar, simpleSearchAction);

        // Remove the replacement and correspondence tabs.
        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        tabbedPane.remove(1);
        tabbedPane.remove(1);

    }

    @Override
    protected RunnableGraphController _createActorGraphController() {
        return new DBSearchFrameController();

    }

    /**
     * Close the pattern search window without asking anything. 
     * 
     * <p>When the user is closing this window, it just hides for the simple
     * search frame. So this does not require to ask whether the user wants to
     * save or not.</p>
     * 
     * @return true to close the window. 
     */
    @Override
    protected boolean _close() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected classes                 ////

    /**
     * Controller for this frame.
     */
    protected class DBSearchFrameController extends
            TransformationActorGraphController {

        protected DBSearchFrameController() {
            super();
        }

        protected void initializeInteraction() {
            super.initializeInteraction();
            Action oldConfigureAction = _configureAction;
            _configureAction = new DBSearchConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);
            _configureMenuFactory
                    .addMenuItemListener(GraphPatternSearchEditor.this);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                private classes                            ////

    private class DBMatchAction extends FigureAction {

        public DBMatchAction() {
            super("Match Model");

            GUIUtilities.addIcons(this,
                    new String[][] {
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.LARGE_ICON },
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.ROLLOVER_ICON },
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.ROLLOVER_SELECTED_ICON },
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Search Ptolemy models in Database"
                    + "(Ctrl+1)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        //////////////////////////////////////////////////////////////////////
        ////                public  methods                               ////

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            // Perform the clicking of the search button from the simple
            // search frame to perform the search. 
            _simpleSearchFrame.clickSearchButton(e);

            //         // create the new SearchResultFrame
            //            SearchResultsFrame searchResultsFrame = new SearchResultsFrame(
            //                    _containerModel, _sourceFrame, GraphPatternSearchEditor.this
            //                            .getConfiguration());
            //
            //            SearchResultBuffer searchResultBuffer = new SearchResultBuffer();
            //
            //            // register the result listener from the search result frame
            //            // to the search result buffer
            //            searchResultBuffer.addObserver(searchResultsFrame);
            //
            //            // get the search criteria
            //            // for this requirement, only the attributes part
            //            SearchCriteria searchCriteria = new SearchCriteria();
            //
            //            // get the pattern specified by the user
            //            TransformationRule rule = getFrameController()
            //                    .getTransformationRule();
            //            Pattern pattern = rule.getPattern();
            //
            //            // create a new arraylist to contain all the attributes
            //            ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
            //
            //            // TODO to delete later 
            //            //            try {
            //            //                // get the criteria information from the model configuration
            //            //                GTIngredientList ingredientAttributesList = pattern
            //            //                        .getCriteriaAttribute().getIngredientList();
            //            //
            //            //                for (Iterator iterator = ingredientAttributesList.iterator(); iterator
            //            //                        .hasNext();) {
            //            //                    GTIngredient gtIngredient = (GTIngredient) iterator.next();
            //            //
            //            //                    // only check the criteria related to attributes here.
            //            //                    if (!(gtIngredient instanceof PortCriterion)
            //            //                            && !(gtIngredient instanceof SubclassCriterion)) {
            //            //
            //            //                        PTDBSearchAttribute attribute = new PTDBSearchAttribute();
            //            //
            //            //                        attribute.setName(gtIngredient.getValue(0).toString());
            //            //
            //            //                        if (gtIngredient instanceof AttributeCriterion) {
            //            //                            attribute.setToken(gtIngredient.getValue(2)
            //            //                                    .toString());
            //            //                        }
            //            //
            //            //                        attributesList.add(attribute);
            //            //                    }
            //            //
            //            //                }
            //            //            } catch (MalformedStringException e1) {
            //            //
            //            //                // ignore
            //            //            } catch (IllegalActionException e2) {
            //            //
            //            //                // ignore
            //            //            } catch (NameDuplicationException e3) {
            //            //
            //            //                // ignore
            //            //            }
            //            // TODO to delete later 
            //
            //            // get the attributes from the pattern and add to the list
            //            List<NamedObj> attributes = pattern.attributeList();
            //
            //            for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            //                NamedObj attribute = (NamedObj) iterator.next();
            //
            //                if (attribute instanceof Variable) {
            //                    attributesList.add((Variable) attribute);
            //                }
            //            }
            //
            //            // Get the attributes from the simple search window. 
            //            if (_simpleSearchFrame != null) {
            //                // Get the attributes. 
            //                if (_simpleSearchFrame.getAttributes() != null) {
            //                    for (Attribute attribute : _simpleSearchFrame
            //                            .getAttributes()) {
            //                        attributesList.add(attribute);
            //                    }
            //                }
            //
            //                // Get the model name search criteria from the simple search window. 
            //                if (_simpleSearchFrame.getModelName() != null
            //                        && !_simpleSearchFrame.getModelName().trim().isEmpty()) {
            //                    searchCriteria.setModelName(_simpleSearchFrame
            //                            .getModelName());
            //                }
            //            }
            //
            //            // set the attributes to the search criteria accordingly
            //            searchCriteria.setAttributes(attributesList);
            //
            //            // Create the graph pattern search criteria
            //            DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();
            //
            //            // get the ports specified by the user
            //            List<Port> portsList = pattern.portList();
            //            ArrayList<Port> ports = new ArrayList<Port>();
            //
            //            for (Iterator iterator = portsList.iterator(); iterator.hasNext();) {
            //                Port port = (Port) iterator.next();
            //                ports.add(port);
            //            }
            //
            //            dbGraphSearchCriteria.setPortsList(ports);
            //
            //            // get the relations specified by the user
            //            List<Relation> relationsList = pattern.relationList();
            //            ArrayList<Relation> relations = new ArrayList<Relation>();
            //
            //            for (Iterator iterator = relationsList.iterator(); iterator
            //                    .hasNext();) {
            //                Relation relation = (Relation) iterator.next();
            //                relations.add(relation);
            //            }
            //
            //            dbGraphSearchCriteria.setRelationsList(relations);
            //
            //            // get the component entities specified by the user
            //            ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();
            //
            //            ArrayList<CompositeEntity> compositeEntities = new ArrayList<CompositeEntity>();
            //
            //            for (Iterator iterator = pattern.entityList().iterator(); iterator
            //                    .hasNext();) {
            //                Entity entity = (Entity) iterator.next();
            //
            //                if (entity instanceof CompositeEntity) {
            //                    compositeEntities.add((CompositeEntity) entity);
            //                } else if (entity instanceof ComponentEntity) {
            //                    componentEntities.add((ComponentEntity) entity);
            //                }
            //
            //            }
            //
            //            //            _getAtomicEntities(pattern, componentEntities);
            //
            //            dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);
            //            dbGraphSearchCriteria.setCompositeEntities(compositeEntities);
            //            dbGraphSearchCriteria.setPattern(pattern);
            //
            //            // set the DBGraph search criteria to the whole search criteria
            //            searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);
            //
            //            // Check whether any search criteria has been set.  At least one of
            //            // the attribute, model name, port, or component entity search 
            //            // criteria needs to be set in the search criteria. 
            //            if (attributesList.size() == 0
            //                    && pattern.portList().isEmpty()
            //                    && componentEntities.isEmpty()
            //                    && (searchCriteria.getModelName() == null || searchCriteria
            //                            .getModelName().trim().isEmpty())) {
            //                JOptionPane.showMessageDialog(GraphPatternSearchEditor.this,
            //                        "In order to narrow the search, please specify search criteria.  At least one"
            //                                + " of attribute, model name, port or"
            //                                + " component"
            //                                + " entity needs to be set in the search "
            //                                + "criteria.");
            //            } else {
            //
            //                // Show the search result frame.
            //                searchResultsFrame.pack();
            //                searchResultsFrame.setVisible(true);
            //
            //                // Call the Search Manager to trigger the search.
            //                SearchManager searchManager = new SearchManager();
            //                try {
            //                    searchManager.search(searchCriteria, searchResultBuffer);
            //
            //                } catch (DBConnectionException e1) {
            //                    searchResultsFrame.setVisible(false);
            //                    searchResultsFrame.dispose();
            //                    MessageHandler.error("Cannot perform the search now.", e1);
            //
            //                } catch (DBExecutionException e2) {
            //                    searchResultsFrame.setVisible(false);
            //                    searchResultsFrame.dispose();
            //                    MessageHandler.error("Cannot perform the search now.", e2);
            //                }
            //
            //                //            // TODO This is just for testing
            //                //                for (Iterator iterator = searchCriteria.getAttributes()
            //                //                        .iterator(); iterator.hasNext();) {
            //                //                    NamedObj namedObj = (NamedObj) iterator.next();
            //                //                    System.out.println("attribute: " + namedObj);
            //                //                    if (namedObj instanceof Variable) {
            //                //                        System.out.println("variable: "
            //                //                                + namedObj.getClassName());
            //                //                        try {
            //                //                            System.out.println(((Variable) namedObj).getToken()
            //                //                                    .getClass());
            //                //                        } catch (IllegalActionException e1) {
            //                //                            // TODO Auto-generated catch block
            //                //                            e1.printStackTrace();
            //                //                        }
            //                //                    }
            //                //
            //                //                }
            //
            //                //            // TODO this is just for testing the Graph part, delete later
            //                //                System.out.println("search criteira: "
            //                //                        + searchCriteria.getDBGraphSearchCriteria());
            //                //                System.out.println("components :"
            //                //                        + searchCriteria.getDBGraphSearchCriteria()
            //                //                                .getComponentEntitiesList());
            //                //
            //                //                for (Iterator iterator = searchCriteria
            //                //                        .getDBGraphSearchCriteria().getComponentEntitiesList()
            //                //                        .iterator(); iterator.hasNext();) {
            //                //                    ComponentEntity componentEntity = (ComponentEntity) iterator
            //                //                            .next();
            //                //                    System.out.println(componentEntity);
            //                //                }
            //                //
            //                //                System.out.println("composite entities: "
            //                //                        + searchCriteria.getDBGraphSearchCriteria()
            //                //                                .getCompositeEntities());
            //                //
            //                //                for (Iterator iterator = searchCriteria
            //                //                        .getDBGraphSearchCriteria().getCompositeEntities()
            //                //                        .iterator(); iterator.hasNext();) {
            //                //                    CompositeEntity compositeEntity = (CompositeEntity) iterator
            //                //                            .next();
            //                //                    System.out.println(compositeEntity);
            //                //                }
            //                //
            //                //                System.out.println("ports: "
            //                //                        + searchCriteria.getDBGraphSearchCriteria()
            //                //                                .getPortsList());
            //                //                for (Iterator iterator = searchCriteria
            //                //                        .getDBGraphSearchCriteria().getPortsList().iterator(); iterator
            //                //                        .hasNext();) {
            //                //                    Port port = (Port) iterator.next();
            //                //                    System.out.println(port);
            //                //                }
            //                //
            //                //                System.out.println("relations: "
            //                //                        + searchCriteria.getDBGraphSearchCriteria()
            //                //                                .getRelationsList());
            //                //                for (Iterator iterator = searchCriteria
            //                //                        .getDBGraphSearchCriteria().getRelationsList()
            //                //                        .iterator(); iterator.hasNext();) {
            //                //                    Relation relation = (Relation) iterator.next();
            //                //                    System.out.println(relation);
            //                //                }
            //                //
            //                //                System.out.println("done testing");
            //                //            // TODO done and delete later
            //            

        }

        // TODO to be deleted later 
        //        private void _getAtomicEntities(CompositeEntity compositeEntity,
        //                ArrayList<ComponentEntity> componentEntities) {
        //
        //            if (compositeEntity != null) {
        //                List<Entity> entities = compositeEntity.entityList();
        //
        //                for (Iterator iterator = entities.iterator(); iterator
        //                        .hasNext();) {
        //                    Entity entity = (Entity) iterator.next();
        //                    if (entity instanceof CompositeEntity) {
        //                        _getAtomicEntities((CompositeEntity) entity,
        //                                componentEntities);
        //
        //                    } else if (entity instanceof ComponentEntity) {
        //                        componentEntities.add((ComponentEntity) entity);
        //                    }
        //                }
        //            }
        //        }

    }

    private static class DBSearchConfigureAction extends ConfigureAction {

        public DBSearchConfigureAction(String description) {
            super(description);
        }

        protected void _openDialog(Frame parent, NamedObj target,
                ActionEvent event) {
            JOptionPane.showMessageDialog(new Frame(), "Under construction.");
        }
    }

    //    private class SimpleSearchAction extends FigureAction {
    //
    //        public SimpleSearchAction() {
    //            super("Simple Search Configure");
    //
    //            GUIUtilities.addIcons(this, new String[][] {
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.LARGE_ICON },
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.ROLLOVER_ICON },
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.ROLLOVER_SELECTED_ICON },
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.SELECTED_ICON } });
    //
    //            putValue("tooltip", "Configure Simple Search Criteria" + "(Ctrl+2)");
    //            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
    //                    KeyEvent.VK_2, Toolkit.getDefaultToolkit()
    //                            .getMenuShortcutKeyMask()));
    //        }
    //
    //        //////////////////////////////////////////////////////////////////////
    //        ////                public  methods                               ////
    //
    //        public void actionPerformed(ActionEvent e) {
    //            super.actionPerformed(e);
    //
    //            if (_simpleSearchFrame == null) {
    //                _simpleSearchFrame = new AdvancedSimpleSearchFrame(
    //                        GraphPatternSearchEditor.this);
    //            }
    //
    //            _simpleSearchFrame.pack();
    //            _simpleSearchFrame.setVisible(true);
    //
    //        }
    //
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private NamedObj _containerModel;
    private SimpleSearchFrame _simpleSearchFrame;
    private JFrame _sourceFrame;

}
