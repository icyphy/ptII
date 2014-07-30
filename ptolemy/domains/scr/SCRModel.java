/* An SCR model actor used for requirements specification.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.domains.scr;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
An SCR model actor used for requirements specification. For more information
about SCR see
<a href="http://www.nrl.navy.mil/itd/chacs/node/126">http://www.nrl.navy.mil/itd/chacs/node/126</a>.
<p>
An SCR model is a modal model with a different way of configuring transitions,
set and output actions. To view an SCR model as an FSM actor, set the parameter
<i>openAsSCRActor</i> to false.
@author Patricia Derler
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (pd)
@Pt.AcceptedRating Red (pd)
 */
public class SCRModel extends ModalModel {

    /** Construct an SCR model in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SCRModel(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct an SCR model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public SCRModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Initialize private variables.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        Parameter parameter = new Parameter(this, "openAsSCRActor");
        parameter.setTypeEquals(BaseType.BOOLEAN);
        parameter.setToken("true");
    }

}
