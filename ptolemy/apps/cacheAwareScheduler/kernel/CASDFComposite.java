/* An aggregation of SDF actors for Cache Aware Scheduling experiments.

 Copyright (c) 2003 The Regents of the University of California.
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

import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Writer;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.apps.cacheAwareScheduler.lib.ExperimentalActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor
/**
This is a composite actor for use in the sdf domain to conduct experiments
with the cache aware scheudler. 

@author Sanjeev Kohli
@version $Id$
*/
public class CASDFComposite extends TypedCompositeActor {

    /** Construct a composite actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have 
     *  an SDF Director as its local director and a cache aware scheduler will
     *  be associated with this director.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CASDFComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set version to 0.
        _version = 0;

        // Copying what wireless composite does in its constructor
        getMoMLInfo().className =
            "ptolemy.apps.CacheAwareScheduler.kernel.CASDFComposite";

        // Set parameters.
        dSPMSize = new Parameter(this, "dSPMSize");
        dSPMSize.setTypeEquals(BaseType.INT);
        dSPMSize.setExpression("50 + roundToInt(random()*150)");

        iSPMSize = new Parameter(this, "iSPMSize");
        iSPMSize.setTypeEquals(BaseType.INT);
        iSPMSize.setExpression("50 + roundToInt(random()*150)");

        noOfActors = new Parameter(this, "noOfActors");
        noOfActors.setTypeEquals(BaseType.INT);
        noOfActors.setExpression("4");

        sdfDirector = new SDFDirector(this, "sdfDirector");
        _cacheAwareScheduler = 
            new CacheAwareScheduler(sdfDirector, "MyScheduler");

        // Randomly generate a vectorization factor for the SDF Director 
        sdfDirector.vectorizationFactor.setExpression("1 + roundToInt(random()*9)");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The size of Data Scratchpad Memory   */
    public Parameter dSPMSize;

    /** The size of Instruction Scratchpad Memory
     */
    public Parameter iSPMSize;

    /** The number of actors in the chain-structured graph
     */
    public Parameter noOfActors;

 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Doesn't do anything. Empty for now.
     */
    public void fire() {
        // Leave it Empty
    }

    /** Return the version of this composite actor.
     */
    public int getVersion() {
        return _version;
    }

    /** Generate the random chain structured graph, and construct schedule.
     */
    public void preinitialize() throws IllegalActionException {

        // Evaluate (validate) the required parameters.
        dSPMSize.validate();
        iSPMSize.validate();
        noOfActors.validate();
        sdfDirector.vectorizationFactor.validate();

        // Increment the composite actor version since the graph is going
        // to change. The scheduler checks it to recompute the schedule
        _version++;

        // Generate Random chain-structured graph and put in 
        // attributes
        Token token;
        token = noOfActors.getToken();
        int totalActors = ((IntToken)token).intValue();
        
        ExperimentalActor[] actors = new ExperimentalActor[totalActors];

        try {
            for(int i = 0; i < totalActors; i++) {
                // Instantiate a new actor of type ExperimentalActor
                actors[i] = new ExperimentalActor(this, "actor" + (i+1));
                
                // Randomly assign production and consumption rates to it
                if(i == 0) {
                    actors[i].input_tokenConsumptionRate.setToken("0");
                } else {
                    actors[i].input_tokenConsumptionRate.setToken("1 + roundToInt(random()*9)");
                }
                if(i == (totalActors - 1)) {
                    actors[i].output_tokenProductionRate.setToken("0");
                } else {
                    actors[i].output_tokenProductionRate.setToken("1 + roundToInt(random()*9)");
                }
                
                // Connect the input and output ports to the previous and next
                // actor in chain respectively. No connections need to be 
                // made to the input of first actor and output of last actor.
                if(!(i == 0)) {
                    TypedIORelation relation = 
                        new TypedIORelation(this, "actor" + i + "output");
                    actors[i-1].output.link(relation);
                    actors[i].input.link(relation);
                }   // end of if(1(i ==0))
            }
        } catch(Exception ex) {
            //  System.out.println("Exception Caught while generating the random " + "graph. It is " + ex);
        }
        
        // The schedule gets generated in the super.preinitialize. So, no
        // need to invoke _cacheAwareScheduler.getSchedule() seperately.
        super.preinitialize();
        cacheAwareSchedule = _cacheAwareScheduler.getSchedule();
    }

    /** Return False as this composite actor has to be fired only once 
     *  in every execution to generate the schedule.
     */ 
    public boolean postfire() {
        return false;
    }

    /** Print the generated Cache Aware Schedule.
     */
    public void wrapup() throws IllegalActionException {
        // Display the generated Cache Aware Schedule, the associated
        // data and instruction miss penalties and the associated 
        // penalties for Minimum Activation Schedule.
        System.out.println();
        System.out.println();
        System.out.println(cacheAwareSchedule);
        int imp =  _cacheAwareScheduler.instructionMissPenalty();
        int dmp =  _cacheAwareScheduler.dataMissPenalty();
        System.out.println("The IMP for Cache Aware Schedule is : " 
                + imp);
        System.out.println("The DMP for Cache Aware Schedule is : "
                + dmp);
        System.out.println("The total CMP for Cache Aware Schedule is : "
                + (imp + dmp));
        _cacheAwareScheduler.calculateMPMBScheduleCMP();
        _cacheAwareScheduler.calculateSAMAScheduleCMP();   
        super.wrapup(); 
   }

    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////
    
    // The generated Cache Aware Schedule
    public Schedule cacheAwareSchedule;

    // The associated SDF director
    public SDFDirector sdfDirector;


    ///////////////////////////////////////////////////////////////////
    ////                    protected functions                    ////
    
    protected void _exportMoMLContents(Writer output, int depth) {

    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                    ////
    
    // The cache aware scheduler
    private CacheAwareScheduler _cacheAwareScheduler;

    // The version of composite actor
    private int _version;

}
