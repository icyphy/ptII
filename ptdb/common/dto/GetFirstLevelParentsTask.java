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

///////////////////////////////////////////////////////////////////
//// GetFirstLevelParentsTask

/**
 * Task to fetch the first level parents for the given model. First level
 * parents are models that have an immediate reference to the given model.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class GetFirstLevelParentsTask extends Task {

    /**
     * Construct an instance of this class and set the model for which the first
     * level parents need to be fetched.
     * @param model Model for which the first level parents need to be fetched.
     */
    public GetFirstLevelParentsTask(XMLDBModel model) {
        this._model = model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the model for which the first level parents need to be fetched.
     * @return Model for which the first level parents need to be fetched.
     * @see #setModel
     */
    public XMLDBModel getModel() {
        return _model;
    }

    /**
     * Set the model for which the first level parents need to be fetched.
     * @param model Model for which the first level parents need to be fetched.
     * @see #getModel
     */
    public void setModel(XMLDBModel model) {
        this._model = model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /* Model for which the first level parents need to be fetched. */
    XMLDBModel _model;
}
