/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/*
 *
 */
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////////
//// UpdateParentsToNewVersionTask

/**
 * Task to update the selected parents to the new version of the model.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class UpdateParentsToNewVersionTask extends Task {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the new model that was created.
     * @return The new model that was created.
     * @see #setNewModel
     */
    public XMLDBModel getNewModel() {
        return _newModel;
    }

    /**
     * Return the old model that was being updated.
     * @return The old model that was being updated.
     * @see #setOldModel
     */
    public XMLDBModel getOldModel() {
        return _oldModel;
    }

    /**
     * Return the list of selected first level parents' model names.
     * @return The list of selected first level parents' model names.
     * @see #setParentsList
     */
    public ArrayList<String> getParentsList() {
        return _parentsList;
    }

    /**
     * Set the new model that was created.
     * @param newModel The new model that was created.
     * @see #getNewModel
     */
    public void setNewModel(XMLDBModel newModel) {
        _newModel = newModel;
    }

    /**
     * Set the old model that was being updated.
     * @param oldModel The old model that was being updated.
     * @see #getOldModel
     */
    public void setOldModel(XMLDBModel oldModel) {
        _oldModel = oldModel;
    }

    /**
     * Set the list of selected first level parents' model names.
     * @param parentsList The list of selected first level parents' model names.
     * @see #getParentsList
     */
    public void setParentsList(ArrayList<String> parentsList) {
        _parentsList = parentsList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /* The new model that was created. */
    XMLDBModel _newModel;
    /* The old model that was being updated. */
    XMLDBModel _oldModel;
    /* The list of selected first level parents' model names. */
    ArrayList<String> _parentsList;
}
