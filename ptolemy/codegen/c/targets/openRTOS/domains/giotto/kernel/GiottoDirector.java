/* Code generator helper class associated with the GiottoDirector class.

 Copyright (c) 2009-2010 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Type;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// GiottoDirector

/**
 Code generator helper associated with the GiottoDirector class. This class
 is also associated with a code generator.

 @author Shanna-Shaye Forbes, Man-Kit Leung, Ben Lickly
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */

public class GiottoDirector extends
        ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {

    /** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(
            ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);
        if (_debugging) {
            _debug("GiottoDirector constructor in OpenRTOS target called");

        }
    }

    /**
     * Generate the fire code for the director.
     * @return A string containing the fire code.
     * @exception IllegalActionException If thrown by the superclass or thrown
     * while generating code for the director.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("//generate firecode in GiottoDirector called" + _eol);
        if (_isTopDirectorFSM()) {
            code.append(_eol
                    + "//should append fire code for Giotto inside fsm" + _eol);
            code
                    .append(_eol + "//should take the other mode semaphores"
                            + _eol);
            code.append("xSemaphoreGive($actorSymbol()_scheduler_start);"
                    + _eol);
            //code.append(_eol+""+_eol);
        } else {
            code.append(_generateFireCode());
        }

        return code.toString();
    }

    /**
     * Generate The fire function code. This method is called when the firing
     * code of each actor is not inlined. Each actor's firing code is in a
     * function with the same name as that of the actor.
     *
     * @return The fire function code.
     * @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        if (_debugging) {
            _debug("generateFireFunctionCode called from OpenRTOS giotto director***************");
        }
        StringBuffer code = new StringBuffer(" ");//super.generateFireFunctionCode());

        return code.toString();

        //content moved to _generateActorsCode() which is called in preinitialize b/c code wasn't being generated for 
        //nested actors

    }

    /**
     * Get the files needed by the code generated from this helper class. This
     * base class returns an empty set.
     * @return A set of strings that are header files needed by the code
     * generated from this helper class.
     * @exception IllegalActionException Not Thrown in this base class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        if (_debugging) {
            _debug("generateheader files openRTOS giotto director called here");
        }

        HashSet files = new HashSet();
        files.add("<stdio.h>");
        //files.add("\"semphr.h\"");
        return files;
    }

    /**
     * Generate the initialize code.
     * @return The initialize code of the directory.
     * @exception IllegalActionException If thrown while appending to the the
     * block or processing the macros.
     */
    public String generateInitializeCode() throws IllegalActionException {
        if (_debugging) {
            _debug("generateInitializeCode from openRTOS giotto director called here");
        }

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
        if (_isTopGiottoDirector()) {
            args.set(2, "tskIDLE_PRIORITY + (unsigned portCHAR)"
                    + _MAX_PRIORITY_LEVEL);
        } else {
            args.set(2, "tskIDLE_PRIORITY + (unsigned portCHAR)"
                    + (_MAX_PRIORITY_LEVEL - _depthInGiottoHierarchy())); // non top level scheduler so give priority one lower than the highest priority scheduling thread
        }
        if (_isTopGiottoDirector()) {
            code.append(_generateBlockCode("createTask", args)); //create the scheduler thread
        }
        for (int frequencyValue : frequencies) {

            // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            //frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));

            //come back and create handlers for each of these frequency threads

            // name for the thread.
            args.set(0, _getThreadName(frequencyValue));
            // stack size.
            args.set(1, _getStackSize(_getActors(frequencyValue)));
            // priority.
            args.set(2, "tskIDLE_PRIORITY + (unsigned portCHAR)"
                    + currentPriorityLevel);

            code.append(_generateBlockCode("createTask", args)); // need to figure out how to pass the method names to fire as an argument

            ArrayList args1 = new ArrayList();
            args1.add("");
            args1.set(0, _getThreadName(frequencyValue) + "start");
            code.append(_generateBlockCode("createBinarySemaphore", args1));
            args1.set(0, _getThreadName(frequencyValue) + "done");
            code.append(_generateBlockCode("createBinarySemaphore", args1));
            currentPriorityLevel++;

            // We did minus 1 because we reserved the max
            // priority level for the scheduling thread.
            currentPriorityLevel %= _MAX_PRIORITY_LEVEL - 1;
        }
        code.append("//initialize actor variables" + _eol);
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {
            List<TypedIOPort> portList = actor.outputPortList();
            Iterator outpts = portList.iterator();
            Attribute initOutput = ((Entity) actor)
                    .getAttribute("initialOutputValue");
            TypedIOPort tempPt;
            while (outpts.hasNext()) {
                tempPt = (TypedIOPort) outpts.next();
                if (initOutput == null) {
                    code.append(_getPortName(tempPt) + " = 0;" + _eol);
                } else {
                    //check the different types of the output port
                    code.append(_getPortName(tempPt)
                            + " = "
                            + ((IntToken) ((Variable) initOutput).getToken())
                                    .intValue() + ";" + _eol);
                }
            }

        }
        code.append(super.generateInitializeCode());

        // System.out.println("about to return:"+_eol+processCode(code.toString()));
        return processCode(code.toString());

    }

    /**
     * Generate C code for the content of the main loop.
     * @return String containing the content of the main method.
     * @exception IllegalActionException If thrown while traversing the model.
     */
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generate main loop from openRTOS giotto director called here");
        }
        HashSet frequencies = new HashSet();
        int attributueValue;
        // go through all the actors and get their frequencies
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            attributueValue = _getFrequency(actor);

            frequencies.add(attributueValue);
        }

        //Attribute iterations = _director.getAttribute("iterations");
        // code.append( _eol+"g_ulSystemClock = SysCtlClockGet();"+_eol);
        if (_isTopGiottoDirector()) {
            //   code.append("vTaskStartScheduler();"+_eol);
            code.append(_eol + _generateBlockCode("mainCode") + _eol);
        }
        return code.toString();

    }

    /**
     * Generate the mode transition code. The mode transition code
     * generated in this method is executed after each global
     * iteration, e.g., in HDF model.  Do nothing in this base class.
     *
     * @param code The string buffer that the generated code is appended to.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
        System.out.println("I should generate mode transition code here");
    }

    /**
     * Generate code to create the threads.
     * @return the code for the threads.
     * @exception IllegalActionException If thrown while accessing the model.
     */
    public String generateMyThreads() throws IllegalActionException {
        // FIXME: Change the name of this method so that it does not
        // have "My" in it.

        // System.out.println("generateMyThreads called");
        StringBuffer code = new StringBuffer();
        ArrayList args1 = new ArrayList();

        double period = _getPeriod();
        String periodString = Double.toString(period);
        int dotIndex = periodString.indexOf('.');
        if (dotIndex == -1) {
            // no decimal just multiply by 1000
            periodString = Double.toString(period * 1000);
        } else {
            char temp[] = periodString.toCharArray();
            int count = 0;
            int i;
            for (i = dotIndex; i < periodString.length() - 1; i++) {
                temp[i] = temp[i + 1];
                count++;
            }
            temp[i] = '0'; //terminate the string
            count++;
            if (_debugging) {
                _debug("before padding with zeros: " + periodString);
            }
            StringBuffer zeros = new StringBuffer();
            while (count < 3) {
                zeros.append('0');
                count++;
            }
            periodString = new String(temp) + zeros.toString();
        }
        if (periodString.charAt(0) == '0') // leading zero so remove it
        {
            periodString = periodString.substring(1, periodString.length());

        }

        args1.add("");
        //if (_isTopGiottoDirector())
        //{
        args1.set(0, periodString);
        //        code.append("\\run driver code here"+_eol);
        //code.append(_generateBlockCode("createSchedulerThread", args1));
        //if ()
        code.append(generateSchedulerThread(periodString));
        if (_debugging) {
            _debug("*************just generated the scheduling thread for director: $actorSymbol()_");
        }
        //}

        if (!_isTopGiottoDirector()) {
            //code.append(_eol+"//not the top most Giotto director"+_eol);
            //Actor myOuterActor = (TypedCompositeActor) _director.getContainer();
            //int outerActorFrequency = _getFrequency(myOuterActor);
            //code.append(_eol+"// I think I'm inside actor: "+_director.getContainer().getFullName()+" it has frequency"+outerActorFrequency+_eol);
        }

        ArrayList<String> ActorFrequencies[] = _getActorFrequencyTable();
        //code.append("}"+_eol);

        // System.out.println("period string is "+periodString);
        code.append("//I should append the frequencethread stuff here" + _eol);
        HashSet frequencies = _getAllFrequencies();
        Object myFrequencies[] = frequencies.toArray();
        Arrays.sort(myFrequencies);
        int i = 0;
        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            i = (Integer) myFrequencies[k];
            code.append("static void $actorSymbol()_frequency" + i
                    + "(void * pvParameters){" + _eol);
            //code.append("portTickType xLastWakeTime;"+_eol);
            //code.append("int count;"+_eol);
            //code.append("char buff[50];"+_eol);
            //code.append("portTickType xFrequency = 0;"+_eol);
            //+ periodString+")/"+i+"/ "+outerActorFrequency+ "/portTICK_RATE_MS;"+_eol);
            //code.append("xLastWakeTime = gxLastWakeTime;"+_eol);
            //code.append("count = 0;"+_eol);
            code.append("   for (;;){" + _eol);
            //code.append("vTaskDelayUntil(&xLastWakeTime,xFrequency);"+_eol);
            //code.append("xFrequency =("+ periodString+")/"+i+"/ "+outerActorFrequency+ "/portTICK_RATE_MS;"+_eol);
            //code.append("count++;"+_eol);
            // code.append("// here I should call driver code method for each of the actors"+_eol);
            //code.append("  //call the methods for the tasks at this frequency of "+ i+_eol);
            //code.append("//sprintf(buff,\"f"+i+"thread %d\",count);"+_eol);
            //code.append("//RIT128x96x4StringDraw(buff, 0,_,15);"+_eol);
            code.append("     if (xSemaphoreTake($actorSymbol()_frequency" + i
                    + "start,portMAX_DELAY)== pdTRUE){" + _eol);
            //code.append("if (xSemaphoreGive($actorSymbol()_frequency"+i+"start)){"+_eol);
            //code.append("//not able to release input ready semaphore");
            //code.append(_eol+"}"+_eol);
            for (int j = 0; j < ActorFrequencies[i].size(); j++) {
                //call generate driver code for each of the actors
                code.append(ActorFrequencies[i].get(j) + "();" + _eol);
            }
            code.append("xSemaphoreGive($actorSymbol()_frequency" + i
                    + "done);" + _eol);
            //code.append("} else {"+_eol);//close
            //code.append("//wait until it's avaliable"+_eol);
            //code.append("vTaskSuspend(NULL);");
            code.append(_eol + "}" + _eol); // close if
            code.append(_eol + "}" + _eol);// close the for loop
            if (i < _getAllFrequencies().size()) {
                code.append("}" + _eol);// close the method loop
            }

        }

        return processCode(code.toString());
    }

    /**
     * Generate the postfire code.
     * 
     * @return The generated postfire code.
     * @exception IllegalActionException If thrown while appending to the the
     * block or processing the macros.
     */
    public String generatePostFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generatePostFireCode from openRTOS giotto director called here");
        }

        //for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
        //        .getContainer()).deepEntityList()) {

        //for each of the actors generate postfire code
        //code.append(generatePostFireCode(actor)); 
        //}

        return code.toString();
    }

    /**
     * Generate the preinitialize code.
     * @return A string of the preinitialize code for the helper.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        double myPeriod = _getPeriod();
        double myWCET = getWCET();
        if (_debugging) {
            _debug("My period is :" + myPeriod + " and my WCET is :" + myWCET);
        }
        if (myWCET > myPeriod) {
            throw new IllegalActionException("Unable to Schedule: Period of "
                    + myPeriod + "(s) and WCET of " + myWCET + "(s)");

        }

        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {
            System.out.println("Actor " + actor.getFullName()
                    + " has priority " + _getPriority(actor));

        }

        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());
        // Declare the thread handles.
        if (_debugging) {
            _debug("generatePreinitializeCode from openRTOS giotto director called here");
        }

        if (_isTopGiottoDirector()) {
            code.append("void Warn(char * data){" + _eol
                    + "RIT128x96x4StringDraw(data,0,0,20);" + _eol + "}");
            code.append("//speed of the processor clock" + _eol);
            code.append(" unsigned long g_ulSystemClock;" + _eol);
            code.append("portTickType gxLastWakeTime;" + _eol);
        }
        code.append(_generateBlockCode("preinitBlock"));
        HashSet<Integer> frequencies = _getAllFrequencies();

        //StringBuffer frequencyTCode = new StringBuffer();

        ArrayList args = new ArrayList();
        args.add("");

        //int currentPriorityLevel = 1;
        if (_isTopGiottoDirector()) {
            args.set(0, "$actorSymbol()_scheduler");
            code.append(_generateBlockCode("declareTaskHandle", args));
        }
        for (int frequencyValue : frequencies) {
            // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            //            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));

            args.set(0, _getThreadName(frequencyValue));
            code.append(_generateBlockCode("declareTaskHandle", args));
            args.set(0, _getThreadName(frequencyValue) + "start");
            code.append(_generateBlockCode("declareSemaphoreHandle", args));
            args.set(0, _getThreadName(frequencyValue) + "done");
            code.append(_generateBlockCode("declareSemaphoreHandle", args));
        }

        /* code.append(_eol+"//about to add semaphores for all the output ports"+_eol);
            for (Actor actor : (List<Actor>) 
                    ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
                List outputPortList = actor.outputPortList();
                Iterator outputPorts = outputPortList.iterator();
                while (outputPorts.hasNext())
                {
                    IOPort outpt = (IOPort)outputPorts.next();
                    args.set(0, _getActorName(actor)+"_"+outpt.getName());

                    code.append(_generateBlockCode("declareSemaphoreHandle", args)+_eol);   
                }

            }
         */

        code.append(_eol + _generateOutputDriverCode());
        code.append(_generateDriverCode());
        code.append(_generateActorsCode());

        if (_debugging) {
            _debug("I should check to see if I'm the top most Giotto director here.. ");
        }
        if (_isTopDirectorFSM()) {
            code.append(_generateFireCode());
        }
        return processCode(code.toString());
    }

    /**
     * Return an unique label for the given port channel referenced
     * by the given helper. By default, this delegates to the helper to
     * generate the reference. Subclass may override this method
     * to generate the desire label according to the given parameters.
     * @param port The given port.
     * @param channelAndOffset The given channel and offset.
     * @param forComposite Whether the given helper is associated with
     *  a CompositeActor
     * @param isWrite The type of the reference. True if this is
     *  a write reference; otherwise, this is a read reference.
     * @param helper The specified helper.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the helper throws it while
     *  generating the label.
     */
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
            throws IllegalActionException {

        if (port.isOutput()) {
            if (channelAndOffset[0].equals("")) {
                channelAndOffset[0] = "0";
            }
            // will need to take care of the case of where the output is for a composite actor
            return CodeGeneratorHelper.generateName(port);
            // return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0];  
        } else {
            return super.getReference(port, channelAndOffset, forComposite,
                    isWrite, helper);
        }
    }

    /** 
     * Return a reference to the driver.
     * This method is similar to the getReference() method however it it tailored 
     * for use by a driver method.
     * @param port The port whose information is desired.
     * @param channelAndOffset The given channel and offset.
     * @param forComposite Whether the given helper is associated with
     *  a CompositeActor
     * @param isWrite The type of the reference. True if this is
     *  a write reference; otherwise, this is a read reference.
     * @param helper The specified helper.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the helper throws it while
     *  generating the label.
     */
    public String getDriverReference(TypedIOPort port,
            String[] channelAndOffset, boolean forComposite, boolean isWrite,
            CodeGeneratorHelper helper) throws IllegalActionException {

        if (port.isOutput()) {
            if (channelAndOffset[0].equals("")) {
                channelAndOffset[0] = "0";
            }
            // will need to take care of the case of where the output is for a composite actor
            return CodeGeneratorHelper.generateName(port) + "_PORT";
            // return CodeGeneratorHelper.generateName(port)+"_"+channelAndOffset[0];  
        } else {
            return super.getReference(port, channelAndOffset, forComposite,
                    isWrite, helper);
        }
    }

    /**
     * Generate code for the scheduler thread.
     * @param period The period of the Giotto director.
     * @return Code that creates the Scheduler thread.
     * @exception IllegalActionException If thrown while accessing
     * the model.
     */
    public String generateSchedulerThread(String period)
            throws IllegalActionException {
        if (_debugging) {
            _debug("genereateSchedulerThread called");
        }
        StringBuffer code = new StringBuffer();
        ArrayList<String> ActorFrequencies[] = _getActorFrequencyTable();
        int outerActorFrequency = 1;
        if (!_isTopGiottoDirector()) {
            Actor myOuterActor = (TypedCompositeActor) _director.getContainer();
            outerActorFrequency = _getFrequency(myOuterActor);
        }
        if (_isTopGiottoDirector()) {
            code
                    .append("static void $actorSymbol()_scheduler(void * pvParameters){"
                            + _eol);
        }
        code.append("portTickType xLastWakeTime;" + _eol);
        code.append("int schedTick;" + _eol);
        // code.append("char buff[50];"+_eol);

        HashSet frequencies = _getAllFrequencies();
        Object myFrequencies[] = frequencies.toArray();
        int intFrequencies[] = new int[_getAllFrequencies().size()];
        for (int l = 0; l < _getAllFrequencies().size(); l++) {
            intFrequencies[l] = (Integer) myFrequencies[l];

        }

        int myLCM = _lcm(intFrequencies);
        if (_debugging) {
            _debug("The LCM of my frequencies are " + myLCM);
        }
        code.append("const portTickType xFrequency = (((" + period + "/"
                + myLCM + ")/" + outerActorFrequency + ")/portTICK_RATE_MS);"
                + _eol);

        int ss0;
        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            ss0 = (Integer) myFrequencies[k];
            code.append("  char warn" + ss0 + " = 0;" + _eol);
        }
        if (!_isTopGiottoDirector()) {
            code.append("int i;" + _eol);
        }

        if (_isTopGiottoDirector()) {
            code.append("xLastWakeTime = xTaskGetTickCount();" + _eol);
            code.append("gxLastWakeTime = xLastWakeTime;" + _eol);
        } else {
            code.append("xLastWakeTime = gxLastWakeTime;" + _eol);
        }
        code.append("schedTick = 0;" + _eol);
        //create warning variables

        //take all the inputs for the frequency threads so that they can't start until they've gotten the go ahead from the scheduler
        code.append("//take semaphores" + _eol);
        int ss;
        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            ss = (Integer) myFrequencies[k];
            code.append("xSemaphoreTake($actorSymbol()_frequency" + ss
                    + "start,(portTickType)0);" + _eol);
        }
        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            ss = (Integer) myFrequencies[k];
            code.append("xSemaphoreTake($actorSymbol()_frequency" + ss
                    + "done,(portTickType)0);" + _eol);
        }
        if (_isTopGiottoDirector()) {
            code.append("    for (;;){" + _eol);
        } else {
            code.append("    for (i = 0;i < " + myLCM + ";i++){" + _eol);
        }
        Arrays.sort(myFrequencies);

        int i = 0;
        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            i = (Integer) myFrequencies[k];
            code.append("if (schedTick %" + (myLCM / i) + " == 0){" + _eol);

            for (int j = 0; j < ActorFrequencies[i].size(); j++) {
                //call generate driver code for each of the actors    
                code.append(ActorFrequencies[i].get(j) + "_driver_out();"
                        + _eol);
            }

            code.append("}" + _eol);
        }

        i = 0;
        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            i = (Integer) myFrequencies[k];
            code.append("if (schedTick %" + (myLCM / i) + " == 0){" + _eol);

            for (int j = 0; j < ActorFrequencies[i].size(); j++) {
                //call generate driver code for each of the actors
                code.append(ActorFrequencies[i].get(j) + "_driver();" + _eol);
            }
            code.append("xSemaphoreGive($actorSymbol()_frequency" + i
                    + "start);" + _eol);
            code.append("}" + _eol);
        }

        code.append(_eol + "     vTaskDelayUntil(&xLastWakeTime,xFrequency);"
                + _eol);
        code.append("schedTick++;" + _eol);
        code.append("if (schedTick == " + (myLCM) + ") {" + _eol);
        code.append("schedTick = 0;" + _eol);
        code.append("}" + _eol + _eol);

        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            i = (Integer) myFrequencies[k];
            code.append("if (schedTick %" + (myLCM / i) + " == 0){" + _eol);
            //take the semaphore
            code.append("if (xSemaphoreTake($actorSymbol()_frequency" + i
                    + "done,(portTickType)0) ==  pdFALSE){" + _eol + "warn" + i
                    + " = 1;" + _eol + "Warn(\"" + i + "overrun\");" + _eol
                    + "}" + _eol);

            code.append(_eol + "}" + _eol);
        }

        for (int k = 0; k < _getAllFrequencies().size(); k++) {
            i = (Integer) myFrequencies[k];
            code.append("if (schedTick %" + (myLCM / i) + " == 0){" + _eol);
            //take the semaphore
            code.append("if (warn" + i + " == 1){" + _eol
                    + "xSemaphoreTake($actorSymbol()_frequency" + i
                    + "done,portMAX_DELAY); " + _eol + _eol + "warn" + i
                    + " = 0;" + _eol + "}");// end if warn

            code.append(_eol + "}" + _eol); // end if sched tick
        }

        code.append("    }" + _eol);
        if (!_isTopGiottoDirector()) {
            code.append("//should transfer code to outputs here" + _eol);
            Actor actor = (Actor) _director.getContainer();
            code.append("// in container " + actor.getFullName() + _eol);
            {// move to output ports
                List<IOPort> myOutputs = actor.outputPortList();
                if (_debugging) {
                    _debug("I have " + myOutputs.size()
                            + " port(s) to send info to");
                }
                Iterator myItr = myOutputs.iterator();
                IOPort port;
                CodeGeneratorHelper myHelper;
                String srcReference;
                String sinkReference;
                int ii = 0; //sink index counter
                int j = 0; // src index counter
                while (myItr.hasNext()) {
                    port = (IOPort) myItr.next();
                    List connectToMe = port.insideSourcePortList();
                    Iterator tome = connectToMe.iterator();
                    while (tome.hasNext()) {
                        IOPort tempp = (IOPort) tome.next();

                        // port is sink
                        //tempp is source

                        String channelOffset[] = { "0", "0" };
                        if (_debugging) {
                            _debug("the sender port is named "
                                    + tempp.getFullName()
                                    + " and the reciever is "
                                    + port.getFullName());
                        }
                        myHelper = (CodeGeneratorHelper) this._getHelper(tempp
                                .getContainer());

                        channelOffset[0] = Integer.valueOf(ii).toString();
                        if (_debugging) {
                            _debug("channel offset is " + channelOffset[0]);
                        }
                        srcReference = this.getReference((TypedIOPort) tempp,
                                channelOffset, false, true, myHelper);
                        if (_debugging) {
                            _debug("after first call to getReference");
                        }
                        myHelper = (CodeGeneratorHelper) _getHelper(actor);
                        channelOffset[0] = Integer.valueOf(j).toString();
                        if (_debugging) {
                            _debug("channel offset is " + channelOffset[0]);
                        }
                        sinkReference = this.getReference((TypedIOPort) port,
                                channelOffset, false, true, myHelper);
                        if (_debugging) {
                            _debug("after second call to getReference");
                        }
                        j++;

                        if (_debugging) {
                            _debug("I think the source Reference is "
                                    + srcReference
                                    + " and it's display name is "
                                    + tempp.getDisplayName());
                            _debug("I think the sink Reference is "
                                    + sinkReference
                                    + " and it's display name is "
                                    + port.getDisplayName());
                        }
                        ArrayList args = new ArrayList();
                        args.add(sinkReference);
                        args.add(srcReference);

                        code.append(_generateBlockCode("updatePort", args)
                                + _eol);
                    } //end while tome.hasNext()
                    ii++; // not sure if this is the correct place to increment i

                }// end while myItr.hasNext()
            }
        }
        code.append("      }" + _eol);

        return processCode(code.toString());
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        if (_debugging) {
            _debug("//generate transferInputsCode inside OpenRTOS Giotto director called.");
        }

    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        if (_debugging) {
            _debug("//generate transferOutputsCode inside OpenRTOS Giotto director called.");
        }
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
            variableDeclarations.append(helperObject
                    .generateVariableDeclaration());
            if ((actor instanceof CompositeActor) != true) {
                List<TypedIOPort> actorPorts = actor.outputPortList();
                if (actorPorts.size() > 0) {
                    Iterator portItr = actorPorts.iterator();
                    TypedIOPort actorport;
                    String type;
                    while (portItr.hasNext()) {
                        actorport = (TypedIOPort) portItr.next();
                        //variableDeclarations.append(_eol+"//not composite actor so may need extra variable, later, make sure you check the type. It's default to static int at the moment"+_eol);
                        type = _targetType(actorport.getType());
                        variableDeclarations.append("static " + type + " "
                                + _getActorName(actor) + "_output;" + _eol);
                    }
                }

            }
            variableDeclarations
                    .append(_generatePortVariableDeclarations(actor));
            variableDeclarations
                    .append("//############end variables for this actor: "
                            + actor.getFullName() + _eol);
            //no need to create port variables if the normal output variables are read at the right time... they can be ports.. this way you only need two variables for double buffering
        }

        return variableDeclarations.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Given a block name, generate code for that block. This method is called
     * by actors helpers that have simple blocks that do not take parameters or
     * have widths.
     * @param blockName The name of the block.
     * @param args A list of arguments to be passed to the code block.
     * @return The code for the given block.
     * @exception IllegalActionException If illegal macro names are found, or if
     * there is a problem parsing the code block from the helper .c file.
     */
    protected String _generateBlockCode(String blockName, List args)
            throws IllegalActionException {
        return _codeStream.getCodeBlock(blockName, args);
    }

    /**
     * Generate the fire code for the director.
     * In this case the fire code is simply the OpenRTOS thread code.
     * @return The generated code.
     * @exception IllegalActionException If the thread code cannot be generated.
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append("//fire code should be here. I'm from the openRTOS GiottoDirector "+_eol);
        if (_debugging) {
            _debug("generateFireCode from openRTOS giotto director called here");
        }
        code
                .append("//Beginning of generateFireCode inside OpenRTOS GiottoDirector***************");
        //code.append("scheduler()");

        if (_isTopGiottoDirector()) {//if (!_isTopDirectorFSM()) {
            //top director isn't fsm so close the method that would normally contain the fire code 
            //if the inline option was enabled and being used. Inline is invalid for giotto codegen
            code.append(_eol + "}" + _eol);
        }
        //create thread methods here

        code.append(generateMyThreads());

        if (_isTopDirectorFSM()) {
            //insert a close parenthesis b/c it's not done automatically in the code
            code.append(_eol + "}" + _eol);
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
        if (_debugging) {
            _debug("_generateInputVariableDeclaration called form OpenRTOS Giotto Director");
        }
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

            code.append("static " + _targetType(inputPort.getType()) + " "
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
        if (_debugging) {
            _debug("_gneerateOutputVariableDeclaration called form OpenRTOS Giotto Director");
        }
        StringBuffer code = new StringBuffer();

        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if (!outputPort.isOutsideConnected()
                    || outputPort.isInsideConnected()) {
                code.append("static " + _targetType(outputPort.getType()) + " "
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return how may Giotto directors are above this director.
     */
    private int _depthInGiottoHierarchy() {
        int depth = 0;
        ptolemy.actor.Director director = ((TypedCompositeActor) _director
                .getContainer()).getExecutiveDirector();

        while (director != null) {
            if (director instanceof ptolemy.domains.giotto.kernel.GiottoDirector) {
                depth += 1;
            }
            director = ((TypedCompositeActor) director.getContainer())
                    .getExecutiveDirector();

        }
        if (_debugging) {
            _debug("My depth in the Giotto hierarcy is : " + depth);
        }
        return depth;
    }

    /**
     * Generates methods for all the actors seen by this director
     *      * @exception IllegalActionException
     */
    private String _generateActorsCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generateActors Code has been called");
        }
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            String actorFullName = _getActorName(actor);
            if (_debugging) {
                _debug("I have an actor named " + actorFullName
                        + " going to generate fireCode for it now.");
            }
            // generate methods with fire code for the actors inside a composite actor with an sdf director
            if (actor instanceof CompositeActor) {

                if (actor.getDirector() instanceof ptolemy.domains.sdf.kernel.SDFDirector) {
                    for (Actor actor1 : (List<Actor>) ((TypedCompositeActor) actor
                            .getDirector().getContainer()).deepEntityList()) {
                        CodeGeneratorHelper actor1Helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor1);
                        code.append(actor1Helper.generateFireFunctionCode());
                    }

                }
            }

            code.append(_eol + "void " + actorFullName
                    + _getFireFunctionArguments() + " {" + _eol);

            if (actor instanceof CompositeActor) {

                if (_debugging) {
                    _debug("composite actor: " + actor.getFullName()
                            + " so doing stuff for that from actor code");
                }

                if (actor.getClass().getName().contains(
                        "ptolemy.actor.lib.jni.EmbeddedCActor")) {
                    List<IOPort> myInputs = actor.inputPortList();
                    Iterator myItr = myInputs.iterator();
                    IOPort port;
                    CodeGeneratorHelper myHelper;
                    String srcReference;
                    String sinkReference;
                    int i = 0; //sink index counter
                    int j = 0; // src index counter
                    while (myItr.hasNext()) {

                        port = (IOPort) myItr.next();
                        List connectToMe = port.sourcePortList();
                        Iterator tome = connectToMe.iterator();
                        while (tome.hasNext()) {

                            IOPort tempp = (IOPort) tome.next();
                            // port is sink
                            //tempp is source
                            String channelOffset[] = { "0", "0" };
                            if (_debugging) {
                                _debug("the sender port is named "
                                        + tempp.getFullName()
                                        + " and the reciever is "
                                        + port.getFullName());
                            }
                            myHelper = (CodeGeneratorHelper) this
                                    ._getHelper(tempp.getContainer());
                            channelOffset[0] = Integer.valueOf(i).toString();
                            if (_debugging) {
                                _debug("channel offset is " + channelOffset[0]);
                            }
                            srcReference = this.getDriverReference(
                                    (TypedIOPort) tempp, channelOffset, false,
                                    true, myHelper);
                            if (_debugging) {
                                _debug("after first call to getReference");
                            }
                            myHelper = (CodeGeneratorHelper) _getHelper(actor);
                            channelOffset[0] = Integer.valueOf(j).toString();
                            if (_debugging) {
                                _debug("channel offset is " + channelOffset[0]);
                            }
                            sinkReference = this.getReference(
                                    (TypedIOPort) port, channelOffset, false,
                                    true, myHelper);
                            if (_debugging) {
                                _debug("after second call to getReference");
                            }
                            j++;

                            if (_debugging) {
                                _debug("I think the source Reference is "
                                        + srcReference
                                        + " and it's display name is "
                                        + tempp.getDisplayName());
                                _debug("I think the sink Reference is "
                                        + sinkReference
                                        + " and it's display name is "
                                        + port.getDisplayName());
                            }
                            ArrayList args = new ArrayList();
                            args.add(sinkReference);
                            args.add(srcReference);

                            code.append(_generateBlockCode("updatePort", args)
                                    + _eol);
                        } // while (tome.hasNext())
                        i++; // not sure if this is the correct place to increment i
                    }//end while (myItr.hasNext())

                    //transfer input values to embedded actor inputs
                    myItr = myInputs.iterator();
                    String outerInput;
                    String innerInput;
                    while (myItr.hasNext()) {
                        IOPort thisport = (IOPort) myItr.next();
                        outerInput = _getPortName(thisport);
                        innerInput = outerInput.replace("_input",
                                "_EmbeddedActor_input");
                        code.append(_eol + innerInput + " = " + outerInput
                                + ";" + _eol);

                    }
                    // now call the method name
                    code.append(_getActorName(actor) + "_EmbeddedActor();"
                            + _eol);
                    //for now assume that there are no outputs to transfer out
                    // end if jni actor
                } else if (actor.getDirector() instanceof ptolemy.domains.giotto.kernel.GiottoDirector) {
                    code.append(_eol + actorHelper.generateFireFunctionCode2());
                } else if (!(actor.getDirector() instanceof ptolemy.domains.fsm.kernel.FSMDirector)) {
                    //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                    // moving this code here is just a test
                    code.append(_eol + actorHelper.generateFireFunctionCode2());
                    // moving the above code here is just a test.
                    List<IOPort> myOutputs = actor.outputPortList();
                    if (_debugging) {
                        _debug("I have " + myOutputs.size()
                                + " port(s) to send info to");
                    }
                    Iterator myItr = myOutputs.iterator();
                    IOPort port;
                    CodeGeneratorHelper myHelper;
                    String srcReference;
                    String sinkReference;
                    int i = 0; //sink index counter
                    int j = 0; // src index counter
                    while (myItr.hasNext()) {
                        port = (IOPort) myItr.next();
                        List connectToMe = port.insideSourcePortList();

                        Iterator tome = connectToMe.iterator();
                        while (tome.hasNext()) {
                            IOPort tempp = (IOPort) tome.next();

                            // port is sink
                            //tempp is source

                            String channelOffset[] = { "0", "0" };
                            if (_debugging) {
                                _debug("the sender port is named "
                                        + tempp.getFullName()
                                        + " and the reciever is "
                                        + port.getFullName());
                            }
                            myHelper = (CodeGeneratorHelper) this
                                    ._getHelper(tempp.getContainer());

                            channelOffset[0] = Integer.valueOf(i).toString();
                            if (_debugging) {
                                _debug("channel offset is " + channelOffset[0]);
                            }
                            srcReference = this.getReference(
                                    (TypedIOPort) tempp, channelOffset, false,
                                    true, myHelper);
                            if (_debugging) {
                                _debug("after first call to getReference");
                            }
                            myHelper = (CodeGeneratorHelper) _getHelper(actor);
                            channelOffset[0] = Integer.valueOf(j).toString();
                            if (_debugging) {
                                _debug("channel offset is " + channelOffset[0]);
                            }
                            sinkReference = this.getReference(
                                    (TypedIOPort) port, channelOffset, false,
                                    true, myHelper);
                            if (_debugging) {
                                _debug("after second call to getReference");
                            }
                            j++;

                            // temp+= _generateTypeConvertFireCode(sourcePort,port);//+_eol; 
                            if (_debugging) {
                                _debug("I think the source Reference is "
                                        + srcReference
                                        + " and it's display name is "
                                        + tempp.getDisplayName());
                                _debug("I think the sink Reference is "
                                        + sinkReference
                                        + " and it's display name is "
                                        + port.getDisplayName());
                            }
                            ArrayList args = new ArrayList();
                            args.add(sinkReference);
                            args.add(srcReference);

                            code.append(_generateBlockCode("updatePort", args)
                                    + _eol);
                        } //end while tome.hasNext()
                        i++; // not sure if this is the correct place to increment i

                    }// end while myItr.hasNext()

                    // end if not fsm
                } else {
                    code.append(_eol + actorHelper.generateFireFunctionCode2());
                }
                //end composite actor

            } else {
                if (_debugging) {
                    _debug("not composite actor");
                }

                code.append(_eol + actorHelper.generateFireFunctionCode2());

            }
            code.append("}" + _eol);

        } // end for every actor  

        if (_debugging) {
            _debug("returning: " + _eol + code.toString());
        }
        return code.toString();

    }

    /** Generate sanitized name for the given named object. Remove all
     *  underscores to avoid conflicts with systems functions.
     *  @param namedObj The named object for which the name is generated.
     *  @return The sanitized name.
     */
    private static String _generateDriverName(NamedObj namedObj) {
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
        return name.replaceAll("\\$", "Dollar") + "_driver";

    }

    /** Generate the content of  driver methods. For each actor update it's inputs to the 
     *  outputs stored in ports. The PORT allows double buffering, in this case the output
     *  variable is used as the port. PORT here is simply a common variable, not a PORT in 
     *  the general Ptolemy II actor sense
     *  
     *  NOTE: Duplicate ports connected through a fork are removed. IE. if an input is connected to a fork
     *  and the fork is connected to two other places... it removes the first place from the list of places and keeps the last place
     *  need to ask Jackie if there is a way to work around this b/c Reciever [][] recievers = getRecievers doesn't work.
     *  @return code that copies inputs from a port in a driver method
     */
    String _generateDriverCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generateDriver Code has been called");
        }
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            List inputPortList = actor.inputPortList();
            if (_debugging) {
                _debug("this actor" + actor.getDisplayName() + " has "
                        + inputPortList.size() + " input ports.");
            }
            Iterator inputPorts = inputPortList.iterator();

            String actorDriverCode = "";
            String sinkReference = "";
            String srcReference = "";
            String temp = "";
            StringBuffer transferIn = new StringBuffer();
            int i = 0; //sink index counter
            int j = 0; // src index counter
            CodeGeneratorHelper myHelper;

            while (inputPorts.hasNext()) {
                i = 0; // this is a test to see if this is to be done here, if so remove the i++ from the end of the loop
                j = 0;
                TypedIOPort port = (TypedIOPort) inputPorts.next();
                if (_debugging) {
                    _debug("this port's name is " + port.getFullName());
                }
                //Receiver[][] channelArray = port.getReceivers();
                // port.
                List<IOPort> cip = port.sourcePortList();
                if (cip.size() > 0) {

                    if (_debugging) {
                        _debug("sourcePortList contains: ");
                    }
                    Iterator tome2 = cip.iterator();
                    while (tome2.hasNext()) {
                        IOPort tempp = (IOPort) tome2.next();
                        if (_debugging) {
                            _debug(tempp.getFullName() + " ");
                        }
                    }
                    if (_debugging) {
                        _debug(" ");
                    }
                }

                //List<IOPort> connectedPorts = port.deepConnectedOutPortList();
                List<IOPort> connectToMe = port.sourcePortList();//port.connectedPortList(); //connectedPortList();
                if (_debugging) {
                    _debug("connectToMe size is " + connectToMe.size());
                    _debug("before remove double connections");
                }
                Iterator tome = connectToMe.iterator();
                if (_debugging) {
                    _debug("currently connectToMe size is "
                            + connectToMe.size());
                }
                tome = connectToMe.iterator();
                while (tome.hasNext()) {
                    IOPort tempp = (IOPort) tome.next();
                    if (_debugging) {
                        _debug("******I'm connected to I think: "
                                + tempp.getFullName());
                    }
                }

                // Iterator cpIterator = connectedPorts.iterator();
                Iterator cpIterator = connectToMe.iterator();
                while (cpIterator.hasNext()) {//&&(j <connectToMe.size()-1)) {
                    TypedIOPort sourcePort = (TypedIOPort) cpIterator.next();
                    // FIXME: figure out the channel number for the sourcePort.
                    // if you need to transfer inputs inside
                    if (actor instanceof CompositeActor) {

                        if (_debugging) {
                            _debug("composite actor so doing stuff for that");
                        }

                        transferIn
                                .append(("//should transfer input for this actor to from the outside to inside" + _eol));

                    }

                    if (_debugging) {
                        _debug(" j is " + j + "and size of connect to me is "
                                + connectToMe.size());
                    }
                    String channelOffset[] = { "0", "0" };

                    if (_debugging) {
                        _debug("the sender port is named "
                                + sourcePort.getFullName()
                                + " and the reciever is " + port.getFullName());
                    }
                    myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                            .getContainer());

                    channelOffset[0] = Integer.valueOf(i).toString();
                    if (_debugging) {
                        _debug("channel offset is " + channelOffset[0]);
                    }
                    srcReference = this.getDriverReference(
                            (TypedIOPort) sourcePort, channelOffset, false,
                            true, myHelper);
                    if (_debugging) {
                        _debug("after first call to getReference");
                    }
                    myHelper = (CodeGeneratorHelper) _getHelper(actor);
                    channelOffset[0] = Integer.valueOf(j).toString();
                    if (_debugging) {
                        _debug("channel offset is " + channelOffset[0]);
                    }
                    sinkReference = this.getReference((TypedIOPort) port,
                            channelOffset, false, true, myHelper);
                    j++;

                    if (_debugging) {
                        _debug("I think the source Reference is "
                                + srcReference + " and it's display name is "
                                + sourcePort.getDisplayName());
                        _debug("I think the sink Reference is " + sinkReference
                                + " and it's display name is "
                                + port.getDisplayName());
                    }

                    temp = _typeConversion(sourcePort, port);
                    if (_debugging) {
                        _debug("I think the source Reference is "
                                + srcReference + " and it's display name is "
                                + sourcePort.getDisplayName());
                        _debug("I think the sink Reference is " + sinkReference
                                + " and it's display name is "
                                + port.getDisplayName());
                    }
                    String src;
                    temp = _typeConversion(sourcePort, port);
                    if (temp.length() == 0) {
                        src = srcReference;
                    } else {
                        src = temp + "(" + srcReference + ")";
                    }

                    actorDriverCode += sinkReference + " = " + src + ";" + _eol;

                }

                if (actor instanceof CompositeActor) {
                    // It is not necessary to generate transfer out code, 
                    //since the fanout actor drivers will read the necessary values from the ports                    
                    //GiottoDirector directorHelper = (GiottoDirector) _getHelper(actor.getDirector());
                    //directorHelper._generateTransferOutputsCode(port, transferOut);

                    //generateTransferInputsCode(inputPort, code);
                    //transferOut.append("//should transfer input for this actor to from inside to outside"+_eol);
                }

                i++; // increment the ofset variable // not sure if this is correct since we're using iterators 
            }
            if (_debugging) {
                _debug("actorDriverCode is now:");
                _debug(actorDriverCode);
            }

            //ArrayList args = new ArrayList();
            code.append("void " + _generateDriverName((NamedObj) actor));
            code.append("(){" + _eol);
            code.append(actorDriverCode + _eol);
            //code.append(_generateBlockCode("driverCode", args));
            code.append("}" + _eol);
        }
        if (_debugging) {
            _debug("about to return :");
            _debug(code.toString());
        }
        return code.toString();
    }

    /** Generate the content of output driver methods. The output
     *  driver updates the value of a port to be that of the output of
     *  the latest execution of a task.
     *  @return code that copies outputs to a port
     */
    String _generateOutputDriverCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_debugging) {
            _debug("generateDriver Code has been called");
        }
        String sinkReference;
        String srcReference;
        StringBuffer actorDriverCode;
        CodeGeneratorHelper myHelper;

        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();
            sinkReference = "";
            srcReference = "";
            actorDriverCode = new StringBuffer();
            while (outputPorts.hasNext()) {
                IOPort sourcePort = (IOPort) outputPorts.next();
                // FIXME: figure out the channel number for the sourcePort.
                // if you need to transfer inputs inside
                String channelOffset[] = { "0", "0" };
                int i = sourcePort.getWidth();
                myHelper = (CodeGeneratorHelper) this._getHelper(sourcePort
                        .getContainer());
                if (i > 1) {
                    for (int j = 0; j < i; j++) {
                        channelOffset[0] = Integer.valueOf(j).toString();
                        if (sourcePort.isMultiport()) {
                            code
                                    .append("//multiport so need to transfer over the info");
                        }
                        sinkReference = this.getReference(
                                (TypedIOPort) sourcePort, channelOffset, false,
                                true, myHelper);
                        srcReference = sinkReference.replace(((Integer) j)
                                .toString(), "PORT_" + j);
                        ArrayList args = new ArrayList();
                        args.add(sinkReference);
                        args.add(srcReference);
                        actorDriverCode.append(_generateBlockCode("updatePort",
                                args));
                    }
                } else {
                    channelOffset[0] = "0";
                    srcReference = this.getReference((TypedIOPort) sourcePort,
                            channelOffset, false, true, myHelper);
                    sinkReference = srcReference + "_PORT";
                    ArrayList args = new ArrayList();
                    args.add(sinkReference);
                    args.add(srcReference);
                    actorDriverCode.append(_generateBlockCode("updatePort",
                            args));
                }
            }
            if (_debugging) {
                _debug("actorDriverCode is now:");
                _debug(actorDriverCode.toString());
            }

            ArrayList args = new ArrayList();
            args.add(_generateDriverName((NamedObj) actor) + "_out");
            args.add(actorDriverCode.toString());
            code.append(_generateBlockCode("driverCode", args));
        }
        if (_debugging) {
            _debug("about to return :");
            _debug(code.toString());
        }
        return code.toString();
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @param source The source port, ignored by this method.
     * @param sink The sink port, ignored by this method.
     * @return The generated code, in this case, return the empty string.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String _generateTypeConvertFireCode(IOPort source, IOPort sink)
            throws IllegalActionException {
        if (_debugging) {
            _debug("generateTypeConvertFireCode in OpenRTOS giotto director called");
        }
        return "";
    }

    /**
     * Return the worst case execution time (WCET) seen by this
     * director.
     * @return The Worst Case Execution Time (WCET).
     * @exception IllegalActionException If there is a problem determining
     * the WCET or a problem accessing the model.
     */
    public double getWCET() throws IllegalActionException {
        double wcet = 0;
        double actorFrequency = 0;
        double actorWCET = 0;
        int actorCount = 0;
        CodeGeneratorHelper directorHelper;
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {
            actorCount++;
            Attribute frequency = ((Entity) actor).getAttribute("frequency");
            Attribute WCET = ((Entity) actor).getAttribute("WCET");

            if (actor instanceof CompositeActor) {

                if (_debugging) {
                    _debug("Composite Actor, if it has a director I need to ask it for it's WCET");
                }
                Director dir = actor.getDirector();

                directorHelper = (CodeGeneratorHelper) _getHelper(actor
                        .getDirector());
                System.out.println(dir.getFullName());
                System.out.println(directorHelper.getWCET());
                //ptolemy.codegen.actor.Director df = new ptolemy.codegen.actor.Director(
                //        actor.getDirector());
                //if (dir == null)
               
                double dummyWCET = directorHelper.getWCET();//df.getWCET();//dir.getWCET();
                if (_debugging) {
                    _debug("Composite Actor:" + actor.getFullName()
                            + " has WCET " + dummyWCET);
                }
                wcet += dummyWCET;
                
            } else {

                if (frequency == null) {
                    actorFrequency = 1;
                } else {
                    actorFrequency = ((IntToken) ((Variable) frequency)
                            .getToken()).intValue();
                }
                if (WCET == null) {
                    actorWCET = 0.01;
                } else {
                    actorWCET = ((DoubleToken) ((Variable) WCET).getToken())
                            .doubleValue();
                }

                wcet += (actorFrequency * actorWCET);
            }
            if (_debugging) {
                _debug("with actor " + actor.getFullName()
                        + " wect thus far is " + wcet);
            }
        }
        if (_debugging) {
            _debug("actor count is " + actorCount);
        }
        // now determine the WCET of the scheduler
        HashSet frequencies = _getAllFrequencies();
        Object myFrequencies[] = frequencies.toArray();
        int intFrequencies[] = new int[_getAllFrequencies().size()];
        for (int l = 0; l < _getAllFrequencies().size(); l++) {
            intFrequencies[l] = (Integer) myFrequencies[l];

        }

        int myLCM = _lcm(intFrequencies);
        double schedulerWCET = 0.01; // this value needs to be calculated

        wcet += schedulerWCET * myLCM;

        return wcet;
    }

    /** Generate PORT variables. A PORT allows control over the value read
     *  A port is an efficient way to handle double buffering
     *  @return port variables
     */
    private String _generatePortVariableDeclarations(Actor actor)
            throws IllegalActionException {
        if (_debugging) {
            _debug("get Port Variable Declarations called");
        }
        StringBuffer code = new StringBuffer();

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            if (true) {
                code.append("static " + _targetType(outputPort.getType()) + " "
                        + generateName(outputPort) + "_PORT");

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
        if (_debugging) {
            _debug("about to return: " + code.toString());
        }
        return code.toString();
    }

    /**
     * Generates a two dimensional array of actors at each frequency
     * @return ArrayList with actor names at each frequency
     * @exception IllegalActionException
     */
    private ArrayList<String>[] _getActorFrequencyTable()
            throws IllegalActionException {
        if (_debugging) {
            _debug("getActorFrequencyTable called");
        }
        HashSet<Integer> allFrequncies = _getAllFrequencies();
        int frequencyCount = allFrequncies.size();

        Object theseFrequencies[] = allFrequncies.toArray();
        int maxfrequency = (Integer) theseFrequencies[0];
        int it = 0;
        while (it < frequencyCount) {

            if ((Integer) theseFrequencies[it] > maxfrequency) {
                maxfrequency = (Integer) theseFrequencies[it];
            }
            if (_debugging) {
                _debug("fequency value: " + theseFrequencies[it]);
            }
            it++;
        }

        frequencyCount = maxfrequency;
        ArrayList<String> actorNamesPerFrequency[] = new ArrayList[frequencyCount + 1];
        for (int i = 0; i < frequencyCount + 1; i++) {
            actorNamesPerFrequency[i] = new ArrayList<String>();

        }
        if (_debugging) {
            _debug("table has size " + actorNamesPerFrequency.length);
        }
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            String temp = _getActorName(actor);
            if (_debugging) {
                _debug("actor " + temp + " has frequency :"
                        + _getFrequency(actor) + "frequency count is: "
                        + frequencyCount);
            }
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

    /**
     * Generates a string of the actor's name
     * @param actor
     *       */
    private String _getActorName(Actor actor) {
        String actorFullName = actor.getFullName();
        actorFullName = actorFullName.substring(1, actorFullName.length());
        actorFullName = actorFullName.replace('.', '_');
        actorFullName = actorFullName.replace(' ', '_');
        return actorFullName;
    }

    /**
     * Generates  a set of the different frequencies seen by 
     * this Giotto Director 
     * @return a Hashset of integers containing the frequencies seen 
     * by the Giotto director
     * @exception IllegalActionException
     */
    private HashSet<Integer> _getAllFrequencies() throws IllegalActionException {
        HashSet frequencies = new HashSet();
        int i = 0;
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {

            int attributueValue = _getFrequency(actor);
            i++;
            frequencies.add(attributueValue);

            //come back and figure out how to keep a list of all 
            //the actors at each of the frequencies so you can call the right
            //methods from the threads
            // frequencies.add(_getFrequency(actor));
        }

        return frequencies;
    }

    /**
     * Generates a string representation of the port's name
     * @param port
     *       */
    private String _getPortName(IOPort port) {
        String portFullName = port.getFullName();
        portFullName = portFullName.substring(1, portFullName.length());
        portFullName = portFullName.replace('.', '_');
        portFullName = portFullName.replace(' ', '_');

        return portFullName;
    }

    /**
     * Determines the frequeny of the actor passed in as a parameter
     * @param actor
     *       * @exception IllegalActionException
     */
    private int _getFrequency(Actor actor) throws IllegalActionException {
        Attribute frequency = ((Entity) actor).getAttribute("frequency");

        if (frequency == null) {
            return 1;
        } else {
            return ((IntToken) ((Variable) frequency).getToken()).intValue();
        }
    }

    /**
     * Determines the true frequency of the actor , which can be used as it's priority
     * I.E. If the actor has a frequency 1, and it is inside a composite actor at the top of the director chain with frequency2,
     * the actor's true frequency is 1*2 = 2, which is the priority it should be given.
     * @param actor
     *       * @exception IllegalActionException
     */
    private int _getPriority(Actor actor) throws IllegalActionException {
        Attribute frequency = ((Entity) actor).getAttribute("frequency");
        int myFrequency = 0;
        ptolemy.actor.Director director = ((TypedCompositeActor) _director
                .getContainer()).getDirector();
        System.out.println("getPriority called on " + actor.getFullName());
        if (director != null) {
            if (frequency == null) {
                myFrequency = 1;
            } else {
                myFrequency = ((IntToken) ((Variable) frequency).getToken())
                        .intValue();
                myFrequency *= _getPriority((Actor) actor.getContainer());
            }
        } else {
            System.out.println("director is now null");
        }
        return myFrequency;
    }

    /**
     * Determines the period of this Giotto Director. If it is not the top most Giotto director
     * return the period of the Executive Giotto Director
     * @return the period of the top most/ executive Giotto director
     * @exception IllegalActionException
     */
    private double _getPeriod() throws IllegalActionException {
        ptolemy.actor.Director director = ((TypedCompositeActor) _director
                .getContainer()).getExecutiveDirector();

        while (director != null
                && !(director instanceof ptolemy.domains.giotto.kernel.GiottoDirector)) {
            director = ((TypedCompositeActor) director.getContainer())
                    .getExecutiveDirector();
        }
        if (director == null) {
            // This is the case for the outer-most Giotto director.
            Attribute period = _director.getAttribute("period");
            double periodValue;

            if (period != null) {
                periodValue = ((DoubleToken) ((Variable) period).getToken())
                        .doubleValue();
            } else {
                throw new IllegalActionException(this, "There is no period"
                        + "specified in the outer-most Giotto director");
            }
            return periodValue;
        }

        // Get the frequency.  
        // Ben I'm not sure why you originally called get frequency here
        int frequency = _getFrequency((Actor) _director.getContainer());
        return ((GiottoDirector) _getHelper(director))._getPeriod() / frequency;

    }

    /**
     * Determines the stack size necessary for the actors passed in
     * @param actors
     *       */
    private int _getStackSize(List<Actor> actors) {
        // FIXME: there may be static analysis in the future.
        // However, we are hard-coding this number for now.
        return 100;
    }

    /**
     * Generates the name of a thread for a specified frequency value.
     * @param frequencyValue
     * @return the string containing the generated thread name
     */
    private String _getThreadName(int frequencyValue) {
        return "$actorSymbol(frequency)" + frequencyValue;
    }

    /**
     * Determines if this Giotto director is the top most Giotto Director
     *       */
    private boolean _isTopGiottoDirector() {
        ptolemy.actor.Director director = ((TypedCompositeActor) _director
                .getContainer()).getExecutiveDirector();

        if (director == null) { // true for the top most director

            return true;
        }

        return false;
    }

    /**
     * Determines if this director is inside an FSM director
     * @return true if inside an FSM director, false otherwise.
     */
    private boolean _isTopDirectorFSM() {
        boolean returnValue = false;

        ptolemy.actor.Director director = ((TypedCompositeActor) _director
                .getContainer()).getExecutiveDirector();

        if (director != null
                && (director instanceof ptolemy.domains.fsm.kernel.FSMDirector)) {
            returnValue = true;
        }

        return returnValue;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    //// this piece of code was copied from the Giotto Scheduler method
    /** This method was copied from the GiottoScheduler method. It determines
     * the least common multiple of the numbers passed in in array and returns 
     * the value
     * 
     */
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

    /**
     * Determines which type conversion is necessary when going from the source to 
     * the sink port
     * @param source - the source port
     * @param sink - the sink port
     * @return a string indicating which type of conversion needs to happen
     *         i.e. InttoDouble, etc. 
     */
    private String _typeConversion(TypedIOPort source, TypedIOPort sink) {
        String sourceType;
        String sinkType;
        sourceType = _targetType(source.getType());
        sinkType = _targetType(sink.getType());
        if (sourceType.equals(sinkType)) {
            return "";
        } else {
            char tc = sourceType.charAt(0);
            sourceType = Character.toUpperCase(tc) + sourceType.substring(1);
            tc = sinkType.charAt(0);
            sinkType = Character.toUpperCase(tc) + sinkType.substring(1);
            sinkType = sinkType.replace(tc, Character.toUpperCase(tc));
            return sourceType + "to" + sinkType;
        }
    }

    /** Determines the target type of Type.
     *  Currently forces the Token type to default to an integers
     * */
    private String _targetType(Type ptType) {
        String ttype = targetType(ptType);
        if (ttype.equals("Token")) {
            return "int";
        } else {
            return ttype;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static int _MAX_PRIORITY_LEVEL = 254;
}
