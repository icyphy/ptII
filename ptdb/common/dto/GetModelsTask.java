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

/**
 *
 * A task request to fetch a model from the database.
 *
 * <p>It is used as a data transfer object and hold the model name
 * with its getter and setter method.</p>
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 *
 */
public class GetModelsTask extends Task {

    /**
     * Construct an instance of the object and set the model name to be fetched
     * from the database.
     *
     * @param modelName the model name to be fetched from the database.
     */
    public GetModelsTask(String modelName) {
        _modelName = modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the model name that is needed to be fetched from the database.
     *
     * @return The model name.
     *
     * @see #setModelName
     */
    public String getModelName() {
        return _modelName;

    }

    /**
     * Set the model name to be fetched from the database.
     *
     * @param modelName the name of the model to be fetched from the database.
     *
     * @see #getModelName
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The model name. */
    private String _modelName;

}
