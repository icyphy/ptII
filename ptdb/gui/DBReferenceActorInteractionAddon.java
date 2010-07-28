package ptdb.gui;

import javax.swing.JOptionPane;

import diva.graph.GraphController;

import ptdb.common.dto.XMLDBModel;
import ptolemy.data.expr.StringConstantParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.actor.ActorInteractionAddon;
import ptolemy.vergil.toolbox.FigureAction;

/**
 * Implementation of interface for interaction with actors.  This 
 * implementation defines how to interact with database reference actors.
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */
public class DBReferenceActorInteractionAddon implements ActorInteractionAddon {

    /** Determine of a given actor is a database reference actor.
     * 
     * @param actor The actor of interest.
     * @return True if the actor is a database reference actor, False otherwise.
     */
    public boolean isActorOfInterestForLookInside(NamedObj actor) {
        
        if(actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {

            if(actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) 
                instanceof StringConstantParameter && 
                ((StringParameter) actor
                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                .getExpression().equals("TRUE")) {
                            
                return true;
                   
            }
               
        }
        
        return false;
    }

    /**
     * Show a dialog box warning the user that changes will not be propagated 
     * to the database reference model.
     * @param figureAction The FigureAction from which the call is being made.
     */
    public void lookInsideAction(FigureAction figureAction) {      
        
        JOptionPane.showMessageDialog(figureAction.getFrame(), 
                "Changes to this actor will not " +
                "be saved to the database.  " +
                "To make changes to the " +
                "referenced model, open " +
                "it from the database.", 
                "Open Actor",
                JOptionPane.WARNING_MESSAGE, null);
        
    }

    /**
     * Get an instance of a DBActorController to be used for control of 
     * database reference actors. 
     * @param controller The associated graph controller.
     * @param fullAccess Indication if the controller should be instantiated
     *                  with Full access.
     * @return An new instance a DBActorController.
     */
    public ActorController getControllerInstance(GraphController controller, boolean fullAccess) {

        if(fullAccess){
        
            return new DBActorController(controller, ActorController.FULL);
        
        } else {

            return new DBActorController(controller, ActorController.PARTIAL);
            
        }
    
    }

    /**
     * Get an instance of a DBActorController to be used for control of 
     * database reference actors.  This assumes full access.
     * @param controller The associated graph controller.
     * @param fullAccess Indication if the controller should be instantiated
     *                  with Full access.
     * @return An instance of the appropriate controller.
     */
    public ActorController getControllerInstance(GraphController controller) {

        return new DBActorController(controller, ActorController.FULL);
    }

    /** Determine of a given actor is a database reference actor.
     * 
     * @param actor The actor of interest.
     * @return True if the actor is a database reference actor, False otherwise.
     */
    public boolean isActorOfInterestForOpenInstance(NamedObj actor) {
        
        if(actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {

            if(actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) 
                instanceof StringConstantParameter && 
                ((StringParameter) actor
                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                .getExpression().equals("TRUE")) {
                            
                return true;
                   
            }
               
        }
        
        return false;
    
    }

    /**
     * Show a dialog box warning the user that changes will not be propagated 
     * to the database reference model.
     * @param figureAction The FigureAction from which the call is being made.
     */
    public void openInstanceAction(FigureAction figureAction) {
        JOptionPane.showMessageDialog(figureAction.getFrame(), 
                "Changes to this instance will not " +
                "be saved to the database.  " +
                "To make changes to the " +
                "referenced model, open " +
                "it from the database.", 
                "Open Instance",
                JOptionPane.WARNING_MESSAGE, null);
    }
    
    /** Determine of a given actor is a database reference actor.
     * 
     * @param actor The actor of interest.
     * @return True if the actor is a database reference actor, False otherwise.
     */
    public boolean isActorOfInterestForAddonController(NamedObj actor) {
        
        if(actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {

            if(actor.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) 
                instanceof StringConstantParameter && 
                ((StringParameter) actor
                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                .getExpression().equals("TRUE")) {
                            
                return true;
                   
            }
               
        }
        
        return false;
    }

}
