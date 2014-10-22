/**
 * A composite entity that is the model contained by an ExceptionManager.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 *
 */
package ptolemy.actor.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ExceptionManagerModel

/**
 * A composite entity that is the model contained by an ExceptionManager.
 *
 * @author Elizabeth Latronico, based on ptolemy.data.ontologies.OntologySolverModel by Charles Shelton.
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 * @see ExceptionManager
 */
public class ExceptionManagerModel extends CompositeEntity {



    /** Construct an ExceptionManager in the specified workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ExceptionManagerModel(Workspace workspace) {
        super(workspace);
        _modelContainer = null;
    }

    /** Create a new ExceptionManagerModel with the specified name and container.
     *  @param container The container for the solver model.
     *  @param name The name for the solver model.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the container already contains an
     *   entity with the specified name.
     */

    public ExceptionManagerModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _modelContainer = null;
    }


    /** Create a new ExceptionManagerModel with the specified workspace and the
     *  specified ExceptionManager.
     *  @param exceptionManager The ExceptionManager that contains the model.
     *  @param workspace The workspace that will list the entity.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ExceptionManagerModel(ExceptionManager exceptionManager,
            Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _modelContainer = exceptionManager;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the ExceptionManager that contains the model.
     *  @return The ExceptionManager that contains this model.
     *  @see #setModelContainer(ExceptionManager)
     */
    public ExceptionManager getModelContainer() {
        return _modelContainer;
    }

    /** Set the exception manager that contains this model.
     *  @param exceptionManager The exception manager that should contain this
     *  model.
     *  @see #getModelContainer()
     */
    public void setModelContainer(ExceptionManager exceptionManager) {
        _modelContainer = exceptionManager;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The ExceptionManager that contains this model */
    private ExceptionManager _modelContainer;
}
