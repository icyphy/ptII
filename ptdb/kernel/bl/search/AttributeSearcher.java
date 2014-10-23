/*
@Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptdb.kernel.bl.search;

import java.util.ArrayList;
import java.util.HashMap;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.PTDBGenericAttribute;
import ptdb.common.dto.PTDBSearchAttribute;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBExecutionException;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// AttributeSearcher

/**
 * The concrete searcher to handle the search by attributes criteria.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class AttributeSearcher extends AbstractSearcher implements
AbstractDBSearcher {

    /**
     * Construct the AttributeSearcher according to the input search criteria.
     *
     * @param searchCriteria The search criteria that input by the user.
     */
    public AttributeSearcher(SearchCriteria searchCriteria) {

        if (searchCriteria.getAttributes() != null
                && searchCriteria.getAttributes().size() > 0) {

            // Get the attribute list from the search criteria's attributes list.

            HashMap<String, PTDBGenericAttribute> attributesMap = new HashMap<String, PTDBGenericAttribute>();

            for (Attribute originalAttribute : searchCriteria.getAttributes()) {

                if (!attributesMap.containsKey(originalAttribute.getName())) {

                    PTDBGenericAttribute newAttribute = new PTDBGenericAttribute(
                            originalAttribute.getName());

                    // Set the values and class name to newAttribute from
                    // originalAttribute.
                    //                    newAttribute.setAttributeName(originalAttribute.getName());

                    //newAttribute.addValue(((Variable) originalAttribute)
                    //        .getValueAsString());
                    newAttribute.addValue(((Variable) originalAttribute)
                            .getExpression());

                    if (originalAttribute instanceof PTDBSearchAttribute) {
                        if (!((PTDBSearchAttribute) originalAttribute)
                                .isGenericAttribute()) {

                            newAttribute.setClassName(originalAttribute
                                    .getClassName());
                        } else {

                            newAttribute
                            .setClassName(((PTDBSearchAttribute) originalAttribute)
                                    .getGenericClassName());

                        }
                    } else {
                        newAttribute.setClassName(originalAttribute
                                .getClassName());
                    }

                    attributesMap.put(newAttribute.getAttributeName(),
                            newAttribute);
                } else {
                    PTDBGenericAttribute attribute = attributesMap
                            .get(originalAttribute.getName());
                    //attribute.addValue(((Variable) originalAttribute)
                    //        .getValueAsString());
                    attribute.addValue(((Variable) originalAttribute)
                            .getExpression());
                }

            }

            // Convert the attributes map to list.
            ArrayList<PTDBGenericAttribute> attributeList = new ArrayList<PTDBGenericAttribute>(
                    attributesMap.values());

            _attributesCriteria = attributeList;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Checks whether the attribute search criteria has been set in
     *  this attribute searcher instance.
     *
     * @return true - if the search criteria has been set.<br>
     *         false - if the search criteria has not been set.
     */

    @Override
    protected boolean _isSearchCriteriaSet() {

        if (_attributesCriteria == null || _attributesCriteria.size() == 0) {
            return false;
        }

        return true;

    }

    /**
     * Perform the actual search for the attributes search criteria.
     *
     * @exception DBExecutionException Thrown by the DBConnection if
     * unexpected problem happens during the execution of DB query tasks.
     */

    @Override
    protected void _search() throws DBExecutionException {

        AttributeSearchTask attributeSearchTask = new AttributeSearchTask();

        attributeSearchTask.setAttributesList(_attributesCriteria);

        // Call the executeAttributeTask() method from the DBConnection class
        // and set the returned results.
        _currentResults = _dbConnection
                .executeAttributeSearchTask(attributeSearchTask);

        if (_currentResults == null) {
            // The db layer cannot perform the searching, so make the search
            // criteria not set.
            _attributesCriteria = null;
        } else {
            // Pass the intermediate results.
            handleIntermediateResults(_currentResults, this);
        }

        // Mark this searcher as passed in the chain.
        _pass();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * This field contains the search criteria of attributes.
     */
    private ArrayList<PTDBGenericAttribute> _attributesCriteria;

}
