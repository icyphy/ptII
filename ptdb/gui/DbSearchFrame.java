/*
 * 
 */
package ptdb.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.KeyStroke;

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.PTDBSearchAttribute;
import ptdb.common.dto.SearchCriteria;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.MalformedStringException;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gt.ingredients.criteria.SubclassCriterion;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.gt.TransformationEditor;
import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////
//// DbSearchFrame

/**
 * The UI frame for the advanced DB search window. It has the specific searching
 * features for PTDB.
 * 
 * @author Alek Wang
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
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

    //////////////////////////////////////////////////////////////////////
    ////		protected methods 				//////

    @Override
    protected void _addMenus() {

        super._addMenus();

        // add the menu of searching in the database
        DBMatchAction dbMatchAction = new DBMatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, dbMatchAction);

        GUIUtilities.addToolBarButton(_toolbar, dbMatchAction);

    }

    //////////////////////////////////////////////////////////////////////
    ////		private classes				//////

    private class DBMatchAction extends FigureAction {

        public DBMatchAction() {
            super("Match Model");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/match.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/match_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/match_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/match_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Search Ptolemy models in Database"
                    + "(Ctrl+1)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        //////////////////////////////////////////////////////////////////////
        ////                public  methods                          //////

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            // TODO create the new SearchResultFrame
            // SearchResultFrame searchResultFrame = new SearchResultFrame();

            // create the new search result buffer 
            SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

            // TODO register the result listener from the search result frame 
            // to the search result buffer
            // searchResultBuffer.register(searchResultFrame.getResultListener);

            // get the search criteria
            // for this requirement, only the attributes part
            SearchCriteria searchCriteria = new SearchCriteria();

            // get the pattern specified by the user
            TransformationRule rule = getFrameController()
                    .getTransformationRule();
            Pattern pattern = rule.getPattern();

            // create a new arraylist to contain all the attributes 
            ArrayList<Attribute> attributesList = new ArrayList<Attribute>();

            try {
                // get the criteria information from the model configuration
                GTIngredientList ingredientAttributesList = pattern
                        .getCriteriaAttribute().getIngredientList();

                for (Iterator iterator = ingredientAttributesList.iterator(); iterator
                        .hasNext();) {
                    GTIngredient gtIngredient = (GTIngredient) iterator.next();

                    // only check the criteria related to attributes here. 
                    if (!(gtIngredient instanceof PortCriterion)
                            && !(gtIngredient instanceof SubclassCriterion)) {

                        PTDBSearchAttribute attribute = new PTDBSearchAttribute();

                        attribute.setName(gtIngredient.getValue(0).toString());

                        if (gtIngredient instanceof AttributeCriterion) {
                            attribute.setToken(gtIngredient.getValue(2)
                                    .toString());
                        }

                        attributesList.add(attribute);
                    }

                }
            } catch (MalformedStringException e1) {

                e1.printStackTrace();
            } catch (IllegalActionException e2) {

                e2.printStackTrace();
            } catch (NameDuplicationException e3) {

                e3.printStackTrace();
            }

            // get the attributes from the pattern and add to the list
            List<NamedObj> attributes = pattern.attributeList();

            for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
                NamedObj attribute = (NamedObj) iterator.next();

                if (attribute instanceof Attribute) {
                    attributesList.add((Attribute) attribute);
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

            _getAtomicEntities(pattern, componentEntities);

            dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);

            // set the DBGraph search criteria to the whole search criteria 
            searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

            // TODO show the search result frame
            // searchResultFrame.setVisible(true);

            // TODO call the Search Manager to trigger the search
            //  SearchManager searchManager = new SearchManager();
            //            try {
            //                searchManager.search(searchCriteria, searchResultBuffer);
            //                
            //            } catch (DBConnectionException e1) {
            //               
            //                e1.printStackTrace();
            //            } catch (DBExecutionException e1) {
            //         
            //                e1.printStackTrace();
            //            }

            //            // TODO This is just for testing
            //            for (Iterator iterator = searchCriteria.getAttributes().iterator(); iterator.hasNext();) {
            //                NamedObj namedObj = (NamedObj) iterator.next();
            //                if (namedObj instanceof Variable) {
            //                    System.out.println(namedObj.getClassName());
            //                    try {
            //                        System.out.println(((Variable) namedObj).getToken().getClass());
            //                    } catch (IllegalActionException e1) {
            //                        // TODO Auto-generated catch block
            //                        e1.printStackTrace();
            //                    }
            //                }
            //                
            //            }
            //            

//            // TODO this is just for testing the Graph part, delete later
//            System.out.println("search criteira: "
//                    + searchCriteria.getDBGraphSearchCriteria());
//            System.out.println("components :"
//                    + searchCriteria.getDBGraphSearchCriteria()
//                            .getComponentEntitiesList());
//
//            for (Iterator iterator = searchCriteria.getDBGraphSearchCriteria()
//                    .getComponentEntitiesList().iterator(); iterator.hasNext();) {
//                ComponentEntity componentEntity = (ComponentEntity) iterator
//                        .next();
//                System.out.println(componentEntity);
//            }
//
//            System.out.println("ports: "
//                    + searchCriteria.getDBGraphSearchCriteria().getPortsList());
//            for (Iterator iterator = searchCriteria.getDBGraphSearchCriteria()
//                    .getPortsList().iterator(); iterator.hasNext();) {
//                Port port = (Port) iterator.next();
//                System.out.println(port);
//            }
//
//            System.out.println("relations: "
//                    + searchCriteria.getDBGraphSearchCriteria()
//                            .getRelationsList());
//            for (Iterator iterator = searchCriteria.getDBGraphSearchCriteria()
//                    .getRelationsList().iterator(); iterator.hasNext();) {
//                Relation relation = (Relation) iterator.next();
//                System.out.println(relation);
//            }
//
//            System.out.println("done testing");
//            // TODO done and delete later 
        }

        private void _getAtomicEntities(CompositeEntity compositeEntity,
                ArrayList<ComponentEntity> componentEntities) {

            if (compositeEntity != null) {
                List<Entity> entities = compositeEntity.entityList();

                for (Iterator iterator = entities.iterator(); iterator
                        .hasNext();) {
                    Entity entity = (Entity) iterator.next();
                    if (entity instanceof CompositeEntity) {
                        _getAtomicEntities((CompositeEntity) entity,
                                componentEntities);

                    } else if (entity instanceof ComponentEntity) {
                        componentEntities.add((ComponentEntity) entity);
                    }
                }
            }
        }

    }

}
