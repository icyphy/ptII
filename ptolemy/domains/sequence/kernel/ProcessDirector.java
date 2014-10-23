/* Director for the sequence model of computation plus processes.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MultiHashMap;

/**
 * The ProcessDirector implements a sequencing model of computation
 * plus process names.  The order of execution is given by adding ProcessAttributes
 * to actors in the model.
 *
 * A ProcessAttribute is a tuple (string processName, int sequenceNumber)
 *
 * Please see SequencedModelDirector for more details on how the schedule
 * is computed.
 *
 * @author Elizabeth Latronico (Bosch)
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 */

public class ProcessDirector extends SequencedModelDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The ProcessDirector will have a default scheduler of type SequenceScheduler.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ProcessDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The ProcessDirector will have a default scheduler of type SequenceScheduler.
     *
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ProcessDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The ProcessDirector will have a default scheduler of type
     *   SequenceScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ProcessDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** Parameter that contains the default list of processes to run. */
    public Parameter defaultProcessExecutionArray;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the process execution array so that the default process
     *  execution will run.
     */
    public void clearProcessExecutionArray() {
        _processExecutionArray = null;
    }

    /** Get a firingIterator for each process and execute the model.
     *  Currently, the processes are sorted alphabetically; however,
     *  they could be sorted differently in the future.
     *
     *  Also, the processes could "pre-empt" each other, by using the
     *  appropriate firingIterator.  I.e. it isn't necessary to fire
     *  everything from start to finish for one firingIterator before
     *  choosing a different one.
     *
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration. <p>
     *
     *  This method may be overridden by some domains to perform additional
     *  domain-specific operations.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Collected all the unexecuted sequence numbers from each process in a single list.
        ArrayList<SequenceAttribute> unexecutedSequenceNumbersList = new ArrayList<SequenceAttribute>();

        // Execute the processes in the order specified by the process execution array.
        // This array will either be set by the containing composite actor, or specified in
        // the director's defaultProcessExecutionArray.
        // If the array is not specified in either place, preinitialize will default its value
        // to an alphabetically ordered list of all processes for the container component.
        for (String processName : _processExecutionArray) {
            // Get the SequenceSchedule for this process (similar to StaticSchedulingDirector)
            // Note:  This may need to move to preinitialize() if more analysis is done
            SequenceSchedule seqSchedule = _processToSchedule.get(processName);

            // Check for null
            if (seqSchedule == null) {
                throw new IllegalActionException(
                        "Process Director: No schedule was generated for process "
                                + processName);
            }

            // Call function in SequencedModelDirector to execute the schedule
            // Same for process and sequence directors
            fireSchedule(seqSchedule);

            // Add all the unexecuted actors to the list after the process' full schedule has been executed.
            unexecutedSequenceNumbersList.addAll(seqSchedule
                    .getUnexecutedList());
        }

        // If the fireUnexectedActors parameter is true,
        // fire all the unexecuted actors from all processes on branches that are not taken
        // after the full schedules from all processes are completed.
        if (((BooleanToken) fireUnexecutedActors.getToken()).booleanValue()) {
            SequenceSchedule unexecutedSchedule = null;

            while (!unexecutedSequenceNumbersList.isEmpty()) {
                unexecutedSchedule = _scheduler.getSchedule(
                        unexecutedSequenceNumbersList, false);

                fireSchedule(unexecutedSchedule);
                unexecutedSequenceNumbersList = (ArrayList<SequenceAttribute>) unexecutedSchedule
                        .getUnexecutedList();
            }
        }
    }

    /** Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly.  In addition, performing scheduling
     *  during preinitialization enables it to be present during code generation.
     *  The order in which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {

        // First, identify which sequenced actors go with which processes
        // Assemble a separate list of sequenced actors for each process
        // Pass this list into the preinitialize method for the superclass
        // (SequencedModelDirector)

        // The SequencedModelDirector supports a preinitialize method that takes
        // a set of sequenced actors
        // Calls getContainedEntities to create a list of all sequenced actor
        // attributes _sequencedList.
        // Container should be correct??
        //        CompositeActor compositeActor = (CompositeActor) getContainer();
        super.preinitialize();

        _processSeqMap = new MultiHashMap();

        // Create a hash table according to process name, with a list of
        // sequenced actors for each process
        _createProcessActorList();

        // Create a schedule for each process
        // setScheduler is called in init() of SequencedModelDirector
        _processToSchedule = new Hashtable();

        // If the process execution array is null or we are using the default process execution,
        // then check the default process execution array parameter.
        // If that is not set, then set the process execution array to just
        // be an ordered list of all processes.
        if (_processExecutionArray == null || _usingDefaultProcessArray == true) {
            _usingDefaultProcessArray = true;
            if (_getDefaultProcessExecutionArray().length > 0) {
                _processExecutionArray = _getDefaultProcessExecutionArray();
            } else {
                ArrayList<String> processes = new ArrayList<String>();
                for (String processName : (Set<String>) _processSeqMap.keySet()) {
                    processes.add(processName);
                }
                Collections.sort(processes);
                _processExecutionArray = processes.toArray(new String[processes
                                                                      .size()]);
            }
        }

        // Get key set from _processSeqMap
        for (String processName : _processExecutionArray) {
            // Get collection of ProcessAttributes for this key
            ArrayList processAttributes = (ArrayList) _processSeqMap
                    .get(processName);

            // Sort it
            Collections.sort(processAttributes);

            /*
            System.out.println("Sorted process attributes:");

            // Print out the sorted processAttributes
            Iterator i = processAttributes.iterator();
            while (i.hasNext())
            {
                System.out.println(((ProcessAttribute) i.next()).getFullName());
            }
             */

            // Create a schedule for it
            SequenceSchedule seqSchedule = _scheduler
                    .getSchedule(processAttributes);

            // Place this schedule in the hash table
            // Do we need to save this in the future, or just
            // save the FiringIterator?
            _processToSchedule.put(processName, seqSchedule);
        }

        // FIXME:  This does not work yet for the process director.
        // Should probably refactor some code from the SequencedModelScheduler to execute
        // per-Scheduler and not per-Schedule.

        // Once all processes have a schedule created, we check with the schedule to see if
        // any unreachable actors exist
        // Check to see if there are any unreachable upstream actors
        // Different in that all schedules must be processed before calling this
        /*
        if (_scheduler.unreachableActorExists())
        {
            // Throw an exception for unreachable actors
            // One exclusion:  TestExceptionHandler actors are not reported
            // since these are disconnected from the model, but don't have any
            // functionality for the model
            // This could be changed in the future to have an option to allow unreachable actors
            //System.out.println("There are unreachable upstream actors in the model: ");
            StringBuffer unreachableActors = new StringBuffer("");

            for (Actor a : _scheduler.unreachableActorList())
            {
                //System.out.println("Unreachable: " + a.getFullName());
                unreachableActors.append(a.getFullName() + ", ");
            }

            // System.out.println("Unreachable actors: " + unreachableActors);

            // Remove the last two characters ", "
            // Throw exception
            throw new IllegalActionException("There are unreachable upstream actors in the model: " + unreachableActors.substring(0, unreachableActors.length() - 2));
        }
         */

        // The SequenceSchedule will check for duplicate attributes
    }

    /** Set the process execution order by providing an array of
     *  process names.
     *  @param processArray The array of process names to be executed in
     *   the order they are specified in the array.
     */
    public void setProcessExecutionArray(String[] processArray) {
        _processExecutionArray = processArray;
        _usingDefaultProcessArray = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize the object.   In addition to all the parameters in
     *  the SequencedModelDirector base class, we also initialize the
     *  defaultProcessExecutionArray parameter which is specific to the
     *  Process Director.
     *
     *  @exception IllegalActionException If there is a problem instantiating
     *   the director's parameters.
     *  @exception NameDuplicationException If there is a problem instantiating
     *   the director's parameters.
     */
    @Override
    protected void _init() throws IllegalActionException,
    NameDuplicationException {
        super._init();

        defaultProcessExecutionArray = new Parameter(this,
                "Default Process Execution Array");
        defaultProcessExecutionArray.setTypeEquals(new ArrayType(
                BaseType.STRING));

        _processExecutionArray = null;
        _usingDefaultProcessArray = false;
    }

    /** Return the default process execution array specified by the defaultProcessExecutionArray
     *  parameter.
     *  @return The string array of process names to be executed.
     *  @exception IllegalActionException If there is a problem getting the array token from the parameter.
     */
    protected String[] _getDefaultProcessExecutionArray()
            throws IllegalActionException {
        if (defaultProcessExecutionArray.getExpression().equals("")) {
            return new String[0];
        } else {
            ArrayToken processNameArrayToken = (ArrayToken) defaultProcessExecutionArray
                    .getToken();
            Token[] processExecutionTokenArray = processNameArrayToken
                    .arrayValue();

            String[] processNameExecutionArray = new String[processExecutionTokenArray.length];
            for (int i = 0; i < processExecutionTokenArray.length; i++) {
                processNameExecutionArray[i] = ((StringToken) processExecutionTokenArray[i])
                        .stringValue();
            }
            return processNameExecutionArray;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The hashMap for Process and corresponding sequence numbers. */
    protected MultiHashMap _processSeqMap;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a MultiHashMap storing ProcessID as key and corresponding list of actor sequence
     * or process attributes associated with it.
     *
     * Uses the _sequencedList as an input, which has a list of all sequenced
     * actors in the model.
     *
     * @exception IllegalActionException If there is a problem creating the actor list.
     */
    private void _createProcessActorList() throws IllegalActionException {

        String processID = "";

        // Iterate through _sequencedList
        Iterator seqIterator = _sequencedList.iterator();
        Object seq = null;

        while (seqIterator.hasNext()) {
            // Get the next attribute
            // If it is a ProcessAttribute, add to the hash map
            seq = seqIterator.next();
            if (seq instanceof ProcessAttribute) {
                processID = ((ProcessAttribute) seq).getProcessName();

                // Beth added 01/21/09 - Ignore the process name "None", or the empty string
                // These should be treated as dependent actors
                if (!processID.equalsIgnoreCase("None")
                        && processID.length() > 0) {
                    _processSeqMap.put(processID, seq);
                }

                //else {

                // Otherwise, do nothing.  Actors with no process name,
                // but that have a sequence number, are treated as
                // dependent actors
                //processID = _DEFAULT_PROCESS;
                //}

            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A table of mappings between processes and SequenceSchedules. */
    private Hashtable<String, SequenceSchedule> _processToSchedule;

    /** The array of String names of processes to execute in a specific order. */
    private String[] _processExecutionArray;

    /** Flag that keeps track of whether or not the director is using a
     *  default process execution and not one set by the
     *  {@link #setProcessExecutionArray} method.
     */
    private boolean _usingDefaultProcessArray;
}
