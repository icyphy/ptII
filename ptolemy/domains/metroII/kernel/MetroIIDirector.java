/* Director for simplified MetroII semantic.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIIDirector

/**
 * <p> A MetroII Director governs the execution of a CompositeActor with
 * simplified MetroII semantics. </p>
 *
 * <p> The CompositeActor could contain two types of actors: Ptolemy
 * actors and Metropolis actors. Ptolemy actors are the usual actors
 * in Ptolemy, such as Const, Ramp, Display, etc. Metropolis actor is
 * either a customized Ptolemy actor (e.g. MetroIIModalModel) or a
 * MetroIICompositeActor that contains a MetroII compatible director,
 * (e.g.  MetroIISRDirector, MetroIIPNDirector). A
 * MetroIICompositeActor can still contain any Ptolemy actors. The
 * Metropolis actors are supposed to have the identical behaviors as
 * the corresponding Ptolemy actor (e.g.  MetroIIModalModel should
 * react the same way as ModalModel does). The difference is the
 * behaviors of Metropolis actor are associated with MetroII events. A
 * MetroII event is in one of the three statuses: PROPOSED, WAITING,
 * NOTIFIED. The reaction of a Metropolis actor is not executed until
 * the associated event is notified. The executions of Metropolis
 * actors are governed by the MetroIIDirector via updating the
 * statuses of events. </p>
 *
 * <p> The semantic has the two phases:</p>
 * <ol>
 * <li> Phase 1: Model Execution </li>
 * <li> Phase 2: Constraint Resolution</li>
 * </ol>
 *
 * <p>In phase 1, the MetroIIDirector calls prefire(), fire(), and
 * postfire() of each Ptolemy actor in the model and calls prefire(),
 * getfire() and postfire() of each Metropolis actor in the model. The
 * getfire() function can be seen as the same as fire() except for
 * some event proposing code inserted in the middle. The execution of
 * getfire() is in a separate thread.  When the thread proceeds to the
 * event proposing code, the thread will send a message (a list of
 * events with PROPOSED) to the director on the upper level and then
 * suspend. The thread will not proceed until the status of at least
 * one event in the list is changed to NOTIFIED. </p>
 *
 * <p> In phase 2, the MetroIIDirector collects all the events and
 * updates the event status based on the mapping constraints. A
 * mapping constraint is a rendezvous constraint that requires all the
 * specified events are in the status of PROPOSED or NOTIFIED. If an
 * event satisfies all the constraints, the status will be updated to
 * NOTIFIED, otherwise the status is updated to WAITING. In the next
 * iteration, the Metropolis Actors are executed based on the event
 * statuses. </p>
 *
 * <p> Known issues:
 * <ol>
 * <li> the 'stop execution' may not work properly. </li>
 * </ol>
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIDirector extends Director {
    /** Construct a director in the given container with the given
     *  name.  The container argument must not be null, or a
     *  NullPointerException will be thrown.  If the name argument is
     *  null, then the name is set to the empty string. Increment the
     *  version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MetroIIDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _initializeParameters();
        initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A mapping constraint is a pair of events that are rendezvous.
     *  Mapping file is a text file that specifies such constraints.
     *  In mapping file, each line is a mapping constraint, which
     *  contains two event names separated by a space.
     *
     *  <p>_mappingFileName is a string that contains the absolute
     *  path of the mapping file.</p>
     *
     *  The default value of _mappingFileName is null, which means no
     *  mapping constraint is specified.
     */
    public FileParameter mappingFileName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mappingFileName) {
            StringToken mappingFileNameToken = (StringToken) mappingFileName
                    .getToken();
            if (mappingFileNameToken == null || mappingFileNameToken.equals("")) {
                mappingFileName = null;
            } else {

                File file = mappingFileName.asFile();
                if (file != null) {
                    String filename = file.getAbsolutePath();
                    if (!filename.equals("")) {
                        try {
                            System.out.println(filename); 
                            _mappingConstraintSolver.readMapping(filename);
                        } catch (IOException ex) {
                            throw new IllegalActionException(this, ex,
                                    "Failed to open mapping file \"" + filename
                                    + "\".");
                        }
                        if (_debugging) {
                            _debug(_mappingConstraintSolver.toString());
                        }
                    }
                }
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Initialize the model controlled by this director. Call the 
     *  initialize() of super class and then wrap each actor that 
     *  is controlled by this director.
     *
     *  This method should typically be invoked once per execution, after the
     *  preinitialization phase, but before any iteration. It may be
     *  invoked in the middle of an execution, if reinitialization is
     *  desired. 
     *
     *  This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize(); 
        
        Nameable container = getContainer();

        Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                .iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof MetroIIEventHandler) {
                _actorList.add(new MetroIIActorGeneralWrapper(actor));
            } else {
                _actorList.add(new MetroIIActorIterationWrapper(actor));
            }
        }
    }
    
    /**
    * Call the prefire(), fire(), and postfire() methods of each actor
    * in the model. If the actor is a CompositeActor that contains a
    * MetroII compatible director, the getfire() method is called
    * instead of fire(). getfire() does everything fire() does and
    * proposes events in firing. When events are proposed, the
    * CompositeActor is blocked, waiting for the resolution of mapping
    * constraints The MetroIIDirector collects all the events and
    * resets the statuses of events based on the mapping
    * constraints. In the next iteration, CompositeActor executes
    * based on the updated statuses of events.
    */
    public void fire() throws IllegalActionException {
        super.fire();

        while (!_stopRequested) {
            LinkedList<Event.Builder> globalMetroIIEventList = new LinkedList<Event.Builder>();
          
            // Phase I: base model execution
            for (MetroIIActorInterface actor : _actorList) {
                LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();
                actor.startOrResume(metroIIEventList);
                globalMetroIIEventList.addAll(metroIIEventList);
            }

            // Phase II: constraint resolution
            _mappingConstraintSolver.resolve(globalMetroIIEventList);
        }

        if (_stopRequested) {
            for (MetroIIActorInterface actor : _actorList) {
                actor.close();
            }
        }
    }

    /** Clone the object into the specified workspace. The new object
     *  is <i>not</i> added to the directory of that workspace (you
     *  must do this yourself if you want it there).
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIIDirector newObject = (MetroIIDirector) super.clone(workspace);
        newObject._mappingConstraintSolver = new MappingConstraintSolver(
                _maxEvent);
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize parameters. This is called by the constructor.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
            NameDuplicationException {
        mappingFileName = new FileParameter(this, "mappingFileName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /** The maximum number of events.
     */
    private final int _maxEvent = 1000;

    /** The constraint solver
     *
     */
    private MappingConstraintSolver _mappingConstraintSolver = new MappingConstraintSolver(
            _maxEvent);

    private LinkedList<MetroIIActorInterface> _actorList = new LinkedList<MetroIIActorInterface>();
    /**
     * Read constraints from the mapping file.
     * @param filename mapping file
     */
}
