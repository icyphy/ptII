/*
 * 
 */
package ptdb.common.util;

import java.util.ArrayList;
import java.util.Iterator;

import ptdb.common.dto.XMLDBModel;

///////////////////////////////////////////////////////////////
//// XMLDBModelManager

/**
 * Common util class for some common operations on the XMLDBModel objects. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class XMLDBModelManager {

    //////////////////////////////////////////////////////////////////////
    ////                    public methods                            ////

    /**
     * Intersect the XMLDBModels results from two list, and take the common
     * ones and return them in a new list. 
     * 
     * @param previousResults The list of the first batch of models. 
     * @param currentResults The list of the second batch of models. 
     * @return The list of common models from both lists. 
     */
    public static ArrayList<XMLDBModel> intersectResults(
            ArrayList<XMLDBModel> previousResults,
            ArrayList<XMLDBModel> currentResults) {

        // If the previous result is empty, just return the results found
        // in this searcher.
        if (previousResults == null || previousResults.size() == 0) {
            return currentResults;
        }

        // If the current result is empty, just return the empty set.
        if (currentResults == null || currentResults.size() == 0) {
            return currentResults;
        }

        java.util.Hashtable<String, XMLDBModel> existingModels = new java.util.Hashtable<String, XMLDBModel>();
        ArrayList<XMLDBModel> returnedResults = new ArrayList<XMLDBModel>();

        for (Iterator iterator = previousResults.iterator(); iterator.hasNext();) {
            XMLDBModel xmldbModel = (XMLDBModel) iterator.next();
            existingModels.put(xmldbModel.getModelName(), xmldbModel);
        }

        for (Iterator iterator = currentResults.iterator(); iterator.hasNext();) {
            XMLDBModel xmldbModel = (XMLDBModel) iterator.next();
            if (existingModels.get(xmldbModel.getModelName()) != null) {
                returnedResults.add(xmldbModel);
            }
        }

        return returnedResults;
    }

}
