package ptdb.gui;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ptdb.common.dto.XMLDBModel;
import ptolemy.actor.gui.Configuration;

///////////////////////////////////////////////////////////////////
////SearchResultPanel

/**
* An extended JPanel displaying a search result model and all of the branches of its 
* parent hierarchy.  The panel layout is taken care of in the constructor.  A public method getSelections()
* is available for getting the list of all model names for models that have been selected for
* loading.
* 
* @author Lyle Holsinger
* @since Ptolemy II 8.1
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

public class SearchResultPanel extends JPanel {

    /**
     * Construct a panel associated with a search result. 
     *
     * @param dbModel
     *        The model returned as a search result.      
     * @param configuration
     *        The configuration under which an effigy of this model would be generated.
     */
    public SearchResultPanel(XMLDBModel dbModel, Configuration configuration) {

        _modelName = dbModel.getModelName();
        _configuration = configuration;

        _modelPanel = new ModelPanel(dbModel, _configuration);
        add(_modelPanel);

        JLabel hierarchyLabel = new JLabel("Model Hierarchy:");
        add(hierarchyLabel);

        for (ArrayList<XMLDBModel> hierarchy : dbModel.getParents()) {

            _parentPanelList.add(new ParentHierarchyPanel(hierarchy,
                    _modelName, _configuration));

        }

        ListIterator listIterator = _parentPanelList.listIterator();

        while (listIterator.hasNext()) {

            add((ParentHierarchyPanel) listIterator.next());

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * @return The model name.
     */
    public String getModelName() {

        return _modelName;

    }

    /** Traverse the list of ParentHierarchyPanels.  Call the getSelections() of
     * each ParentHierarchyPanel and add all model names to the list.  Finally,
     * Call getValue() for the ModelPanel.  If it returns true (checkbox is checked),
     * Add the model name to the ArrayList.  
     * 
     * @return ArrayList<String> 
     *         A list of models that have been selected for loading.
     */
    public ArrayList<String> getSelections() {

        ArrayList<String> returnList = new ArrayList();

        for (ParentHierarchyPanel hierarchy : _parentPanelList) {

            returnList.addAll(hierarchy.getSelections());

        }

        if (_modelPanel.getValue()) {

            returnList.add(_modelPanel.getModelName());

        }

        return returnList;

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private ModelPanel _modelPanel;
    private ArrayList<ParentHierarchyPanel> _parentPanelList;
    private String _modelName;
    private Configuration _configuration;

}
