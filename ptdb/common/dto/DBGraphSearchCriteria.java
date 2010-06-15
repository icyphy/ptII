/*
 *
 */
package ptdb.common.dto;

import java.util.ArrayList;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;

///////////////////////////////////////////////////////////////////
//// DBGraphSearchCriteria

/**
 * The DTO (data transfer object) that all the search criteria input by the
 * user, for the graph searching through the XML database.
 *
 * <p>It is constructed by the GUI layer class to pass the search criteria for
 * graph DB search to the database layer.</p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class DBGraphSearchCriteria {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the component entities from the Graph search pattern.
     *
     * @return The component entities from the graph search pattern.
     */
    public ArrayList<ComponentEntity> getComponentEntitiesList() {
        return _componentEntitiesList;
    }

    /**
     * Get the composite entities form the graph search pattern. 
     * 
     * @return The composite entities from the graph search pattern. 
     */
    public ArrayList<CompositeEntity> getCompositeEntities() {
        return _compositeEntitiesList;
    }

    /**
     * Get the ports from the Graph search pattern.
     *
     * @return The ports from the graph search pattern.
     */
    public ArrayList<Port> getPortsList() {
        return _portsList;
    }

    /**
     * Get the relations from the Graph search pattern.
     *
     * @return The relations from the graph search pattern.
     */
    public ArrayList<Relation> getRelationsList() {
        return _relationsList;
    }

    /**
     * Set the component entities from the Graph search pattern.
     *
     * @param componentEntitiesList The component entities from the graph
     *  search pattern.
     */
    public void setComponentEntitiesList(
            ArrayList<ComponentEntity> componentEntitiesList) {
        _componentEntitiesList = componentEntitiesList;
    }

    /**
     * Set the composite entities from the graph search pattern. 
     * 
     * @param compositeEntitiesList The composite entities from the graph search
     *   pattern.
     */
    public void setCompositeEntities(
            ArrayList<CompositeEntity> compositeEntitiesList) {
        _compositeEntitiesList = compositeEntitiesList;
    }

    /**
     * Set the ports from the Graph search pattern.
     *
     * @param portsList The ports from the graph search pattern.
     */
    public void setPortsList(ArrayList<Port> portsList) {
        _portsList = portsList;
    }

    /**
     * Set the relations from the Graph search pattern.
     *
     * @param relationsList The relations from the graph search pattern.
     */
    public void setRelationsList(ArrayList<Relation> relationsList) {
        _relationsList = relationsList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ArrayList<ComponentEntity> _componentEntitiesList;

    private ArrayList<CompositeEntity> _compositeEntitiesList;

    private ArrayList<Port> _portsList;

    private ArrayList<Relation> _relationsList;

}
