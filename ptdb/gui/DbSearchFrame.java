/*
 *
 */
package ptdb.gui;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
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
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// DbSearchFrame

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
public class DbSearchFrame extends TransformationEditor {

    /**
     * Construct the DbSearchFrame.
     *
     * @param entity  The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     */
    public DbSearchFrame(CompositeEntity entity, Tableau tableau) {
        super(entity, tableau);
    }

    /**
     * Construct the DbSearchFrame.
     *
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     * @param defaultLibrary An attribute specifying the default library to
     *   use if the model does not have a library.
     */
    public DbSearchFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _addMenus() {

        super._addMenus();

        // add the menu of searching in the database
        DBMatchAction dbMatchAction = new DBMatchAction();

        GUIUtilities.addToolBarButton(_toolbar, dbMatchAction);

        // remove the replacement and correspondence tabs 
        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        tabbedPane.remove(1);
        tabbedPane.remove(1);

    }

    @Override
    protected RunnableGraphController _createActorGraphController() {
        return new DBSearchFrameController();

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
            _configureMenuFactory.addMenuItemListener(DbSearchFrame.this);
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

            // create the new SearchResultFrame
            SearchResultsFrame searchResultsFrame = new SearchResultsFrame(
                    new NamedObj(), DbSearchFrame.this, DbSearchFrame.this
                            .getConfiguration());

            SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

            // register the result listener from the search result frame
            // to the search result buffer
            searchResultBuffer.addObserver(searchResultsFrame);

            // get the search criteria
            // for this requirement, only the attributes part
            SearchCriteria searchCriteria = new SearchCriteria();

            // get the pattern specified by the user
            TransformationRule rule = getFrameController()
                    .getTransformationRule();
            Pattern pattern = rule.getPattern();

            // create a new arraylist to contain all the attributes
            ArrayList<Attribute> attributesList = new ArrayList<Attribute>();

            // TODO to delete later 
            //            try {
            //                // get the criteria information from the model configuration
            //                GTIngredientList ingredientAttributesList = pattern
            //                        .getCriteriaAttribute().getIngredientList();
            //
            //                for (Iterator iterator = ingredientAttributesList.iterator(); iterator
            //                        .hasNext();) {
            //                    GTIngredient gtIngredient = (GTIngredient) iterator.next();
            //
            //                    // only check the criteria related to attributes here.
            //                    if (!(gtIngredient instanceof PortCriterion)
            //                            && !(gtIngredient instanceof SubclassCriterion)) {
            //
            //                        PTDBSearchAttribute attribute = new PTDBSearchAttribute();
            //
            //                        attribute.setName(gtIngredient.getValue(0).toString());
            //
            //                        if (gtIngredient instanceof AttributeCriterion) {
            //                            attribute.setToken(gtIngredient.getValue(2)
            //                                    .toString());
            //                        }
            //
            //                        attributesList.add(attribute);
            //                    }
            //
            //                }
            //            } catch (MalformedStringException e1) {
            //
            //                // ignore
            //            } catch (IllegalActionException e2) {
            //
            //                // ignore
            //            } catch (NameDuplicationException e3) {
            //
            //                // ignore
            //            }
            // TODO to delete later 

            // get the attributes from the pattern and add to the list
            List<NamedObj> attributes = pattern.attributeList();

            for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
                NamedObj attribute = (NamedObj) iterator.next();

                if (attribute instanceof Variable) {
                    attributesList.add((Variable) attribute);
                }
            }

            // set the attributes to the search criteria accordingly
            searchCriteria.setAttributes(attributesList);

            // Create the graph pattern search criteria
            DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

            // get the ports specified by the user
            List<Port> portsList = pattern.portList();
            ArrayList<Port> ports = new ArrayList<Port>();

            for (Iterator iterator = portsList.iterator(); iterator.hasNext();) {
                Port port = (Port) iterator.next();
                ports.add(port);
            }

            dbGraphSearchCriteria.setPortsList(ports);

            // get the relations specified by the user
            List<Relation> relationsList = pattern.relationList();
            ArrayList<Relation> relations = new ArrayList<Relation>();

            for (Iterator iterator = relationsList.iterator(); iterator
                    .hasNext();) {
                Relation relation = (Relation) iterator.next();
                relations.add(relation);
            }

