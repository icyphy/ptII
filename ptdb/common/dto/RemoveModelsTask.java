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
////RemoveModelsTask

/**
 *
 * A task request to remove a list of models from the database.
 *
 * <p>It is used as a data transfer object that hold a list of models as
 * XMLDBModel objects with its getter and setter methods.</p>
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class RemoveModelsTask extends Task {

    /**
     * Construct an object from this class and set the models list.
     * @param modelsList The models list to be removed from the database.
     */
    public RemoveModelsTask(ArrayList<XMLDBModel> modelsList) {
        this._modelsList = modelsList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the list of models to be deleted from the database.
     * @return The list of models to be deleted from the database.
     *
     * @see #setModelsList
     */
    public ArrayList<XMLDBModel> getModelsList() {
        return _modelsList;
    }

    /**
     * Set the list of models to be deleted from the database.
     * @param modelsList List of models to be deleted from the database.
     *
     * @see #getModelsList
     */
    public void setModelsList(ArrayList<XMLDBModel> modelsList) {
        this._modelsList = modelsList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of models to be deleted. */
    ArrayList<XMLDBModel> _modelsList = null;

}
