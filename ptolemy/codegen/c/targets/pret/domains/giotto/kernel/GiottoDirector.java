/* Code generator helper class associated with the GiottoDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.pret.domains.giotto.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.giotto.kernel.GiottoReceiver;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;


//////////////////////////////////////////////////////////////////
////GiottoDirector

/**
 Code generator helper associated with the GiottoDirector class. This class
 is also associated with a code generator.

 @author Ben Lickly,Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 7.2
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public class GiottoDirector extends ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {

    public String generateFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        //code.append("//fire code should be here. I'm from the openRTOS GiottoDirector "+_eol);
        System.out.println("generateFireCode from pret giotto director called here");
             
        return code.toString();
    }
    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        System.out.println("generateFireFunctionCode called from pret giotto director***************");
        StringBuffer code = new StringBuffer(" ");//super.generateFireFunctionCode());
        code.append("//generateFireFunctionCode called from pret giotto director");
        return code.toString();
        
        //content moved to _generateActorsCode() which is called in preinitialize b/c code wasn't being generated for 
        //nested actors
           
    
    }
    public String generatePreinitializeCode() throws IllegalActionException {
            StringBuffer code = new StringBuffer(super.generatePreinitializeCode());
            // Declare the thread handles.
            System.out.println("generatePreinitializeCode from pret giotto director called here");
            code.append("//should driver code be here");
            /*
            code.append(_generateBlockCode("preinitBlock"));
            HashSet<Integer> frequencies = _getAllFrequencies();
    
            StringBuffer frequencyTCode = new StringBuffer();
    
            ArrayList args = new ArrayList();
            args.add("");
    
            int currentPriorityLevel = 1;
            if(_isTopGiottoDirector())
            {
                args.set(0, "$actorSymbol()_scheduler");
                code.append(_generateBlockCode("declareTaskHandle", args));
            }
            for(int frequencyValue : frequencies) {
                // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
    //            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));
    
               args.set(0, _getThreadName(frequencyValue));
               code.append(_generateBlockCode("declareTaskHandle", args));
            }
    
            code.append("//driver code should be below here******************"+_eol);
           code.append(_generateDriverCode());
           code.append(_generateActorsCode());
           //code.append("am i the top most director??");
            //System.out.println("I should check to see if I'm the top most Giotto director here.. ");
           */
            code.append(_eol+_generateDriverCode());
            code.append(_generateActorsCode());
          return processCode(code.toString());
        }
    ////////////////////////////////////////////////////////////////////////
            ////                         public methods                         ////
            public String generateMainLoop() throws IllegalActionException {
                StringBuffer code = new StringBuffer();
        
                Attribute iterations = _director.getAttribute("iterations");
                if (iterations == null) {
                    code.append(_eol + "while (true) {" + _eol);
                } else {
                    int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                            .intValue();
                    if (iterationCount <= 0) {
                        code.append(_eol + "while (true) {" + _eol);
                    } else {
                        // Declare iteration outside of the loop to avoid
                        // mode" with gcc-3.3.3
                        code.append(_eol + "int iteration;" + _eol);
                        code.append("for (iteration = 0; iteration < "
                                + iterationCount + "; iteration ++) {" + _eol);
                        //call the actor methods here....
                    }
                }
        
        //Begin Shanna Comment out here
               
                code.append(_generateFireCode());
             
                // The code generated in generateModeTransitionCode() is executed
                // after one global iteration, e.g., in HDF model.
                ActorCodeGenerator modelHelper = (ActorCodeGenerator) _getHelper(_director
                        .getContainer());
                modelHelper.generateModeTransitionCode(code);
        
                /*if (callPostfire) {
                    code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                            + "break;" + _eol + _INDENT2 + "}" + _eol);
                }
                 */
               
               code.append(generatePostfireCode());
                
               
        //end Shanna comment out here
                code.append("}" + _eol);
        
                return code.toString();
            }
    /** Generate a variable declaration for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
    
           CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
           variableDeclarations.append(helperObject.generateVariableDeclaration());
           variableDeclarations.append(_generatePortVariableDeclarations(actor));
           
           
           
           
           
           
           
           variableDeclarations.append("//should attempt to append port here"+_eol);
           // variableDeclarations.append(helperObject.);
        }
        
       return variableDeclarations.toString();
    }

    
 public Set getHeaderFiles() throws IllegalActionException {
        HashSet files = new HashSet();
        files.add("\"deadline.h\"");
        return files;
    }
public String getPortVariableDeclarations()  throws IllegalActionException {
    return "getPortVariableDeclaration from Giotto Director Called";
}
public String getReference(TypedIOPort port, String[] channelAndOffset,
        boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
throws IllegalActionException {

    if(port.isOutput())
    {// do own thing here}
    // will need to take care of the case of where the output is for a composite actor
        //return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0]+"_PORT";
        return "*"+CodeGeneratorHelper.generateName(port)+"_PORT";  // will need to handle multiple channels later
        //return "//dummy for write port";
    }
       else
        //return "//from else";
           return super.getReference(port, channelAndOffset, forComposite, isWrite,helper);
}
/** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);
       
    }

    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        double period = _getPeriod();
        int threadID = 0;
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
            ActorCodeGenerator helper = (ActorCodeGenerator) _getHelper(actor);
    
            // FIXME: generate deadline instruction w/ period
    
            // Note: Currently, the deadline instruction takes the number 
            // of cycle as argument. In the future, this should be changed
            // to use time unit, which is platform-independent. 
            // So for now, we will assume the clock speed to be 250 Mhz. 
            // Thus, in order to get a delay of t seconds (= period/frequency) 
            // for each actor, we will need to wait for 
            // period/frequency * 250,000,000 cycles/second.
            
            //int cycles = (int)(250000000 * period / _getFrequency(actor));
            int cycles = (int)(250000000 * period / _getFrequency(actor));
            code.append("#ifdef THREAD_" + threadID + "\n");
            // for
            String index = CodeGeneratorHelper.generateName((NamedObj) actor) + "_frequency";
            code.append("for (int " + index + " = 0; " + index + " < " +
                    _getFrequency(actor) + "; ++" + index + ") {" + _eol);
            code.append("DEAD(" + cycles  + ");" + _eol);
            code.append(_getActorName(actor)+"_driver();"+_eol);
            code.append(_getActorName(actor)+"();"+_eol);
           // code.append(helper.generateFireCode());
            //code.append(helper.generatePostfireCode());
    
            code.append("}" + _eol); // end of for loop
    
            code.append("#endif /* THREAD_" + threadID + "*/\n");
            threadID++;
        }
        return code.toString();
    }
    private String _generatePortVariableDeclarations(Actor actor) throws IllegalActionException {
        
        System.out.println("get Port Variable Declarations called");
        StringBuffer code = new StringBuffer();
    
        Iterator outputPorts = actor.outputPortList()
                .iterator();
    
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
    
            // If either the output port is a dangling port or
            // the output port has inside receivers.
           //if (!outputPort.isOutsideConnected() || outputPort.isInsideConnected()) {
            if(true){
                //not sure how this will work for multiports yet
              
                code.append("/*"+_getActorName(actor)+ "'s PORT variable declarations.*/"+_eol);
                code.append("volatile " + targetType(outputPort.getType()) + " * "
                        + generateName(outputPort)+"_PORT = ("+targetType(outputPort.getType()) + " * ) 0x"+Integer.toHexString(_getThenIncrementCurrentSharedMemoryAddress()));
    
                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }
    
                int bufferSize = getBufferSize(outputPort);
    
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
            else
            {
                System.out.println("didn't match if");
            }
           
       // return "should define port here from CCodeGEneratorHelper";
        }
        //System.out.println("about to return: "+code.toString());
        return code.toString();
       }
    private int _getFrequency(Actor actor) throws IllegalActionException {
        Attribute frequency = ((Entity)actor).getAttribute("frequency");
        if (frequency == null) {
            return 1;
        } else {
            return ((IntToken) ((Variable) frequency).getToken()).intValue();
        }
    }
    private double _getPeriod() throws IllegalActionException {
    
        /*
         * Director = _director.container.director
         * while (!director instanceof giottodirector) {
         *   if (director.container.director exists)
         *     director = director.container.director
         *   else return periodValue attribute
         * }
         * return director._getPeriod() * director.container."frequency"
         */
        
        Director director = ((TypedCompositeActor)
                _director.getContainer()).getExecutiveDirector();
        
        while (director != null &&
               !(director instanceof ptolemy.domains.giotto.kernel.GiottoDirector)) {
                director = ((TypedCompositeActor)
                        director.getContainer()).getExecutiveDirector();
        }
        if (director == null) {
            // This is the case for the outer-most Giotto director.
            Attribute period = _director.getAttribute("period");
            double periodValue;
    
            if (period != null) {
                periodValue = ((DoubleToken) ((Variable) period).getToken())
                .doubleValue();
            } else {
                throw new IllegalActionException(this, "There is no period" +
                            "specified in the outer-most Giotto director");
            }
            return periodValue;            
        }
    
        // Get the frequency.
        int frequency = _getFrequency((Actor)_director.getContainer());
        return ((GiottoDirector) _getHelper(director))._getPeriod() / frequency;
        
    }
    
    private String _getActorName(Actor actor) {
        String actorFullName = actor.getFullName();
         actorFullName = actorFullName.substring(1,actorFullName.length());
         actorFullName = actorFullName.replace('.', '_');
         actorFullName = actorFullName.replace(' ', '_');
        return actorFullName;
    }
    
    /** Generate the content of a driver methods. Copy outputs variables to their output ports,
     *  and inputs from a port the the input variable. The PORT allows double buffering.
     *  PORT here is simply a common variable, not a PORT in the general Ptolemy II actor sense
     *  @param none
     *  @return code that copies outputs to a port, and inputs from a port in a driver method
     */ String _generateDriverCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        System.out.println("generateDriver Code has been called");
        
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
    
            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();
    
            String actorDriverCode = "";
            // this is a hack to try to copy the output of the actor to the port in the driver code
            CodeGeneratorHelper myHelper;
            //this is currently a hack. let's see how long it continues to work
            /*if(outputPorts .hasNext())
            {
               actorDriverCode+=_getActorName(actor)+"_output_0_PORT = "+_getActorName(actor)+"_output_0;"+_eol;
                
            }
            */
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                Receiver[][] channelArray = port.getRemoteReceivers();
    
                for (int i = 0; i < channelArray.length; i++) {
                    Receiver[] receiverArray = channelArray[i];
    
                    for (int j = 0; j < receiverArray.length; j++) {
                        GiottoReceiver receiver = (GiottoReceiver) receiverArray[j];
                        IOPort sinkPort = receiver.getContainer();
                        
                        ArrayList args = new ArrayList();
    
                        // FIXME: figure out the channel number for the sinkPort.
                        String channelOffset [] = {"0","0"};
                        myHelper = (CodeGeneratorHelper) this._getHelper(sinkPort.getContainer());
                        String sinkReference = this.getReference((TypedIOPort)sinkPort,channelOffset,false,true,myHelper);//"##ref(sinkPort)";
                        
                        channelOffset[0]= Integer.valueOf(i).toString();
                        myHelper = (CodeGeneratorHelper)_getHelper(actor);
                        String srcReference = this.getReference((TypedIOPort)port,channelOffset,false,false,myHelper);//"##ref(sinkPort)";
                       
                        
                        args.add(sinkReference);
                        args.add(srcReference);
                        
                        actorDriverCode += _generateBlockCode("updatePort", args);
                        
                    }
                }
            }
            System.out.println("actorDriverCode is now:");
            System.out.println(actorDriverCode);
           // if(outputPortList.size() > 1)
          //  if(actorDriverCode.length() >= 1) // not sure if this is the correct check
            {// for now generate driver methods for all the actors
            ArrayList args = new ArrayList();
            args.add(_generateDriverName((NamedObj) actor));
            
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            
            
            String temp = helper.generateTypeConvertFireCode();
            // this was there originally. Will need to modify the 
            //TypeConvertFireCode method or create method for the actor in this file 
            //to do type conversion and append the port as well
            
            //args.add(actorDriverCode);
           if(temp.length()== 0)
            {
                args.add(actorDriverCode);
            }
            else
            {
                args.add(temp);
                System.out.println("temp was added as the argument to generate block code");
            }
            code.append(_generateBlockCode("driverCode", args));
            }
        }
        System.out.println("about to return :");
        System.out.println(code.toString());
        return code.toString();
    }
     
     private static String _generateDriverName(NamedObj namedObj)
     {
         String name = StringUtilities.sanitizeName(namedObj.getFullName());
         // FIXME: Assume that all objects share the same top level. In this case,
         // having the top level in the generated name does not help to
         // expand the name space but merely lengthen the name string.
         //        NamedObj parent = namedObj.toplevel();
         //        if (namedObj.toplevel() == namedObj) {
         //            return "_toplevel_";
         //        }        
         //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
         if (name.startsWith("_")) {
             name = name.substring(1, name.length());
         }
         return name.replaceAll("\\$", "Dollar")+"_driver";
         
         
         
     }


     private String _generateActorsCode() throws IllegalActionException {
         StringBuffer code = new StringBuffer();
         System.out.println("generateActors Code has been called");
              
          
          for (Actor actor : (List<Actor>) 
                  ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
          
          /*
          Iterator actors = ((CompositeActor) _director.getContainer())
          .deepEntityList().iterator();
                                 
          while (actors.hasNext()) {
              Actor actor = (Actor) actors.next();
              */
              CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
              //Iterators localActors =((CompositeActor)actor.()
              //to do
              // how do you access the actors in a composite actor
              
              String actorFullName = _getActorName(actor);    
              System.out.println("I have an actor named "+actorFullName+" going to generate fireCode for it now.");
     
              code.append(_eol + "void " + actorFullName+ _getFireFunctionArguments() + " {"
                      + _eol);
              //code.append(actorHelper.generateFireFunctionCode());
              code.append(actorHelper.generateFireCode());
              //code.append("$actorSymbol()");
              //code.append("//created by director "+_director.getDisplayName()+_eol);
             code.append("}" + _eol);
              
           }
          return code.toString();
          
         
     
     
     }
     int _getThenIncrementCurrentSharedMemoryAddress()throws IllegalActionException
     {
         currentSharedMemoryAddress += 32;
         if(currentSharedMemoryAddress> 0x405FFFFF)
         {
             throw new IllegalActionException("out of shared data space on PRET");
         }
         return currentSharedMemoryAddress-32;
         
     }

static private int currentSharedMemoryAddress = 0x3F800000;
    
    
}