            dbGraphSearchCriteria.setRelationsList(relations);

            // get the component entities specified by the user
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

            //            _getAtomicEntities(pattern, componentEntities);

            dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);
            dbGraphSearchCriteria.setCompositeEntities(compositeEntities);
            dbGraphSearchCriteria.setPattern(pattern);

            // set the DBGraph search criteria to the whole search criteria
            searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

            // Check whether any search criteria has been set. 
            if (attributesList.size() == 0 && pattern.portList().isEmpty()
                    && pattern.relationList().isEmpty()
                    && pattern.entityList().isEmpty()) {
                JOptionPane.showMessageDialog(DbSearchFrame.this,
                        "Please specify search criteria.");
            } else {

                // Show the search result frame.
                searchResultsFrame.pack();
                searchResultsFrame.setVisible(true);

                // Call the Search Manager to trigger the search.
                SearchManager searchManager = new SearchManager();
                try {
                    searchManager.search(searchCriteria, searchResultBuffer);

                } catch (DBConnectionException e1) {
                    searchResultsFrame.setVisible(false);
                    searchResultsFrame.dispose();
                    MessageHandler.error("Cannot perform the search now.", e1);

                } catch (DBExecutionException e2) {
                    searchResultsFrame.setVisible(false);
                    searchResultsFrame.dispose();
                    MessageHandler.error("Cannot perform the search now.", e2);
                }

                //            // TODO This is just for testing
                //                for (Iterator iterator = searchCriteria.getAttributes()
                //                        .iterator(); iterator.hasNext();) {
                //                    NamedObj namedObj = (NamedObj) iterator.next();
                //                    System.out.println("attribute: " + namedObj);
                //                    if (namedObj instanceof Variable) {
                //                        System.out.println("variable: "
                //                                + namedObj.getClassName());
                //                        try {
                //                            System.out.println(((Variable) namedObj).getToken()
                //                                    .getClass());
                //                        } catch (IllegalActionException e1) {
                //                            // TODO Auto-generated catch block
                //                            e1.printStackTrace();
                //                        }
                //                    }
                //
                //                }

                //            // TODO this is just for testing the Graph part, delete later
                //                System.out.println("search criteira: "
                //                        + searchCriteria.getDBGraphSearchCriteria());
                //                System.out.println("components :"
                //                        + searchCriteria.getDBGraphSearchCriteria()
                //                                .getComponentEntitiesList());
                //
                //                for (Iterator iterator = searchCriteria
                //                        .getDBGraphSearchCriteria().getComponentEntitiesList()
                //                        .iterator(); iterator.hasNext();) {
                //                    ComponentEntity componentEntity = (ComponentEntity) iterator
                //                            .next();
                //                    System.out.println(componentEntity);
                //                }
                //
                //                System.out.println("composite entities: "
                //                        + searchCriteria.getDBGraphSearchCriteria()
                //                                .getCompositeEntities());
                //
                //                for (Iterator iterator = searchCriteria
                //                        .getDBGraphSearchCriteria().getCompositeEntities()
                //                        .iterator(); iterator.hasNext();) {
                //                    CompositeEntity compositeEntity = (CompositeEntity) iterator
                //                            .next();
                //                    System.out.println(compositeEntity);
                //                }
                //
                //                System.out.println("ports: "
                //                        + searchCriteria.getDBGraphSearchCriteria()
                //                                .getPortsList());
                //                for (Iterator iterator = searchCriteria
                //                        .getDBGraphSearchCriteria().getPortsList().iterator(); iterator
                //                        .hasNext();) {
                //                    Port port = (Port) iterator.next();
                //                    System.out.println(port);
                //                }
                //
                //                System.out.println("relations: "
                //                        + searchCriteria.getDBGraphSearchCriteria()
                //                                .getRelationsList());
                //                for (Iterator iterator = searchCriteria
                //                        .getDBGraphSearchCriteria().getRelationsList()
                //                        .iterator(); iterator.hasNext();) {
                //                    Relation relation = (Relation) iterator.next();
                //                    System.out.println(relation);
                //                }
                //
                //                System.out.println("done testing");
                //            // TODO done and delete later
            }

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

}
