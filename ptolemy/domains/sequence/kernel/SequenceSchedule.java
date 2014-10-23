/* A count and a list of schedule elements.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.BooleanSelect;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.domains.sequence.lib.Break;
import ptolemy.domains.sequence.lib.While;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// SequenceSchedule

/**
 An extension of the Schedule class to support sequenced actors.
 This includes support for control actors and support for upstream actors.
 If control actors are present, the firing of a SequenceSchedule depends on
 which control branches are chosen.

 The SequenceSchedule is different from other schedules in that it

 This class overwrites actorIterator() and firingIterator().
 FIXME:  actorIterator() is not implemented yet.

 The firingIterator() will evaluate the condition of control actors and determine
 which actors to place in the scheduler next.

 FIXME:  add(), insert(), remove() currently not supported.  What should they do for this
 kind of schedule?  Not sure how other entities should be allowed to add to schedule.

 FIXME:  size() is also not correct.  This should probably count all actors in the
 sub-graphs as well?  How should the control branches be reflected in the size()?
 Should it just be the number of actors scheduled so far?

 FIXME:  toString() not implemented.

 The SequenceSchedule schedules itself...

 // Begin Ptolemy standard comments

 This class represents a static schedule of actor executions.  An
 instance of this class is returned by the scheduler of a model to
 represent order of actor firings in the model.  A schedule consists of
 a list of schedule elements and the number of times the schedule
 should repeat (called the <i>iteration count</i>). <p>

 Each element of
 the schedule is represented by an instance of the ScheduleElement
 class.  Each element may correspond to a number of firings of a single
 actor (represented by the Firing class) or an entire sub-schedule
 (represented by a hierarchically contained instance of this class).
 This nesting allows this concise representation of looped schedules.
 The nesting can be arbitrarily deep, but must be a tree where the
 leaf nodes represent actor firings.  It is up to the scheduler to
 enforce this requirement. <p>

 The add() and remove() methods are used to add or
 remove schedule elements. Only elements of type ScheduleElement (Schedule
 or Firing) may be added to the schedule list. Otherwise an exception will
 occur. <p>

 The iteration count is set by the
 setIterationCount() method. If this method is not invoked, a default value
 of one will be used. <p>

 As an example, suppose that we have an SDF graph containing actors
 A, B, C, and D, with the firing order ABCBCBCDD.

 This firing order can be represented by a simple looped schedule.  The
 code to create this schedule appears below.

 <p>
 <pre>
 Schedule S = new Schedule();
 Firing S1 = new Firing();
 Schedule S2 = new Schedule();
 Firing S3 = new Firing();
 S.add(S1);
 S.add(S2);
 S.add(S3);
 S1.setActor(A);
 S2.setIterationCount(3);
 Firing S2_1 = new Firing();
 Firing S2_2 = new Firing();
 S2_1.setActor(B);
 S2_2.setActor(C);
 S2.add(S2_1);
 S2.add(S2_2);
 S3.setIterationCount(2);
 S3.setActor(D);
 </pre>
 <p>

 Note that this implementation is not synchronized. It is therefore not safe
 for a thread to make modifications to the schedule structure while
 multiple threads are concurrently accessing the schedule.
 <h1>References</h1>
 S. S. Bhattacharyya, P K. Murthy, and E. A. Lee,
 Software Syntheses from Dataflow Graphs, Kluwer Academic Publishers, 1996.

 @author Brian K. Vogel, Steve Neuendorffer, Elizabeth Latronico (Bosch)
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
 @see ptolemy.actor.sched.Firing
 @see ptolemy.actor.sched.ScheduleElement
 */
public class SequenceSchedule extends Schedule {
    /** Construct a schedule with iteration count of one and an
     *  empty schedule list. This constructor should be used when
     *  creating a root schedule. The constructor that takes a
     *  parameter should be used when creating a subschedule.
     */

    // FIXME:
    // Allow a default constructor?  This doesn't really make sense for a SequenceSchedule.
    public SequenceSchedule() {
        super();
    }

