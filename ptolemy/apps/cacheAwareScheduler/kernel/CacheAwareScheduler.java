/* A Cache aware scheduler for the chain-structured SDF graphs

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.apps.cacheAwareScheduler.kernel;

// Ptolemy imports
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FunctionDependency;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.domains.sdf.kernel.SDFUtilities;
import ptolemy.apps.cacheAwareScheduler.lib.ExperimentalActor;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Fraction;
import ptolemy.math.ExtendedMath;

// JAVA imports
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

////////////////////////////////////////////////////////////////////////////
//// CacheAwareScheduler
/**

   Notes: 
   1. At present, this scheduler can only handle chain structured graphs.
   2. The _generateMPMBSchedule() function is specialized to generate
      the MPM-MBScheudle for only 4 actor chain-structured graphs. This
      function needs to be re-written if it has to support arbitrary length
      chain-structured graphs. For more information, read documentation of 
      this function. Rest all functions and schedule generations are generic. 

   @see ptolemy.domains.sdf.SDFScheduler

   @author Sanjeev Kohli
   @version $Id$
   @since Ptolemy II 2.0
 */

public class CacheAwareScheduler extends SDFScheduler {

    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Cache Aware Scheduler".
     */
    public CacheAwareScheduler() 
            throws IllegalActionException, NameDuplicationException {
        super();
        setName("Cache Aware Scheduler");
        _dataMissPenalty = 0;
        _instructionMissPenalty = 0;
        _noOfActors = 0;
        _containerVersion = 0;
        _vectorizationFactor = 1;
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Cache Aware Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public CacheAwareScheduler(Workspace workspace) 
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        setName("Cache Aware Scheduler");
        _dataMissPenalty = 0;
        _instructionMissPenalty = 0;
        _noOfActors = 0;
        _containerVersion = 0;
        _vectorizationFactor = 1;
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CacheAwareScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _dataMissPenalty = 0;
        _instructionMissPenalty = 0;
        _noOfActors = 0;
        _containerVersion = 0;
        _vectorizationFactor = 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                Public Member Functions                    ////

    /** Calculate the cache miss penalty associated with the given 
     *  chain-structured graph's minimum program memory - minimum buffer
     *  schedule.
     */
    public int calculateMPMBScheduleCMP() throws IllegalActionException {
        if(_noOfActors !=4){
            System.out.println("The MPMBSchedule generation works only for "
                    + "4 actor chain-structured graph at present."); 
            return -1;
        }

        _MPMBSchedule = _generateMPMBSchedule();
        _linearizedMPMBSchedule = new Schedule();
        // Calling the _unloopSchedule method on _MPMBSchedule populates
        // the _linearizedMPMBSchedule.
        _unloopSchedule(_MPMBSchedule);

        //        System.out.println();
        //System.out.println("The MPMBS Schedule is :");
        //System.out.println(_MPMBSchedule);
        //System.out.println();

        // Calculating the IMP, DMP and CMP of the MPMBSchedule.
        int cmp = _calculateAssociatedPenalties(_linearizedMPMBSchedule);
        
        //System.out.println("The Linearized MPMBS Schedule is :");
        //System.out.println(_linearizedMPMBSchedule);
        //System.out.println();

        return cmp;
    }

    /** Calculate the IMP, DMP and CMP of the given linearized schedule.
     *  It is done by simulating one execution of the given schedule. 
     *  
     *  @param schedule The linearized schedule whole IMP, DMP and CMP 
     *   have to be calculated.
     *  @param dmp The variable to store data miss penalty in.
     *  @param imp The variable to store instruction miss penalty in.
     */
    private int _calculateAssociatedPenalties(Schedule schedule) 
            throws IllegalActionException {
        // Initializing the imp, dmp and cmp.
        int imp = 0;
        int dmp = 0;
        int cmp = 0;
        ScratchpadMemory dSPM = new ScratchpadMemory(_dataSPMSize);
        boolean[] iSPM = new boolean[_noOfActors];
        // Initializing the ISPM
        for(int i = 0; i < _noOfActors; i++) iSPM[i] = false;
        int usedISPM = 0;
        // Initializing the actors record for simulation of the execution.
        ActorRecord[] record = new ActorRecord[_noOfActors];
        for(int i = 0; i < _noOfActors; i++) {
            record[i] = new ActorRecord();
            record[i].actorNo = _actorsRecord[i].actorNo;
            record[i].consumptionRate = _actorsRecord[i].consumptionRate;
            record[i].productionRate = _actorsRecord[i].productionRate;
            // No actor has fired yet, so, this is zero for all rite now.
            record[i].fired = 0;
            if(i == 0) {
                record[i].fireable = _firingCount[i];
                record[i].inputTokensAvailable = 
                    _firingCount[i] * record[i].consumptionRate;
            } else {
                record[i].fireable = 0;
                record[i].inputTokensAvailable = 0;
            }
            record[i].toFire = _firingCount[i];
            record[i].toProduce = _firingCount[i] * record[i].productionRate;
            record[i].toConsume = _firingCount[i] * record[i].consumptionRate;
            record[i].produced = 0;
            record[i].consumed = 0; 
            record[i].codeSize =  _actorsRecord[i].codeSize;
        } // end of for loop that initializes the record Array.
       
        int currActor = 0;
        int iterationCount = 0;

        Iterator iterator = schedule.iterator();
        while(iterator.hasNext()) {
            ScheduleElement element = (ScheduleElement)iterator.next();
            if(element instanceof Firing) {
                iterationCount = ((Firing)element).getIterationCount();
                if(((Firing)element).getActor() 
                        == (Actor)_orderToActor.get(new Integer(0))) 
                    currActor = 0;
                else if(((Firing)element).getActor() 
                        == (Actor)_orderToActor.get(new Integer(1))) 
                    currActor = 1;
                else if(((Firing)element).getActor() 
                        == (Actor)_orderToActor.get(new Integer(2))) 
                    currActor = 2;
                else if(((Firing)element).getActor() 
                        == (Actor)_orderToActor.get(new Integer(3))) 
                    currActor = 3;
                else throw new IllegalActionException("Could not recognize "
                        + "the index of the actor associated with this "
                        + "firing.");
                

                // Calculating the DMP for this firing element.
                for(int i = 1; i <= iterationCount; i++) {
                    // Remove consumed tokens from DSPM. For first actor,
                    // consumption rate is zero, so, no need to check 
                    // for it.
                    if(currActor != 0) {
                        MemoryAddress tempAddress = new MemoryAddress();
                        for(int p = 1; p <= record[currActor].consumptionRate; p++) {
                            tempAddress.setFields((currActor-1), 
                                    (record[currActor].consumed + p));
                            if(dSPM.contains(tempAddress)) 
                                dSPM.evict(tempAddress); 
                        }     // end of for(int p = 1...) loop
                    }  // end of if(currActor != 0)
                    
                    // Add Produced tokens to DSPM. For last actor,
                    // production rate is zero, so, no need to check
                    // for it.
                    if(currActor != (_noOfActors - 1)) {
                        for(int p = 1; p <= record[currActor].productionRate; p++) {
                            if(dSPM.isFull()) {
                                //dmp += (_DREADTIME + _DWRITETIME);
                                dmp += (record[currActor].productionRate 
                                        - (p-1)) * (_DREADTIME + _DWRITETIME);
                                break;
                            } else {
                                MemoryAddress tempAddress = 
                                    new MemoryAddress(currActor, 
                                            record[currActor].produced + p);
                                dSPM.add(tempAddress);
                            }  // end of else for if(dSPM.isFull())
                        }  // end of for loop
                    }  // end of if(currActor != (_noOfActors - 1)
                    

                    // Update the current actor's record
                    record[currActor].fired ++;
                    record[currActor].fireable --;
                    record[currActor].produced += 
                        record[currActor].productionRate;
                    record[currActor].consumed +=
                        record[currActor].consumptionRate;
                    record[currActor].inputTokensAvailable -=
                        record[currActor].consumptionRate;
                    if(currActor != (_noOfActors - 1)) {
                        record[currActor + 1].inputTokensAvailable 
                            += record[currActor].productionRate;
                        // Update the fireable field of the successor.
                        record[currActor + 1].fireable = 
                            record[currActor + 1].inputTokensAvailable/record[currActor + 1].consumptionRate;
                    }    // end of if(currActor != (_noOfActors - 1)
                } // end of for(int i = 1;..) loop          

                // Calculating IMP associated with bringing the current actor
                // to I-SPM. If the current actor is not present in I-SPM, 
                // we calculate the IMP for bringing this actor in I-SPM in
                // the same way we calculate it for elements of Cache Aware
                // Schedule.
                if(iSPM[currActor] == false) {
                    // See if any fully fired actors are residing in 
                    // I-Scratchpad. If any of them is found, evict it 
                    // and free that space.
                    for(int i = 0; i < _noOfActors; i++) {
                        if(iSPM[i] == true) {
                            if(record[i].fired == record[i].toFire) {
                                iSPM[i] = false;
                                usedISPM -= record[i].codeSize;
                            }    // end of if(record[i].fired == ..)
                        }    // end of if(iSPM[i] == true)
                    }    // end of for(int i = 0; ...) loop
                    
                    if(usedISPM + record[currActor].codeSize > 
                            _instructionSPMSize) {

                        // Temp Variables
                        boolean foundActorToSwap = false;
                        int swapActor = -1;
                        int swapActorSize = 0;
                        int maxActor = -1;
                        int maxActorSize = 0;
                        // To be used in case we need to remove a set of actors
                        // to bring this successor in the I-Scratchpad.
                        ArrayList swapActorList = new ArrayList();
                        int swapListSize = 0;
                        
                        while((swapListSize + _instructionSPMSize - usedISPM) < record[currActor].codeSize) {

                            foundActorToSwap = false;
                            maxActor = -1;
                            swapActor = -1;
                            maxActorSize = 0;
                            swapActorSize = 0;

                            for(int j = 0; j < _noOfActors; j++) {
                                if(iSPM[j]) {
                                    if(!swapActorList.contains(new Integer(j))) {
                                        if((record[j].codeSize + swapListSize + _instructionSPMSize - usedISPM) >= record[currActor].codeSize){
                                            if(foundActorToSwap == true) {
                                                if(swapActorSize > 
                                                        record[j].codeSize) {
                                                    // Replace the current swap actor
                                                    // with this newly found smaller 
                                                    // size swap actor.
                                                    swapActor = j;
                                                    swapActorSize =
                                                        record[j].codeSize;
                                                }    // end of if(swapActorSize..)
                                            }     // end of if(foundActorToSwap)
                                            else {
                                                foundActorToSwap = true;
                                                swapActor = j;
                                                swapActorSize =
                                                    record[j].codeSize;
                                            }
                                        } // end of finding the swap actor.
                                        if(record[j].codeSize > 
                                                maxActorSize) {
                                            maxActor = j;
                                            maxActorSize = 
                                                record[j].codeSize;
                                        }    // end of if(actorsRecord..)
                                    }    // end of if(!swapActorList..)
                                }    // end of if(actorsInISPM[j])
                            }    // end of for(int j = 0;...) loop
                    
                            if(foundActorToSwap) {
                                swapActorList.add(new Integer(swapActor));
                                swapListSize += swapActorSize;
                            } else {
                                swapActorList.add(new Integer(maxActor));
                                swapListSize += maxActorSize;
                            }
                        } // end of while(swapListSize....)
                
                        Iterator tempActors = swapActorList.iterator();
                        while(tempActors.hasNext()) {
                            int tempSwapActor = 
                                ((Integer)tempActors.next()).intValue();
                            int tempSwapActorSize = 
                                record[tempSwapActor].codeSize;
                            iSPM[tempSwapActor] = false;
                            usedISPM -= tempSwapActorSize ;     
                        }   // end of while(tempActors.hasNext())
                        iSPM[currActor] = true;
                        usedISPM += record[currActor].codeSize;
                    }    // end of if(usedISPM + ...)
                    else {
                        usedISPM += record[currActor].codeSize;
                        iSPM[currActor] = true;
                    }    // end of else for if(usedISPM + ..)
                    imp += _IREADTIME * record[currActor].codeSize;
                } // end of if(iSPM[currActor] == false) 
            } // end of if(element instanceof Firing)
            else throw new IllegalActionException("The linearized schedule "
                    + "whose penalty has to be calculated contains an "
                    + "element that is not of type Firing. This function "
                    + "can not calculate the penalties of such a schedule.");
        } // end of while(iterator.hasNext())

        cmp = imp + dmp;
        /*
        System.out.println("IMP for MPMBS Schedule : " + imp);
        System.out.println("DMP for MPMBS Schedule : " + dmp);
        System.out.println("Total CMP for MPMBS Schedule : " 
                + cmp );
        */
        // System.out.println();
        // System.out.println("MPMBS - CMP : " + cmp);
        return cmp;
    }

    /** Calculate the cache miss penalty associated with the given 
     *  chain-structured graph's minimum activation schedule.
     *
     *  @return The CMP associated for the Minimum Activation Schedule
     */
    public int calculateSAMAScheduleCMP() {
        int cmp = 0;
        int dmp = 0;
        int imp = 0;
        _SAMASchedule = new Schedule();
        // Iterate over the actors in the chain-structured graph and
        // update the imp and dmp associated with each of them.
        for(int i = 0; i < _noOfActors; i++) {
            Firing F = new Firing();
            _SAMASchedule.add(F);
            F.setIterationCount(_firingCount[i]);
            F.setActor((Actor)_orderToActor.get(new Integer(i)));

            imp += _actorsRecord[i].codeSize * _IREADTIME;
            int totalTokensProduced = 
                _actorsRecord[i].productionRate * _actorsRecord[i].toFire;
            if(totalTokensProduced > _dataSPMSize) 
                dmp += (totalTokensProduced - _dataSPMSize) 
                    * (_DREADTIME + _DWRITETIME);
        }
        cmp = imp + dmp;
        // System.out.println();
        System.out.println("The SAMAS Schedule is : ");
        System.out.println(_SAMASchedule);
        
        System.out.println("IMP for SAMAS Schedule : " + imp);
        System.out.println("DMP for SAMAS Schedule : " + dmp);
        System.out.println("Total CMP for SAMAS Schedule : " 
             + cmp );
        
        //System.out.println("SAMAS - CMP : " + cmp );
        return cmp;
    }

    /** Returns the data miss penalty associated with the generated
     *  cache aware schedule.
     *
     *  @return The data miss penalty.
     */
    public int dataMissPenalty() {
        return _dataMissPenalty;
    }

    /** Returns the instruction miss penalty associated with the generated
     *  cache aware schedule.
     *
     *  @return The instruction miss penalty.
     */
    public int instructionMissPenalty() {
        return _instructionMissPenalty;
    }

    ///////////////////////////////////////////////////////////////////
    ////                Public Member Variables                    ////
    

    ///////////////////////////////////////////////////////////////////
    ////                Protected Member Functions                 ////
    
    /** Checks if the given SDF graph is chain-structured or not
     *
     *  @return True, if the SDF graph is chain-structured else false.
     */ 
    protected boolean _chainStructured() {
        // FIXME. 1. Check if the given SDF graph is chain structured or not.
        return true;
    }

    /** Returns the cache aware schedule for a given chain-structured
     *  SDF graph and given instruction and data cache sizes.
     *
     *  @return The cache aware schedule.
     *  @exception IllegalActionException If the given graph isn't 
     *   chain-structured.
     */
    protected Schedule _getSchedule() throws IllegalActionException{
        StaticSchedulingDirector director = 
            (StaticSchedulingDirector)getContainer();
        CompositeActor container = (CompositeActor)director.getContainer();
            
        if(_containerVersion == ((CASDFComposite)container).getVersion()) {
            return _cacheAwareSchedule; 
        }
        else _containerVersion = ((CASDFComposite)container).getVersion();
        
        // Initialize the local variables
        _noOfActors = 0;
        _dataMissPenalty = 0;
        _instructionMissPenalty = 0;
        _usedInstructionSPM = 0;
        _instructionSPMSize = 0;
        _dataSPMSize = 0;

        if(_chainStructured()) {
            // Find out the total no of actors.   
            for (Iterator entities = container.deepEntityList().iterator();
                 entities.hasNext();) {
                ComponentEntity entity = (ComponentEntity)entities.next();
                if (entity instanceof Actor) {
                    _noOfActors++;
                }
            }

            // Allocate memory for _firingCount and _actorsRecord.
            _firingCount = new int[_noOfActors];
            _actorsRecord = new ActorRecord[_noOfActors];
            for(int i = 0; i < _noOfActors; i++)
                _actorsRecord[i] = new ActorRecord();
            _instructionSPM = new boolean[_noOfActors];
            _populateActorsRecord();
            _generateCacheAwareSchedule();
            
            return _cacheAwareSchedule;
        }
        else throw new IllegalActionException("The given graph isn't"
                + " chain-structured. Can't be scheduled by the cache aware"
                + " scheduler.");        
    }

    ///////////////////////////////////////////////////////////////////
    ////                Protected Member Variables                 ////
    
    ///////////////////////////////////////////////////////////////////
    ////                Private Member Functions                   ////
    
    /** This function calculates the Data Miss Penalty (DMP) to be paid
     *  in case we fire the specified actor maximum possible number of times
     *  at an instant, instead of stopping after a certain number of firings 
     *  after which the Data Scratchpad Memory gets full.
     *
     *  @param actorNo The actor whose DMP has to be calculated.
     *  @param maxFiringsPossible Maximum possible firings of the given actor
     *   at the given instant.
     *  @param fired No of firings after which we are calculating the DMP.
     *   This function assumes that the Data Scratchpad becomes full after
     *   these many firings of the specified actor.
     *  @return The Data Miss Penalty to be paid if we fire the specified
     *   actor maximum possible times.
     *  @exception IllegalActionException If maxFiringsPossible is less than
     *   fired.
     */
    // NOTE: This function has a complexity of O(n) where n is the number of
    // Actors in the chain-structured SDF graph.
    private int _calculateDMP(int actorNo, int maxFiringsPossible, 
            int fired) throws IllegalActionException {
        if(maxFiringsPossible < fired) 
            throw new IllegalActionException("Can't calculate Data"
                    + " Miss Penalty, maxPossibleFirings is less than fired.");
        
        // Temp variable to store data miss penalty
        int penalty = 0;
        // Temp variable that copies the D-Scratchpad's current used space.
        int tempUsedSpace = _dataSPM.usedSpace();
        // Temp variable to store the D-Scratchpad's total size.
        int tempDSPMSize = _dataSPM.size();

        penalty = (maxFiringsPossible - fired) * 
            _actorsRecord[actorNo].productionRate;

        // Temp variables
        int tempTokensInsideDSPM = 
            fired * _actorsRecord[actorNo].productionRate;
        int tempTokensOutsideDSPM = 
            (maxFiringsPossible-fired) * _actorsRecord[actorNo].productionRate;
        int tempFreeSpaceInDSPM = 0;

        // Check if the immediate successor is fireable.
        int successorNo = actorNo + 1;
        int successorFirings = 0;
        // The following if loop is necessary for initialization of 
        // successor firings, because the last actor doesn't have any
        // successors and we need to make sure that _actorsRecord[_noOfActors]
        // isn't accessed.
        if(successorNo < _noOfActors) 
            // In calculation of DMP, we neglect the inputTokensAvailable
            // at the successor because we want to calculate the DMP caused
            // specifically by the new tokens produced by this actor.
            successorFirings  = (maxFiringsPossible * _actorsRecord[successorNo-1].productionRate)/_actorsRecord[successorNo].consumptionRate;
        // If the immediate successor isn't fireable, then return a penalty of
        // zero.
        if(successorFirings == 0) {
            penalty = 0;
            return penalty;
        }

        // Else go on and compute the Data Miss Penalty at each successor.
        while((successorFirings > 0) && (successorNo < _noOfActors)){
     
            if(_actorsRecord[successorNo].productionRate > 
                    _actorsRecord[successorNo].consumptionRate) {
                int newTokensProduced = successorFirings * 
                    (_actorsRecord[successorNo].productionRate);
                if(successorFirings * _actorsRecord[successorNo].consumptionRate
                        > tempTokensInsideDSPM) {
                    int extraSpaceNeeded = newTokensProduced - 
                        tempTokensInsideDSPM;
                    if(tempFreeSpaceInDSPM > extraSpaceNeeded) {
                        // No penalty added as the extra space was found in 
                        // D-Scratchpad itself. Nothing will be written 
                        // outside the SPM. Free space will reduce inside SPM.
                        penalty += 0 ;
                        tempFreeSpaceInDSPM -= extraSpaceNeeded;
                        tempTokensInsideDSPM = newTokensProduced;
                        tempTokensOutsideDSPM = 0;
                    }      //   end of if(tempFreeSpaceInDSPM > ..)
                    else {
                        // Some data will be written outside SPM. The 
                        // corresponding penalty has to be added. Free
                        // space will go to zero inside SPM.
                        penalty += extraSpaceNeeded - tempFreeSpaceInDSPM;
                        tempTokensInsideDSPM += tempFreeSpaceInDSPM;
                        tempTokensOutsideDSPM = extraSpaceNeeded - 
                            tempFreeSpaceInDSPM;
                        tempFreeSpaceInDSPM = 0;
                    }      //    end of else for if(tempFreeSpaceInDSPM > ..)
                }     // end of if(successorFirings*actorsRecord...)
                else {
                    int tempTokensConsumed = successorFirings * 
                        _actorsRecord[successorNo].consumptionRate;
                    int extraSpaceNeeded = newTokensProduced - 
                        tempTokensConsumed;
                    if(tempFreeSpaceInDSPM > extraSpaceNeeded) {
                        // No penalty added as the extra space was found in 
                        // D-Scratchpad itself. Nothing will be written 
                        // outside the SPM. Free space will reduce inside SPM.
                        penalty += 0;
                        tempFreeSpaceInDSPM -= extraSpaceNeeded;
                        tempTokensInsideDSPM = newTokensProduced;
                        tempTokensOutsideDSPM = 0;
                    }       // end of if(tempFreeSpaceInDSPM > ..)
                    else {
                        // Some data will be written outside SPM. The 
                        // corresponding penalty has to be added. Free
                        // space will go to zero inside SPM.
                        penalty += extraSpaceNeeded - tempFreeSpaceInDSPM;
                        tempTokensInsideDSPM = tempTokensConsumed + 
                            tempFreeSpaceInDSPM;
                        tempTokensOutsideDSPM = 
                            extraSpaceNeeded - tempFreeSpaceInDSPM;
                        tempFreeSpaceInDSPM = 0;
                    }       // end of else for if(tempFreeSpaceInDSPM > ..)
                }    // end of else for if(successorFirings*actorsRecord...)
            }      // end of if(_actorsRecord[successorNo]..)
            else {
                int newTokensProduced = successorFirings * 
                    (_actorsRecord[successorNo].productionRate);
                if(successorFirings * _actorsRecord[successorNo].consumptionRate
                        > tempTokensInsideDSPM) {
                    if(newTokensProduced > tempTokensInsideDSPM) {
                        int extraSpaceNeeded = newTokensProduced -
                            tempTokensInsideDSPM;
                        if(tempFreeSpaceInDSPM > extraSpaceNeeded) {
                            // No penalty added as the extra space was found 
                            // in D-Scratchpad itself. Nothing will be written 
                            // outside the SPM. Free space will reduce inside 
                            // SPM.
                            penalty += 0;
                            tempFreeSpaceInDSPM -= extraSpaceNeeded;
                            tempTokensInsideDSPM = newTokensProduced;
                            tempTokensOutsideDSPM = 0;
                        }   //   end of if(tempFreeSpaceInDSPM > ..)
                        else { 
                            // Some data will be written outside SPM. The 
                            // corresponding penalty has to be added. Free
                            // space will go to zero inside SPM.
                            penalty += extraSpaceNeeded - tempFreeSpaceInDSPM;
                            tempTokensInsideDSPM += tempFreeSpaceInDSPM;
                            tempTokensOutsideDSPM = extraSpaceNeeded -
                                tempFreeSpaceInDSPM;
                            tempFreeSpaceInDSPM = 0;
                        }   //   end of else for if(tempFreeSpaceInDSPM > ..)
                    }    //   end of if(newTokensProduced > ..)
                    else {
                        // Nothing will be written outside the SPM, infact
                        // free space will increase inside SPM.
                        int extraSpaceProduced = tempTokensInsideDSPM - 
                            newTokensProduced;
                        penalty += 0;
                        tempFreeSpaceInDSPM += extraSpaceProduced;
                        tempTokensInsideDSPM = newTokensProduced;
                        tempTokensOutsideDSPM = 0;
                    }    // end of else for if(newTokensProduced > ..)   
                }       // end of if(successorFirings*actorsRecord..)
                else {
                    // Nothing will be written outside the SPM. Free space 
                    // will increase inside SPM.
                    penalty += 0;
                    int tempTokensConsumed = successorFirings * 
                        _actorsRecord[successorNo].consumptionRate;
                    int extraSpaceProduced = tempTokensConsumed - 
                        newTokensProduced;
                    tempFreeSpaceInDSPM += extraSpaceProduced;
                    tempTokensInsideDSPM = newTokensProduced;
                    tempTokensOutsideDSPM = 0;
                }       // end of else for if(successorFirings*actorsRecord..)
            }     // end of else for if(_actorsRecord[successorNo]..)

            // Increment the successor number and update successorFirings
            successorNo++;
            // The following if loop is required to make sure that 
            // _actorsRecord[_noOfActors] isn't accessed as it doesn't exist.
            if(successorNo < _noOfActors) {
                successorFirings = (successorFirings * _actorsRecord[successorNo-1].productionRate)/_actorsRecord[successorNo].consumptionRate;
            }         // end of if(successorNo < _noOfActors) 
        }          // end of while((successorFirings > 0) && ...) loop

        // Scale the penalty appropriately. Whatever data is written to
        // the data memory is read as well, hence the scaling factors is
        // (_DREADTIME + _DWRITETIME).
        penalty = penalty * (_DREADTIME + _DWRITETIME);
        return penalty;
    }

    /** This function calculates the Instruction Miss Penalty (IMP) to be paid
     *  in case we stop execution of the specified actor after a certain no of
     *  firings when it could fired maximum no of times possible at that 
     *  instant.
     *
     *  @param actorNo The actor whose IMP has to be calculated.
     *  @param maxFiringsPossible Maximum possible firings of the given actor
     *   at the given instant.
     *  @param fired No of firings after which we are calculating the IMP.
     *  @return The Instruction Miss Penalty to be paid.
     *  @exception IllegalActionException If maxFiringsPossible is less than
     *   fired or if fired is equal to zero.
     */
    // NOTE: This function has a complexity of O(n^3) where n is the number of
    // Actors in the chain-structured SDF graph.
    // NOTE: The calling function needs to make sure that the parameter
    // fired isn't equal to Zero.
    private int _calculateIMP(int actorNo, int maxFiringsPossible, 
            int fired) throws IllegalActionException {
        if(maxFiringsPossible < fired) 
            throw new IllegalActionException("Can't calculate Instruction"
                    + " Miss Penalty, maxPossibleFirings is less than fired.");
        if(fired == 0) 
            throw new IllegalActionException("Can't calculate Instruction"
                    + " Miss Penalty, fired parameter is zero.");

        // Temp variable to store the instruction miss penalty
        int penalty = 0;
        // Temp variable used to calculate IMP. It is equal to 
        // ceiling(maxFiringsPossible/fired)
        int multiplicationFactor = 0;
        multiplicationFactor = maxFiringsPossible/fired; 
        if((multiplicationFactor*fired) != maxFiringsPossible)
            multiplicationFactor++;

        if(multiplicationFactor == 1) return penalty;
        multiplicationFactor--;

        // Make a local copy of the I-Scratchpad contents
        boolean[] actorsInISPM = new boolean[_noOfActors];
        for(int i = 0; i <_noOfActors; i++) 
            actorsInISPM[i] = _instructionSPM[i];
        int tempISPMSize = _usedInstructionSPM;
          
        // Check if the immediate successor is fireable.
        int successorNo = actorNo + 1;
        int successorFirings = 0;
        // The following if loop is necessary for initialization of 
        // successor firings, because the last actor doesn't have any
        // successors and we need to make sure that _actorsRecord[_noOfActors]
        // isn't accessed.
        if(successorNo < _noOfActors) 
            successorFirings  = (_actorsRecord[successorNo].inputTokensAvailable + (fired * _actorsRecord[successorNo-1].productionRate))/_actorsRecord[successorNo].consumptionRate;

        // If the immediate successor isn't fireable, then return a penalty of
        // zero.
        if(successorFirings == 0) {
            penalty = 0;
            return penalty;
        }
        
        // See if any fully fired actors are residing in I-Scratchpad
        // copy. If any of them is found, evict it and free that space.
        for(int i = 0; i < _noOfActors; i++) {
            if(actorsInISPM[i] == true) {
                if(_actorsRecord[i].fired == _actorsRecord[i].toFire) {
                    actorsInISPM[i] = false;
                    tempISPMSize -= _actorsRecord[i].codeSize;
                }    // end of if(_actorsRecord..)
            }    // end of if(actorsInISPM[i] == true)
        }    // end of for(int i = 0; ...) loop
        
        while((successorFirings > 0) && (successorNo < _noOfActors)){
            // Check if this successor is already present in I-scratchpad copy
            // or not. If not, calculate the penalty to bring it in there.
            // If no actor needs to be evicted to bring this successor, then
            // no penalty is added.
            if(!actorsInISPM[successorNo]) {
                if(tempISPMSize + _actorsRecord[successorNo].codeSize 
                        > _instructionSPMSize) {
                        // Temp Variables
                        boolean foundActorToSwap = false;
                        int swapActor = -1;
                        int swapActorSize = 0;
                        int maxActor = -1;
                        int maxActorSize = 0;
                        // To be used in case we need to remove a set of actors
                        // to bring this successor in the I-Scratchpad.
                        ArrayList swapActorList = new ArrayList();
                        int swapListSize = 0;

                        while((swapListSize + _instructionSPMSize - tempISPMSize) < _actorsRecord[successorNo].codeSize) {
                            foundActorToSwap = false;
                            maxActor = -1;
                            swapActor = -1;
                            maxActorSize = 0;
                            swapActorSize = 0;

                            for(int j = 0; j < _noOfActors; j++) {
                                if(actorsInISPM[j]) {
                                    if(!swapActorList.contains(new Integer(j))) {
                                        if((_actorsRecord[j].codeSize + swapListSize + _instructionSPMSize - tempISPMSize) >= _actorsRecord[successorNo].codeSize){
                                            if(foundActorToSwap == true) {
                                                if(swapActorSize > 
                                                        _actorsRecord[j].codeSize) {
                                                    // Replace the current swap actor
                                                    // with this newly found smaller 
                                                    // size swap actor.
                                                    swapActor = j;
                                                    swapActorSize =
                                                        _actorsRecord[j].codeSize;
                                                }    // end of if(swapActorSize..)
                                            }     // end of if(foundActorToSwap)
                                            else {
                                                foundActorToSwap = true;
                                                swapActor = j;
                                                swapActorSize =
                                                    _actorsRecord[j].codeSize;
                                            }
                                        } // end of finding the swap actor.
                                        if(_actorsRecord[j].codeSize > 
                                                maxActorSize) {
                                            maxActor = j;
                                            maxActorSize = 
                                                _actorsRecord[j].codeSize;
                                        }    // end of if(actorsRecord..)
                                    }    // end of if(!swapActorList..)
                                }    // end of if(actorsInISPM[j])
                            }    // end of for(int j = 0;...) loop
                    
                            if(foundActorToSwap) {
                                swapActorList.add(new Integer(swapActor));
                                swapListSize += swapActorSize;
                            } else {
                                swapActorList.add(new Integer(maxActor));
                                swapListSize += maxActorSize;
                            }
                        } // end of while(swapListSize....)
                        
                        Iterator tempActors = swapActorList.iterator();
                        while(tempActors.hasNext()) {
                            int tempSwapActor = 
                                ((Integer)tempActors.next()).intValue();
                            int tempSwapActorSize = 
                                _actorsRecord[tempSwapActor].codeSize;
                            // Add IMP to bring this successor by evicting 
                            // the swap actor. Also modify the contents of
                            // the copy of instruction SPM (actorsInISPM)
                            // and its size
                            if(tempSwapActor < actorNo) penalty += 0;
                            else if(tempSwapActor > actorNo)
                                penalty += 
                                    (multiplicationFactor * tempSwapActorSize);
                            // if the current actor itself needs to be evicted
                            // to bring the successor in I-Scratchpad then 
                            // we need to add the penalty for bringing the 
                            // current actor back to I-Scratchpad.
                            else penalty += (multiplicationFactor * (tempSwapActorSize + _actorsRecord[successorNo].codeSize));
                            
                            actorsInISPM[tempSwapActor] = false;
                            tempISPMSize -= tempSwapActorSize ;     
                        }   // end of while(tempActors.hasNext())
                        actorsInISPM[successorNo] = true;
                        tempISPMSize += _actorsRecord[successorNo].codeSize;
                }    // end of if(tempISPMSize + ...)
                else {
                    // Simply bring this actor to the scratchpad's temporary
                    // copy. No penalty is added.
                    actorsInISPM[successorNo] = true;
                    tempISPMSize += _actorsRecord[successorNo].codeSize;
                }
            }          //end of if(!actorsInISPM[successorNo]

            // Increment the successor number and update successorFirings
            successorNo++;
            // The following if loop is required to make sure that 
            // _actorsRecord[_noOfActors] isn't accessed as it doesn't exist.
            if(successorNo < _noOfActors) {
                successorFirings = 
                    (_actorsRecord[successorNo].inputTokensAvailable + successorFirings * _actorsRecord[successorNo-1].productionRate)/_actorsRecord[successorNo].consumptionRate;
            }         // end of if(successorNo < _noOfActors) 
        }          // end of while((successorFirings > 0) && ...) loop

        // Scale the penalty appropriately. The instructions are only read
        // from the Instruction Memory, they are never written back. Hence
        // the scaling factor is _IREADTIME.
        penalty = penalty * _IREADTIME;
        return penalty;
    }

    /** This method computes the order of each actor given in the 
     *  chain-structured graph. The first actor has an order 0 and the
     *  last actor in the n-actor chain has an order n-1.
     */
    private void _computeOrder() throws IllegalActionException {
        DirectedAcyclicGraph dag = _constructDirectedGraph();
        Object[] sort = (Object[]) dag.topologicalSort();
        // Allocate a new hash table with the equal to the
        // number of actors sorted.
        _orderToActor = new Hashtable(sort.length);
        for (int i = sort.length - 1; i >= 0; i--) {
            Actor actor = (Actor)sort[i];
            // Insert the hashtable entry.
            _orderToActor.put(new Integer(i), actor);
        }
    }

    /** Construct a directed graph with the nodes representing actors and
     *  directed edges representing dependencies.  
     *
     *  @return The directed graph that is constructed.
     */
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Declare the new graph.
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        SDFDirector director = 
            (SDFDirector)getContainer();
        CompositeActor container = (CompositeActor)director.getContainer();

        CompositeActor castContainer = container;

        // Get the function dependency object of the container of this 
        // director. If there is no such attribute, construct one.
        FunctionDependency functionDependency = castContainer.getFunctionDependencies();
         
        // The FunctionDependency attribute is used to construct
        // the schedule. If the schedule needs recalculation,
        // the FunctionDependency also needs recalculation.
        functionDependency.invalidate();
      
        // FIXME: The following may be a very costly test. 
        // -- from the comments of former implementation. 
        // If the port based data flow graph contains directed
        // loops, the model is invalid. An IllegalActionException
        // is thrown with the names of the actors in the loop.
        Object[] cycleNodes = functionDependency.getCycleNodes();
        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i])
                        .getContainer().getFullName());
                }
            }
            throw new IllegalActionException(this.getContainer(),
                    "Found zero delay loop including: " + names.toString());
        }

        // First, include all actors as nodes in the graph.
        // get all the contained actors.
        Iterator actors = castContainer.deepEntityList().iterator();
        while (actors.hasNext()) {
            dag.addNodeWeight(actors.next());
        }

        // Next, create the directed edges by iterating the actors again.
        actors = castContainer.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            // Get the FunctionDependency attribute of current actor.
            functionDependency = actor.getFunctionDependencies();
            // The following check may not be necessary since the FunctionDependency
            // attribute is constructed before. However, we check
            // it anyway. 
            if (functionDependency == null) {
                throw new IllegalActionException(this, "doesn't " +
                        "contain a valid FunctionDependency attribute.");
            }

            // get all the input ports of the current actor
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort)inputPorts.next();

                Set notDirectlyDependentPorts = 
                    functionDependency.getIndependentOutputPorts(inputPort);

                // get all the output ports of the current actor.
                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();

                    if (notDirectlyDependentPorts != null && 
                        notDirectlyDependentPorts.contains(outputPort)) {
                        // Skip the port without direct dependence.
                        continue;
                    }
                    // find the inside input ports connected to outputPort
                    Iterator inPortIterator =
                        outputPort.deepConnectedInPortList().iterator();
                    int referenceDepth = outputPort.depthInHierarchy();
                    while (inPortIterator.hasNext()) {
                        IOPort port = (IOPort)inPortIterator.next();
                        if (port.depthInHierarchy() < referenceDepth) {
                            // This destination port is higher in the hierarchy.
                            // We may be connected to it on the inside,
                            // in which case, we do not want to record
                            // this link.  To check whether we are connected
                            // on the inside, we check whether the container
                            // of the destination port deeply contains
                            // source port.
                            if (((NamedObj)port.getContainer())
                                    .deepContains(outputPort)) {
                                continue;
                            }
                        }
                        
                        Actor successor = (Actor)(port.getContainer());
                        // If the destination is the same as the current 
                        // actor, skip the destination.
                        if (successor.equals(actor)) {
                            continue;
                        }

                        // create an arc from the current actor to the successor.
                        if (dag.containsNodeWeight(successor)) {
                            // 'contains' replaced with 'containsNodeWeight'
                            // Should not affect function since former has
                            // already been defined in Graph.java as latter.
                            dag.addEdge(actor, successor);
                        } else {
                            // This happens if there is a
                            // level-crossing transition.
                            throw new IllegalActionException(this,
                                    "Level-crossing transition from "
                                    + ((Nameable)actor).getFullName() + " to "
                                    + ((Nameable)successor).getFullName());
                        }
                    }
                }
            }
        }
        
        return dag;
    }

    /** Generates the cache aware schedule for the given SDF graph and 
     *  given data and instruction scratchpad memory sizes.
     */
    private void _generateCacheAwareSchedule() throws IllegalActionException {
        _cacheAwareSchedule = new Schedule();
        _dataSPM = new ScratchpadMemory(_dataSPMSize);
        // Temporary variables
        int totalFirings = 0;
        int firingsDone = 0;
        for(int i = 0; i < _noOfActors; i++) {
            totalFirings += _firingCount[i];
            // Initializing the ISPM
            _instructionSPM[i] = false;
        }
        // Initialising the ISPM
        _usedInstructionSPM = 0;

        // Initially the first actor is the current actor. Total number
        // of actors are 'n'. The corresponding indices are 0 to n-1.
        int currentActor = 0;
        if(!(_actorsRecord[currentActor].fireable > 0))
            throw new NotSchedulableException("Can't generate cache aware"
                    + " schedule, the first actor isn't fireable once.");

        // Temporary variables
        boolean canPutMoreDataInDSPM;
        int tempFiringsOfCurrentActor;
        int tempMaxFirings;


        // Main while loop that continues till all actors are fired completely
        while(firingsDone != totalFirings){
            // Assignment of temporary variables
            canPutMoreDataInDSPM = true;
            tempFiringsOfCurrentActor = 0;
            tempMaxFirings = _actorsRecord[currentActor].fireable;
            
            // Update the IMP for this actor. If it is already present
            // in ISPM, no IMP is added, else we need to add the 
            // corresponding IMP associated with bringing this actor's code
            // to the ISPM. If space needs to be made in ISPM to bring the 
            // current actor, then the smallest possible actor is evicted. 
            // If no one actor can make space for the current actor, then
            //  a set of actors is evicted to make space for the currentActor.
            _updateIMP(currentActor);
            
            while((canPutMoreDataInDSPM == true) && 
                    (_actorsRecord[currentActor].fireable > 0)) {
                
                // Fire the current actor till Data SPM can accomodate the
                // produced data or till maximum possible firings are done for 
                // this current actor. If Data SPM gets full or
                // can't take in the data produced by another firing of this
                // actor, set the flag canPutMoreDataInDSPM
                
                // Find the number of tokens to be consumed from the DSPM
                // by this firing of the current actor
                int tokensConsumedFromDSPMInThisFiring = 0;
                if(currentActor != 0) {
                    MemoryAddress tempAddress = new MemoryAddress();
                    for(int p = 1; 
                        p <= _actorsRecord[currentActor].consumptionRate; p++) {
                        tempAddress.setFields((currentActor-1), 
                                (_actorsRecord[currentActor].consumed + p));
                        if(_dataSPM.contains(tempAddress)) 
                            tokensConsumedFromDSPMInThisFiring++;
                    }     // end of for(int p = 1...) loop
                }     // end of if(currentActor != 0)
                
                // Check if the tokens produced by a firing of the current
                // actor will be accomodated by DSPM. This is useful only if
                // the tokens produced by the current actor are more than
                // the tokens consumed by it.
                if(_actorsRecord[currentActor].productionRate > 
                        tokensConsumedFromDSPMInThisFiring) {
                    
                    int tempNewTokensAddedToDSPM = _actorsRecord[currentActor].productionRate - tokensConsumedFromDSPMInThisFiring;
                    if(_dataSPM.freeSpace() < tempNewTokensAddedToDSPM) {
                        canPutMoreDataInDSPM = false;
                    }    // end of if(_dataSPM.freeSpace...)
                    else {
                        // Increment the temp firings of the current actor.
                        tempFiringsOfCurrentActor++;
                        // Remove consumed tokens from DSPM. For first actor,
                        // consumption rate is zero, so, no need to check 
                        // for it.
                        if(currentActor != 0) {
                            MemoryAddress tempAddress = new MemoryAddress();
                            for(int p = 1; p <= _actorsRecord[currentActor].consumptionRate; p++) {
                                tempAddress.setFields((currentActor-1), 
                                        (_actorsRecord[currentActor].consumed 
                                                + p));
                                if(_dataSPM.contains(tempAddress)) {
                                    _dataSPM.evict(tempAddress);
                                }    // end of if(_dataSPM.contains...)
                            }     // end of for(int p = 1...) loop
                        }     // end of if(currentActor != 0)
                        
                        // Add Produced tokens to DSPM. For last actor,
                        // production rate is zero, so, no need to check
                        // for it.
                        if(currentActor != (_noOfActors-1)) {
                            for(int p = 1; 
                                p<=_actorsRecord[currentActor].productionRate; p++) {
                                MemoryAddress tempAddress = 
                                    new MemoryAddress(currentActor, 
                                            _actorsRecord[currentActor].produced+ p);
                                _dataSPM.add(tempAddress);
                            }
                        }
                        // Update firingsDone variable.
                        firingsDone++;
                        // Update the current actor's record
                        _actorsRecord[currentActor].fired++;
                        _actorsRecord[currentActor].fireable--;
                        _actorsRecord[currentActor].produced += 
                            _actorsRecord[currentActor].productionRate;
                        _actorsRecord[currentActor].consumed +=
                            _actorsRecord[currentActor].consumptionRate;
                        _actorsRecord[currentActor].inputTokensAvailable -=
                            _actorsRecord[currentActor].consumptionRate;
                        if(currentActor != (_noOfActors - 1)) {
                            _actorsRecord[currentActor + 1].inputTokensAvailable += _actorsRecord[currentActor].productionRate;
                            // Update the fireable field of the successor.
                            _actorsRecord[currentActor + 1].fireable = 
                                _actorsRecord[currentActor + 1].inputTokensAvailable/_actorsRecord[currentActor + 1].consumptionRate;
                        }    // end of if(currentActor != ....)
                    }      // end of else for if(_dataSPM.freeSpace...)
                }      // end of if(_actorsRecord[...]..)
                else {
                    // Increment the temp firings of the current actor.
                    tempFiringsOfCurrentActor++;
                    // Remove consumed tokens from DSPM. For first actor,
                    // consumption rate is zero, so, no need to check for it.
                    if(currentActor != 0) {
                        MemoryAddress tempAddress = new MemoryAddress();
                        for(int p = 1; p <= _actorsRecord[currentActor].consumptionRate; p++) {
                            tempAddress.setFields((currentActor-1), 
                                    (_actorsRecord[currentActor].consumed + p));
                            if(_dataSPM.contains(tempAddress)) {
                                _dataSPM.evict(tempAddress);
                            }    // end of if(_dataSPM.contains...)
                        }     // end of for(int p = 1...) loop
                    }     // end of if(currentActor != 0)
                    
                    // Add Produced tokens to DSPM. For last actor,
                    // production rate is zero, so, no need to check for it.
                    if(currentActor != (_noOfActors-1)) {
                        for(int p = 1; 
                            p<=_actorsRecord[currentActor].productionRate;p++) {
                            MemoryAddress tempAddress = 
                                new MemoryAddress(currentActor, 
                                        _actorsRecord[currentActor].produced+p);
                            _dataSPM.add(tempAddress);
                        }
                    }
                    // Update firingsDone variable.
                    firingsDone++;
                    // Update the current actor's record
                    _actorsRecord[currentActor].fired++;
                    _actorsRecord[currentActor].fireable--;
                    _actorsRecord[currentActor].produced += 
                        _actorsRecord[currentActor].productionRate;
                    _actorsRecord[currentActor].consumed +=
                        _actorsRecord[currentActor].consumptionRate;
                    _actorsRecord[currentActor].inputTokensAvailable -=
                        _actorsRecord[currentActor].consumptionRate;
                    if(currentActor != (_noOfActors - 1)) {
                        _actorsRecord[currentActor + 1].inputTokensAvailable +=
                            _actorsRecord[currentActor].productionRate;
                        // Update the fireable field of the successor.
                        _actorsRecord[currentActor + 1].fireable = 
                            _actorsRecord[currentActor + 1].inputTokensAvailable/_actorsRecord[currentActor + 1].consumptionRate;
                    }    // end of if(currentActor != ....)
                }    // end of else for if(_actorsRecord[...]..)
            }    // end of while((canPutMoreDataInDSPM == true) && ...)
            
            // Temporary Variable
            Actor actor;
            
            // Check if the current actor is still fireable but we have
            // stopped because DSPM can't accomodate any more of the produced
            // data.
            if(!canPutMoreDataInDSPM) {
                if(tempFiringsOfCurrentActor > 0) {
                    if(_calculateDMP(currentActor, tempMaxFirings, 
                            tempFiringsOfCurrentActor) > 
                            _calculateIMP(currentActor, tempMaxFirings, 
                                    tempFiringsOfCurrentActor)) {
                        
                        // DMP is higher than IMP, so, switch control
                        // to the successor if its fireable atleast once, 
                        // else fire the current actor completely. Now pass 
                        // the control to its successor if its fireable 
                        // atleast once. If the successor isn't fireable
                        // atleast once even now pass the control to the 
                        // nearest predecessor that is fireable atleast once. 
                        
                        // NOTE: We aren't checking if the current actor is
                        // the last actor in this if loop because, for last
                        // actor, the control should never come here because
                        // canPutMoreDataInDSPM is always true for last 
                        // the last actor.
                        if(_actorsRecord[currentActor + 1].fireable > 0) {
                            // Add this element to the cache aware schedule
                            Firing S1 = new Firing();
                            S1.setIterationCount(tempFiringsOfCurrentActor);
                            actor = (Actor)_orderToActor.get(new Integer(currentActor));
                            S1.setActor(actor);
                            _cacheAwareSchedule.add(S1);
                            currentActor += 1;
                        }
                        else {
                            // Fire the current actor completely.
                            
                            // Remove consumed tokens from DSPM. For first 
                            // actor, consumption rate is zero, so, no need
                            // to check for it.
                            if(currentActor != 0) {
                                MemoryAddress tempAddress =new MemoryAddress();
                                for(int p = 1; p <= (_actorsRecord[currentActor].consumptionRate * (tempMaxFirings - tempFiringsOfCurrentActor)); p++) {
                                    tempAddress.setFields((currentActor-1), 
                                            (_actorsRecord[currentActor].consumed + p));
                                    if(_dataSPM.contains(tempAddress)) {
                                        _dataSPM.evict(tempAddress);
                                    }    // end of if(_dataSPM.contains...)
                                }     // end of for(int p = 1...) loop
                            }     // end of if(currentActor != 0)
                            
                            // Add produced tokens to DSPM, if they can be 
                            // added. For last actor, production rate is zero,
                            // so, no need to check for it.
                            if(currentActor != (_noOfActors - 1)) {
                                int tempProducedTokens = _actorsRecord[currentActor].productionRate * (tempMaxFirings - tempFiringsOfCurrentActor);
                                if(_dataSPM.freeSpace() < tempProducedTokens){
                                    int tem = _dataSPM.freeSpace();
                                // Update the Data Miss Penalty (DMP)
                                    _dataMissPenalty += 
                                        (_DREADTIME + _DWRITETIME) * 
                                        (tempProducedTokens - tem);
                                    for(int p = 1; p <= tem; p++) {
                                        MemoryAddress tempAddress = 
                                            new MemoryAddress(currentActor, 
                                                    _actorsRecord[currentActor].produced + p);
                                        _dataSPM.add(tempAddress);
                                    }    // end of for(int p = 1...)
                                    
                                }  // end of if(_dataSPM.freeSpace()..)
                                else {
                                    for(int p = 1; 
                                        p <= tempProducedTokens; p++) {
                                        MemoryAddress tempAddress = 
                                            new MemoryAddress(currentActor, 
                                                    _actorsRecord[currentActor].produced+p);
                                        _dataSPM.add(tempAddress);
                                    }    // end of for(int p = 1...)
                                }   // end of else for if(_dataSPM.free...)
                            }    // end of if(currentActor != ..)
                            
                            // Update firingsDone variable
                            int tempExtraFirings = tempMaxFirings - 
                                tempFiringsOfCurrentActor;
                            firingsDone += tempExtraFirings;
                            // Update the current actor's record
                            _actorsRecord[currentActor].fired += 
                                tempExtraFirings;
                            _actorsRecord[currentActor].fireable -=
                                tempExtraFirings;
                            _actorsRecord[currentActor].produced += 
                                (_actorsRecord[currentActor].productionRate *
                                        tempExtraFirings);
                            _actorsRecord[currentActor].consumed +=
                                (_actorsRecord[currentActor].consumptionRate * 
                                        tempExtraFirings);
                            _actorsRecord[currentActor].inputTokensAvailable -=
                                (_actorsRecord[currentActor].consumptionRate * 
                                        tempExtraFirings);
                            if(currentActor != (_noOfActors - 1)) {
                                _actorsRecord[currentActor + 1].inputTokensAvailable += (_actorsRecord[currentActor].productionRate * tempExtraFirings);
                                // Update the fireable field of the successor
                                _actorsRecord[currentActor + 1].fireable = 
                                    _actorsRecord[currentActor + 1].inputTokensAvailable/_actorsRecord[currentActor + 1].consumptionRate;
                            }    // end of if(currentActor != ....)
                            
                            // Add this element to the cache aware schedule
                            Firing S1 = new Firing();
                            S1.setIterationCount(tempMaxFirings);
                            actor = (Actor)_orderToActor.get(new Integer(currentActor));
                            S1.setActor(actor);
                            _cacheAwareSchedule.add(S1);
                            
                            // Now check if the successor is fireable atleast 
                            // once. If it is, make it the current actor else
                            // find the nearest predecessor that is fireable 
                            // atleast once, make it the current actor.
                            if(_actorsRecord[currentActor + 1].fireable > 0)
                                currentActor += 1;
                            else {
                                boolean tempFlag = false;
                                int tempPredecessor = currentActor - 1;
                                while((tempFlag == false) && 
                                        (tempPredecessor > 0)) {
                                    if(_actorsRecord[tempPredecessor].fireable 
                                            > 0) 
                                        tempFlag = true;
                                    else {
                                        if(tempPredecessor > 0)
                                            tempPredecessor -= 1;
                                    }    // end of else for if(actorsRecord..) 
                                }    // end of while((tempFlag == false) &&..)
                                currentActor = tempPredecessor;
                            }   // end of else for if(_actorsRecord[]..fire..)
                        }  // end of else for if(_actorsRecord[..].fireable > 0)
                    }    // end of if(_calculateDMP() > ..)
                    else {
                        
                        // DMP is lesser than or equal to IMP. Fire 
                        // the current actor completely and pass the control
                        // to its successor if its fireable atleast once else 
                        // pass the control to the nearest predecessor that 
                        // is fireable atleast once.
                        
                        // NOTE: The current actor can never be the last actor,
                        // because canPutMoreDataInDSPM is never false for
                        // the last actor.
                        
                        // Remove consumed tokens from DSPM. For first 
                        // actor, consumption rate is zero, so, no need
                        // to check for it.
                        if(currentActor != 0) {
                            MemoryAddress tempAddress = new MemoryAddress();
                            for(int p = 1; p <= (_actorsRecord[currentActor].consumptionRate * (tempMaxFirings - tempFiringsOfCurrentActor)); p++) {
                                tempAddress.setFields((currentActor-1), 
                                        (_actorsRecord[currentActor].consumed + p));
                                if(_dataSPM.contains(tempAddress)) {
                                    _dataSPM.evict(tempAddress);
                                }    // end of if(_dataSPM.contains...)
                            }     // end of for(int p = 1...) loop
                        }     // end of if(currentActor != 0)
                        
                        // Add produced tokens to DSPM, if they can be 
                        // added. For last actor, production rate is zero,
                        // so, no need to check for it.
                        if(currentActor != (_noOfActors - 1)) {
                            int tempProducedTokens = _actorsRecord[currentActor].productionRate * (tempMaxFirings - tempFiringsOfCurrentActor);
                            if(_dataSPM.freeSpace() < tempProducedTokens){
                                int tem = _dataSPM.freeSpace();
                                // Update the Data Miss Penalty (DMP)
                                _dataMissPenalty += 
                                    (_DREADTIME + _DWRITETIME) * 
                                    (tempProducedTokens - tem);
                                for(int p = 1; p <= tem; p++) {
                                    MemoryAddress tempAddress = 
                                        new MemoryAddress(currentActor, 
                                                _actorsRecord[currentActor].produced + p);
                                    _dataSPM.add(tempAddress);
                                }    // end of for(int p = 1...)
                            }  // end of if(_dataSPM.freeSpace()..)
                            else {
                                for(int p = 1; 
                                    p <= tempProducedTokens; p++) {
                                    MemoryAddress tempAddress = 
                                        new MemoryAddress(currentActor, 
                                                _actorsRecord[currentActor].produced + p);
                                    _dataSPM.add(tempAddress);
                                }    // end of for(int p = 1...)
                            }   // end of else for if(_dataSPM.free...)
                        }    // end of if(currentActor != ..)
                        
                        // Update firingsDone variable
                        int tempExtraFirings = tempMaxFirings - 
                            tempFiringsOfCurrentActor;
                        firingsDone += tempExtraFirings;
                        // Update the current actor's record
                        _actorsRecord[currentActor].fired += 
                            tempExtraFirings;
                        _actorsRecord[currentActor].fireable -=
                            tempExtraFirings;
                        _actorsRecord[currentActor].produced += 
                            (_actorsRecord[currentActor].productionRate *
                                    tempExtraFirings);
                        _actorsRecord[currentActor].consumed +=
                            (_actorsRecord[currentActor].consumptionRate * 
                                    tempExtraFirings);
                        _actorsRecord[currentActor].inputTokensAvailable -=
                            (_actorsRecord[currentActor].consumptionRate * 
                                    tempExtraFirings);
                        if(currentActor != (_noOfActors - 1)) {
                            _actorsRecord[currentActor + 1].inputTokensAvailable += (_actorsRecord[currentActor].productionRate * tempExtraFirings);
                                // Update the fireable field of the successor
                            _actorsRecord[currentActor + 1].fireable = 
                                _actorsRecord[currentActor + 1].inputTokensAvailable/_actorsRecord[currentActor + 1].consumptionRate;
                        }    // end of if(currentActor != ....)
                        
                        // Add this element to the cache aware schedule
                        Firing S1 = new Firing();
                        S1.setIterationCount(tempMaxFirings);
                        actor = (Actor)_orderToActor.get(new Integer(currentActor));
                        S1.setActor(actor);
                        _cacheAwareSchedule.add(S1);
                        
                        // Now check if the successor is fireable atleast 
                        // once. If it is, make it the current actor else
                        // find the nearest predecessor that is fireable 
                        // atleast once, make it the current actor.
                        if(_actorsRecord[currentActor + 1].fireable > 0)
                            currentActor += 1;
                        else {
                            boolean tempFlag = false;
                            int tempPredecessor = currentActor - 1;
                            while((tempFlag == false) && 
                                    (tempPredecessor > 0)) {
                                if(_actorsRecord[tempPredecessor].fireable 
                                        > 0) 
                                    tempFlag = true;
                                else {
                                    if(tempPredecessor > 0)
                                        tempPredecessor -= 1;
                                }    // end of else for if(actorsRecord..) 
                            }    // end of while((tempFlag == false) &&..)
                            currentActor = tempPredecessor;
                        }   // end of else for if(_actorsRecord[]..fire..)
                    }    // end of else for if(_calculateDMP() > ..)
                }    // end of if(tempFiringsOfCurrentActor > 0)
                else {
                    // Even the data produced by one firing of the
                    // current actor can't be accomodated by DSPM. Fire the
                    // current actor completely and then check if its
                    // successor is fireable atleast once. If it is, pass on
                    // the control to it, else pass the control to the 
                    // nearest predecessor that is fireable atleast once.
                    
                    // NOTE: The current actor can never be the last actor,
                    // because canPutMoreDataInDSPM is never false for
                    // the last actor.
                    
                    // Remove consumed tokens from DSPM. For first 
                    // actor, consumption rate is zero, so, no need
                    // to check for it.
                    if(currentActor != 0) {
                        MemoryAddress tempAddress =new MemoryAddress();
                        for(int p = 1; p <= (_actorsRecord[currentActor].consumptionRate * (tempMaxFirings - tempFiringsOfCurrentActor)); p++) {
                            tempAddress.setFields((currentActor - 1), 
                                    (_actorsRecord[currentActor].consumed + p));
                            if(_dataSPM.contains(tempAddress)) {
                                _dataSPM.evict(tempAddress);
                            }    // end of if(_dataSPM.contains...)
                        }     // end of for(int p = 1...) loop
                    }     // end of if(currentActor != 0)
                    
                    // Add produced tokens to DSPM, if they can be 
                    // added. For last actor, production rate is zero,
                    // so, no need to check for it.
                    if(currentActor != (_noOfActors - 1)) {
                        int tempProducedTokens = _actorsRecord[currentActor].productionRate * (tempMaxFirings - tempFiringsOfCurrentActor);
                        if(_dataSPM.freeSpace() < tempProducedTokens){
                            int tem = _dataSPM.freeSpace();
                                // Update the Data Miss Penalty (DMP)
                            _dataMissPenalty += 
                                (_DREADTIME + _DWRITETIME) * 
                                (tempProducedTokens - tem);
                            for(int p = 1; p <= tem; p++) {
                                MemoryAddress tempAddress = 
                                    new MemoryAddress(currentActor, _actorsRecord[currentActor].produced + p);
                                _dataSPM.add(tempAddress);
                            }    // end of for(int p = 1...)
                        }  // end of if(_dataSPM.freeSpace()..)
                        else {
                            for(int p = 1; 
                                p <= tempProducedTokens; p++) {
                                MemoryAddress tempAddress = 
                                    new MemoryAddress(currentActor, _actorsRecord[currentActor].produced + p);
                                _dataSPM.add(tempAddress);
                            }    // end of for(int p = 1...)
                        }   // end of else for if(_dataSPM.free...)
                    }    // end of if(currentActor != ..)
                    
                    // Update firingsDone variable
                    int tempExtraFirings = tempMaxFirings - 
                        tempFiringsOfCurrentActor;
                    firingsDone += tempExtraFirings;
                    // Update the current actor's record
                    _actorsRecord[currentActor].fired += 
                        tempExtraFirings;
                    _actorsRecord[currentActor].fireable -=
                        tempExtraFirings;
                    _actorsRecord[currentActor].produced += 
                        (_actorsRecord[currentActor].productionRate *
                                tempExtraFirings);
                    _actorsRecord[currentActor].consumed +=
                        (_actorsRecord[currentActor].consumptionRate * 
                                tempExtraFirings);
                    _actorsRecord[currentActor].inputTokensAvailable -=
                        (_actorsRecord[currentActor].consumptionRate * 
                                tempExtraFirings);
                    if(currentActor != (_noOfActors - 1)) {
                        _actorsRecord[currentActor + 1].inputTokensAvailable += (_actorsRecord[currentActor].productionRate * tempExtraFirings);
                        // Update the fireable field of the successor
                        _actorsRecord[currentActor + 1].fireable = 
                            _actorsRecord[currentActor + 1].inputTokensAvailable/_actorsRecord[currentActor + 1].consumptionRate;
                    }    // end of if(currentActor != ....)
                    
                    // Add this element to the cache aware schedule
                    Firing S1 = new Firing();
                    S1.setIterationCount(tempMaxFirings);
                    actor = 
                        (Actor)_orderToActor.get(new Integer(currentActor));
                    S1.setActor(actor);
                    _cacheAwareSchedule.add(S1);
                    
                    // Now check if the successor is fireable atleast 
                    // once. If it is, make it the current actor else
                    // find the nearest predecessor that is fireable 
                    // atleast once, make it the current actor.
                    if(_actorsRecord[currentActor + 1].fireable > 0)
                        currentActor += 1;
                    else {
                        boolean tempFlag = false;
                        int tempPredecessor = currentActor - 1;
                        while((tempFlag == false) && 
                                (tempPredecessor > 0)) {
                            if(_actorsRecord[tempPredecessor].fireable > 0) 
                                tempFlag = true;
                            else {
                                if(tempPredecessor > 0)
                                    tempPredecessor -= 1;
                            }    // end of else for if(actorsRecord..) 
                        }    // end of while((tempFlag == false) &&..)
                        currentActor = tempPredecessor;
                    }   // end of else for if(_actorsRecord[]..fire..)
                }    // end of else for if(tempFiringsOfCurrentActor > 0)
            }    //   end of if(!canPutMoreDataInDSPM)
            else {
                
                // Add this element to the cache aware schedule
                Firing S1 = new Firing();
                S1.setIterationCount(tempMaxFirings);
                actor = (Actor)_orderToActor.get(new Integer(currentActor));
                S1.setActor(actor);
                _cacheAwareSchedule.add(S1);
                
                // The current actor has fired maximum possible
                // times. It is no longer fireable. Pass the control to 
                // its successor if its fireable atleast once else pass 
                // the control to the nearest predecessor that is 
                // fireable atleast once.
                
                // NOTE: If the current actor is the last actor, pass the 
                // control to its nearest predecessor that is fireable atleast
                // once. Don't check its successor.
                if(currentActor != (_noOfActors - 1)) {
                    if(_actorsRecord[currentActor + 1].fireable > 0)
                        currentActor += 1;
                    else {
                        boolean tempFlag = false;
                        int tempPredecessor = currentActor - 1;
                        while((tempFlag == false) && 
                                (tempPredecessor > 0)) {
                            if(_actorsRecord[tempPredecessor].fireable > 0) 
                                tempFlag = true;
                            else {
                                if(tempPredecessor > 0) 
                                    tempPredecessor -= 1;
                            }    // end of else for if(actorsRecord..) 
                        }    // end of while((tempFlag == false) &&..)
                        currentActor = tempPredecessor;
                    }   // end of else for if(_actorsRecord[]..fire..)
                }    // end of if(currentActor != (_noOfActors - 1)
                else {
                    boolean tempFlag = false;
                    int tempPredecessor = currentActor - 1;
                    while((tempFlag == false) && 
                            (tempPredecessor > 0)) {
                        if(_actorsRecord[tempPredecessor].fireable > 0) 
                            tempFlag = true;
                        else {
                            if(tempPredecessor > 0) 
                                tempPredecessor -= 1;
                        }    // end of else for if(actorsRecord..) 
                    }    // end of while((tempFlag == false) &&..)
                    currentActor = tempPredecessor;
                }  // end of else for if(currentActor != (_noOfActors - 1)
            }     // end of else for if(!canPutMoreDataInDSPM)
        }   // end of while(firingsDone != ....)   
    }
    
    /** Generates the MPMBS Schedule for the given chain-structured graph.
     *  Note that at present, the implementation of this function is a hack
     *  and will work only for 4 actor chain-structured graphs. So, it needs
     *  a proper implementation (as suggested by the Shuvra and Murthy paper
     *  on joint minimization of actor code and buffer size) if it has to be
     *  used for any other set of actors. 
     *
     *  Another important note: Do not try to understand this function coz it
     *  is written in a very arcane way, it is totally incomprehensible.
     *  
     *  @return The MPMBS Schedule.
     */
    private Schedule _generateMPMBSchedule() {
        Schedule[] schedules = new Schedule[5];
        int[] bufferRequirement = new int[5];
        for(int i = 0; i <5; i++) {
            schedules[i] = new Schedule();
            bufferRequirement[i] = 0;
        }
        int minBufferScheduleIndex = 0;
        
        int fA = 1;
        int fB = 1;
        int fC = 1;
        int fD = 1;
        int fAB = 1;
        int fBC = 1;
        int fCD = 1;
        int fABC = 1;
        int fBCD = 1;
        int fABCD = 1;

        // Make all 5 schedules and then check which one
        // has the least data memory requirement.
        
        // Generating schedule 1 (index 0)
        fC = _firingCount[2];
        fD = _firingCount[3];
        fCD = ExtendedMath.gcd(fC, fD);
        fC = fC/fCD;
        fD = fD/fCD;
        bufferRequirement[0] += fC*_actorsRecord[2].productionRate;

        fB = _firingCount[1];
        fBCD = ExtendedMath.gcd(fB, fCD);
        fB = fB/fBCD;
        fCD = fCD/fBCD;
        bufferRequirement[0] += fB*_actorsRecord[1].productionRate;
        
        fA = _firingCount[0];
        fABCD = ExtendedMath.gcd(fA, fBCD);
        fA = fA/fABCD;
        fBCD = fBCD/fABCD;
        bufferRequirement[0] += fA*_actorsRecord[0].productionRate;
        
        minBufferScheduleIndex = 0;

        schedules[0].setIterationCount(fABCD);

        Firing F1 = new Firing();
        schedules[0].add(F1);
        F1.setIterationCount(fA);
        F1.setActor((Actor)_orderToActor.get(new Integer(0)));

        Schedule S1 = new Schedule();
        schedules[0].add(S1);
        S1.setIterationCount(fBCD);
        Firing F2 = new Firing();
        S1.add(F2);
        F2.setIterationCount(fB);
        F2.setActor((Actor)_orderToActor.get(new Integer(1)));
        Schedule S2 = new Schedule();
        S1.add(S2);
        S2.setIterationCount(fCD);
        Firing F3 = new Firing();
        S2.add(F3);
        F3.setIterationCount(fC);
        F3.setActor((Actor)_orderToActor.get(new Integer(2)));
        Firing F4 = new Firing();
        S2.add(F4);
        F4.setIterationCount(fD);
        F4.setActor((Actor)_orderToActor.get(new Integer(3)));
        
        // FIXME. printing for debugging.
        /* System.out.println();
        System.out.println("First schedule (MPMBS generation) is :");
        System.out.println(schedules[0]);
        System.out.println();
        System.out.println("Alternatively it is : (" + fABCD + "(" + fA 
                + "A)(" + fBCD + "(" + fB + "B)(" + fCD + "(" + fC + "C)(" 
                + fD + "D))))"); 
        System.out.println("Its buffer requirement is : " 
                + bufferRequirement[0]);
        */

        // Generating schedule 2 (index 1)

        fB = _firingCount[1];
        fC = _firingCount[2];
        fBC = ExtendedMath.gcd(fB, fC);
        fB = fB/fBC;
        fC = fC/fBC;
        bufferRequirement[1] += fB*_actorsRecord[1].productionRate;

        fD = _firingCount[3];
        fBCD = ExtendedMath.gcd(fBC, fD);
        fD = fD/fBCD;
        fBC = fBC/fBCD;
        bufferRequirement[1] += fBC*fC*_actorsRecord[2].productionRate;
        
        fA = _firingCount[0];
        fABCD = ExtendedMath.gcd(fA, fBCD);
        fA = fA/fABCD;
        fBCD = fBCD/fABCD;
        bufferRequirement[1] += fA*_actorsRecord[0].productionRate;
        
        if(bufferRequirement[1] < bufferRequirement[minBufferScheduleIndex]) 
            minBufferScheduleIndex = 1;

        schedules[1].setIterationCount(fABCD);

        Firing F5 = new Firing();
        schedules[1].add(F5);
        F5.setIterationCount(fA);
        F5.setActor((Actor)_orderToActor.get(new Integer(0)));

        Schedule S3 = new Schedule();
        schedules[1].add(S3);
        S3.setIterationCount(fBCD);
        Schedule S4 = new Schedule();
        S3.add(S4);
        S4.setIterationCount(fBC);
        Firing F6 = new Firing();
        S4.add(F6);
        F6.setIterationCount(fB);
        F6.setActor((Actor)_orderToActor.get(new Integer(1)));
        Firing F7 = new Firing();
        S4.add(F7);
        F7.setIterationCount(fC);
        F7.setActor((Actor)_orderToActor.get(new Integer(2)));
        Firing F8 = new Firing();
        S3.add(F8);
        F8.setIterationCount(fD);
        F8.setActor((Actor)_orderToActor.get(new Integer(3)));
        
        // FIXME. printing for debugging.
        /* System.out.println();
        System.out.println("Second schedule (MPMBS generation) is :");
        System.out.println(schedules[1]);
        System.out.println();
        System.out.println("Alternatively it is : (" + fABCD + "(" + fA 
                + "A)(" + fBCD + "(" + fBC + "(" + fB + "B)(" + fC + "C))(" 
                + fD + "D)))"); 
        System.out.println("Its buffer requirement is : " 
                + bufferRequirement[1]);
        */

        // Generating schedule 3 (index 2)
        fA = _firingCount[0];
        fB = _firingCount[1];
        fAB = ExtendedMath.gcd(fA, fB);
        fA = fA/fAB;
        fB = fB/fAB;
        bufferRequirement[2] += fA*_actorsRecord[0].productionRate;

        fC = _firingCount[2];
        fD = _firingCount[3];
        fCD = ExtendedMath.gcd(fC, fD);
        fC = fC/fCD;
        fD = fD/fCD;
        bufferRequirement[2] += fC*_actorsRecord[2].productionRate;

        fABCD = ExtendedMath.gcd(fAB, fCD);
        fAB = fAB/fABCD;
        fCD = fCD/fABCD;
        bufferRequirement[2] += fB*fAB*_actorsRecord[1].productionRate;

        if(bufferRequirement[2] < bufferRequirement[minBufferScheduleIndex]) 
            minBufferScheduleIndex = 2;

        schedules[2].setIterationCount(fABCD);

        Schedule S5 = new Schedule();
        schedules[2].add(S5);
        S5.setIterationCount(fAB);
        Firing F9 = new Firing();
        S5.add(F9);
        F9.setIterationCount(fA);
        F9.setActor((Actor)_orderToActor.get(new Integer(0)));
        Firing F10 = new Firing();
        S5.add(F10);
        F10.setIterationCount(fB);
        F10.setActor((Actor)_orderToActor.get(new Integer(1)));

        Schedule S6 = new Schedule();
        schedules[2].add(S6);
        S6.setIterationCount(fCD);
        Firing F11 = new Firing();
        S6.add(F11);
        F11.setIterationCount(fC);
        F11.setActor((Actor)_orderToActor.get(new Integer(2)));
        Firing F12 = new Firing();
        S6.add(F12);
        F12.setIterationCount(fD);
        F12.setActor((Actor)_orderToActor.get(new Integer(3)));

        // FIXME. printing for debugging.
        /* System.out.println();
        System.out.println("Third schedule (MPMBS generation) is :");
        System.out.println(schedules[2]);
        System.out.println();
        System.out.println("Alternatively it is : (" + fABCD + "(" + fAB 
                + "(" + fA + "A)(" + fB + "B))(" + fCD + "(" + fC 
                + "C)(" + fD + "D)))");
        System.out.println("Its buffer requirement is : " 
                + bufferRequirement[2]);
        */

        // Generating schedule 4 (index 3)
        fA = _firingCount[0];
        fB = _firingCount[1];
        fAB = ExtendedMath.gcd(fA, fB);
        fA = fA/fAB;
        fB = fB/fAB;
        bufferRequirement[3] += fA*_actorsRecord[0].productionRate;

        fC = _firingCount[2];
        fABC = ExtendedMath.gcd(fAB, fC);
        fAB = fAB/fABC;
        fC = fC/fABC;
        bufferRequirement[3] += fB*fAB*_actorsRecord[1].productionRate;   
        
        fD = _firingCount[3];
        fABCD = ExtendedMath.gcd(fABC, fD);
        fABC = fABC/fABCD;
        fD = fD/fABCD;
        bufferRequirement[3] += fC*fABC*_actorsRecord[2].productionRate;

        if(bufferRequirement[3] < bufferRequirement[minBufferScheduleIndex]) 
            minBufferScheduleIndex = 3;

        schedules[3].setIterationCount(fABCD);

        Schedule S7 = new Schedule();
        schedules[3].add(S7);
        S7.setIterationCount(fABC);
        Schedule S8 = new Schedule();
        S7.add(S8);
        S8.setIterationCount(fAB);
        Firing F13 = new Firing();
        S8.add(F13);
        F13.setIterationCount(fA);
        F13.setActor((Actor)_orderToActor.get(new Integer(0)));
        Firing F14 = new Firing();
        S8.add(F14);
        F14.setIterationCount(fB);
        F14.setActor((Actor)_orderToActor.get(new Integer(1)));
        Firing F15 = new Firing();
        S7.add(F15);
        F15.setIterationCount(fC);
        F15.setActor((Actor)_orderToActor.get(new Integer(2)));

        Firing F16 = new Firing();
        schedules[3].add(F16);
        F16.setIterationCount(fD);
        F16.setActor((Actor)_orderToActor.get(new Integer(3)));

        // FIXME. printing for debugging.
        /* System.out.println();
        System.out.println("Fourth schedule (MPMBS generation) is :");
        System.out.println(schedules[3]);
        System.out.println();
        System.out.println("Alternatively it is : (" + fABCD + "(" + fABC 
                + "(" + fAB + "(" + fA + "A)(" + fB + "B))("  + fC 
                + "C))(" + fD + "D))");
        System.out.println("Its buffer requirement is : " 
                + bufferRequirement[3]);
        */

        // Generating schedule 5 (index 4)
        fB = _firingCount[1];
        fC = _firingCount[2];
        fBC = ExtendedMath.gcd(fB, fC);
        fB = fB/fBC;
        fC = fC/fBC;
        bufferRequirement[4] += fB*_actorsRecord[1].productionRate;

        fA = _firingCount[0];
        fABC = ExtendedMath.gcd(fA, fBC);
        fA = fA/fABC;
        fBC = fBC/fABC;
        bufferRequirement[4] += fA*_actorsRecord[0].productionRate; 

        fD = _firingCount[3];
        fABCD = ExtendedMath.gcd(fABC, fD);
        fABC = fABC/fABCD;
        fD = fD/fABCD;  
        bufferRequirement[4] += fC*fBC*fABC*_actorsRecord[2].productionRate;

        if(bufferRequirement[4] < bufferRequirement[minBufferScheduleIndex]) 
            minBufferScheduleIndex = 4;

        schedules[4].setIterationCount(fABCD);

        Schedule S9 = new Schedule();
        schedules[4].add(S9);
        S9.setIterationCount(fABC);
        Firing F17 = new Firing();
        S9.add(F17);
        F17.setIterationCount(fA);
        F17.setActor((Actor)_orderToActor.get(new Integer(0)));
        Schedule S10 = new Schedule();
        S9.add(S10);
        S10.setIterationCount(fBC);
        Firing F18 = new Firing();
        S10.add(F18);
        F18.setIterationCount(fB);
        F18.setActor((Actor)_orderToActor.get(new Integer(1)));
        Firing F19 = new Firing();
        S10.add(F19);
        F19.setIterationCount(fC);
        F19.setActor((Actor)_orderToActor.get(new Integer(2)));

        Firing F20 = new Firing();
        schedules[4].add(F20);
        F20.setIterationCount(fD);
        F20.setActor((Actor)_orderToActor.get(new Integer(3)));

        // FIXME. printing for debugging.
        /*System.out.println();
        System.out.println("Fifth schedule (MPMBS generation) is :");
        System.out.println(schedules[4]);
        System.out.println();
        System.out.println("Alternatively it is : (" + fABCD + "(" + fABC 
                + "(" + fA + "A)(" + fBC + "(" + fB + "B)("  + fC 
                + "C)))(" + fD + "D))");
        System.out.println("Its buffer requirement is : " 
                + bufferRequirement[4]);
        */
        return schedules[minBufferScheduleIndex];
    }

    /** Generates the firing vector by multiplying the repetition vector
     *  and vectorization factor.  Also this function assigns a numeric
     *  value to each actor according to its ordering in the graph.
     *  Finally it populates the _actorsRecord array with all the required
     *  information for each actor.
     *
     *  @exception IllegalActionException If the SDF graph is not 
     *   chain-structured.
     *  @exception NotSchedulableException If the data or instruction 
     *   scratchpad sizes are non-positive integers or if the instruction
     *   scratchpad size is less than the code size of any given actor.
     */
    private void _populateActorsRecord() throws IllegalActionException {
          
        SDFDirector director = (SDFDirector)getContainer();
        CompositeActor container = (CompositeActor)director.getContainer();

        // A linked list containing all the actors.
        LinkedList allActorList = new LinkedList();
        // Populate it. Also find the total no of actors present in the
        // given chain-structured graph.
        for (Iterator entities = container.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            // Fill allActorList with the list of things that we can schedule
            if (entity instanceof ExperimentalActor) {
                allActorList.addLast(entity);
            }
        }   // end of for(Iterator entities = ....) loop

        // Temporary variable
        Token token;
        // Finding the vectorization factor that is provided by the user.
        _vectorizationFactor = 1;
        if (director instanceof SDFDirector) {
            token = ((SDFDirector)director).vectorizationFactor.getToken();
            _vectorizationFactor = ((IntToken)token).intValue();
         }
        if (_vectorizationFactor < 1) {
            throw new NotSchedulableException(this,
                    "The supplied vectorizationFactor must be " +
                    "a positive integer. " +
                    "The given value was: " + _vectorizationFactor);
        }

        // FIXME Checking the sizes of instruction and data scratchpads
        // for positive values and also for instruction scratchpad to be big
        // enough to accomodate the biggest given actor.

        // The above FIXME is not fixed for the time being as the 
        // graph construction takes care of the code size constraint.

        if (container instanceof CASDFComposite) {
            token = ((CASDFComposite)container).iSPMSize.getToken();
            _instructionSPMSize = ((IntToken)token).intValue();
            // token = ((CASDFComposite)container).dSPMSize.getToken();
            // _dataSPMSize = ((IntToken)token).intValue();
        }
        /*if(_dataSPMSize < 1) {
            throw new NotSchedulableException(this,
                    "The supplied dataScratchpadSize must be " +
                    "a positive integer. " +
                    "The given value was: " + _dataSPMSize);
        }*/
        else if(_instructionSPMSize < 1) {
            throw new NotSchedulableException(this,
                    "The supplied instructionScratchpadSize must be " +
                    "a positive integer. " +
                    "The given value was: " + _instructionSPMSize);
        }
            
        // externalRates maps from external
        // ports to the number of tokens that that port
        // will produce or consume in each firing.
        // It gets populated with the fractional production ratios
        // and is used in the end to set final rates on external ports.
        // This map is initialized to zero.
        Map externalRates = new TreeMap(new SDFUtilities.NamedObjComparator());

        // Initialize externalRates to zero.
        for (Iterator ports = container.portList().iterator();
             ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            externalRates.put(port, Fraction.ZERO);
        }

        // Getting the repetition vector for the given SDF graph.
        Map entityToFiringsPerIteration = 
            _solveBalanceEquations(container, allActorList, externalRates);

        // Normalize the number of for each actor using the
        // vectorizationFactor.
        _normalizeFirings(_vectorizationFactor, entityToFiringsPerIteration,
                externalRates);
        
        _firingVector = entityToFiringsPerIteration;

        // Computing the order of each order where order means 
        // the corresponding actor's topological ordering. For 
        // chain-structured graphs, only one ordering is possible. For 
        // n-actors, the order of each actor is in between 0 to 'n-1' 
        // with 0 being for the first actor and 'n' for the last. This 
        // ordering info, per actor, is stored in the Hashtable 
        // _orderToActor.
        _computeOrder();

        // Temporary variable
        int tempCodeSize = 0;
        int tokenRate;
        int tempTotalTokensProduced = 0;

        // Populating the _firingCount and _actorsRecord.
        for(int i = 0; i < _noOfActors; i++) {
            ExperimentalActor actor = (ExperimentalActor)_orderToActor.get(new Integer(i));
            _firingCount[i] = ((Integer)_firingVector.get((ComponentEntity)actor)).intValue();
            _actorsRecord[i].actorNo = i;

            if(i == 0) _actorsRecord[i].consumptionRate = 0;
            else {
                token = ((ExperimentalActor)actor).input_tokenConsumptionRate.getToken();
                _actorsRecord[i].consumptionRate = 
                    ((IntToken)token).intValue();
            }

            if(i == (_noOfActors - 1)) _actorsRecord[i].productionRate = 0;
            else {
                token = ((ExperimentalActor)actor).output_tokenProductionRate.getToken();
                _actorsRecord[i].productionRate = 
                    ((IntToken)token).intValue();
            }          

            // No actor has fired yet, so fired field is initialized to zero.
            _actorsRecord[i].fired = 0;
 
            if(i == 0) {
                _actorsRecord[i].fireable = _firingCount[i];
                _actorsRecord[i].inputTokensAvailable = 
                    _firingCount[i] * _actorsRecord[i].consumptionRate;
            }
            else {
                _actorsRecord[i].fireable = 0;
                _actorsRecord[i].inputTokensAvailable = 0;
            }
            _actorsRecord[i].toFire = _firingCount[i];

            _actorsRecord[i].toProduce = 
                _firingCount[i] * _actorsRecord[i].productionRate;

            _actorsRecord[i].toConsume = 
                _firingCount[i] * _actorsRecord[i].consumptionRate;

            _actorsRecord[i].produced = 0;
            _actorsRecord[i].consumed = 0; 

            // Check if the codeSize of this actor is greater than the
            // Instruction Scratchpad size. If it is, throw an exception.
            token = ((ExperimentalActor)actor).codeSize.getToken();
            tempCodeSize = ((IntToken)token).intValue();
            if(tempCodeSize > _instructionSPMSize ){
                throw new NotSchedulableException(this,
                        "The supplied instructionScratchpadSize is : "
                        +  _instructionSPMSize + ". It is less than"
                        + " the size of actor " + actor 
                        + " which is : " + tempCodeSize );
            }
            else {
                _actorsRecord[i].codeSize =  tempCodeSize;
            }
            tempTotalTokensProduced += _actorsRecord[i].toFire * _actorsRecord[i].productionRate;
        }   // end of for(i = 0;..) loop

        // Setting the data SPM equal to 20% of the total number of tokens
        // produced if vectorization factor is 1. Else, letting
        // the _dataSPMSize as it is (if its not 0). If it is 0, then 
        // updating its size to be 20% of total data consumed.
        if(_vectorizationFactor == 1) {
            //_dataSPMSize = 13;
            _dataSPMSize = (int)(0.2*tempTotalTokensProduced);
            _lastDataSPMSize = _dataSPMSize;
        }
        else _dataSPMSize = _lastDataSPMSize;
        // Printing the various parameters
        System.out.println();
        System.out.println();
        System.out.println("(Scheduler) Total Actors : " + _noOfActors);
        System.out.println("Total Data to be Produced : " + tempTotalTokensProduced);
        System.out.println("(Scheduler) D-SPM Size : " + _dataSPMSize);
        System.out.println("(Scheduler) I-SPM Size : " + _instructionSPMSize);
        System.out.println("(Scheduler) Vectorization Factor : " + _vectorizationFactor);

        for(int i = 0; i < _noOfActors; i++) {
         // Printing the actor's parameters
            System.out.println("Actor " + (i+1) + "'s Consumption Rate is "
                    + _actorsRecord[i].consumptionRate);
            System.out.println("Actor " + (i+1) + "'s Production Rate is "
                    + _actorsRecord[i].productionRate);
            System.out.println("Actor " + (i+1) + "'s Firing Count is "
                    + _actorsRecord[i].toFire);
            System.out.println("Actor " + (i+1) + "'s Code Size is "
                    + _actorsRecord[i].codeSize);
        }    // end of for(i = 0;..) loop
    }

    /** Generates an unlooped (linearized) schedule from a given looped
     *  schedule. It is used to calculate the DMP and IMP for a given looped 
     *  schedule like the MPMBS Schedule.
     *
     *  @param loopedSchedule The looped schedule to be unlooped.
     *  @return The unlooped (linearized) schedule.
     */
    private void _unloopSchedule(Schedule loopedSchedule) 
            throws IllegalActionException {
        int iterationCount = loopedSchedule.getIterationCount();
        for(int i = 1; i <= iterationCount; i++) {
            Iterator loopedScheduleIterator = loopedSchedule.iterator();
            while(loopedScheduleIterator.hasNext()) {
                ScheduleElement element = ((ScheduleElement)loopedScheduleIterator.next());
                if(element instanceof Firing) {
                    _linearizedMPMBSchedule.add(element);
                } else if(element instanceof Schedule) {
                    _unloopSchedule((Schedule)element);
                } else throw new IllegalActionException("The MPMBS Schedule " 
                        + "contains an element that is not of type Firing or "
                        + "Schedule.");   
            }   // end of while loop
        }    // end of for loop
    }

    /** Update the Instruction Miss Penalty when the specified actor 
     *  us activated. If its already present in the I-SPM, then no
     *  penalty is added, else a penalty is added to bring its code to
     *  the ISPM. 
     *
     *  If space needs to be made in ISPM to bring the current actor, 
     *  then the smallest possible actor is evicted. If no one actor can
     *  make space for the current actor, then a set of actors is evicted
     *  to make space for the currentActor.
     *  
     *  @param actorNo The actor that needs to be activated.
     */
    private void _updateIMP(int actorNo) {
        if(_instructionSPM[actorNo] == false) {

            // See if any fully fired actors are residing in I-Scratchpad.
            // If any of them is found, evict it and free that space.
            for(int i = 0; i < _noOfActors; i++) {
                if(_instructionSPM[i] == true) {
                    if(_actorsRecord[i].fired == _actorsRecord[i].toFire) {
                        _instructionSPM[i] = false;
                        _usedInstructionSPM -= _actorsRecord[i].codeSize;
                    }    // end of if(_actorsRecord..)
                }    // end of if(_instructionSPM[i] == true)
            }    // end of for(int i = 0; ...) loop

            if(_usedInstructionSPM + _actorsRecord[actorNo].codeSize >
                    _instructionSPMSize) {
                
                // Temp Variables
                boolean foundActorToSwap = false;
                int swapActor = -1;
                int swapActorSize = 0;
                int maxActor = -1;
                int maxActorSize = 0;
                // To be used in case we need to remove a set of actors
                // to bring this successor in the I-Scratchpad.
                ArrayList swapActorList = new ArrayList();
                int swapListSize = 0;

                while((swapListSize + _instructionSPMSize - _usedInstructionSPM) < _actorsRecord[actorNo].codeSize) {

                    foundActorToSwap = false;
                    maxActor = -1;
                    swapActor = -1;
                    maxActorSize = 0;
                    swapActorSize = 0;

                    for(int j = 0; j < _noOfActors; j++) {
                        if(_instructionSPM[j]) {
                            if(!swapActorList.contains(new Integer(j))) {
                                if((_actorsRecord[j].codeSize + swapListSize + _instructionSPMSize - _usedInstructionSPM) >= _actorsRecord[actorNo].codeSize){
                                    if(foundActorToSwap == true) {
                                        if(swapActorSize > 
                                                _actorsRecord[j].codeSize) {
                                            // Replace the current swap actor
                                            // with this newly found smaller 
                                            // size swap actor.
                                            swapActor = j;
                                            swapActorSize =
                                                _actorsRecord[j].codeSize;
                                        }    // end of if(swapActorSize..)
                                    }     // end of if(foundActorToSwap)
                                    else {
                                        foundActorToSwap = true;
                                        swapActor = j;
                                        swapActorSize =
                                            _actorsRecord[j].codeSize;
                                    }
                                } // end of finding the swap actor.
                                if(_actorsRecord[j].codeSize > 
                                        maxActorSize) {
                                    maxActor = j;
                                    maxActorSize = 
                                        _actorsRecord[j].codeSize;
                                }    // end of if(actorsRecord..)
                            }    // end of if(!swapActorList..)
                        }    // end of if(actorsInISPM[j])
                    }    // end of for(int j = 0;...) loop
                    
                    if(foundActorToSwap) {
                        swapActorList.add(new Integer(swapActor));
                        swapListSize += swapActorSize;
                    } else {
                        swapActorList.add(new Integer(maxActor));
                        swapListSize += maxActorSize;
                    }
                } // end of while(swapListSize....)
                
                Iterator tempActors = swapActorList.iterator();
                while(tempActors.hasNext()) {
                    int tempSwapActor = 
                        ((Integer)tempActors.next()).intValue();
                    int tempSwapActorSize = 
                        _actorsRecord[tempSwapActor].codeSize;
                    _instructionSPM[tempSwapActor] = false;
                    _usedInstructionSPM -= tempSwapActorSize ;     
                }   // end of while(tempActors.hasNext())
                _instructionSPM[actorNo] = true;
                _usedInstructionSPM += _actorsRecord[actorNo].codeSize;
            }    // end of if(_usedInstructionSPM + ...)
            else {
                _usedInstructionSPM += _actorsRecord[actorNo].codeSize;
                _instructionSPM[actorNo] = true;
            }    // end of else for if(_usedInstructionSPM + ..)
            _instructionMissPenalty += 
                _IREADTIME * _actorsRecord[actorNo].codeSize;
        }   // end of if(_instructionSPM[actorNo] == false)
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Inner Classes                     ////

    /** This class serves as a data structure for the cache aware scheduler.
     *  It is a record for a particular actor's:
     *  Actor number,
     *  Production rate, 
     *  Consumption rate,
     *  Total tokens to be produced, 
     *  Tokens produced, 
     *  Total tokens to be consumed,
     *  Tokens consumed, 
     *  Total no of firings,
     *  No of firings already done,
     *  No of times its fireable at an instant.
     */
    private class ActorRecord {
        
        /** Construct an ActorRecord
         */
        public ActorRecord() {
            actorNo = -1;
            consumed = 0;
            produced = 0;
            fireable = 0;
            fired = 0;
            inputTokensAvailable = 0;
            consumptionRate = 0;
            productionRate = 0;
            toConsume = 0;
            toProduce = 0;
            toFire = 0;
            codeSize = 0;
        }

        ///////////////////////////////////////////////////////////////////
        ////                Public Member Variables                   ////
    
        // To store the actor number.
        public int actorNo;
        // The code size of the actor.
        public int codeSize;
        // To keep an account on the number of tokens already consumed by the 
        // actor.
        public int consumed;
        // The consumption rate of the actor.
        public int consumptionRate;
        // To keep an account on the number of possible firings of the actor
        // at an instant.
        public int fireable;
        // To keep an account on the number of times the actor has already 
        // been fired.
        public int fired;
        // No of input tokens available to consume.
        public int inputTokensAvailable;
        // To keep an account on the number of tokens already produced by the 
        // actor.
        public int produced;
        // The production rate of the actor.
        public int productionRate;
        // To store the total tokens to be consumed by the actor. For source
        // actors, this is zero.
        public int toConsume;
        // To store the total no of times the actor is supposed to fire.
        public int toFire;
        // To store the total tokens to be produced by the actor. For sink 
        // actors, this is zero.
        public int toProduce;
    }

    ///////////////////////////////////////////////////////////////////
    ////                Private Member Variables                   ////

    // Constants

    // Time taken to read one unit of data from the instruction memory.
    private static final int _IREADTIME = 1;
    // Time taken to read one unit of data from the data  memory.
    private static final int _DREADTIME = 1;
    // Time taken to write one unit of data from the data memory.
    private static final int _DWRITETIME = 1;

    // Variables

    // The record for each actor during the schedule generation. The schedule
    // is generated by simulating an iteration of the graph, hence this 
    // record is very critical for the correct schedule generation.
    private ActorRecord[] _actorsRecord;
    // The cache aware schedule that gets generated by this scheduler.
    private Schedule _cacheAwareSchedule;
    // The version of container which is a composite actor in this case
    private int _containerVersion;
    // The total Data Miss Penalty (DMP) associated with the cache aware 
    // schedule.
    private int _dataMissPenalty;
    // The data scratchpad memory.
    private ScratchpadMemory _dataSPM;
    // The size of the data scratchpad memory.
    private int _dataSPMSize;
    // The firing vector for the given SDF graph. It has the firing count
    // for each actor and can be accessed using their respective orderings.
    private int[] _firingCount;
    // The firing vector.  A map from actor to an integer representing the
    // number of times the actor will fire.
    private Map _firingVector;
    // The total Instruction Miss Penalty (IMP) associated with the cache
    // aware schedule.
    private int _instructionMissPenalty;
    // The instruction scratchpad memory. We don't need to check individual
    // block level contents, we just need to know whether an actors is present
    // or not in the I-Scratchpad.
    private boolean[] _instructionSPM;
    // The size of the instruction scratchpad memory.
    private int _instructionSPMSize;
    // The size of D-SPM in last schedule generation.
    private int _lastDataSPMSize;
    // The looped MPMBS Schedule
    private Schedule _MPMBSchedule;
    // The total no of actors in the SDF graph.
    private int _noOfActors;
    // A Hashtable stores the mapping of each actor to its depth (ordering).
    private Hashtable _orderToActor = null;
    // The SAMAS schedule for the given chain-structured graph.
    private Schedule _SAMASchedule;
    // Vectorization factor for the given SDF graph
    private int _vectorizationFactor;
    // The unlooped or linearized version of MPMBS schedule
    private Schedule _linearizedMPMBSchedule;
    // Total used space in the I-Scratchpad Memory.
    private int _usedInstructionSPM;
}
