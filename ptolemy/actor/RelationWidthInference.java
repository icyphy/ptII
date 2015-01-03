/* A utility class to infer the width of relations that don't specify the width

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

package ptolemy.actor;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////RelationWidthInference

/**
A class that offers convenience utility methods to infer the widths of
relations in a composite actor.


@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
 */

public class RelationWidthInference {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create RelationWidthInference, the algorithm for width inference.
     * Also set the top level to the value given as argument.
     * @param topLevel The top level CompositeActor.
     * @exception IllegalArgumentException If the specified actor is not the
     *   top level container. That is, its container is not null.
     */
    public RelationWidthInference(CompositeActor topLevel) {

        if (topLevel == null) {
            throw new IllegalArgumentException(
                    "The toplevel should not be a null pointer.");
        }

        _topLevel = topLevel;
    }

    /** Determine whether widths are currently being inferred or not.
     *  @return True When widths are currently being inferred.
     */
    public boolean inferringWidths() {
        return _inferringWidths;
    }

    /**
     *  Infer the width of the relations for which no width has been
     *  specified yet.
     *  The specified actor must be the top level container of the model.
     *  @exception IllegalActionException If the widths of the relations at port are not consistent
     *                  or if the width cannot be inferred for a relation.
     */
    public void inferWidths() throws IllegalActionException {
        if (_needsWidthInference) {
            // Extra test for compositeActor != null since when the manager is changed
            // the old manager gets a null pointer as compositeActor.
            // Afterwards width inference should not be done anymore on this manager
            // (this will throw a null pointer exception since _topLevel will be set to null).
            if (_topLevel.getContainer() instanceof CompositeActor) {
                throw new IllegalArgumentException(
                        "Width inference failed: The specified actor is "
                                + "not the top level container.");
            }
            final boolean logTimings = false;
            boolean checkConsistencyAtMultiport = true;

            {
                Token checkConsistency = ModelScope.preferenceValue(_topLevel,
                        "_checkWidthConsistencyAtMultiports");
                if (checkConsistency instanceof BooleanToken) {
                    checkConsistencyAtMultiport = ((BooleanToken) checkConsistency)
                            .booleanValue();
                }
            }
            boolean checkWidthConstraints = true;

            {
                Token checkConstraints = ModelScope.preferenceValue(_topLevel,
                        "_checkWidthConstraints");
                if (checkConstraints instanceof BooleanToken) {
                    checkWidthConstraints = ((BooleanToken) checkConstraints)
                            .booleanValue();
                }
            }

            long startTime = 0L;
            // FindBugs: avoid dead local store.
            if (logTimings) {
                startTime = new Date().getTime();
            }

            try {
                _topLevel.workspace().getWriteAccess();
                _inferringWidths = true;

                Set<ComponentRelation> relationList = _topLevel
                        .deepRelationSet();
                Set<IORelation> workingRelationSet = new HashSet<IORelation>();
                Set<IOPort> workingPortSet = new HashSet<IOPort>();
                Set<IOPort> workingDefaultPortSet = new HashSet<IOPort>();
                HashSet<IORelation> unspecifiedSet = new HashSet<IORelation>();
                HashSet<IOPort> portsToCheckConsistency = new HashSet<IOPort>();
                HashSet<IOPort> portsThatCanBeIngnoredForConsistencyCheck = new HashSet<IOPort>();

                for (ComponentRelation componentRelation : relationList) {
                    if (componentRelation instanceof IORelation) {
                        IORelation relation = (IORelation) componentRelation;
                        if (!relation._skipWidthInference()) {
                            if (relation.needsWidthInference()) {
                                unspecifiedSet.add(relation);
                            }
                            if (!relation.isWidthFixed()
                                    && relation.needsWidthInference()) {
                                // If connected to non-multiports => the relation should be 1
                                List<?> linkedObjects = relation
                                        .linkedObjectsList();
                                if (linkedObjects.isEmpty()) {
                                    relation._setInferredWidth(0);
                                    workingRelationSet.add(relation);
                                } else {
                                    for (Object object : linkedObjects) {
                                        if (object instanceof IOPort) {
                                            IOPort port = (IOPort) object;

                                            if (!port.isMultiport()) {
                                                relation._setInferredWidth(1);
                                                //FIXME: Can be zero in the case that this relation
                                                //      has no other port connected to it

                                                workingRelationSet
                                                .add(relation);
                                                break; //Break the for loop.
                                            } else {
                                                //Add known outside relations
                                                for (Object connectedRelationObject : port
                                                        .linkedRelationList()) {
                                                    IORelation connectedRelation = (IORelation) connectedRelationObject;
                                                    if (connectedRelation != null
                                                            && connectedRelation
                                                            .isWidthFixed()) {
                                                        workingRelationSet
                                                        .add(connectedRelation);
                                                    }
                                                }
                                                //Add known inside relations
                                                for (Object connectedRelationObject : port
                                                        .insideRelationList()) {
                                                    IORelation connectedRelation = (IORelation) connectedRelationObject;
                                                    if (connectedRelation != null
                                                            && connectedRelation
                                                            .isWidthFixed()) {
                                                        workingRelationSet
                                                        .add(connectedRelation);
                                                    }
                                                }
                                                if (port.hasWidthConstraints()) {
                                                    workingPortSet.add(port);
                                                }
                                                if (port.getDefaultWidth() >= 0) {
                                                    workingDefaultPortSet
                                                    .add(port);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                LinkedList<IORelation> workingRelationList = new LinkedList<IORelation>(
                        workingRelationSet);
                workingRelationSet = null;

                if (logTimings) {
                    System.out
                    .println("Width inference - initialization: "
                            + (System.currentTimeMillis() - startTime)
                            + " ms.");
                }

                long afterinit = 0L;
                // FindBugs: avoid dead local store.
                if (logTimings) {
                    afterinit = new Date().getTime();
                }

                boolean continueInference = true;

                while (continueInference
                        && !unspecifiedSet.isEmpty()
                        && (!workingPortSet.isEmpty()
                                || !workingRelationList.isEmpty() || !workingDefaultPortSet
                                .isEmpty())) {

                    while (!workingRelationList.isEmpty()
                            && !unspecifiedSet.isEmpty()) {

                        //IORelation relation = workingSet2.pop();
                        // pop has been added to LinkedList in Java 1.6
                        // (cfr. http://download.oracle.com/javase/6/docs/api/java/util/LinkedList.html#pop() ).
                        // Hence we use get an remove for the time being...
                        IORelation relation = workingRelationList.get(0);
                        workingRelationList.remove(0);

                        unspecifiedSet.remove(relation);
                        assert !relation.needsWidthInference();
                        int width = relation.getWidth();
                        assert width >= 0;

                        // All relations in the same relation group need to have the same width
                        for (Object otherRelationObject : relation
                                .relationGroupList()) {
                            IORelation otherRelation = (IORelation) otherRelationObject;
                            if (relation != otherRelation
                                    && otherRelation.needsWidthInference()) {
                                otherRelation._setInferredWidth(width);
                                unspecifiedSet.remove(otherRelation);
                                // We don't need to add otherRelation to unspecifiedSet since
                                // we will process all ports linked to the complete relationGroup
                                // all at once.
                            }
                        }

                        // Now we see whether we can determine the widths of relations directly connected
                        // at the multiports linked to this relation.

                        // linkedPortList() can contain a port more than once. We only want
                        // them once. We will also only add multiports
                        HashSet<IOPort> multiports = new HashSet<IOPort>();
                        for (Object port : relation.linkedPortList()) {
                            if (((IOPort) port).isMultiport()) {
                                multiports.add((IOPort) port);
                            }
                        }
                        for (IOPort port : multiports) {
                            List<IORelation> updatedRelations = new LinkedList<IORelation>();
                            _updateRelationsFromMultiport(port,
                                    updatedRelations);
                            if (checkConsistencyAtMultiport
                                    && !updatedRelations.isEmpty()) {
                                // If we have updated relations for this port, it means that it is consistent
                                // and hence we don't need to check consistency anymore.
                                portsThatCanBeIngnoredForConsistencyCheck
                                .add(port);
                                for (IORelation updatedRelation : updatedRelations) {
                                    portsToCheckConsistency
                                    .addAll(updatedRelation
                                            .linkedPortList(port));
                                }
                            }
                            workingRelationList.addAll(updatedRelations);
                        }
                    }

                    // Use the width constraints on ports to infer the widths.
                    if (!workingPortSet.isEmpty() && !unspecifiedSet.isEmpty()) {
                        continueInference = false;
                        LinkedList<IOPort> workingPortList = new LinkedList<IOPort>(
                                workingPortSet);
                        for (IOPort port : workingPortList) {
                            List<IORelation> updatedRelations = new LinkedList<IORelation>();
                            boolean constraintStillUseful = _updateRelationsFromMultiport(
                                    port, updatedRelations);
                            if (!updatedRelations.isEmpty()) {
                                workingPortSet.remove(port);
                                continueInference = true;
                                // We found new information, so we can try to infer
                                // new relations.
                            } else if (!constraintStillUseful) {
                                workingDefaultPortSet.remove(port);
                            }
                            if (checkConsistencyAtMultiport
                                    && !updatedRelations.isEmpty()) {
                                // If we have updated relations for this port, it means that it is consistent
                                // and hence we don't need to check consistency anymore.
                                portsThatCanBeIngnoredForConsistencyCheck
                                .add(port);
                                for (IORelation updatedRelation : updatedRelations) {
                                    portsToCheckConsistency
                                    .addAll(updatedRelation
                                            .linkedPortList(port));
                                }
                            }
                            workingRelationList.addAll(updatedRelations);
                        }
                    }

                    // If we can't infer any widths anymore (workingRelationList.isEmpty())
                    // we will look whether there are ports that have a default width.
                    if (!workingDefaultPortSet.isEmpty()
                            && workingRelationList.isEmpty()) {
                        LinkedList<IOPort> workingDefaultPortList = new LinkedList<IOPort>(
                                workingDefaultPortSet);
                        for (IOPort port : workingDefaultPortList) {
                            List<IORelation> updatedRelations = new LinkedList<IORelation>();
                            boolean constraintStillUseful = _updateRelationsFromDefaultWidth(
                                    port, updatedRelations);
                            if (!updatedRelations.isEmpty()) {
                                workingDefaultPortSet.remove(port);
                                continueInference = true;
                                // We found new information, so we can try to infer
                                // new relations.
                            } else if (!constraintStillUseful) {
                                workingDefaultPortSet.remove(port);
                            }
                            workingRelationList.addAll(updatedRelations);
                        }
                    }
                }

                if (logTimings) {
                    System.out
                    .println("Actual algorithm: "
                            + (System.currentTimeMillis() - afterinit)
                            + " ms.");
                }

                //Consistency check
                if (checkConsistencyAtMultiport) {

                    portsToCheckConsistency
                    .removeAll(portsThatCanBeIngnoredForConsistencyCheck);
                    for (IOPort port : portsToCheckConsistency) {
                        _checkConsistency(port);
                    }
                }
                if (checkWidthConstraints) {
                    for (IOPort port : workingPortSet) {
                        _checkConsistency(port);
                        port.checkWidthConstraints();
                    }
                }

                if (!unspecifiedSet.isEmpty()) {

                    boolean defaultInferredWidthTo1 = false;

                    {
                        Token defaultTo1 = ModelScope.preferenceValue(
                                _topLevel, "_defaultInferredWidthTo1");
                        if (defaultTo1 instanceof BooleanToken) {
                            defaultInferredWidthTo1 = ((BooleanToken) defaultTo1)
                                    .booleanValue();
                        }
                    }

                    if (defaultInferredWidthTo1) {
                        for (IORelation relation : unspecifiedSet) {
                            relation._setInferredWidth(1);
                        }
                    } else {
                        StringBuffer portDetails = new StringBuffer();
                        IORelation relation = unspecifiedSet.iterator().next();
                        Iterator deepPorts = relation.deepLinkedPortList()
                                .iterator();
                        while (deepPorts.hasNext()) {
                            if (portDetails.length() > 0) {
                                portDetails.append("\n");
                            }
                            portDetails.append(((IOPort) deepPorts.next())
                                    .getFullName());
                        }

                        String message1 = "The width of relation "
                                + relation.getFullName()
                                + " can not be uniquely inferred.\n";
                        String message2 = "One possible solution is to create a toplevel parameter "
                                + "named \"_defaultInferredWidthTo1\" with the boolean "
                                + "value true.\n"
                                + "Please make the width inference deterministic by"
                                + " explicitly specifying the width of this relation."
                                + " In the user interface, right click on the "
                                + "relation, select Configure and change the width. "
                                + " Note that some actors may need to have their "
                                + " Java code updated to call setDefaultWidth(1) "
                                + "on the output port. "
                                + "The relation is deeply connected to these ports:\n"
                                + portDetails.toString();
                        Manager manager = ((CompositeActor) relation.toplevel())
                                .getManager();
                        if (manager != null
                                && manager.getState() != Manager.IDLE) {
                            throw new IllegalActionException(
                                    relation,
                                    message1
                                    + "The model is not idle, so stopping the model "
                                    + "might help.\n" + message2);
                        }
                        throw new IllegalActionException(relation, message1
                                + message2);
                    }
                }

            } finally {
                _inferringWidths = false;
                _topLevel.workspace().doneTemporaryWriting();
                if (logTimings) {
                    System.out
                    .println("Time to do width inference: "
                            + (System.currentTimeMillis() - startTime)
                            + " ms.");
                }
            }
            _needsWidthInference = false;
        }
    }

    /**
     *  Return whether the current widths of the relation in the model
     *  are no longer valid anymore and the widths need to be inferred again.
     *  @return True when width inference needs to be executed again.
     */
    public boolean needsWidthInference() {
        return _needsWidthInference;
    }

    /**
     *  Notify the width inference algorithm that the connectivity in the model changed
     *  (width of relation changed, relations added, linked to different ports, ...).
     *  This will invalidate the current width inference.
     */
    public void notifyConnectivityChange() {
        if (!_inferringWidths) {
            _needsWidthInference = true;
        }
        // If we are currently inferring widths we ignore connectivity changes,
        // since evaluating expressions can cause a call of attributesChanged,
        // which results in notifyConnectivityChange. In this case we aren't
        // changing the model, but just getting all parameters.
        // Notice that we use the boolean _inferringWidths without any locking
        // This is to avoid deadlocks... In case we are inferring widths notifyConnectivityChange
        // should only be called from the same thread as the one that is inferring
        // widths and hence there is no issue. When the user is actually changing he model
        // we shouldn't be doing width inference and hence the parameter should not be
        // changing.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Filter the relations for which the width still has to be inferred.
     *  @param  relationList The relations that need to be filtered.
     *  @return The relations for which the width still has to return.
     *  @exception IllegalActionException If the expression for the width cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    static protected Set<IORelation> _relationsWithUnspecifiedWidths(
            List<?> relationList) throws IllegalActionException {
        Set<IORelation> result = new HashSet<IORelation>();
        for (Object relation : relationList) {
            if (relation != null
                    && ((IORelation) relation).needsWidthInference()) {
                result.add((IORelation) relation);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Check whether the widths at a port are consistent. Consistent means that
     * the input and output width is either zero or that the input width is equal
     * to the output width.
     * @param  port The port which will be checked.
     * @exception IllegalActionException If the widths of the relations at port
     *                 are not consistent.
     */
    static private void _checkConsistency(IOPort port)
            throws IllegalActionException {
        // We check whether the inside and outside widths are consistent. In case
        // widths are inferred they should be inferred uniquely. We don't want to have
        // different results depending on where we start in the graph.
        int insideWidth = port._getInsideWidth(null);
        int outsideWidth = port._getOutsideWidth(null);

        // Special case.
        if (port instanceof SubscriptionAggregatorPort) {
            if (insideWidth != 1) {
                throw new IllegalActionException(port,
                        "The inside width is required to be 1. Got "
                                + insideWidth);
            }
            return;
        }

        if (insideWidth != 0 && outsideWidth != 0
                && insideWidth != outsideWidth) {
            throw new IllegalActionException(
                    port,
                    "The inside width ("
                            + insideWidth
                            + ") and the outside width ("
                            + outsideWidth
                            + ") of port "
                            + port.getFullName()
                            + " are not either equal to 0 or not equal to each other and are therefore"
                            + " inconsistent.\nCan't determine a uniquely defined width for"
                            + " the connected relations. A possible fix is to right clicking on either the"
                            + " inside or outside relation and set the width -1.");
        }
    }

    /**
     * Infer the width for the relations connected to the port. If the width can be
     * inferred, update the width and add the relations for which the width has been
     * updated.
     * @param port The port for whose connected relations the width should be inferred.
     * @param updatedRelations The relations for which the width has been updated.
     * @return true When this constraint is still useful (can be used to extra more information).
     * @exception IllegalActionException If the widths of the relations at port are not consistent.
     */
    static private boolean _updateRelationsFromMultiport(IOPort port,
            List<IORelation> updatedRelations) throws IllegalActionException {
        boolean constraintStillUseful = true;
        Set<IORelation> outsideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port
                .linkedRelationList());
        //port.linkedRelationList() returns the outside relations

        int outsideUnspecifiedWidthsSize = outsideUnspecifiedWidths.size();

        NamedObj namedObject = port.getContainer();
        if (namedObject == null) {
            assert false; // not expected
            return false;
        }
        int difference = -1;
        Set<IORelation> unspecifiedWidths = null;
        if (namedObject instanceof AtomicActor) {

            assert outsideUnspecifiedWidthsSize >= 0;
            if (outsideUnspecifiedWidthsSize > 0 && port.hasWidthConstraints()) {
                difference = port.getWidthFromConstraints();
                unspecifiedWidths = outsideUnspecifiedWidths;
                if (difference < 0) {
                    return true; // Constraints still unknown
                }
            } else {
                return false;
            }
        } else {

            Set<IORelation> insideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port
                    .insideRelationList());
            int insideUnspecifiedWidthsSize = insideUnspecifiedWidths.size();

            if (insideUnspecifiedWidthsSize > 0
                    && outsideUnspecifiedWidthsSize > 0) {
                return true;
            }
            if (insideUnspecifiedWidthsSize == 0
                    && outsideUnspecifiedWidthsSize == 0) {
                return false;
            }

            int insideWidth = port._getInsideWidth(null);
            int outsideWidth = port._getOutsideWidth(null);

            if (insideUnspecifiedWidthsSize > 0) {
                unspecifiedWidths = insideUnspecifiedWidths;
                difference = outsideWidth - insideWidth;
            } else {
                assert outsideUnspecifiedWidthsSize > 0;
                unspecifiedWidths = outsideUnspecifiedWidths;
                difference = insideWidth - outsideWidth;
            }

            // We expect that the following case does not exist anymore since atomic
            // actors are handled separately. Hence the assert...
            // For opaque ports we not always want the same behavior.
            // For example at an add-subtract actor we only have information
            // about the outside, and hence we can't infer any widths at this port.
            // In this case difference < 0.
            // However in the case of a multimodel, you often have relations on the
            // inside and width inference needs to happen. In this case difference >=0
            if (port.isOpaque() && difference < 0) {
                assert false; // We don't expect to come in this case
                assert insideWidth == 0;
                return false; // No width inference possible and necessary at this port.
            }
            if (difference < 0) {
                throw new IllegalActionException(
                        port,
                        "The inside and outside widths of port "
                                + port.getFullName()
                                + " are not consistent.\nThe inferred width of relation "
                                + unspecifiedWidths.iterator().next()
                                .getFullName() + " would be negative.");
            }
        }

        assert unspecifiedWidths != null;
        int unspecifiedWidthsSize = unspecifiedWidths.size();

        // Put the next test in comments since the condition
        // difference >= unspecifiedWidthsSize only needs to be fulfilled
        // in case we don't allow inferred widths to be zero.
        //
        // if (difference > 0 && difference < unspecifiedWidthsSize) {
        //     throw new IllegalActionException(port,
        //             "The inside and outside widths of port " + port.getFullName()
        //             + " are not consistent.\n Can't determine a uniquely defined width for relation"
        //             + unspecifiedWidths.iterator().next().getFullName());
        // }

        if (unspecifiedWidthsSize == 1 || unspecifiedWidthsSize == difference
                || difference == 0) {
            int width = difference / unspecifiedWidthsSize;
            assert width >= 0;
            for (IORelation relation : unspecifiedWidths) {
                relation._setInferredWidth(width);
                updatedRelations.add(relation);
            }
            constraintStillUseful = false;
        }
        return constraintStillUseful;
    }

    /** Infer the width for the relations connected to the port (which should have a default width).
     *  If the width can be inferred, update the width and add the relations for which the width
     *  has been updated.
     *  @param port The port for whose connected relations the width should be inferred.
     *  @param updatedRelations The relations for which the width has been updated.
     *  @return true When this constraint is still useful (can be used to extra more information).
     *  @exception IllegalActionException If the expression for the width cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    static private boolean _updateRelationsFromDefaultWidth(IOPort port,
            List<IORelation> updatedRelations) throws IllegalActionException {
        boolean constraintStillUseful = true;

        Set<IORelation> outsideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port
                .linkedRelationList());
        //port.linkedRelationList() returns the outside relations

        int outsideUnspecifiedWidthsSize = outsideUnspecifiedWidths.size();

        NamedObj namedObject = port.getContainer();
        if (namedObject == null) {
            assert false; // not expected
            return false;
        }
        assert outsideUnspecifiedWidthsSize >= 0;
        if (outsideUnspecifiedWidthsSize > 0) {
            int difference = port.getDefaultWidth();
            assert difference >= 0;
            int unspecifiedWidthsSize = outsideUnspecifiedWidths.size();
            if (unspecifiedWidthsSize == 1
                    || unspecifiedWidthsSize == difference || difference == 0) {
                int width = difference / unspecifiedWidthsSize;
                assert width >= 0;
                for (IORelation relation : outsideUnspecifiedWidths) {
                    relation._setInferredWidth(width);
                    updatedRelations.add(relation);
                }
                constraintStillUseful = false;
            }
        } else {
            constraintStillUseful = false;
        }
        return constraintStillUseful;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //True when we are inferring widths
    private boolean _inferringWidths = false;

    //True when width inference needs to happen again
    private boolean _needsWidthInference = true;

    //The top level of the model.
    private CompositeActor _topLevel = null;
}
