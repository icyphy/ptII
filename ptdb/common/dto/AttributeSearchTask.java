/*
 *
 */
package ptdb.common.dto;

import java.util.ArrayList;

import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// AttributeSearchTask

/**
 * Contain the attribute search criteria to
 * execute attribute search task.
 *
 * @author Ashwini Bijwe
 *
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class AttributeSearchTask extends Task {

    /**
     *  Construct an instance of AttributeSearchTask
     *  and set it as a select task.
     */
    public AttributeSearchTask() {
        _isUpdateTask = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                                    ////

    /**
     * Add the given attribute to the search attribute list.
     *
     * @param attribute Attribute to add to the
     * search attribute list.
     */
    public void addAttribute(Attribute attribute) {
        if (_attributesList == null) {
            _attributesList = new ArrayList<Attribute>();
        }

        _attributesList.add(attribute);
    }

    /**
     * Return the search attribute list.
     * @see #setAttributesList
     * @return The search attribute list.
     */
    public ArrayList<Attribute> getAttributesList() {
        return _attributesList;
    }

    /**
     * Set the search attribute list to the given list.
     * @see #getAttributesList
     * @param attributesList List of attributes for search.
     */
    public void setAttributesList(ArrayList<Attribute> attributesList) {
        _attributesList = attributesList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                           ////
    /**
     * The search attribute list.
     */
    private ArrayList<Attribute> _attributesList;
}
