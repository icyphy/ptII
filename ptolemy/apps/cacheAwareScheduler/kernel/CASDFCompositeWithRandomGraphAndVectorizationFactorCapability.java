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

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
        //iSPMSize.setExpression("77");

        noOfActors = new Parameter(this, "noOfActors");
        noOfActors.setTypeEquals(BaseType.INT);
        noOfActors.setExpression("4");

        sdfDirector = new SDFDirector(this, "sdfDirector");
        _cacheAwareScheduler = 
            new CacheAwareScheduler(sdfDirector, "MyScheduler");

        // Randomly generate a vectorization factor for the SDF Director 
        // sdfDirector.vectorizationFactor.setExpression("1 + roundToInt(random()*9)");
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
        sdfDirector.vectorizationFactor.setExpression("1");
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

        /*
        // The following code generates a given 4 actor chain-structured graph
        try {
            actors[0] = new ExperimentalActor(this, "actor1");
            actors[0].input_tokenConsumptionRate.setToken("0");
            actors[0].output_tokenProductionRate.setToken("6");
            actors[0].codeSize.setToken("26");

            actors[1] = new ExperimentalActor(this, "actor2");
            actors[1].input_tokenConsumptionRate.setToken("8");
            actors[1].output_tokenProductionRate.setToken("3");
            actors[1].codeSize.setToken("8");
            TypedIORelation relation1 = 
            new TypedIORelation(this, "actor1output");
            actors[0].output.link(relation1);
            actors[1].input.link(relation1);
            
            actors[2] = new ExperimentalActor(this, "actor3");
            actors[2].input_tokenConsumptionRate.setToken("1");
            actors[2].output_tokenProductionRate.setToken("2");
            actors[2].codeSize.setToken("28");
            TypedIORelation relation2 = 
                new TypedIORelation(this, "actor2output");
            actors[1].output.link(relation2);
            actors[2].input.link(relation2);
            
            actors[3] = new ExperimentalActor(this, "actor4");
            actors[3].input_tokenConsumptionRate.setToken("1");
            actors[3].output_tokenProductionRate.setToken("0");
            actors[3].codeSize.setToken("3");
            TypedIORelation relation3 = 
                new TypedIORelation(this, "actor3output");
            actors[2].output.link(relation3);
            actors[3].input.link(relation3);
        } catch(Exception ex) {
            //System.out.println("Exception Caught while generating the defined " + "graph. It is " + ex);
        }
        */
        
        // The following code, (under try) generates a random 
        // chain-structured graph. The actors in this random graph 
        // have randomly generated production and consumption rates. 

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
        //cacheAwareSchedule = _cacheAwareScheduler.getSchedule();
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

        // FIXME.
        // Variables for writing results to a file. Used for storing data 
        // while running schedulers on randomly generated graphs. 
        FileOutputStream file;
        PrintStream filePrinter;
        try {
            file = new FileOutputStream("CMP-3Schedulers.txt", true);
            filePrinter = new PrintStream(file);
        } catch ( IOException e) {
            System.out.println("Unable to create Results file");
            return;
        }   
        // FIXME Over.

        // Generating the cache aware schedule for 6 vectorization factors
        // and the associated penalties for all 3 schedules (MPMBS, SAMAS
        // and CAS).
       
        // Note that we do not increment _version when i = 1 because
        // super.preinitialize already computed the CAS schedule for i = 1.
        // We just run the loop at i = 1 to print the respective data.
        for(int i = 1; i <= 20; i++) {

            if(i ==1) {
                System.out.println();
                System.out.println("******* Vectorization Factor : " + i 
                        + "*******" );
                System.out.println();
            }
            if(i == 1) sdfDirector.vectorizationFactor.setExpression("1");
            else if(i == 2) {
                sdfDirector.vectorizationFactor.setExpression("2");
                _version++;
            }
            else if(i == 3) {
                sdfDirector.vectorizationFactor.setExpression("3");
                _version++;
            }
            else if(i == 4) {
                sdfDirector.vectorizationFactor.setExpression("4");
                _version++;
            }
            else if(i == 5) {
                sdfDirector.vectorizationFactor.setExpression("5");
                _version++;
            }
            else if(i == 6) {
                sdfDirector.vectorizationFactor.setExpression("6");
                _version++;
            }
            else if(i == 7) {
                sdfDirector.vectorizationFactor.setExpression("7");
                _version++;
            }
            else if(i == 8) {
                sdfDirector.vectorizationFactor.setExpression("8");
                _version++;
            }
            else if(i == 9) {
                sdfDirector.vectorizationFactor.setExpression("9");
                _version++;
            }
            else if(i == 10) {
                sdfDirector.vectorizationFactor.setExpression("10");
                _version++;
            }

            else if(i == 11) {
                sdfDirector.vectorizationFactor.setExpression("11");
                _version++;
            }
            else if(i == 12) {
                sdfDirector.vectorizationFactor.setExpression("12");
                _version++;
            }
            else if(i == 13) {
                sdfDirector.vectorizationFactor.setExpression("13");
                _version++;
            }
            else if(i == 14) {
                sdfDirector.vectorizationFactor.setExpression("14");
                _version++;
            }
            else if(i == 15) {
                sdfDirector.vectorizationFactor.setExpression("15");
                _version++;
            }
            else if(i == 16) {
                sdfDirector.vectorizationFactor.setExpression("16");
                _version++;
            }
            else if(i == 17) {
                sdfDirector.vectorizationFactor.setExpression("17");
                _version++;
            }
            else if(i == 18) {
                sdfDirector.vectorizationFactor.setExpression("18");
                _version++;
            }
            else if(i == 19) {
                sdfDirector.vectorizationFactor.setExpression("19");
                _version++;
            }
            else if(i == 20) {
                sdfDirector.vectorizationFactor.setExpression("20");
                _version++;
            }
            
            cacheAwareSchedule = _cacheAwareScheduler.getSchedule();

            // Display the generated Cache Aware Schedule, the associated
            // data and instruction miss penalties and the associated 
            // penalties for Minimum Activation Schedule.
            
            // FIXME. Uncomment the following lines eventually.
            //System.out.println();
            //System.out.println("The CAS schedule is :");
            //System.out.println(cacheAwareSchedule);
            int imp =  _cacheAwareScheduler.instructionMissPenalty();
            int dmp =  _cacheAwareScheduler.dataMissPenalty();
            int cmpCAS = imp + dmp;
            if(i == 1) System.out.println();
            /*
            System.out.println("The IMP for Cache Aware Schedule is : " 
                    + imp);
            System.out.println("The DMP for Cache Aware Schedule is : "
                    + dmp);
            System.out.println("The total CMP for Cache Aware Schedule is : "
                    + (imp + dmp));
            */
            if(i == 1) System.out.println("CAS - CMP   : " + cmpCAS);
            filePrinter.print(cmpCAS);
            filePrinter.print("\t");
            // Printing the other two schedules (MPMBS and SAMAS) and 
            // their respective penalties
            int cmpMPMBS = _cacheAwareScheduler.calculateMPMBScheduleCMP();
            if(i == 1) System.out.println("MPMBS - CMP : " + cmpMPMBS);
            filePrinter.print(cmpMPMBS);
            filePrinter.print("\t");
            int cmpSAMAS = _cacheAwareScheduler.calculateSAMAScheduleCMP();
            if(i == 1) System.out.println("SAMAS - CMP : " + cmpSAMAS);
            filePrinter.print(cmpSAMAS);
            if(i == 20) filePrinter.println();
            else filePrinter.print("\t\t\t\t");
        }
        System.out.println();
        System.out.println("----------------- END ---------------");
        System.out.println();

        // FIXME.
        try {
            filePrinter.flush();
            file.close();
        } catch(IOException e) {
            System.out.println("Can not close Results file");
        }
        // FIXME OVER.

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
