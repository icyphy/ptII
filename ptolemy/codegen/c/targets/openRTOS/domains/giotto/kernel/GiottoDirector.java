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
package ptolemy.codegen.c.targets.openRTOS.domains.giotto.kernel;

import java.util.*;


import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.giotto.kernel.GiottoReceiver;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;



////GiottoDirector

/**
 Code generator helper associated with the GiottoDirector class. This class
 is also associated with a code generator.

 @author Shanna-Shaye Forbes, Man-Kit Leung, Ben Lickly
 @version $Id$
 @since Ptolemy II 7.2
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */

public class GiottoDirector extends ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {


    private static int _MAX_PRIORITY_LEVEL = 16;


    /** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);
        System.out.println("GiottoDirector constructor in OpenRTOS target called");
            
    }
    
    public String generateFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        if(_isTopDirectorFSM())
            code.append(_eol+"//should append fire code for Giotto inside fsm"+_eol);
        else
            code.append(_generateFireCode());
        
        return code.toString();
    }
    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    
    /// this is my second hack at this method. Hopefully hit generates what we expect kindof
    public String generateFireFunctionCode() throws IllegalActionException {
        System.out.println("generateFireFunctionCode called from OpenRTOS giotto director***************");
        StringBuffer code = new StringBuffer(" ");//super.generateFireFunctionCode());
         
        return code.toString();
        
        //content moved to _generateActorsCode() which is called in preinitialize b/c code wasn't being generated for 
        //nested actors
           
    
    }

    public Set getHeaderFiles()throws IllegalActionException{
        System.out.println("generateheader files openRTOS giotto director called here");
        
        HashSet files = new HashSet();
        files.add("<stdio.h>");
        return files;
    }

    public String generateInitializeCode() throws IllegalActionException {
        System.out.println("generateInitializeCode from openRTOS giotto director called here");
        
        StringBuffer code = new StringBuffer();
        // to call the c codeblocks in *.c
        code.append(this._generateBlockCode("initLCD"));
    
       HashSet<Integer> frequencies = _getAllFrequencies();
             //  StringBuffer frequencyTCode = new StringBuffer();
        
    
        int currentPriorityLevel = 1;
    
        ArrayList args = new ArrayList();
        
        args.add("");
        args.add("");
        args.add("");
        
        
        args.set(0, "$actorSymbol()_scheduler"); 
        // stack size.
        args.set(1, 100); 
        // priority.
        if(_isTopGiottoDirector())
        {
        args.set(2, _MAX_PRIORITY_LEVEL+1);
        }else{
            args.set(2, _MAX_PRIORITY_LEVEL);  // non top level scheduler so give priority one lower than the highest priority scheduling thread
        }
        code.append(_generateBlockCode("createTask", args));  //create the scheduler thread
    
        for(int frequencyValue : frequencies) {
            
            // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            //frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));
    
            //come back and create handlers for each of these frequency threads
    
            // name for the thread.
            args.set(0, _getThreadName(frequencyValue)); 
            // stack size.
            args.set(1, _getStackSize(_getActors(frequencyValue))); 
            // priority.
            args.set(2, currentPriorityLevel); 
    
            code.append(_generateBlockCode("createTask", args)); // need to figure out how to pass the method names to fire as an argument
           
            ArrayList args1 = new ArrayList();
            args1.add("");
            args1.set(0, _getThreadName(frequencyValue)+"input");
            code.append(_generateBlockCode("createBinarySemaphore",args1));
            args1.set(0, _getThreadName(frequencyValue)+"output");
            code.append(_generateBlockCode("createBinarySemaphore",args1));
            currentPriorityLevel++;
            
            // We did minus 1 because we reserved the max
            // priority level for the scheduling thread.
            currentPriorityLevel %= _MAX_PRIORITY_LEVEL - 1;
        }
        
      /*  for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
            
            List outputPortList = actor.outputPortList();
            //System.out.println("this actor"+actor.getDisplayName()+" has "+inputPortList.size()+" input ports.");
            Iterator outputPorts = outputPortList.iterator();
            while(outputPorts.hasNext())
            {
                IOPort outpt = (IOPort)outputPorts.next();
                int width = outpt.getWidth();
                if(width > 1)
                    code.append("static int"+_getActorName(actor)+"_"+outpt.getName()+"_PORT["+width+"]"+_eol);
                else
                    code.append("static int"+_getActorName(actor)+"_"+outpt.getName()+"_PORT"+_eol);   
           
            }
        }
        */
        code.append(super.generateInitializeCode());
        
        System.out.println("about to return:"+_eol+processCode(code.toString()));
        return processCode(code.toString());   
        
    }
