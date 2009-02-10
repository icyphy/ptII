/* A utility class to infer the width of relations that don't specify the width

Copyright (c) 2008 The Regents of the University of California.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
////RelationWidthInference

/**
A class that offers convenience utility methods to infer the widths of
relations in a composite actor.


@author Bert Rodiers
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (rodiers)
*/

public class RelationWidthInference {
   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////


    /**
     *  Infer the width of the relations for which no width has been
     *  specified yet. 
     *  The specified actor must be the top level container of the model.
     *  @exception IllegalActionException If the widths of the relations at port are not consistent
     *                  or if the width cannot be inferred for a relation.
     */
    public void inferWidths() throws IllegalActionException {
        if (_needsWidthInference) {
            final boolean logTimings = false;
            final boolean checkConsistency = true;
            long startTime = (new Date()).getTime();

            try {
                _topLevel.workspace().getWriteAccess();

                Set<ComponentRelation> relationList = _topLevel.deepRelationSet();
                Set<IORelation> workingRelationSet = new HashSet<IORelation>();
                Set<IOPort> workingPortSet = new HashSet<IOPort>();
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
                            if (!relation.isWidthFixed() && relation.needsWidthInference()) {
                                // If connected to non-multiports => the relation should be 1                        
                                for (Object object : relation.linkedObjectsList()) {
                                    if (object instanceof IOPort) {
                                        IOPort port = (IOPort) object;
                                        
                                        if (!port.isMultiport()) {
                                            relation._setInferredWidth(1);
                                                //FIXME: Can be zero in the case that this relation
                                                //      has no other port connected to it
                                            
                                            workingRelationSet.add(relation);
                                            break; //Break the for loop.
                                        } else {
                                            //Add known outside relations
                                            for (Object connectedRelationObject : port.linkedRelationList()) {
                                                IORelation connectedRelation = (IORelation) connectedRelationObject;
                                                if (connectedRelation != null && connectedRelation.isWidthFixed()) {
                                                    workingRelationSet.add(connectedRelation);
                                                }
                                            }
                                            //Add known inside relations
                                            for (Object connectedRelationObject : port.insideRelationList()) {
                                                IORelation connectedRelation = (IORelation) connectedRelationObject;
                                                if (connectedRelation != null && connectedRelation.isWidthFixed()) {
                                                    workingRelationSet.add(connectedRelation);
                                                }
                                            }
                                            if (port.hasWidthConstraints()) {
                                                workingPortSet.add(port);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                LinkedList<IORelation> workingRelationList = new LinkedList<IORelation>(workingRelationSet);
                workingRelationSet = null;                               

                if (logTimings) {
                    System.out.println("Width inference - initialization: " +                
                            (System.currentTimeMillis() - startTime)
                                    + " ms.");
                }
                                
                long afterinit = (new Date()).getTime();
                
                boolean continueInference = true;
                
                while (continueInference && !unspecifiedSet.isEmpty() &&
                        (!workingPortSet.isEmpty() || !workingRelationList.isEmpty())) {
                    
                    while (!workingRelationList.isEmpty() && !unspecifiedSet.isEmpty()) {
                        
                        //IORelation relation = workingSet2.pop();
                        // pop has been added to LinkedList in Java 1.6
                        // (cfr. http://java.sun.com/javase/6/docs/api/java/util/LinkedList.html#pop() ).
                        // Hence we use get an remove for the time being...                        
                        IORelation relation = workingRelationList.get(0);
                        workingRelationList.remove(0);
                        
                        unspecifiedSet.remove(relation);
                        assert  !relation.needsWidthInference(); 
                        int width = relation.getWidth();            
                        assert  width >= 0;
                        
                        // All relations in the same relation group need to have the same width
                        for (Object otherRelationObject : relation.relationGroupList()) {
                            IORelation otherRelation = (IORelation) otherRelationObject;
                            if (relation != otherRelation && otherRelation.needsWidthInference()) {
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
                            List<IORelation> updatedRelations = _relationsWithUpdatedWidthForMultiport(port);
                            if (checkConsistency && !updatedRelations.isEmpty()) {
                                // If we have updated relations for this port, it means that it is consistent
                                // and hence we don't need to check consistency anymore.
                                portsThatCanBeIngnoredForConsistencyCheck.add(port);
                                for(IORelation updatedRelation : updatedRelations) {
                                    portsToCheckConsistency.addAll(updatedRelation.linkedPortList(port));             
                                }
                            }
                            workingRelationList.addAll(updatedRelations);
                        }
                    }
                    if (!workingPortSet.isEmpty() && !unspecifiedSet.isEmpty()) {
                        continueInference = false;
                        LinkedList<IOPort> workingPortList = new LinkedList<IOPort>(workingPortSet);
                        // TODO: here we only extract width from the ports with constraints,
                        //      we should also check whether all constraints match.
                        for (IOPort port : workingPortList) {                            
                            List<IORelation> updatedRelations = _relationsWithUpdatedWidthForMultiport(port);
                            if (!updatedRelations.isEmpty()) {
                                workingPortSet.remove(port);
                                continueInference = true;
                                    // We found new information, so we can try to infer
                                    // new relations.
                            }
                            if (checkConsistency && !updatedRelations.isEmpty()) {
                                // If we have updated relations for this port, it means that it is consistent
                                // and hence we don't need to check consistency anymore.
                                portsThatCanBeIngnoredForConsistencyCheck.add(port);
                                for(IORelation updatedRelation : updatedRelations) {
                                    portsToCheckConsistency.addAll(updatedRelation.linkedPortList(port));             
                                }
                            }
                            workingRelationList.addAll(updatedRelations);
                        }                        
                    }
                }
                
                if (logTimings) {
                    System.out.println("Actual algorithm: " +                
                            (System.currentTimeMillis() - afterinit)
                                    + " ms.");
                }
                
                if (!unspecifiedSet.isEmpty()) {
                    IORelation relation = unspecifiedSet.iterator().next();
                    throw new IllegalActionException(relation, 
                            "The width of relation " + relation.getFullName() +
                            " can not be uniquely inferred.\n"                        
                            + "Please make the width inference deterministic by"
                            + " explicitly specifying the width of this relation.");
                    
                }
                
                //Consistency check
                if (checkConsistency) {
                    // TODO: also check the constraints.
                    portsToCheckConsistency.removeAll(portsThatCanBeIngnoredForConsistencyCheck);
                    for (IOPort port : portsToCheckConsistency) {
                        _checkConsistency(port);
                    }
                }   
            } finally {   
                _topLevel.workspace().doneTemporaryWriting();
                if (logTimings) {
                    System.out.println("Time to do width inference: " +                
                            (System.currentTimeMillis() - startTime)
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
        _needsWidthInference = true;        
    }    

    /**
     * Set the top level to the value given as argument.
     * @param topLevel The top level CompositeActor.
     * @exception IllegalArgumentException If the specified actor is not the
     *   top level container. That is, its container is not null.
     */
     public void setTopLevel(CompositeActor topLevel) {
         // Extra test for compositeActor != null since when the manager is changed
         // the old manager gets a null pointer as compositeActor.
         // Afterwards width inference should not be done anymore on this manager
         // (this will throw a null pointer exception since _topLevel will be set to null).
         if (topLevel != null && topLevel.getContainer() != null) {
             throw new IllegalArgumentException(
                     "TypedCompositeActor.resolveTypes: The specified actor is "
                             + "not the top level container.");
         }
         
         _topLevel = topLevel;       
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
     static protected Set<IORelation> _relationsWithUnspecifiedWidths(List<?> relationList) throws IllegalActionException {
         Set<IORelation> result = new HashSet<IORelation>();
         for (Object relation : relationList) {
             if (relation != null && ((IORelation) relation).needsWidthInference()) {
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
     static private void _checkConsistency(IOPort port) throws IllegalActionException {
         // We check whether the inside and outside widths are consistent. In case
         // widths are inferred they should be inferred uniquely. We don't want to have
         // different results depending on where we start in the graph.
         int insideWidth = port._getInsideWidth(null);
         int outsideWidth = port._getOutsideWidth(null);
         
         if (insideWidth != 0 && outsideWidth != 0 && insideWidth != outsideWidth) {
             throw new IllegalActionException(port, 
                     "The inside width (" + insideWidth + ") and the outside width ("
		     + outsideWidth + ") of port " + port.getFullName()
                     + " are not either equal to 0 or not equal to each other and are therefore"
		     + " inconsistent.\nCan't determine a uniquely defined width for"
                     + " the connected relations. A possible fix is to right clicking on either the"
	             + " inside or outside relation and set the width -1.");
         }
     }
    
    /**
     * Infer the width for the relations connected to the port. If the width can be
     * inferred, update the width and return the relations for which the width has been
     * updated.
     * @param  port The port for whose connected relations the width should be inferred.
     * @return The relations for which the width has been updated. 
     * @exception IllegalActionException If the widths of the relations at port are not consistent.  
     */
    static private List<IORelation> _relationsWithUpdatedWidthForMultiport(IOPort port) throws IllegalActionException {              
        List<IORelation> result = new LinkedList<IORelation>();        
        
        Set<IORelation> outsideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port.linkedRelationList());
                //port.linkedRelationList() returns the outside relations
        
        int outsideUnspecifiedWidthsSize = outsideUnspecifiedWidths.size();
        
        NamedObj namedObject = port.getContainer();
        if (namedObject == null) {
            assert false; // not expected
            return result;
        }
        int difference = -1;
        Set<IORelation> unspecifiedWidths = null;
        if (namedObject instanceof AtomicActor) {
            assert outsideUnspecifiedWidthsSize >= 0;
            if (outsideUnspecifiedWidthsSize > 0 && port.hasWidthConstraints()) {
                difference = port.getWidthFromConstraints();
                unspecifiedWidths = outsideUnspecifiedWidths;
                if (difference < 0) {
                    return result; // Constraints still unknown
                }
            } else {
                return result;
            }
        } else {
            
            Set<IORelation> insideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port.insideRelationList());
            int insideUnspecifiedWidthsSize = insideUnspecifiedWidths.size(); 
            
            if ((insideUnspecifiedWidthsSize > 0 && outsideUnspecifiedWidthsSize > 0) 
                    || (insideUnspecifiedWidthsSize == 0 && outsideUnspecifiedWidthsSize == 0)) {
                return result;
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
                return result; // No width inference possible and necessary at this port.            
            }
            if (difference < 0) {
                throw new IllegalActionException(port, 
                        "The inside and outside widths of port " + port.getFullName()
                        + " are not consistent.\nThe inferred width of relation "
                        + unspecifiedWidths.iterator().next().getFullName()
                        + " would be negative.");            
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
        
        if (unspecifiedWidthsSize == 1 || unspecifiedWidthsSize == difference || difference == 0) {
            int width = difference / unspecifiedWidthsSize;
            assert width >= 0;
            for (IORelation relation : unspecifiedWidths) {
                relation._setInferredWidth(width);
                result.add(relation);
            }
        }
        return result;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    //True when width inference needs to happen again
    private boolean _needsWidthInference = true;
    
    //The top level of the model.    
    private CompositeActor _topLevel = null;

       
}
