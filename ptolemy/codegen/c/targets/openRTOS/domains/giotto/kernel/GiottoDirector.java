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
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;



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

//at the moment I'm not sure exactly what should go in this specific implementation. It will be filled out as the semester progresses.
public class GiottoDirector extends ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {


    private static int _MAX_PRIORITY_LEVEL = 16;


    /** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);
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
            currentPriorityLevel++;
            
            // We did minus 1 because we reserved the max
            // priority level for the scheduling thread.
            currentPriorityLevel %= _MAX_PRIORITY_LEVEL - 1;
        }
     
        code.append(super.generateInitializeCode());
      
        return processCode(code.toString());   
        
    }
    public String generateSchedulerThread(String period) throws IllegalActionException{
    StringBuffer code = new StringBuffer();
    
    code.append("static void scheduler(void * pvParameters){"+_eol);
    code.append("portTickType xLastWakeTime;"+_eol);
    code.append("const portTickType xFrequency = "+period+"/portTICK_RATE_MS;"+_eol);
    code.append("xLastWakeTime = xTaskGetTickCount();"+_eol);
    code.append("    for(;;){"+_eol);
    code.append("     vTaskDelayUntil(&xLastWakeTime,xFrequency);"+_eol);
    code.append("//run driver code here"+_eol);
    //sync outputs to ports, sync inputs to ports
    code.append("//handle updates, mode switches, and switching the double buffer pointers"+_eol);
    code.append("    }"+_eol);
    code.append("      }"+_eol);
           
    return processCode(code.toString());
    }
    
    public String generateMyThreads()throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        ArrayList args1 = new ArrayList();
        
        double period = _getPeriod();
        String periodString =Double.toString(period);
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
        code.append(generateDriverCode());
        
        args1.add("");
        if(_isTopGiottoDirector())
        {
        args1.set(0,periodString); 
//        code.append("\\run driver code here"+_eol);
        //code.append(_generateBlockCode("createSchedulerThread", args1));
        code.append(generateSchedulerThread(periodString));
        }
        
        ArrayList<String> ActorFrequencies[] = _getActorFrequencyTable();
        //code.append("}"+_eol);
     
       // System.out.println("period string is "+periodString);
        code.append("//I should append the frequencethread stuff here"+_eol);
       for(int i = 1; i <= _getAllFrequencies().size();i++){
        code.append("static void $actorSymbol()_frequency"+i+"(void * pvParameters){"+_eol);
        code.append("portTickType xLastWakeTime;"+_eol);
        code.append("const portTickType xFrequency =("+ periodString+")/"+i+ "/portTICK_RATE_MS;"+_eol);
        code.append("xLastWakeTime = xTaskGetTickCount();"+_eol);
        code.append("   for(;;){"+_eol);
        code.append("vTaskDelayUntil(&xLastWakeTime,xFrequency);"+_eol);
        code.append("// here I should call generate driver code method for each of the actors"+_eol);
        code.append("  //call the methods for the tasks at this frequency of "+ i+_eol);
        for(int j = 0; j<ActorFrequencies[i].size();j++)
        {
            //call generate driver code for each of the actors
            code.append(ActorFrequencies[i].get(j)+"();"+_eol);
        }
        
        
        code.append("}"+_eol);// close the for loop
        if(i < _getAllFrequencies().size())
            code.append("}"+_eol);// close the method loop
 
    }
        
        
        return processCode(code.toString());
    }
    
       
    ArrayList<String>[] _getActorFrequencyTable()throws IllegalActionException{
        //Hashtable<Integer,String> actorsPerFrequency= new Hashtable<Integer,String>();
        //actorsPerFrequency.
        int frequencyCount=_getAllFrequencies().size();
        //ArrayList<String> actorsPerFrequencey[]= new ArrayList<String>(100);
         ArrayList<String> actorNamesPerFrequency[] =  new ArrayList[frequencyCount+1];
         for( int i=0; i<frequencyCount+1;i++)
         {
             actorNamesPerFrequency[i]=new ArrayList<String>();
             
         }

        
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
          
           // actorNamesPerFrequency[_getFrequency(actor)].add(actor.getDisplayName());
            String temp =actor.getFullName();
            temp=temp.substring(1,temp.length());
            temp=temp.replace('.', '_');
                
            actorNamesPerFrequency[_getFrequency(actor)].add(temp);
          
            
          }     
        return actorNamesPerFrequency;
    }

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
    
    /*
        Iterator frequencyIterator = frequencies.iterator();
    
        int frequencyValue;
    
        int currentPriorityLevel = 1;
        for(int i = 0; i < frequencies.size();i++)
        {// assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            frequencyValue = (Integer)frequencyIterator.next();
           // frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));
    
            //come back and create handlers for each of these frequency threads
            //code.append("xTaskHandle "+"frequency"+frequencyValue+"handle;"+_eol);
            //tempcode.append("xTaskCreate(frequency"+frequencyValue+",\"frequency"+frequencyValue+"\" "+",100,NULL,"+currentPriorityLevel+",frequency"+frequencyValue+"handle);"+_eol);
            currentPriorityLevel++;
            currentPriorityLevel %=9;
    
        }
    */
       // code.append(tempcode);
    
    
        //Attribute iterations = _director.getAttribute("iterations");
       