/****
 * Generates C code for the content of the main loop
 * @return String containing the content of the main method
 */

    public String generateMainLoop() throws IllegalActionException{
              StringBuffer code = new StringBuffer();
        System.out.println("generate main loop from openRTOS giotto director called here");
        
        HashSet frequencies= new HashSet();
        int  attributueValue;
        // go through all the actors and get their frequencies
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
            attributueValue =_getFrequency(actor);
    
            frequencies.add(attributueValue);
        }
    
    
    
        //Attribute iterations = _director.getAttribute("iterations");
        code.append( "g_ulSystemClock = SysCtlClockGet();"+_eol);
        if (_isTopGiottoDirector()) {
            code.append("vTaskStartScheduler();"+_eol);
        }
        return code.toString();
    
    }

    /** Generate mode transition code. The mode transition code
     *  generated in this method is executed after each global
     *  iteration, e.g., in HDF model.  Do nothing in this base class.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void generateModeTransitionCode(StringBuffer code)
    throws IllegalActionException {
        System.out.println("I should generate mode transition code here");
    }

    public String generateMyThreads()throws IllegalActionException{
            System.out.println("generateMyThreads called");
            StringBuffer code = new StringBuffer();
            ArrayList args1 = new ArrayList();
            
            double period = _getPeriod();
            String periodString = Double.toString(period);
            int dotIndex = periodString.indexOf('.');
            if(dotIndex == -1){
                // no decimal just multiply by 1000
                periodString = Double.toString(period*1000);
            }
            else{
              char temp[]= periodString.toCharArray();
              int count = 0;
              int i;
              for(i = dotIndex; i < periodString.length()-1; i++){
                  temp[i]=temp[i+1];
                  count++;
              }
              temp[i]='0';  //terminate the string
              count++;
              periodString=new String(temp);
              //System.out.println("before padding with zeros: "+periodString);
              while(count < 3){
                  periodString+='0';
                  count++;
              }
            }
            if(periodString.charAt(0)=='0')  // leading zero so remove it
            {
                periodString = periodString.substring(1,periodString.length());
                
            }
            
            args1.add("");
            //if(_isTopGiottoDirector())
            //{
            args1.set(0,periodString); 
    //        code.append("\\run driver code here"+_eol);
            //code.append(_generateBlockCode("createSchedulerThread", args1));
            //if()
            code.append(generateSchedulerThread(periodString));
            System.out.println("*************just generated the scheduling thread for director: $actorSymbol()_");
            //}
            
            
            int outerActorFrequency = 1;
            if(!_isTopGiottoDirector()){
             //code.append(_eol+"//not the top most Giotto director"+_eol);
             Actor myOuterActor =(TypedCompositeActor)_director.getContainer();
             outerActorFrequency =_getFrequency(myOuterActor);
             //code.append(_eol+"// I think I'm inside actor: "+_director.getContainer().getFullName()+" it has frequency"+outerActorFrequency+_eol);
            }
            
            ArrayList<String> ActorFrequencies[] = _getActorFrequencyTable();
            //code.append("}"+_eol);
         
           // System.out.println("period string is "+periodString);
            code.append("//I should append the frequencethread stuff here"+_eol);
            HashSet frequencies=_getAllFrequencies();
            Object myFrequencies[]=frequencies.toArray();
            Arrays.sort(myFrequencies);
            int i = 0;
           for(int k = 0; k < _getAllFrequencies().size();k++){
               i = (Integer)myFrequencies[k];
            code.append("static void $actorSymbol()_frequency"+i+"(void * pvParameters){"+_eol);
           // code.append("portTickType xLastWakeTime;"+_eol);
            //code.append("int count;"+_eol);
            //code.append("char buff[50];"+_eol);
            //code.append("const portTickType xFrequency =("+ periodString+")/"+i+"/ "+outerActorFrequency+ "/portTICK_RATE_MS;"+_eol);
            //code.append("xLastWakeTime = xTaskGetTickCount();"+_eol);
            //code.append("count = 0;"+_eol);
            code.append("   for(;;){"+_eol);
            //code.append("vTaskDelayUntil(&xLastWakeTime,xFrequency);"+_eol);
            //code.append("count++;"+_eol);
           // code.append("// here I should call driver code method for each of the actors"+_eol);
            //code.append("  //call the methods for the tasks at this frequency of "+ i+_eol);
            //code.append("//sprintf(buff,\"f"+i+"thread %d\",count);"+_eol);
            //code.append("//RIT128x96x4StringDraw(buff, 0,_,15);"+_eol);
            code.append("if(xSemaphoreTake($actorSymbol()_frequency"+i+"input,(portTickType)0)== pdTRUE){"+_eol);
            code.append("if(xSemaphoreGive($actorSymbol()_frequency"+i+"input)){"+_eol);
            code.append("//not able to release input ready semaphore");
            code.append(_eol+"}"+_eol);
            for(int j = 0; j<ActorFrequencies[i].size();j++) {
                //call generate driver code for each of the actors
                code.append(ActorFrequencies[i].get(j)+"();"+_eol);
            }
            code.append("} else{"+_eol);//close
            code.append("//wait until it's avaliable"+_eol);
            code.append("vTaskSuspend(NULL);");
            code.append(_eol+"}"+_eol);  // close else
            code.append(_eol+"}"+_eol);// close the for loop
            if(i < _getAllFrequencies().size())
                code.append("}"+_eol);// close the method loop
     
           }
            
            
            return processCode(code.toString());
        }

    public String generatePostFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        System.out.println("generatePostFireCode from openRTOS giotto director called here");
        
    
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
            //for each of the actors generate postfire code
            //code.append(generatePostFireCode(actor)); 
        }
    
        return code.toString();
    }

    public String generatePreinitializeCode() throws IllegalActionException {
            StringBuffer code = new StringBuffer(super.generatePreinitializeCode());
            // Declare the thread handles.
            System.out.println("generatePreinitializeCode from openRTOS giotto director called here");
            
            code.append("//speed of the processor clock"+_eol);
            code.append(" unsigned long g_ulSystemClock;"+_eol);
            code.append(_generateBlockCode("preinitBlock"));
            HashSet<Integer> frequencies = _getAllFrequencies();
    
            StringBuffer frequencyTCode = new StringBuffer();
    
            ArrayList args = new ArrayList();
            args.add("");
    
            int currentPriorityLevel = 1;
            //if(_isTopGiottoDirector())
            //{
                args.set(0, "$actorSymbol()_scheduler");
                code.append(_generateBlockCode("declareTaskHandle", args));
           // }
            for(int frequencyValue : frequencies) {
                // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
    //            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));
    
               args.set(0, _getThreadName(frequencyValue));
               code.append(_generateBlockCode("declareTaskHandle", args));
               args.set(0, _getThreadName(frequencyValue)+"input");
               code.append(_generateBlockCode("declareSemaphoreHandle",args));
               args.set(0, _getThreadName(frequencyValue)+"output");
               code.append(_generateBlockCode("declareSemaphoreHandle",args));
            }
            
           /* code.append(_eol+"//about to add semaphores for all the output ports"+_eol);
            for (Actor actor : (List<Actor>) 
                    ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
                List outputPortList = actor.outputPortList();
                Iterator outputPorts = outputPortList.iterator();
                while(outputPorts.hasNext())
                {
                    IOPort outpt = (IOPort)outputPorts.next();
                    args.set(0, _getActorName(actor)+"_"+outpt.getName());
                    
                    code.append(_generateBlockCode("declareSemaphoreHandle", args)+_eol);   
                }
                
            }
            */
            
    
            code.append("//driver code should be below here******************"+_eol);
           code.append(_generateOutputDriverCode());
           code.append(_generateDriverCode());
           code.append(_generateActorsCode());
           code.append("// end of generate Preinitialize code here %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
           //code.append("am i the top most director??");
            //System.out.println("I should check to see if I'm the top most Giotto director here.. ");
           
           if(_isTopDirectorFSM()){
               code.append(_eol+"//################# fire code for Giotto stuff here"+_eol);
               code.append(_generateFireCode());
               code.append(_eol+"//end of generate fire code stuff for top director fsm"+_eol);
               }
            return processCode(code.toString());
        }

  
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
    throws IllegalActionException {
      
       if(port.isOutput())
        {
           if(channelAndOffset[0] == "")
           {channelAndOffset[0] = "0";}
        // will need to take care of the case of where the output is for a composite actor
            return CodeGeneratorHelper.generateName(port);
          // return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0];  
        }
 
        else
           
               return super.getReference(port, channelAndOffset, forComposite, isWrite,helper);
    }
    
    public String getDriverReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
    throws IllegalActionException {
      
       if(port.isOutput())
        {
           if(channelAndOffset[0] == "")
           {channelAndOffset[0] = "0";}
        // will need to take care of the case of where the output is for a composite actor
            return CodeGeneratorHelper.generateName(port)+"_PORT";
          // return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0];  
        }
 
        else
           
               return super.getReference(port, channelAndOffset, forComposite, isWrite,helper);
    }

    public String generateSchedulerThread(String period) throws IllegalActionException{
        System.out.println("genereateSchedulerThread called");
    StringBuffer code = new StringBuffer();
    ArrayList<String> ActorFrequencies[] = _getActorFrequencyTable();
    int outerActorFrequency = 1;
    if(!_isTopGiottoDirector()){
     code.append(_eol+"//not the top most Giotto director"+_eol);
     Actor myOuterActor =(TypedCompositeActor)_director.getContainer();
     outerActorFrequency =_getFrequency(myOuterActor);
     code.append(_eol+"// I think I'm inside actor: "+_director.getContainer().getFullName()+" it has frequency"+outerActorFrequency+_eol);
    }
    
    code.append("static void $actorSymbol()_scheduler(void * pvParameters){"+_eol);
    code.append("portTickType xLastWakeTime;"+_eol);
    code.append("int count;"+_eol);
    code.append("int schedTick;"+_eol);
   // code.append("char buff[50];"+_eol);
    
    
    HashSet frequencies=_getAllFrequencies();
    Object myFrequencies[]=frequencies.toArray();
    int intFrequencies[]= new int[_getAllFrequencies().size()];
    for(int l = 0; l < _getAllFrequencies().size(); l++)
    {
        intFrequencies[l] = (Integer)myFrequencies[l];
                
    }
    
    int myLCM = _lcm(intFrequencies);
    System.out.println("The LCM of my frequencies are "+ myLCM);
    
    code.append("const portTickType xFrequency = "+period+"/"+myLCM+"/"+outerActorFrequency+"/portTICK_RATE_MS;"+_eol);
    code.append("xLastWakeTime = xTaskGetTickCount();"+_eol);
    code.append("count = 0;"+_eol);
    code.append("schedTick = 0;"+_eol);
    code.append("    for(;;){"+_eol);
    code.append("     vTaskDelayUntil(&xLastWakeTime,xFrequency);"+_eol);
    code.append("count++;"+_eol);
    
   // code.append("//sprintf(buff,\"sc:%d\",count);"+_eol);
    //code.append("//RIT128x96x4StringDraw(buff, _,40,25);"+_eol);
    
    
    Arrays.sort(myFrequencies);
    
    
    int i = 0;
   for(int k = 0; k < _getAllFrequencies().size();k++){
       i = (Integer)myFrequencies[k];
           code.append("if( schedTick %"+(myLCM/i)+" == 0){"+_eol);
          
           code.append("if(xSemaphoreTake($actorSymbol()_frequency"+i+"output,(portTickType)0) ==  pdTRUE){"+_eol);
          // code.append();
           
           
       for(int j = 0; j<ActorFrequencies[i].size();j++) {
           //call generate driver code for each of the actors    
           code.append(ActorFrequencies[i].get(j)+"_driver_out();"+_eol);
       }
       
       for(int j = 0; j<ActorFrequencies[i].size();j++) {
           //call generate driver code for each of the actors
           code.append(ActorFrequencies[i].get(j)+"_driver();"+_eol);
       }
       code.append("if(xSemaphoreGive($actorSymbol()_frequency"+i+"input)){"+_eol);
       code.append("}"+_eol);
       code.append("vTaskResume("+ _getThreadName(i)+"_task);"+_eol);
       code.append("} else{"+_eol);// close if got semaphore
       code.append("//there was an overrun");
       code.append(_eol+"}"+_eol);
       code.append("}"+_eol);
   }
   code.append("schedTick++;"+_eol); 
   code.append("if(schedTick == "+(myLCM-1)+") {"+_eol);
   code.append("schedTick = 0;"+_eol);
   code.append("}"+_eol);
  
   
    code.append("    }"+_eol);
    code.append("      }"+_eol);
           
    return processCode(code.toString());
    }
    
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
    throws IllegalActionException {
        System.out.println("//generate transferInputsCode inside OpenRTOS Giotto director called.");
    code.append("//generate transferInputsCode inside OpenRTOS Giotto director called."+_eol);
    //super.generateTransferInputsCode(inputPort, code);
    
    }

    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
    throws IllegalActionException {
        System.out.println("//generate transferOutputsCode inside OpenRTOS Giotto director called.");
    code.append(_eol+"//generate transferOutputsCode inside OpenRTOS Giotto director called."+_eol);
    //super.generateTransferOutputsCode(outputPort, code);
    
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
           if((actor instanceof CompositeActor)!= true )
           {
              List <IOPort> actorPorts = actor.outputPortList();
              if(actorPorts.size()> 0)
              {
               variableDeclarations.append(_eol+"//not composite actor so may need extra variable, later, make sure you check the type. It's default to static int at the moment"+_eol);
               variableDeclarations.append("static int "+_getActorName(actor)+"_output;"+_eol);
              }
              
           }
           variableDeclarations.append(_generatePortVariableDeclarations(actor));
         //  variableDeclarations.append(_generateVariableDeclarations(actor));
           variableDeclarations.append("//############end variables for this actor: "+actor.getFullName()+_eol);
            //variableDeclarations.append(_generatePortVariableDeclarations(actor));
           //no need to create port variables if the normal output variables are read at the right time... they can be ports.. this way you only need two variables for double buffering
         }
        
       return variableDeclarations.toString();
    }

    private String _generateVariableDeclarations(Actor actor) {
        // TODO Auto-generated method stub
        StringBuffer code= new StringBuffer();
        // input variables
        // output variables
        
        return code.toString();
        
    }

    protected String _generateBlockCode(String blockName, List args)
    throws IllegalActionException {
        return _codeStream.getCodeBlock(blockName, args);        
    }

    private String _generateFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        //code.append("//fire code should be here. I'm from the openRTOS GiottoDirector "+_eol);
        System.out.println("generateFireCode from openRTOS giotto director called here");
        code.append("//Beginning of generateFireCode inside OpenRTOS GiottoDirector***************");
        //code.append("scheduler()");
        if(!_isTopDirectorFSM()){
            //top director isn't fsm so close the method that would normally contain the fire code 
            //if the inline option was enabled and being used. Inline is invalid for giotto codegen
            code.append(_eol+"}"+_eol);
        }
        //create thread methods here
        code.append("//before calling generateMyThreads***************"+_eol);
        code.append(generateMyThreads());
        code.append("//after calling generateMyThreads***************"+_eol);
        code.append("//End of generateFireCode inside OpenRTOS GiottoDirector***************"+_eol);
        
        if(_isTopDirectorFSM()){
            //insert a close parenthesis b/c it's not done automatically in the code
            code.append(_eol+"}"+_eol);
        }
        
        
        
        return code.toString();
    }

    /** Generate input variable declarations.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected String _generateInputVariableDeclaration()
            throws IllegalActionException {
        System.out.println("_generateInputVariableDeclaration called form OpenRTOS Giotto Director");
        boolean dynamicReferencesAllowed = ((BooleanToken) _codeGenerator.allowDynamicMultiportReference
                .getToken()).booleanValue();
    
        StringBuffer code = new StringBuffer();
    
        Iterator inputPorts = ((Actor) getComponent()).inputPortList()
                .iterator();
    
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
    
            if (!inputPort.isOutsideConnected()) {
                continue;
            }
    
            code.append("static " + targetType(inputPort.getType()) + " "
                    + generateName(inputPort));
    
            int bufferSize = getBufferSize(inputPort);
            if (inputPort.isMultiport()) {
                code.append("[" + inputPort.getWidth() + "]");
                if (bufferSize > 1 || dynamicReferencesAllowed) {
                    code.append("[" + bufferSize + "]");
                }
            } else {
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
            }
    
            code.append(";" + _eol);
        }
    
        return code.toString();
    }

    /** Generate output variable declarations.
     *  @return a String that declares output variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected String _generateOutputVariableDeclaration()
            throws IllegalActionException {
        System.out.println("_gneerateOutputVariableDeclaration called form OpenRTOS Giotto Director");
        StringBuffer code = new StringBuffer();
    
        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();
    
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
    
            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if (!outputPort.isOutsideConnected() || outputPort.isInsideConnected()) {
                code.append("static " + targetType(outputPort.getType()) + " "
                        + generateName(outputPort));
    
                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }
    
                int bufferSize = getBufferSize(outputPort);
    
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
        }
    
        return code.toString();
    }

    private String _generateActorsCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        System.out.println("generateActors Code has been called");
         
         for (Actor actor : (List<Actor>) 
                 ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
   
             CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                        
             String actorFullName = _getActorName(actor);    
             System.out.println("I have an actor named "+actorFullName+" going to generate fireCode for it now.");
    
             code.append(_eol + "void " + actorFullName+ _getFireFunctionArguments() + " {"
                     + _eol);
             code.append("//about to check to see if it's a composite actor"+_eol);
             code.append(_eol+"//my director's name is: "+actor.getDirector().getClassName());
                   //code.append();
             // take the semaphores connected to your output ports
             code.append("// I should take the semaphores for my output ports here"+_eol);
                                    
             if(actor instanceof CompositeActor&&!(actor.getDirector().getClassName()=="ptolemy.domains.fsm.kernel.FSMDirector")) {
                 System.out.println("composite actor: "+actor.getFullName()+" so doing stuff for that from actor code");
                code.append("//this is where I should move stuff to my output ports as a composite actor"+_eol); 
                  //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                 List<IOPort> myOutputs = actor.outputPortList();
                 System.out.println("I have "+myOutputs.size()+" port(s) to send info to");
                 Iterator myItr = myOutputs.iterator();
                 IOPort port;
                 CodeGeneratorHelper myHelper;
                 String srcReference;
                 String sinkReference;
                 int i = 0; //sink index counter
                 int j = 0; // src index counter
                 while(myItr.hasNext())
                 {
                     port = (IOPort)myItr.next();
                     List connectToMe = port.insideSourcePortList();//port.insidePortList();//port.deepInsidePortList();   and port.insidePortList() lists adder plus , as well as adder output
                   
                    //code.append("port: "+port.getFullName()+" has source(s)"+_eol);
               
                     Iterator tome= connectToMe.iterator();
                     while(tome.hasNext())
                     {
                         IOPort tempp = (IOPort)tome.next();
                                                
                         // port is sink
                         //tempp is source
                         
                        // System.out.println(" j is "+j +"and size of connect to me is "+connectToMe.size());
                         String channelOffset [] = {"0","0"};
                       
                           System.out.println("the sender port is named "+tempp.getFullName()+" and the reciever is "+port.getFullName());
                           myHelper = (CodeGeneratorHelper)this._getHelper(tempp.getContainer());
                          // temp+= _generateTypeConvertFireCode(false)+_eol;
                         channelOffset[0] = Integer.valueOf(i).toString();
                         System.out.println("channel offset is "+channelOffset[0]);
                         srcReference = this.getReference((TypedIOPort)tempp,channelOffset,false,true,myHelper);
                         System.out.println("after first call to getReference");
                                               
                         myHelper = (CodeGeneratorHelper)_getHelper(actor);
                         channelOffset[0] = Integer.valueOf(j).toString();
                         System.out.println("channel offset is "+channelOffset[0]);
                         sinkReference = this.getReference((TypedIOPort)port,channelOffset,false,true,myHelper);
                         System.out.println("after second call to getReference");
                         j++;
                           
                          // temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol;                 
                         System.out.println("I think the source Reference is "+srcReference+" and it's display name is "+tempp.getDisplayName());
                         System.out.println("I think the sink Reference is "+sinkReference+" and it's display name is "+port.getDisplayName());
              
                         ArrayList args = new ArrayList();    
                         args.add(sinkReference);
                         args.add(srcReference);
                            
                         code.append(_generateBlockCode("updatePort", args)+_eol);
                         
                         
                     }
                     i++;  // not sure if this is the correct place to increment i
                     
                 

                 }
                 code.append(_eol+"//done with the transfer out for this composite actor"+_eol);
               System.out.println("done with the transfer out for this composite actor");
             } else{
                 System.out.println("not composite actor");
             }
            System.out.println("about to call generateFirecode on the composite actor"); 
            code.append(_eol+"//about to call generateFireCode on the actorHelper");
           code.append(_eol+actorHelper.generateFireCode());
             System.out.println("after calling the generateFireCode on composite actor");
             /*if(actor instanceof CompositeActor&&(actor.getDirector().getClassName()=="ptolemy.domains.fsm.kernel.FSMDirector")){
             //transfer outputs to modal model out
                // I think you may be able to limit it to just teh first port, the modal model controller out. 
                code.append("//this is where I should move stuff to my output ports as a composite actor"+_eol); 
                   //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                  List<IOPort> myOutputs = actor.outputPortList();
                  System.out.println("I have "+myOutputs.size()+" port(s) to send info to");
                  Iterator myItr = myOutputs.iterator();
                  IOPort port;
                  CodeGeneratorHelper myHelper;
                  String srcReference;
                  String sinkReference;
                  int i = 0; //sink index counter
                  int j = 0; // src index counter
                  while(myItr.hasNext())
                  {
                      port = (IOPort)myItr.next();
                      List connectToMe = port.insideSourcePortList();//port.insidePortList();//port.deepInsidePortList();   and port.insidePortList() lists adder plus , as well as adder output
                    
                     //code.append("port: "+port.getFullName()+" has source(s)"+_eol);
                
                      Iterator tome= connectToMe.iterator();
                      while(tome.hasNext())
                      {
                          IOPort tempp = (IOPort)tome.next();
                                                 
                          // port is sink
                          //tempp is source
                          
                         // System.out.println(" j is "+j +"and size of connect to me is "+connectToMe.size());
                          String channelOffset [] = {"0","0"};
                        
                            System.out.println("the sender port is named "+tempp.getFullName()+" and the reciever is "+port.getFullName());
                            myHelper = (CodeGeneratorHelper)this._getHelper(tempp.getContainer());
                           // temp+= _generateTypeConvertFireCode(false)+_eol;
                          channelOffset[0] = Integer.valueOf(i).toString();
                          System.out.println("channel offset is "+channelOffset[0]);
                          srcReference = this.getReference((TypedIOPort)tempp,channelOffset,false,true,myHelper);
                          System.out.println("after first call to getReference");
                                                
                          myHelper = (CodeGeneratorHelper)_getHelper(actor);
                          channelOffset[0] = Integer.valueOf(j).toString();
                          System.out.println("channel offset is "+channelOffset[0]);
                          sinkReference = this.getReference((TypedIOPort)port,channelOffset,false,true,myHelper);
                          System.out.println("after second call to getReference");
                          j++;
                            
                           // temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol;                 
                          System.out.println("I think the source Reference is "+srcReference+" and it's display name is "+tempp.getDisplayName());
                          System.out.println("I think the sink Reference is "+sinkReference+" and it's display name is "+port.getDisplayName());
               
                          ArrayList args = new ArrayList();    
                          args.add(sinkReference);
                          args.add(srcReference);
                             
                          code.append(_generateBlockCode("updatePort", args)+_eol);
                          
                          
                      }
                      i++;  // not sure if this is the correct place to increment i
                      
                  

                  }
                  code.append(_eol+"//done with the transfer out for this composite actor"+_eol);
                System.out.println("done with the transfer out for this composite actor");
                             
             //end transfer modal model outputs out    
             }*/
        
            code.append("}" + _eol);
             
         }
         System.out.println("returning: "+_eol+code.toString());
         return code.toString();
         
        
    
    
    }

    /** Generate sanitized name for the given named object. Remove all
     *  underscores to avoid conflicts with systems functions.
     *  @param namedObj The named object for which the name is generated.
     *  @return The sanitized name.
     */
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
    /** Generate the content of  driver methods. For each actor update it's inputs to the 
     *  outputs stored in ports. The PORT allows double buffering, in this case the output
     *  variable is used as the port. PORT here is simply a common variable, not a PORT in 
     *  the general Ptolemy II actor sense
     *  
     *  NOTE: Duplicate ports connected through a fork are removed. IE. if an input is connected to a fork
     *  and the fork is connected to two other places... it removes the first place from the list of places and keeps the last place
     *  need to ask Jackie if there is a way to work around this b/c Reciever [][] recievers = getRecievers doesn't work.
     *  @param none
     *  @return code that copies inputs from a port in a driver method
     */ 
    String _generateDriverCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        System.out.println("generateDriver Code has been called");
        
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
    
            List inputPortList = actor.inputPortList();
            System.out.println("this actor"+actor.getDisplayName()+" has "+inputPortList.size()+" input ports.");
            Iterator inputPorts = inputPortList.iterator();
    
            String actorDriverCode = "";
            String sinkReference = "";
            String srcReference = "";
            String temp = "";
            StringBuffer transferIn= new StringBuffer();
            StringBuffer transferOut=new StringBuffer();
            String output="";
            int i = 0; //sink index counter
            int j = 0; // src index counter
            CodeGeneratorHelper myHelper;
            
            
           
            
            
            while (inputPorts.hasNext()) {
                i = 0;  // this is a test to see if this is to be done here, if so remove the i++ from the end of the loop
                j = 0;
                 IOPort port = (IOPort)inputPorts.next();
                 System.out.println("this port's name is "+port.getFullName());
                 Receiver[][] channelArray = port.getReceivers();
                // port.
                List<IOPort> cip = port.sourcePortList();
                if(cip.size()>0)
                {
                    System.out.println("sourcePortList contains: ");
                    Iterator tome2 =cip.iterator();
                    while(tome2.hasNext()){
                        IOPort tempp = (IOPort)tome2.next();
                      System.out.print(tempp.getFullName()+" ");  
            
                   }
                    System.out.println(" ");
                }
                
                 
                List<IOPort> connectedPorts = port.deepConnectedOutPortList();
                List<IOPort> connectToMe = port.sourcePortList();//port.connectedPortList(); //connectedPortList();
                System.out.println("connectToMe size is "+connectToMe.size());
                //System.out.println("before remove double connections");
          
                Iterator tome= connectToMe.iterator();
              System.out.println("currently connectToMe size is "+connectToMe.size());
                
                tome= connectToMe.iterator();
                while(tome.hasNext())
                {
                    IOPort tempp = (IOPort)tome.next();
                    System.out.println("******I'm connected to I think: "+tempp.getFullName());  
                }
                 
                // Iterator cpIterator = connectedPorts.iterator();
                Iterator cpIterator = connectToMe.iterator();
                 while(cpIterator.hasNext()){//&&(j <connectToMe.size()-1)){
                   IOPort sourcePort = (IOPort)cpIterator.next();
                // FIXME: figure out the channel number for the sourcePort.
                   // if you need to transfer inputs inside
                   if(actor instanceof CompositeActor) {
                      System.out.println("composite actor so doing stuff for that");
                       //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                       //_generateTransferInputsCode(port, transferIn);
                       transferIn.append(("//should transfer input for this actor to from the outside to inside"+_eol));
                       //generateTransferInputsCode(inputPort, code);
                       
                   }
                
                   System.out.println(" j is "+j +"and size of connect to me is "+connectToMe.size());
                   String channelOffset [] = {"0","0"};
                   
                   System.out.println("the sender port is named "+sourcePort.getFullName()+" and the reciever is "+port.getFullName());
                     myHelper = (CodeGeneratorHelper)this._getHelper(sourcePort.getContainer());
                    // temp+= _generateTypeConvertFireCode(false)+_eol;
                   channelOffset[0] = Integer.valueOf(i).toString();
                   System.out.println("channel offset is "+channelOffset[0]);
                     srcReference = this.getDriverReference((TypedIOPort)sourcePort,channelOffset,false,true,myHelper);
                     System.out.println("after first call to getReference");
                                         
                     myHelper = (CodeGeneratorHelper)_getHelper(actor);
                     channelOffset[0] = Integer.valueOf(j).toString();
                     System.out.println("channel offset is "+channelOffset[0]);
                     sinkReference = this.getReference((TypedIOPort)port,channelOffset,false,true,myHelper);
                     System.out.println("after second call to getReference");
                     j++;
                     
                     temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol;                 
                     System.out.println("I think the source Reference is "+srcReference+" and it's display name is "+sourcePort.getDisplayName());
                     System.out.println("I think the sink Reference is "+sinkReference+" and it's display name is "+port.getDisplayName());
        
                     ArrayList args = new ArrayList();    
                      args.add(sinkReference);
                      args.add(srcReference);
                      
                     
                      actorDriverCode += _generateBlockCode("updatePort", args);
                     }
                 
                 if(actor instanceof CompositeActor) {
                   // It is not necessary to generate transfer out code, 
                     //since the fanout actor drivers will read the necessary values from the ports                    
                     //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                     //directorHelper._generateTransferOutputsCode(port, transferOut);
                     
                     //generateTransferInputsCode(inputPort, code);
                     //transferOut.append("//should transfer input for this actor to from inside to outside"+_eol);
                 }
                 
             i++; // increment the ofset variable // not sure if this is correct since we're using iterators 
            } 
            
            System.out.println("actorDriverCode is now:");
            System.out.println(actorDriverCode);
            
           
            ArrayList args = new ArrayList();  
            args.add(_generateDriverName((NamedObj) actor));
           if(temp.length() == 0)   // if no type conversion is necessary
            output=transferIn.toString()+actorDriverCode+transferOut.toString();
           else
               output=transferIn.toString()+temp+transferOut.toString();
          
           args.add(output);
            code.append(_generateBlockCode("driverCode", args));
            }
        
        System.out.println("about to return :");
        System.out.println(code.toString());
        return code.toString();
    }
     
     /** Generate the content of output driver methods. The output driver updates the value of a port to be that of the
      *  output of the latest execution of a task.
      *  
      *  NOTE: Duplicate ports connected through a fork are removed. IE. if an input is connected to a fork
      *  and the fork is connected to two other places... it removes the first place from the list of places and keeps the last place
      *  need to ask Jackie if there is a way to work around this b/c Reciever [][] recievers = getRecievers doesn't work.
      *  @param none
      *  @return code that copies outputs to a port
      */ String _generateOutputDriverCode() throws IllegalActionException {
         StringBuffer code = new StringBuffer();
         System.out.println("generateDriver Code has been called");
         String sinkReference;
         String srcReference;
         String actorDriverCode=" ";
         CodeGeneratorHelper myHelper;
         
         for (Actor actor : (List<Actor>) 
                 ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
     
     
             List outputPortList = actor.outputPortList();
             Iterator outputPorts = outputPortList.iterator();
             sinkReference= "";
             srcReference= "";
             actorDriverCode ="";
            while(outputPorts.hasNext())
            {
                    IOPort sourcePort = (IOPort)outputPorts.next();
                 // FIXME: figure out the channel number for the sourcePort.
                    // if you need to transfer inputs inside
                   String channelOffset [] = {"0","0"};
                   int i =sourcePort.getWidth();
                   myHelper = (CodeGeneratorHelper)this._getHelper(sourcePort.getContainer());
                   if(i > 1)
                   {
                       for(int j = 0; j< i; j++)
                       {
                        channelOffset[0] = Integer.valueOf(j).toString();
                        sinkReference = this.getReference((TypedIOPort)sourcePort,channelOffset,false,true,myHelper);
                        srcReference = sinkReference.replace( ((Integer)j).toString(), "PORT_"+j);
                        ArrayList args = new ArrayList();    
                        args.add(sinkReference);
                        args.add(srcReference);
                        actorDriverCode += _generateBlockCode("updatePort", args);
                       }
                   }else{
                     channelOffset[0] = "0";
                     srcReference = this.getReference((TypedIOPort)sourcePort,channelOffset,false,true,myHelper);
                     sinkReference = srcReference+"_PORT";
                     ArrayList args = new ArrayList();    
                     args.add(sinkReference);
                     args.add(srcReference);
                     actorDriverCode += _generateBlockCode("updatePort", args);     
                   }
            }
             
             System.out.println("actorDriverCode is now:");
             System.out.println(actorDriverCode);
             
            
             ArrayList args = new ArrayList();  
             args.add(_generateDriverName((NamedObj) actor)+"_out");
             args.add(actorDriverCode);
             code.append(_generateBlockCode("driverCode", args));
             }
         
         System.out.println("about to return :");
         System.out.println(code.toString());
         return code.toString();
     }

     
     private void _generateTransferInputsCode(IOPort source,StringBuffer code) throws IllegalActionException
    {
       //super.generateTransferInputsCode(source, code);
        
    }

    private void _generateTransferOutputsCode(IOPort source,StringBuffer code) throws IllegalActionException
    {
       super.generateTransferOutputsCode(source, code);
        
    }

    /**
     * This function simply overwrites the base class function
     * @exception IllegalActionException Not thrown in this base class.
     */
      public String _generateTypeConvertFireCode(IOPort source,IOPort sink)
        throws IllegalActionException {
          StringBuffer code = new StringBuffer();
          System.out.println("generateTypeConvertFireCode in OpenRTOS giotto director called");
          return code.toString();
       }

    /** Generate PORT variables. A PORT allows control over the value read
      *  A port is an efficient way to handle double buffering
      *  @param none
      *  @return port variables
      */
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
               code.append("static " + targetType(outputPort.getType()) + " "
                       + generateName(outputPort)+"_PORT");
    
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

    private   ArrayList<String>[] _getActorFrequencyTable()throws IllegalActionException{
        System.out.println("getActorFrequencyTable called");
    
        HashSet<Integer> allFrequncies = _getAllFrequencies();
        int frequencyCount= allFrequncies.size();
        //ArrayList<String> actorsPerFrequencey[]= new ArrayList<String>(100);
        // ArrayList<String> actorNamesPerFrequency[] =  new ArrayList[frequencyCount+1];
      
         Object theseFrequencies[]= allFrequncies.toArray();
         int maxfrequency = (Integer)theseFrequencies[0];
         int it = 0;
         while(it < frequencyCount)
         {
             
             if( (Integer)theseFrequencies[it] > maxfrequency)
                 maxfrequency = (Integer)theseFrequencies[it];
            System.out.println("fequency value: "+ theseFrequencies[it]); 
             it++;
         }
         
         frequencyCount = maxfrequency;
         ArrayList<String> actorNamesPerFrequency[] =  new ArrayList[frequencyCount+1];
         for( int i=0; i<frequencyCount+1;i++)
         {
             actorNamesPerFrequency[i]=new ArrayList<String>();
             
         }
         
        System.out.println("table has size "+actorNamesPerFrequency.length);
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
          
           String temp = _getActorName(actor);
            System.out.println("actor "+temp+" has frequency :"+_getFrequency(actor)+"frequency count is: "+frequencyCount);
            actorNamesPerFrequency[_getFrequency(actor)].add(temp);
                     
          }    
        
        return actorNamesPerFrequency;
    }

    private List<Actor> _getActors(int frequencyValue) {
        ArrayList<Actor> actors = new ArrayList<Actor>();
        // TODO:
        //should call _getActorFrequencyTable() and return the correct set of actors
        return actors;
    }

    private String _getActorName(Actor actor) {
        String actorFullName = actor.getFullName();
         actorFullName = actorFullName.substring(1,actorFullName.length());
         actorFullName = actorFullName.replace('.', '_');
         actorFullName = actorFullName.replace(' ', '_');
        return actorFullName;
    }

    /**
     * Generate the thread function name for a given actor.
     * 
     * @param actor
     *            The given actor.
     * @return A unique label for the actor thread function.
     */
    private String _getActorThreadLabel(Actor actor) {
            return CodeGeneratorHelper.generateName((NamedObj) actor)
                            + "_ThreadFunction";
    }

    private HashSet<Integer> _getAllFrequencies() throws IllegalActionException {
        HashSet frequencies= new HashSet();
        int i = 0;
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
            int attributueValue =_getFrequency(actor);
            i++;
            frequencies.add(attributueValue);
    
            //come back and figure out how to keep a list of all 
            //the actors at each of the frequencies so you can call the right
            //methods from the threads
            // frequencies.add(_getFrequency(actor));
        }
        
        return frequencies;
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
        // Ben I'm not sure why you originally called get frequency here
        int frequency = _getFrequency((Actor)_director.getContainer());
        return ((GiottoDirector) _getHelper(director))._getPeriod() / frequency;
    
    }

    private int _getStackSize(List<Actor> actors) {
        // FIXME: there may be static analysis in the future.
        // However, we are hard-coding this number for now.
        return 100;
    }

    private String _getThreadName(int frequencyValue) {
        return "$actorSymbol(frequency)" + frequencyValue;
    }
    
       
    private boolean _isConnected(IOPort portA, IOPort portB) {
        // TODO Auto-generated method stub
        boolean toReturn = false;
        List<IOPort> portAPorts = portA.connectedPortList();
        List<IOPort> portBPorts = portB.connectedPortList();
        String name;
        Iterator portItr = portAPorts.iterator();
        System.out.print(portA.getFullName()+" is connected to : ");
        while(portItr.hasNext()){
            
            name = ((IOPort)portItr.next()).getFullName();
            System.out.print(name+" ");
            if(name.equals(portB.getFullName()))
            {
                toReturn = true;
                break;
            }
        }
        if(toReturn == false)
        {
            System.out.println(" ");
            
            portItr = portBPorts.iterator();
            
            System.out.print(portB.getFullName()+" is connected to : ");
            while(portItr.hasNext()){
                
                name = ((IOPort)portItr.next()).getFullName();
                System.out.print(name+" ");
                if(name.equals(portA.getFullName()))
                {
                    toReturn = true;
                    break;
                }
              }
            
            
            
        }
        System.out.println(" ");
        System.out.println("PORTS "+portA.getFullName()+" "+portB.getFullName()+" are "+toReturn+" connected");
        return toReturn;
    }

    private boolean _isTopGiottoDirector() {
           Director director = ((TypedCompositeActor)
                _director.getContainer()).getExecutiveDirector();
    
           if (director == null) { // true for the top most director
               
            return true;  
           }
                         
       return false;
    }  
       
       
    private boolean _isTopDirectorFSM()
    {
        boolean returnValue = false;
        
        
        
        Director director = ((TypedCompositeActor)
                _director.getContainer()).getExecutiveDirector();
    
        if(director != null &&
                (director instanceof ptolemy.domains.fsm.kernel.FSMDirector)) {
           returnValue = true;
           }
            
        
        return returnValue;
        
        
    }

    private List<IOPort> _removeDoubleConnections(List<IOPort> connectToMe) {
        List<IOPort> result= new ArrayList();
        IOPort ptA;
        IOPort ptB;
        int j = 0;
        int i = 0;
        boolean isConnected = false;
        Iterator lIterator = connectToMe.iterator();
        while(lIterator.hasNext()){
            ptA = (IOPort)lIterator.next();
            i++;
            j=i;
            isConnected = false;
            while(j< connectToMe.size()){
                ptB= connectToMe.get(j);
                isConnected = _isConnected(ptA,ptB);
                if(isConnected)
                { break;}
                else
                {j++;}
            }
            if(!isConnected)
            {result.add(ptA);}
        }
        
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
     //// this piece of code was copied from the Giotto Scheduler method
    private int _lcm(int[] array) {
        int count = array.length;
    
        if (count < 1) {
            throw new RuntimeException(
                    "Length array passed to _lcm() is less than 1?");
        }
    
        int X = array[0];
        int i = 0;
    
        while (true) {
            if ((X % array[i]) == 0) {
                if (i >= (count - 1)) {
                    break;
                }
    
                i++;
            } else {
                X = X + 1;
                i = 0;
            }
        }
    
        return X;
    }
 
    
    
}