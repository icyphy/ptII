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
package ptdb.common.dto;

/**
 *
 * A task request to fetch the reference string the database.
 *
 * <p>The reference string contains an XML skeleton containing all hierarchies
 * where references to the given model occur in the database.</p>
 *
 * @author Lyle Holsinger
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (lholsing)
 * @Pt.AcceptedRating Red (lholsing)
 *
 *
 */

public class GetReferenceStringTask {

    /**
     * Construct an instance of the object and set the name of the model
     * for which to retrieve the reference string.
     *
     * @param modelName the model name to be fetched from the database.
     */
    public GetReferenceStringTask(String modelName) {
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
