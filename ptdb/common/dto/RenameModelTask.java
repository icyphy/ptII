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

///////////////////////////////////////////////////////////////////
//// RenameModelTask

/**
 * A data transfer object that holds the information of the model that its name
 * is required to be changed along with the new name.
 *
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class RenameModelTask {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Construct the object and set the member variables for it.
     *
     * @param existingMode The model that its name need to be changed.
     * @param newModelName The new name for the model.
     */
    public RenameModelTask(XMLDBModel existingMode, String newModelName) {

        _existingModel = existingMode;
        _newModelName = newModelName;
    }

    /**
     * Return the existing model set in the object.
     * @return The existing model set in the object.
     * @see #setExistingModel
     */
    public XMLDBModel getExistingModel() {
        return _existingModel;
    }

    /**
     * Return the new name for the model.
     * @return The new name for the model.
     * @see #setNewModelName
     */
    public String getNewModelName() {
        return _newModelName;
    }

    /**
     * Set the existing model that its name need to be changed.
     * @param existingModel The model that its name needs to be changed.
     * @see #getExistingModel
     */
    public void setExistingModel(XMLDBModel existingModel) {

        _existingModel = existingModel;

    }

    /**
     * Set the new model name.
     * @param newModelName The new model name.
     * @see #getNewModelName
     */
    public void setNewModelName(String newModelName) {
        _newModelName = newModelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The model which its name need to be changed. */
    private XMLDBModel _existingModel;

    /** The new name for the model. */
    private String _newModelName;

}