    /** Instantiate a new schedule based on these data structures.
     *  @param independentList List of independent sequence attributes.
     *  (and, associated actors, found by calling .getContainer
     *  on a SequenceAttribute)
     *  There must be at least one sequence attribute in the _independentList.
     *  @param controlTable A hash table mapping a sequence actor name
     *  to a hash table of branches and a list of dependent actors (if
     *  any) for each branch.
     *  @param sequencedActorsToSubgraph Hash table of sequenced actor
     *  nodes to their subgraphs Used in conjunction with the control
     *  graph for determining the schedule.
     */
    public SequenceSchedule(
            List<SequenceAttribute> independentList,
            Hashtable<SequenceAttribute, Hashtable> controlTable,
            Hashtable<SequenceAttribute, DirectedAcyclicGraph> sequencedActorsToSubgraph) {
        super();

        // Store data structures
        _originalIndependentList = new ArrayList<SequenceAttribute>();
        _independentList = new ArrayList<SequenceAttribute>();
        _unexecutedList = new ArrayList<SequenceAttribute>();

        for (int i = 0; i < independentList.size(); i++) {
            _independentList.add(independentList.get(i));
            _originalIndependentList.add(independentList.get(i));
        }

        _controlTable = controlTable;
        _sequencedActorsToSubgraph = sequencedActorsToSubgraph;

        // Initialize the _schedule to an empty list
        // Note that, for the SequenceSchedule, the _schedule list only contains
        // actors that have already been fired since the order of execution
        // is determined dynamically
        _schedule = new ArrayList<ScheduleElement>();
        _schedulePosition = 0;

        // Start at the first element in the _independentList
        // Inserts may only occur at or after the current position
        _independentListPosition = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor invocation sequence of the schedule in the
     *  form of a sequence of instances of Firing.
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  <p>
     *  Implementation note: This method is optimized to be memory
     *  efficient. It walks the schedule tree structure as the
     *  iterator methods are invoked.
     *
     *  FIXME:  Not implemented for SequenceSchedule
     *
     *  @return An iterator over instances of Firing.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     *  @exception InternalErrorException If the schedule contains
     *   any leaf nodes that are not an instance of Firing.
     */
    // Use superclass
    // public Iterator actorIterator() {

    /** FIXME:  Not implemented.  What does "add" mean for a SequenceSchedule?
     *  Should this be restricted to independently sequenced actors or
     *  could any be added (upstream, dependent on control?)
     *
     *  Add a new ScheduleElement to the list
     *
     *  Append the specified schedule element to the end of the schedule
     *  list. This element must be an instance of ScheduleElement.
     *
     *  @param element The schedule element to add.
     */

    // Use superclass

    /** FIXME:  Again, same problems with insert() as with add()
     *
     *  Insert the specified schedule element at the specified position in
     *  the schedule list. This element must be an instance of
     *  ScheduleElement. An exception is thrown if the index is out of
     *  range.
     *
     *  @param index The index at which the specified element is to be
     *   inserted.
     *  @param element The schedule element to add.
     *  @exception IndexOutOfBoundsException If the specified index is out of
     *   range (index < 0 || index > size()).
     */
    // Use superclass.  In future - new method?  Does this make sense for a
    // control schedule?
    // public void add(int index, ScheduleElement element) {

    /** Return the actor invocation sequence of this schedule in the form
     *  of a sequence of firings. All of the lowest-level nodes of the
     *  schedule should be an instance of Firing. Otherwise an exception will
     *  occur at some point in the iteration.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  <p>
     *  Implementation note: This method is optimized to be memory
     *  efficient. It walks the schedule tree structure as the
     *  iterator methods are invoked.
     *
     *  @return An iterator over a sequence of firings.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    @Override
    public Iterator firingIterator() {
        return new FiringIterator(this);
    }

    /** Return the list of unexecuted actor sequence numbers
     *  that were not executed during the schedule because their branches were not taken.
     *
     *  @return The list of unexecuted actor sequence numbers.
     */
    public List<SequenceAttribute> getUnexecutedList() {
        return _unexecutedList;
    }

    /** Return the element at the specified position in the list.
     *
     * @param index The index of the element to return.
     * @return The element at the specified position in the list.
     */
    // Use superclass
    //public ScheduleElement get(int index) {

    /** FIXME:  What should this iterator return?
     *  Should it be supported?
     *
     *  Return an iterator over the schedule elements of this schedule.
     *  The ordering of elements in the iterator sequence is the order
     *  of the schedule list. The elements of the iterator sequence are
     *  instances of Firing or Schedule.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *
     *  @return An iterator over the schedule elements of this schedule.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    // Use superclass.
    // public Iterator iterator() {

    /**  FIXME:  What should remove do?  Should it be supported?
     *
     *  Remove the schedule element at the specified position in the
     *  schedule list.
     *
     *  @param index The index of the schedule element to be removed.
     *  @return The schedule element that was removed.
     */
    // Use superclass
    //public ScheduleElement remove(int index) {

    /** Return the number of elements in this list.
     *
     *  FIXME:  Change to return the total number of actors?
     *  This will just give the number of independent actors (i.e. a
     *  sub-schedule counts as one element)
     *
     *  @return The number of elements in this list.
     */

    // public int size() {

    /**
     * Output a string representation of this Schedule.
     *
     * FIXME:  Not implemented.
     */
    // Use superclass
    //public String toString() {

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Comments:  Still throw exception if schedule changes?
     *
     *  Allow a sort() mechanism to sort unfired actors (maybe this goes
     *  in the scheduler, or should the firingIterator keep track of
     *  which things have already fired?  but can't tell if completed for sub-schedule).
     *
     *  An adapter class for iterating over the actors of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active.
     */

    // Not doing actor iterator just yet
    // Deleted code - copy from Schedule

    /** For a SequenceSchedule, this iterator actually calculates the
     *  next firing and adds the firing to _schedule.
     *  The firings of a SequenceSchedule are dynamically determined.
     *
     *  FIXME:  Should FiringIterator clear the schedule?  Otherwise, it
     *          may fire things previously added.  Alternatively,
     *          the FiringIterator could advance the schedule position to
     *          the end.  However, some actors would not be fired then.
     *
     *  An adapter class for iterating over the firings of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active. The iterator walks
     *  the schedule tree as the hasNext() and next() methods are
     *  invoked, using a small number of state variables.
     */
    private class FiringIterator implements Iterator {
        /** Construct a SequenceSchedule FiringIterator.
         *
         *  @param seqSchedule The sequence schedule for which actors will be fired.
         */
        public FiringIterator(SequenceSchedule seqSchedule) {
            // Reset the _independentList and the _schedule
            // FIXME:  In the future, save a list of all unexecuted
            // elements in both the _independentList and _schedule
            // for informational purposes

            // Clear _independentList and add in elements
            _independentList.clear();

            for (int i = 0; i < _originalIndependentList.size(); i++) {
                _independentList.add(_originalIndependentList.get(i));
            }

            // Clear the _unexecutedList
            _unexecutedList.clear();

            _schedule = new ArrayList<ScheduleElement>();
            _schedulePosition = 0;

            // If _advance is true, then hasNext() can move
            // to the next node when invoked.
            _advance = true;
            _startingVersion = _getVersion();

            // There was no last actor fired, so there was no last control
            // actor fired
            _currentControlSeqAttr = null;

            // Start at the beginning of the list of sequenced actors
            _independentListPosition = 0;

            // Print out the list
            // For debugging

            /*
            for (int i = 0; i < _independentList.size(); i++)
            {
                // Get the next sequence attribute
                SequenceAttribute seqControlActor = (SequenceAttribute) _independentList.get(i);

                System.out.println("Independent sequence attribute value: " + seqControlActor.getExpression());
                System.out.println("Actor for this sequence attribute: " + ((Actor) seqControlActor.getContainer()).getFullName());

                // From the control table, get the hash table <String, List> corresponding to this SequenceAttribute
                Hashtable branches = (Hashtable) _controlTable.get(seqControlActor);

                // This sequence attribute may or may not correspond to a control element
                // Even if it does, the control element may not have any branches
                if (branches != null)
                {
                    // Get the key set
                    Set<String> branchNames = branches.keySet();
                    Iterator branchIterator = branchNames.iterator();

                    while (branchIterator.hasNext())
                    {
                        String branchName = ((String) branchIterator.next());
                        System.out.println("Branch name: " + branchName);

                        // Get the sequence attributes of actors for this key
                        ArrayList seqAttributes = (ArrayList) branches.get((String) branchIterator.next());

                        if (seqAttributes != null)
                        {
                            Iterator actorIterator = seqAttributes.iterator();
                            while (actorIterator.hasNext())
                            {
                                SequenceAttribute seqDependentActor = (SequenceAttribute) actorIterator.next();
                                System.out.println("Dependent sequence attribute value: " + seqDependentActor.getExpression());
                                System.out.println("Actor for this sequence attribute: " + ((Actor) seqDependentActor.getContainer()).getFullName());
                            }
                        }

                    }
                }
            }
             */

        }

        /** Return true if the iteration has more elements.
         *  Also sets the _currentNode to the next firing to be fired.
         *  (must be a firing, not a schedule)
         *
         *  next() sets _advance to true
         *  Here, only set _advance to false is _lastHasNext will be set to false
         *
         *  @exception ConcurrentModificationException If the schedule
         *   data structure has changed since this iterator
         *   was created.
         *  @exception InternalErrorException If the schedule contains
         *   any leaf nodes that are not an instance of Firing.
         *  @return true if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {

            // This code may look messy, but it simply walks the
            // schedule tree.
            if (_startingVersion != _getVersion()) {

                // For the future:
                _lastHasNext = false;
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else if (_advance == true) {

                // Initialize _lastHasNext to false
                _lastHasNext = false;

                // Optimization
                _advance = false;

                // If the last schedule element fired was a break,
                // then there are no more firings - don't have to check anything else
                if (_schedulePosition > 0
                        && ((Firing) _schedule.get(_schedulePosition - 1))
                        .getActor() instanceof Break) {
                    return _lastHasNext; // which was just set to false
                }

                // If there are more ScheduleElements left in the schedule,
                // then hasNext is true
                else if (_schedulePosition < _schedule.size()) {
                    _lastHasNext = true;
                }

                // Otherwise, if there are more sequenced actors to process,
                // return true
                else if (_independentListPosition < _independentList.size()) {
                    _lastHasNext = true;
                }

                // Otherwise, if there are no more sequenced actors to process,
                // but there is a control actor that was just fired, see
                // if there are any depedendent branches that are true and that
                // contain other actors for this control actor
                else if (_currentControlSeqAttr != null) {
                    ArrayList<SequenceAttribute> seqAttributes = findBranchActors(
                            _currentControlSeqAttr, true);
                    if (seqAttributes != null && !seqAttributes.isEmpty()) {
                        _lastHasNext = true;
                    } else {
                        // If this is a control actor that has no taken dependent branches,
                        // then we still need to add its untaken branches to the unexecuted List.
                        ArrayList<SequenceAttribute> unexecutedSeqAttributes = findBranchActors(
                                _currentControlSeqAttr, false);
                        if (unexecutedSeqAttributes != null
                                && !unexecutedSeqAttributes.isEmpty()) {
                            _unexecutedList.addAll(findBranchActors(
                                    _currentControlSeqAttr, false));
                        }
                    }
                }

            } // end advance == true

            // Return the answer (either calculated just now, or previously)
            return _lastHasNext;
        }

        //        /** Generate a list of actors that have not been fired yet.
        //         *
        //         *  This function is designed to be called by the director after an
        //         *  iteration has been completed, to report the list of actors that
        //         *  were not fired during that iteration.
        //         *
        //         *  However, it can also be called at any time to report the list
        //         *  of actors that have not been fired yet.
        //         *
        //         *  Note that this function checks the whole model, and not just the
        //         *  actors that are used in an individual process.
        //         *
        //         *  @return List of actors that have not been fired
        //         */
        //        public List unfiredActors()
        //        {
        //            ArrayList<Actor> theUnfiredActors = new ArrayList();
        //
        //            // First, construct a sorted set of fired actors from the schedule
        //            // Note that we only include firings up to the current schedule position
        //            // (firings after that have been scheduled but not fired)
        //            // FIXME:  Does this work?  Are actors comparable in order to use them
        //            // in a TreeSet?  Otherwise might have to use just the names
        //            TreeSet firedActors = new TreeSet();
        //
        //            for (int i = 0; i < _schedulePosition; i++)
        //            {
        //                // The _schedule for a SequenceSchedule only contains firings
        //                Firing f = (Firing) _schedule.get(i);
        //                firedActors.add(f.getActor());
        //            }
        //
        //            // FIXME:  What behavior do we want here?
        //            // Currently, just checks for unfired actors that are supposed to be fired
        //            // by this schedule (e.g., within the same process)
        //            // using _sequencedActorToSubgraph
        //            // FIXME:  Change once dag is different
        //
        //            for (Actor key: _sequencedActorsToSubgraph.keySet())
        //            {
        //                if (!firedActors.contains(key))
        //                {
        //                    theUnfiredActors.add(key);
        //                }
        //            }
        //
        //            return theUnfiredActors;
        //        }

        /**  Finds the branch actors for a control actor
         *   corresponding to the sequence attribute passed in.
         *
         *   Returns an empty list if there are none.
         *
         *   @param seq The SequenceAttribute for a control actor.
         *   @param findEnabledActors True if we want to return the actors on the branch(es) that was taken,
         *    false if we want to return the actors on the branch(es) that were not taken.
         *   @return List of actors on the branch of a control actor that should be executed
         */
        private ArrayList<SequenceAttribute> findBranchActors(
                SequenceAttribute seq, boolean findEnabledActors) {

            ArrayList<SequenceAttribute> seqAttributes = new ArrayList<SequenceAttribute>();

            // From the control table, get the hash table <String, List> corresponding to this SequenceAttribute
            Hashtable branches = (Hashtable) _controlTable.get(seq);

            // This sequence attribute may or may not correspond to a control element
            // Even if it does, the control element may not have any branches
            if (branches != null) {
                // Figure out what branch has a token on the control actor
                // Make sure the actor is actually a control actor

                if (seq.getContainer() instanceof ControlActor) {
                    // Get list of enabled output ports

                    ArrayList outPortList = null;

                    // If findEnabledActors is true, return the actors from the executed branch(es),
                    // otherwise return the actors from the unexecuted branch(es).
                    if (findEnabledActors) {
                        outPortList = ((ControlActor) seq.getContainer())
                                .getEnabledOutports();
                    } else {
                        outPortList = ((ControlActor) seq.getContainer())
                                .getDisabledOutports();
                    }

                    // Check for null list
                    // Some actors do not have output ports (e.g. Break)

                    if (outPortList != null) {
                        for (int i = 0; i < outPortList.size(); i++) {
                            TypedIOPort p = (TypedIOPort) outPortList.get(i);

                            // hasToken is only for input ports

                            // Get the port name
                            String portName = p.getName();

                            // Get the list of actors, if any, for this branch
                            // Get the sequence attributes of actors for this key
                            seqAttributes
                            .addAll((List<SequenceAttribute>) branches
                                    .get(portName));

                            // return seqAttributes
                            // The calling function will set _lastHasNext

                        } // end output port list
                    } // end if output port list != null
                } // end instance of control actor

            } // end if branches != null
            return seqAttributes;
        }

        /** Although the iterator must return an "Object", a Schedule should
         *  always return a Firing.
         *
         *  next() actually calculates the next firing.
         *  hasNext() will NOT increment any of the
         *  iterators in any data structure
         *
         *  next() also adds things to _schedule
         *  What to add:
         *
         *  (Check control branches first for adding to schedule!!)
         *  Control branches:  Check _currentControlSeqAttr
         *      which is set by hasNext() to see if a branch is currently
         *      executing.  if so, look up branch in table.  Insert these
         *      actors, ordered by sequence number, into the sequenced actor
         *      list at the current position
         *      (so that we can check for control actors).
         *      Set _currentControlSeqAttr to null.
         *      Move to next position in sequenced item list.
         *      Call next() again to get the firing (applying all rules again).
         *
         *  Atomic non-control actors: Firings for actor plus all upstream.
         *      Upstream actors must not be control actors.
         *      Move to next position in sequenced actor list (_independentList)
         *
         *  Atomic control: Firing for actor.  Set _currentControlSeqAttr and
         *      _previousControlSeqAttr.
         *      Stay at same position in sequenced actor list (_independentList)
         *
         *  Composite opaque actor:  Firing for actor.  The director inside the actor
         *      will control execution.
         *
         *  Composite transparent actor:  There should not be any of these!
         *
         *  Return the next object in the iteration.
         *
         *  @exception InvalidStateException If the schedule
         *   data structure has changed since this iterator
         *   was created.
         *  @return the next object in the iteration.
         *  @exception NoSuchElementException If there is no next element to return.
         */
        @Override
        public Object next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("No element to return.");
            } else if (_startingVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else {
                _advance = true;
                SequenceFiring theFiring = null;

                // First, check the _schedule.  If there are firings left, return
                // the next one.
                // next() only adds FIRINGS to the schedule
                // _schedulePosition starts at 0

                if (_schedulePosition < _schedule.size()) {
                    if (!(_schedule.get(_schedulePosition) instanceof SequenceFiring)) {
                        throw new NoSuchElementException(
                                "Error - SequenceScheduler encounter a ScheduleElement that is not an instance of SequenceFiring");
                    }

                    theFiring = (SequenceFiring) _schedule
                            .get(_schedulePosition);
                    _schedulePosition++;
                }

                else {
                    // Check if we are currently executing a control branch
                    /*
                     *  Control branches:  Check _currentControlSeqAttr
                     *      which is set by hasNext() to see if a branch is currently
                     *      executing.  if so, look up branch in table.  Insert these
                     *      actors, ordered by sequence number, into the sequenced actor
                     *      list at the current position
                     *      (so that we can check for control actors).
                     *      Set _currentControlSeqAttr to null.
                     *      Call next() again to get the firing (applying all rules again).
                     */
                    if (_currentControlSeqAttr != null) {
                        // Check for branch actors to add to the _independentList
                        ArrayList seqAttributes = findBranchActors(
                                _currentControlSeqAttr, true);

                        if (seqAttributes != null && !seqAttributes.isEmpty()) {
                            // If the control actor is a While loop, add it back into
                            // the schedule after all the activated sequence actors.
                            if (_currentControlSeqAttr.getContainer() instanceof While) {
                                seqAttributes.add(_currentControlSeqAttr);
                            }

                            // Add dependent actors last-to-first at current position
                            // (so execution in _independentList will be first-to-last
                            for (int i = seqAttributes.size() - 1; i >= 0; i--) {
                                _independentList.add(_independentListPosition,
                                        (SequenceAttribute) seqAttributes
                                        .get(i));
                            }

                        }

                        // Add all the unexecuted branch(es) actors to the unexecuted list.
                        ArrayList<SequenceAttribute> unexecutedSeqAttributes = findBranchActors(
                                _currentControlSeqAttr, false);
                        if (unexecutedSeqAttributes != null
                                && !unexecutedSeqAttributes.isEmpty()) {
                            _unexecutedList.addAll(unexecutedSeqAttributes);
                        }

                        // Set current control sequence attribute to null
                        _currentControlSeqAttr = null;

                        // Call next again to get the firing
                        // If the firing is not null, we returned something from
                        // the schedule, so increment the schedule position
                        // (because any changes by next() will be lost due to recursion)
                        theFiring = (SequenceFiring) next();
                    }

                    // If we are not executing a control branch, get the next sequenced actor
                    // from _independentList

                    else {

                        /* All actors:  Compute upstream actors.  Add firings for these to _schedule.
                         *    Add sequenced actor to schedule.  Advance _independentListPosition.
                         *
                         * Atomic contol actor:  Additionally, set _currentControlSeqAttr
                         */

                        if (_independentList != null
                                && _independentListPosition < _independentList
                                .size()) {
                            // Get the next sequenced actor
                            SequenceAttribute seq = _independentList
                                    .get(_independentListPosition);

                            //System.out.println("Checking dag.");

                            // Retrieve the associated collection of upstream actors
                            DirectedAcyclicGraph dag = _sequencedActorsToSubgraph
                                    .get(seq);

                            // FIXME:  Throw an exception here, because the dag should always
                            // include the actor itself
                            if (dag != null) {

                                // Sort them.  This is the only thing sorted by the
                                // SequenceSchedule, since I'm not sure how to store a sorted graph?
                                // FIXME:  Store the sorted graph?  is this possible?
                                // The dag actually returns a list of node weights (i.e.
                                // the actor output ports or sequence/process attributes),
                                // and not a list of nodes
                                Object nodes[] = dag.topologicalSort();
                                if (nodes != null) {
                                    //System.out.println("Nodes in the dag:");
                                    // Nodes in dag
                                    //for (int i = 0; i < nodes.length; i++) {
                                    //    System.out.println("Node: " + i + " is " + ((Actor) ((NamedObj) nodes[i]).getContainer()).getFullName());
                                    //}
                                    for (Object node : nodes) {
                                        Actor act = (Actor) ((NamedObj) node)
                                                .getContainer();

                                        // If the actor to be fired is a MultipleFireMethodsInterface, then set the fire method name
                                        // for the actor. Check to see if the node in the graph is a process attribute or an output port.
                                        // If it is an output port, get the method name from the attribute contained by the output port.
                                        // If it is a ProcessAttribute, get the method name from the ProcessAttribute.
                                        // Otherwise, use the default fire method for the actor.
                                        String methodName = null;
                                        if (act instanceof MultipleFireMethodsInterface
                                                && ((MultipleFireMethodsInterface) act)
                                                .numFireMethods() > 1) {
                                            if (node instanceof ProcessAttribute) {
                                                try {
                                                    methodName = ((ProcessAttribute) node)
                                                            .getMethodName();
                                                } catch (IllegalActionException e) {
                                                    throw new NoSuchElementException(
                                                            "Problem scheduling the next actor to fire in the sequence schedule "
                                                                    + "because the ProcessAttribute for a MultipleFireMethodsInterface "
                                                                    + act.getName()
                                                                    + " with more than one fire method has an invalid fire method setting: "
                                                                    + e.getMessage());
                                                }
                                            } else if (node instanceof IOPort) {
                                                StringAttribute methodNameAttribute = (StringAttribute) ((IOPort) node)
                                                        .getAttribute("methodName");
                                                if (methodNameAttribute != null) {
                                                    methodName = methodNameAttribute
                                                            .getValueAsString();
                                                } else {
                                                    throw new NoSuchElementException(
                                                            "Problem scheduling the next actor to fire in the sequence schedule "
                                                                    + "because the output port "
                                                                    + ((IOPort) node)
                                                                    .getName()
                                                                    + " for a MultipleFireMethodsInterface "
                                                                    + act.getName()
                                                                    + " with more than one fire method has no fire method name attribute.");
                                                }
                                            } else {
                                                methodName = ((MultipleFireMethodsInterface) act)
                                                        .getDefaultFireMethodName();
                                            }
                                        }

                                        SequenceFiring nextFiring = new SequenceFiring();
                                        nextFiring.setActor(act);
                                        nextFiring.setMethodName(methodName);
                                        _schedule.add(nextFiring);

                                        // If the actor is a boolean select, fire it twice
                                        // FIXME:  Also Boolean Switch, etc?
                                        if (act instanceof BooleanSelect) {
                                            _schedule.add(nextFiring);
                                        }
                                    }
                                }
                            }

                            else {
                                // Add a firing for the sequenced actor itself
                                // If dag exists, would have included this
                                // FIXME:  Make consistent; just store everything in table?

                                Firing nextFiring = new Firing();
                                nextFiring.setActor((Actor) seq.getContainer());
                                _schedule.add(nextFiring);
                            }

                            // Increment position in the sequenced actor list
                            _independentListPosition++;

                            // If actor is a control actor, set _currentControlSeqAttr
                            // so that branches are found next time around
                            if (seq.getContainer() instanceof ControlActor) {
                                _currentControlSeqAttr = seq;
                            }

                            // Call next() to calculate the next firing
                            theFiring = (SequenceFiring) next();

                        } // If there is nothing left in the list, a null firing will be returned.
                    } // End else not executing a control branch
                } // End else need to add things to the schedule

                return theFiring;

            } // End else !hasNext()
        }

        /** Throw an exception, since removal is not allowed. It really
         *  doesn't make sense to remove a firing from the firing
         *  sequence anyway, since there is not a 1-1 correspondence
         *  between the firing in a firing iterator and a firing in the
         *  schedule.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        //        /** Determines if an actor has already been scheduled to fire.
        //         *  FIXME:  Make this more efficient in the future!  Try hashtable of actors.
        //         *
        //         *  @param act The specified actor.
        //         *  @param methodName The specified method name to be executed for the actor.
        //         *  @return true If the actor has already been scheduled, false otherwise.
        //         */
        //        private boolean alreadyScheduled(Actor act, String methodName) {
        //            boolean checkMethodName = act instanceof MultipleFireMethodsInterface &&
        //                                        ((MultipleFireMethodsInterface) act).numFireMethods() > 1;
        //            for (int i = 0; i < _schedule.size(); i++) {
        //                SequenceFiring f = (SequenceFiring) _schedule.get(i);
        //                if (f.getActor().equals(act) &&
        //                        (!checkMethodName ||
        //                                (checkMethodName && f.getMethodName().equals(methodName)))) {
        //                    return true;
        //                }
        //            }
        //            return false;
        //        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        // Same as in Schedule

        /** Boolean variable that is true if the schedule can advance, false otherwise. */
        private boolean _advance;

        /** Boolean variable that keeps track of whether we are at the next to last position
         *  in the firing iterator.
         */
        private boolean _lastHasNext;

        /** The starting version of the firing iterator. */
        private long _startingVersion;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of schedule elements contained by this schedule.
     *  SequenceSchedules are different since the _schedule is
     *  constructed DYNAMICALLY
     *  The _schedule will be maintained by the actorIterator and
     *  firingIterator to reflect what was computed
     *  FIXME: Change the add method to add to one of the other
     *  data structures instead of the _schedule
     *  Beth - added parameterization &lt;ScheduleElement&gt;
     */
    //protected List<ScheduleElement> _schedule;

    /** Keeps track of our position in the list.  Used by the hasNext() methods
     *  of the iterators.
     */
    protected int _schedulePosition;

    /**
     *  Keeps track of the last-executed control actor.  If this variable is other
     *  than null, need to check the controlTable to get the correct next actor
     *  to execute.
     *  Note that not all control actors may be present in the _independentList
     *  since some might be nested.
     *  All control actors must have a SequenceAttribute (if an actor is nested,
     *  it will still have a SequenceAttribute, but no process name).
     */
    protected SequenceAttribute _currentControlSeqAttr;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of independent sequence attributes
     *  (and, associated actors, found by calling .getContainer
     *  on a SequenceAttribute)
     *  There must be at least one sequence attribute in the _independentList.
     */
    private List<SequenceAttribute> _independentList;

    /** List of sequence attributes for actors that were not executed
     *  in the schedule because they were on branches that were not taken.
     */
    private List<SequenceAttribute> _unexecutedList;

    /** Copy of the original list of independent actors passed in.
     *  This is used by the Firing Iterator, to reset the list each time
     *  a new Firing Iterator is constructed.
     */
    private List<SequenceAttribute> _originalIndependentList;

    /** Position in the _independentList so that we can insert things into it.
     **/
    private int _independentListPosition;

    /** A hash table mapping a sequence actor name to a hash table of
     *  branches and a list of dependent actors (if any) for each branch.
     */
    private Hashtable _controlTable;

    /** Hash table of sequenced actor nodes to their subgraphs
     *  Used in conjunction with the control graph for determining the
     *  schedule.
     */
    private Hashtable<SequenceAttribute, DirectedAcyclicGraph> _sequencedActorsToSubgraph;
}
