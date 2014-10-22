/*
@Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.tdl.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * A TDL task is an SDF actor with TDL specific parameters.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 *
 */
public class TDLTask extends TypedCompositeActor {

    /**
     * Construct a TDL task. You can then change the name with
     * setName(). If the workspace argument is null, then use the default
     * workspace. You should set a director before attempting to execute it. You
     * should set the container before sending data to it. Increment the version
     * number of the workspace.
     *
     * @exception NameDuplicationException Thrown if parameters cannot be set.
     * @exception IllegalActionException Thrown if parameters cannot be set.
     */
    public TDLTask() throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /**
     * Construct a TDL Task in the specified workspace with no container
     * and an empty string as a name. You can then change the name with
     * setName(). If the workspace argument is null, then use the default
     * workspace. You should set a director before attempting to execute it. You
     * should set the container before sending data to it. Increment the version
     * number of the workspace.
     *
     * @param workspace
     *            The workspace that will list the actor.
     * @exception NameDuplicationException
     * @exception IllegalActionException
     */
    public TDLTask(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /**
     * Create a TDL task with a name and a container. The container argument must
     * not be null, or a NullPointerException will be thrown. This actor will
     * use the workspace of the container for synchronization and version
     * counts. If the name argument is null, then the name is set to the empty
     * string. Increment the version of the workspace. This actor will have no
     * local director initially, and its executive director will be simply the
     * director of the container. You should set a director before attempting to
     * execute it.
     *
     * @param container
     *            The container actor.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the container is incompatible with this actor.
     * @exception NameDuplicationException
     *                If the name coincides with an actor already in the
     *                container.
     */
    public TDLTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Frequency of the task invocation in the mode period.
     */
    public Parameter frequency;

    /**
     * Describes if task is a fast task.
     */
    public Parameter fast;

    /**
     * Slot selection string for the task.
     */
    public Parameter slots;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the list of ports of the modal model that this task reads from.
     * @param refinementInputPorts Inputports of the refinement which is
     * the container of this task.
     * @param moduleInputPorts Inputports of the module that contains this task.
     * @return List of modal ports.
     */
    public List<ModalPort> getSensorsReadFrom(List refinementInputPorts,
            List moduleInputPorts) {
        if (_readsFromSensors == null) {
            _readsFromSensors = new ArrayList();
            List<IOPort> taskRefinementInputs = null;

            List<IOPort> taskInputPorts = inputPortList();
            taskRefinementInputs = new ArrayList();
            for (IOPort inputPort : taskInputPorts) {
                taskRefinementInputs.addAll(inputPort.connectedPortList());
            }
            taskRefinementInputs.retainAll(refinementInputPorts);
            for (IOPort taskRefinementInput : taskRefinementInputs) {
                //for (Iterator inputIt = taskRefinementInputs.iterator(); inputIt.hasNext();) {
                List taskModuleInputs = new ArrayList();
                taskModuleInputs
                        .addAll(taskRefinementInput.connectedPortList());
                taskModuleInputs.retainAll(moduleInputPorts);
                _readsFromSensors.addAll(taskModuleInputs);
            }
        }
        return _readsFromSensors;
    }

    /**
     * Create a new TDL port.
     *
     * @param name Name of the TDL port.
     * @return a new TDL Task output port.
     * @exception NameDuplicationException If the name for the port already exists.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();

            TDLTaskPort port = new TDLTaskPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         preivate methods                  ////

    /**
     * Initialize the TDL task.
     *
     * @exception NameDuplicationException Thrown if parameters cannot be set.
     * @exception IllegalActionException Thrown if parameters cannot be set.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        SDFDirector director = new SDFDirector(this, "SDF director");
        director.iterations.setExpression("0");
        frequency = new Parameter(this, "frequency");
        frequency.setExpression("1");
        fast = new Parameter(this, "fast");
        fast.setExpression("false");
        slots = new Parameter(this, "slots");
        slots.setExpression("'1*'");
    }

    /**
     * List of modal ports this task reads from.
     */
    private ArrayList _readsFromSensors;
}
