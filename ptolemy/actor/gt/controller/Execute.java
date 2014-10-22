/* An event to execute the model in the model parameter.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.Token;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.domains.ptera.kernel.PteraErrorEvent;
import ptolemy.domains.ptera.lib.EventUtils;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Execute

/**
 An event to execute the model in the model parameter.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Execute extends GTEvent {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public Execute(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Clone the event into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Execute newObject = (Execute) super.clone(workspace);
        newObject._effigy = null;
        newObject._managers = new LinkedList<Manager>();
        return newObject;
    }

    /** Process this event and execute the model in the model parameter to
     *  completion.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the model cannot be executed, or if
     *   thrown by the superclass.
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        NamedObj toplevel = getModelParameter().getModel().toplevel();
        if (toplevel instanceof CompositeActor) {
            try {
                _debug(new PteraDebugEvent(this, "Prepare to execute model."));

                CompositeActor actor = (CompositeActor) toplevel;
                if (_effigy == null || _effigy.getContainer() == null) {
                    Effigy parentEffigy = EventUtils.findToplevelEffigy(this);
                    if (parentEffigy == null) {
                        // Can't find the effigy, which may mean the user closes
                        // the window for the main model.
                        return data;
                    }
                    try {
                        _effigy = new PtolemyEffigy(parentEffigy,
                                parentEffigy.uniqueName("_executeEffigy"));
                    } catch (NameDuplicationException e) {
                        throw new IllegalActionException(this, e,
                                "Unexpected name duplication exception.");
                    }
                }
                _effigy.setModel(actor);
                Manager oldManager = actor.getManager();
                Manager manager = new Manager(actor.workspace(), "_manager");
                synchronized (_managers) {
                    _managers.add(manager);
                }
                actor.setManager(manager);
                try {
                    _debug(new PteraDebugEvent(this, "Start model execution."));
                    manager.execute();
                    _debug(new PteraDebugEvent(this,
                            "Model execution finished."));
                } finally {
                    synchronized (_managers) {
                        _managers.remove(manager);
                    }
                    actor.workspace().remove(manager);
                    actor.setManager(oldManager);
                }
            } catch (KernelException e) {
                _debug(new PteraErrorEvent(this, "Error occurred while "
                        + "executing model."));
                throw new IllegalActionException(this, e, "Unable to execute "
                        + "model.");
            } finally {
                _effigy.setModel(null);
            }
        } else {
            _debug(new PteraErrorEvent(this,
                    "Unable to execute a model that is "
                            + "not a CompositeActor."));
            throw new IllegalActionException("Unable to execute a model that "
                    + "is not a CompositeActor.");
        }

        return data;
    }

    /** Stop the execution of this event, which causes the model being executed
     *  to be stopped, if any.
     */
    @Override
    public void stop() {
        synchronized (_managers) {
            for (Manager manager : _managers) {
                manager.stop();
            }
        }
    }

    /** The effigy for the executed model.
     */
    private PtolemyEffigy _effigy;

    /** The manager to execute the model.
     */
    private List<Manager> _managers = new LinkedList<Manager>();
}
