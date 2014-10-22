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

///////////////////////////////////////////////////////////////////
//// XMLDBModelWithReferenceChanges

/**
 *
 * Encapsulate the information needed by the business layer to perform a save
 * operation with parent references being affected by the change done on the
 * model to be saved.
 *
 * <p>The Data Transfer Object pattern is used here.</p>
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class XMLDBModelWithReferenceChanges {

    /**
     * Construct a new instance of this class and set the member variables.
     *
     * @param modelToBeSaved The model to be saved in the database with no
     * changes.
     *
     * @param parentsList List of parents that should have the new model name as
     * the reference inside them with the old content.
     *
     * @param versionName A new name of the model which will be converted into a
     * model that contains the content of the modelToBeSaved before saving it.
     */
    public XMLDBModelWithReferenceChanges(XMLDBModel modelToBeSaved,
            ArrayList<String> parentsList, String versionName) {

        _modelToBeSaved = modelToBeSaved;
        _parentsList = parentsList;
        _versionName = versionName;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the model to be saved in the database.
     *
     * @return The model to be saved in the database.
     *
     * @see #setModelToBeSaved
     */
    public XMLDBModel getModelToBeSaved() {
        return _modelToBeSaved;
    }

    /**
     * Return The list of parents being affected by the changes made on the
     * model.
     *
     * @return The list of parents being affected by the changes made on the
     * model.
     *
     * @see #setParentsList
     */
    public ArrayList<String> getParentsList() {
        return _parentsList;
    }

    /**
     * Return The new model name that will be stored in the database.
     *
     * @return The new model name that will be stored in the database.
     *
     * @see #setVersionName
     */
    public String getVersionName() {
        return _versionName;
    }

    /**
     * Set The model to be saved in the database.
     *
     * @param modelToBeSaved The model to be saved in the database.
     *
     * @see #getModelToBeSaved
     */
    public void setModelToBeSaved(XMLDBModel modelToBeSaved) {
        _modelToBeSaved = modelToBeSaved;
    }

    /**
     * Set The list of parents being affected by the changes made on the model.
     *
     * @param parentsList The list of parents being affected by the changes made
     * on the model.
     *
     * @see #getParentsList
     */
    public void setParentsList(ArrayList<String> parentsList) {
        _parentsList = parentsList;
    }

    /**
     * Set The new model name that will be stored in the database.
     *
     * @param versionName The new model name that will be stored in the
     * database.
     *
     * @see #getVersionName
     */
    public void setVersionName(String versionName) {
        _versionName = versionName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The model to be saved in the database. */
    XMLDBModel _modelToBeSaved = null;

    /** The list of parents being affected by the changes made on the model. */
    ArrayList<String> _parentsList = null;

    /** The new model name that will be stored in the database. */
    String _versionName = null;

}
