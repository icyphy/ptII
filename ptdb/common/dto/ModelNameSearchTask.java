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
/*
 *
 */
package ptdb.common.dto;

///////////////////////////////////////////////////////////////////
//// ModelNameSearchTask

/**
 * Task to search for models based on model name.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class ModelNameSearchTask extends Task {
    /**
     * Create an instance of ModelNameSearchTask.
     * @param modelName Model name for which the search task is to be created.
     */
    public ModelNameSearchTask(String modelName) {
        this.modelName = modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the model name.
     * @return The model name.
     * @see #setModelName
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Set the given String as the model name.
     * @param modelName String to be set as model name.
     * @see #getModelName
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String modelName;

}
