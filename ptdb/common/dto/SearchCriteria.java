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

package ptdb.common.dto;

import java.util.ArrayList;

import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// SearchCriteria

/**
 * <p>DTO (Data Transfer Object) which contains all the search criteria input by
 * the user.  It is constructed by the GUI layer class to pass the search
 * criteria. <br>
 *
 * There are three categories of criteria in this class:<br>
 * 1. Some attributes information of the Ptolemy model that the user wants to
 * search on.<br>
 * 2. The XQuery command to be executed directly in the XML database for
 * searching. <br>
 * 3. The pattern to be matched to search the model. </p>
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class SearchCriteria {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the attributes criteria from the search criteria.
     *
     * @return All the attributes input by the user as the search criteria.
     * @see #setAttributes
     */
    public ArrayList<Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Get the graph search criteria from the search criteria.
     *
     * @return The graph search criteria input by the user.
     * @see #setDBGraphSearchCriteria
     */
    public DBGraphSearchCriteria getDBGraphSearchCriteria() {
        return _dbGraphSearchCriteria;
    }

    /**
     * Get the name of the model from the search criteria.
     *
     * @return The model name search criteria.
     * @see #setModelName
     */
    public String getModelName() {
        return _modelName;
    }

    /**
     * Get the MoML of the graph pattern search criteria.
     *
     * @return The MoML of the pattern.
     * @see #setPatternMoML(String)
     */
    public String getPatternMoML() {
        return _patternMoML;
    }

    /**
     * Get the search command criteria from the search criteria.
     *
     * @return The XQuery search command input by the user.
     * @see #setSearchCommand
     */
    public String getSearchCommand() {
        return _searchCommand;
    }

    /**
     * Set the attribute criteria for the search criteria.
     *
     * @param attributes The attributes input by the user to set in the
     * search criteria.
     * @see #getAttributes
     */
    public void setAttributes(ArrayList<Attribute> attributes) {
        this._attributes = attributes;
    }

    /**
     * Set the graph search criteria for the search criteria.
     *
     * @param dbGraphSearchCriteria The graph search criteria input by the user
     * to set in the search criteria.
     * @see #getDBGraphSearchCriteria
     */
    public void setDBGraphSearchCriteria(
            DBGraphSearchCriteria dbGraphSearchCriteria) {
        _dbGraphSearchCriteria = dbGraphSearchCriteria;
    }

    /**
     * Set the name of the model for the search criteria.
     *
     * @param modelName The name of the model to be set in the search criteria.
     * @see #getModelName
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /**
     * Set the MoML for the graph pattern search criteria.
     *
     * @param patternMoML The MoMl of the pattern to be set in the search
     * criteria.
     * @see #getPatternMoML()
     */
    public void setPatternMoML(String patternMoML) {
        _patternMoML = patternMoML;
    }

    /**
     * Set the XQuery search command for the search criteria.
     *
     * @param searchCommand The XQuery search command to set in the search
     * criteria.
     * @see #getSearchCommand
     */
    public void setSearchCommand(String searchCommand) {
        this._searchCommand = searchCommand;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Contains all the specified searching attributes.
     */
    private ArrayList<Attribute> _attributes;

    private DBGraphSearchCriteria _dbGraphSearchCriteria;

    /**
     * The name of the model as the search criteria.
     */
    private String _modelName;

    /**
     * The moml of the pattern.
     */
    private String _patternMoML;

    /**
     * Represents the XQuery search command input by the user.
     */
    private String _searchCommand;

}