        if (_isTopGiottoDirector()) {
            code.append("vTaskStartScheduler();"+_eol);
        }
        return code.toString();
    
    }

    public Set getHeaderFiles()throws IllegalActionException{
        System.out.println("generateheader files openRTOS giotto director called here");
        
        HashSet files = new HashSet();
        files.add("<stdio.h>");
        return files;
    }

    private int _getStackSize(List<Actor> actors) {
        // FIXME: there may be static analysis in the future.
        // However, we are hard-coding this number for now.
        return 100;
    }

    private List<Actor> _getActors(int frequencyValue) {
        ArrayList<Actor> actors = new ArrayList<Actor>();
        // TODO:
        //should call _getActorFrequencyTable() and return the correct set of actors
        return actors;
    }

    private String _getThreadName(int frequencyValue) {
        return "$actorSymbol(frequency)" + frequencyValue;
    }
    
       
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());
        // Declare the thread handles.
        System.out.println("generatePreinitializeCode from openRTOS giotto director called here");
        
        code.append(_generateBlockCode("preinitBlock"));
        HashSet<Integer> frequencies = _getAllFrequencies();

        StringBuffer frequencyTCode = new StringBuffer();

        ArrayList args = new ArrayList();
        args.add("");

        int currentPriorityLevel = 1;
        if(_isTopGiottoDirector())
        {
            args.set(0, "scheduler");
            code.append(_generateBlockCode("declareTaskHandle", args));
        }
        for(int frequencyValue : frequencies) {
            // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));

            args.set(0, _getThreadName(frequencyValue));
            code.append(_generateBlockCode("declareTaskHandle", args));
        }
       
        return processCode(code.toString());
    }
    
    protected String _generateBlockCode(String blockName, List args)
    throws IllegalActionException {
        return _codeStream.getCodeBlock(blockName, args);        
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

    private boolean _isTopGiottoDirector() {
        // TODO Auto-generated method stub
        return true;
    }
    

    public String generateFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        //code.append("//fire code should be here. I'm from the openRTOS GiottoDirector "+_eol);
        System.out.println("generateFireCode from openRTOS giotto director called here");
        //code.append("scheduler()");
        code.append("}"+_eol);
        //create thread methods here
        code.append(generateMyThreads());
        
        return code.toString();
    }

  public String generateFrequencyThreadCode(int i) throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        code.append("static void $actorSymbol($+ frequency"+i+") (void * pvParameters){"+_eol);
        code.append("portTickType xLastWakeTime;"+_eol);
        code.append("const portTickType xFrequency = ("+_getPeriod()+"/"+i+"*1000)/portTICK_RATE_MS;"+_eol);
        code.append("xLastWakeTime xTaskGetTickCount();"+_eol);
        code.append("for(;;){"+_eol);
        code.append("vTaskDealyUntil(&xLastWakeTime,xFrequency);"+_eol);
        code.append("//call the methods for the tasks at this frequency"+_eol);
        code.append("}"+_eol);

        return code.toString();   
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
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
    throws IllegalActionException {
       // System.out.println("getReference from Giotto Director under OpenRTOS called ");
        if(port.isOutput())
        {// do own thing here}
        // will need to take care of the case of where the output is for a composite actor
            return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[1]+"PORT";
            //return "//dummy for write port";
        }
      /* else if(port.isOutput())
        {
            
            return  "//dummy 2";
        }
       /* else if(port.isInput())
        {
            return "//dummy 3";
            
        }*/
        else
            //return "//from else";
               return super.getReference(port, channelAndOffset, forComposite, isWrite,helper);
    }
    
    
 /*   private void _generateThreadFunctionCode(StringBuffer code)
    throws IllegalActionException {

        List actorList = ((CompositeActor) _director.getContainer())
        .deepEntityList();

//      Generate the function for each actor thread.
        for (Actor actor : (List<Actor>) actorList) {
            StringBuffer functionCode = new StringBuffer();

            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            code.append(_eol + "void* " + _getActorThreadLabel(actor)
                    + "(void* arg) {" + _eol);

            // Generate debug code for printing the thread ID and actor name.
            List args = new LinkedList();
            args.add(_generateActorNameFileForDebugging());
            args.add(actor.getDisplayName());
            code.append(_codeStream.getCodeBlock("printThreadName", args));

            // mainLoop

            // Check if the actor is an opague CompositeActor.
            // The actor is guaranteed to be opague from calling
            // deepEntityList(),
            // so all we need to check whether or not it is a CompositeActor.
            if (actor instanceof CompositeActor) {
                Director directorHelper = (Director) _getHelper(actor
                        .getDirector());

                // If so, it should contain a different Director.
                assert (directorHelper != this);

                functionCode.append(directorHelper.generateMainLoop());

                functionCode.append("$incrementReadBlockingThreads(&"
                        + generateDirectorHeader() + ");" + _eol);
            } else {

                String pnPostfireCode = "";

                String loopCode = "while (true) {" + _eol;

                // Generate a for loop if there is a firing count limit.
                if (actor instanceof LimitedFiringSource) {
                    int firingCount = ((IntToken) ((LimitedFiringSource) actor)
                            .firingCountLimit.getToken()).intValue();

                    if (firingCount != 0) {
                        loopCode = "int i = 0;" + _eol +
                        "for (; i < " + firingCount + "; i++) {" + _eol;
                    }
                    pnPostfireCode = _eol;
                }                               

                functionCode.append(loopCode);

                functionCode.append(helper.generateFireCode());

                // FIXME: Doesn't look like the following comment is correct.
                // If not inline, generateFireCode() would be a call
                // to the fire function which already includes the
                // type conversion code.
//              if (inline) {
                functionCode.append(helper.generateTypeConvertFireCode());
//              }

                functionCode.append(helper.generatePostfireCode());

                boolean forComposite = actor instanceof CompositeActor;

                // Increment port offset.
                for (IOPort port : (List<IOPort>) ((Entity) actor).portList()) {
                    // Determine the amount to increment.
                    int rate = 0;
                    try {
                        rate = DFUtilities.getRate(port);
                    } catch (NullPointerException ex) {
                        // int i = 0;
                    }
                    PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);
                    CodeGeneratorHelper portCGHelper = (CodeGeneratorHelper) portHelper;

                    if (portCGHelper.checkRemote(forComposite, port)) {
                        pnPostfireCode += portHelper
                        .updateConnectedPortsOffset(rate, _director);
                    }
                    if (port.isInput()) {
                        pnPostfireCode += portHelper.updateOffset(rate,
                                _director);
                    }
                }

                // Code for incrementing buffer offsets.
                functionCode.append(pnPostfireCode);

                functionCode.append("}" + _eol);
                functionCode.append("$incrementReadBlockingThreads(&"
                        + generateDirectorHeader() + ");" + _eol);
            }

            // wrapup
            functionCode.append(helper.generateWrapupCode());

            functionCode.append("return NULL;" + _eol);
            functionCode.append("}" + _eol);

            // init
            // This needs to be called last because all references
            // need to be collected before generating their initialization.
            String initializeCode = helper.generateInitializeCode();
            String variableInitializeCode = helper
            .generateVariableInitialization();
            code.append(variableInitializeCode);
            code.append(initializeCode);

            code.append(functionCode);
        }
    }
   */ 
    
    
   
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
           variableDeclarations.append(helperObject.generatePortVariableDeclarations());
           //variableDeclarations.append("//should attempt to append port here"+_eol);
           // variableDeclarations.append(helperObject.);
        }
        
       return variableDeclarations.toString();
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
        System.out.println("I shoudl generate mode transition code here");
    }
    
  // I'm not sure if this is the right place to add these methods, I'll try something and see  
    
