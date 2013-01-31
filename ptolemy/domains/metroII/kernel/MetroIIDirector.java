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
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
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
 * <p> A MetroII Director governs the execution of actors with
 * simplified MetroII semantics. </p>
 *
 * <p>
 * MetroIIActorInterface has to be implemented for each actor 
 * governed by MetroIIDirector. Each actor can be seen as a process
 * that could pause with MetroII events returned. The MetroII 
 * events can then be modified by MetroIIDirector. When the process 
 * is resumed, the continued execution may depend on the updated MetroII 
 * events. 
 * </p>
 * <p>
 * Each iteration has two phases. In Phase 1, MetroIIDirector 
 * calls each actor (no particular order should be presumed. See 
 * Note 1 and 2). Each actor runs until it wants to propose MetroII 
 * events: the actor saves the state and returns with MetroII events. 
 * In Phase 2, MetroIIDirector calls the MappingConstraintSolver, 
 * which updates the MetroII events based on the mapping constraints.  
 * </p>
 * <p>
 * Note 1: the MetroIIDirector is mainly used for mapping SEPARATED 
 * models (e.g. functional model and architectural model in PBD). 
 * It's highly recommended that the actors (models) are not 
 * connected by any form of wire/signal.  
 * </p>
 * <p>
 * Note 2: In MetroII (complete version), the order of actors being 
 * called is determined by the SystemC scheduler.  
 * </p>
 * 
 * <p> A simple way to implement MetroIIActorInterface is to have 
 * each actor wrapped by one of the following wrappers:
 * <ol>
 * <li> MetroIIActorIterationWrapper </li>
 * <li> MetroIIActorGeneralWrapper </li>
 * <ol>
 * MetroIIActorIterationWrapper is used for wrapping a Ptolemy actor 
 * that implements prefire(), fire(), and postfire(). 
 * MetroIIActorGeneralWrapper is used for wrapping a MetroII 
 * compatible actor. A MetroII compatible actor is a MetroIIModalModel 
 * or a MetroIICompositeActor that contains a MetroII compatible director
 * (e.g. MetroIISRDirector, MetroIIPNDirector). 
 * </p>
 * 
 * <p> 
 * MappingConstraintSolver updates the MetroII event status based 
 * on the given mapping constraints. A MetroII event is in one of the 
 * three statuses: PROPOSED, WAITING, NOTIFIED. A mapping constraint 
 * is a rendezvous constraint that requires all the
 * specified events are in the status of PROPOSED or NOTIFIED. If an
 * event satisfies all the constraints, the status will be updated to
 * NOTIFIED, otherwise the status is updated to WAITING.
 * </p>
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

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever. 
     *
     */
    public Parameter iterations;

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
                _actorList.add(new MetroIIActorBasicWrapper(actor));
            }
        }
        
        _iterationCount = 0;
    }

    /**
    * Each iteration has two phases. In Phase 1, MetroIIDirector 
    * calls each actor (no particular order should be presumed. See 
    * Note 1 and 2). Each actor runs until it wants to propose MetroII 
    * events: the actor saves the state and returns with MetroII events. 
    * In Phase 2, MetroIIDirector calls the MappingConstraintSolver, 
    * which updates the MetroII events based on the mapping constraints.  
    */
    public void fire() throws IllegalActionException {

        if (!_stopRequested) {
            LinkedList<Event.Builder> globalMetroIIEventList = new LinkedList<Event.Builder>();

            // Phase 1: base model execution
            for (MetroIIActorInterface actor : _actorList) {
                LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();
                actor.startOrResume(metroIIEventList);
                globalMetroIIEventList.addAll(metroIIEventList);
            }

            // Phase 2: constraint resolution
            _mappingConstraintSolver.resolve(globalMetroIIEventList);

            for (Event.Builder mtb : globalMetroIIEventList) {
                System.out.println(mtb.getName());
                System.out.println(mtb.getStatus());
            }
            System.out.println("==========");
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

    public boolean postfire() throws IllegalActionException {
        _iterationCount++;
        int iterationsValue = ((IntToken) (iterations.getToken())).intValue();
        if ((iterationsValue > 0) && (_iterationCount >= iterationsValue)) {
            _iterationCount = 0;
            for (MetroIIActorInterface actor : _actorList) {
                actor.close();
            }
            return false;
        }
        return true;
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
        iterations = new Parameter(this, "iterations");
        iterations.setTypeEquals(BaseType.INT);
        iterations.setExpression("-1"); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /** The iteration count. */
    protected int _iterationCount = 0;

    /** The maximum number of events.
     */
    private final int _maxEvent = 1000;

    /** The constraint solver
     *
     */
    private MappingConstraintSolver _mappingConstraintSolver = new MappingConstraintSolver(
            _maxEvent);

    private LinkedList<MetroIIActorInterface> _actorList = new LinkedList<MetroIIActorInterface>();
}