///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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

    public String getPortVariableDeclarations()  throws IllegalActionException {
        return "getPortVariableDeclaration from Giotto Director Called";
    }
    
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
    throws IllegalActionException {
        System.out.println("\\generate transferInputsCode inside OpenRTOS Giotto director called.");
    code.append("\\generate transferInputsCode inside OpenRTOS Giotto director called.");
    
    }
    
    public void generateTransferOutputsCode(IOPort inputPort, StringBuffer code)
    throws IllegalActionException {
        System.out.println("\\generate transferOutputsCode inside OpenRTOS Giotto director called.");
    code.append("\\generate transferOutputsCode inside OpenRTOS Giotto director called.");
    
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
        StringBuffer code = new StringBuffer();//super.generateFireFunctionCode());
        
       // StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            
            String actorFullName = actor.getFullName();
            actorFullName = actorFullName.substring(1,actorFullName.length());
            actorFullName = actorFullName.replace('.', '_');
                

            code.append(_eol + "void " + actorFullName+ _getFireFunctionArguments() + " {"
                    + _eol);
            code.append(actorHelper.generateFireCode());
            code.append("}" + _eol);
            
            String temp = actorHelper.generateTypeConvertFireCode();//generateTypeConvertFireCode(actorHelper);/
            if(temp.length() > 1)  // only generate driver code if the actor has an output
            {
            code.append(_eol + "void " + actorFullName
                    + _getFireFunctionArguments()+"_driver" + " {"
                    + _eol);
            code.append(temp);
            code.append("}" + _eol);
            }
        }
        return code.toString();
        
       
    }
    
    public String generateDriverCode() throws IllegalActionException
    {
        StringBuffer code = new StringBuffer();
        
        code.append("//copy output to PORTS"+_eol);
        code.append("//copy PORTS to inputs"+_eol);
        return code.toString();
    }
    /*public String generateTypeConvertFireCode(CodeGeneratorHelper actorHelper)
    throws IllegalActionException {
        System.out.println("generateTypeConvertFireCode(boolean) called from GiottoDirector.java");
       StringBuffer code = new StringBuffer();
       
       boolean forComposite = false; // a false parameter is passed in to the originally called 
                                      //generateTypeConvertFireCode in CodeGeneratorHelper.java

        // Type conversion code for inter-actor port conversion.
        Iterator channels = _getTypeConvertChannels().iterator();
        if(channels == null)
        {
            System.out.println("channels has null value");
        }
        else{
            System.out.println("not null");
            System.out.println("channels has value: "+channels.toString());
        }
        while (channels.hasNext()) {
            System.out.println("inside first while ");
            Channel source = (Channel) channels.next();

            if (!forComposite && source.port.isOutput() || forComposite
                    && source.port.isInput()) {

                Iterator sinkChannels =_getTypeConvertSinkChannels(source)
                .iterator();

                while (sinkChannels.hasNext()) {
                    System.out.println("inside second while");
                    Channel sink = (Channel) sinkChannels.next();
                    code.append(_generateTypeConvertStatements(source, sink));
                }
            }
        }
       
       System.out.println("about to return from generateTypeConvertFireCode in openRTOS giottodirector");
       System.out.println("returning with:" +code.toString());
       
       return code.toString();
    }*/
   
    
    
}