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
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;



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
        StringBuffer code = new StringBuffer();
        // to call the c codeblocks in *.c
        code.append(this._generateBlockCode("initLCD"));

        HashSet<Integer> frequencies = _getAllFrequencies();

        StringBuffer frequencyTCode = new StringBuffer();

        int currentPriorityLevel = 1;

        ArrayList args = new ArrayList();
        args.add("");
        args.add("");
        args.add("");

        for(int frequencyValue : frequencies) {
            
            // assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));

            //come back and create handlers for each of these frequency threads

            // name for the thread.
            args.set(0, _getThreadName(frequencyValue)); 
            // stack size.
            args.set(0, _getStackSize(_getActors(frequencyValue))); 
            // priority.
            args.set(0, currentPriorityLevel); 

            code.append(_generateBlockCode("declareTaskHandle", args));
            currentPriorityLevel++;
            
            // We did minus 1 because we reserved the max
            // priority level for the scheduling thread.
            currentPriorityLevel %= _MAX_PRIORITY_LEVEL - 1;
        }

        return processCode(code.toString());
    }

    public String generateMainLoop() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        StringBuffer tempcode = new StringBuffer();
        StringBuffer frequencyTCode= new StringBuffer();
    
        code.append("RIT128x96x4Init(1000000);"+_eol);
        code.append("xTaskHandle schedulerhandle;"+_eol);
        tempcode.append("xTaskCreate(scheduler,"+"\"scheduler\""+",100,NULL,10,schedulerhandle);"+_eol);
    
    
    
        HashSet frequencies= new HashSet();
        int  attributueValue;
        // go through all the actors and get their frequencies
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
    
            attributueValue =_getFrequency(actor);
    
            frequencies.add(attributueValue);
    
    
            //come back and figure out how to keep a list of all 
            //the actors at each of the frequencies so you can call the right
            //methods from the threads
            // frequencies.add(_getFrequency(actor));
        }
    
    
        Iterator frequencyIterator = frequencies.iterator();
    
        int frequencyValue;
    
        int currentPriorityLevel = 1;
        for(int i = 0; i < frequencies.size();i++)
        {// assign the low frequency task the lower priority, doesn't handle wrap around at the moment
            frequencyValue = (Integer)frequencyIterator.next();
            frequencyTCode.append(generateFrequencyThreadCode(frequencyValue));
    
            //come back and create handlers for each of these frequency threads
            code.append("xTaskHandle "+"frequency"+frequencyValue+"handle;"+_eol);
            tempcode.append("xTaskCreate(frequency"+frequencyValue+",\"frequency"+frequencyValue+"\" "+",100,NULL,"+currentPriorityLevel+",frequency"+frequencyValue+"handle);"+_eol);
            currentPriorityLevel++;
            currentPriorityLevel %=9;
    
        }
    
        code.append(tempcode);
    
    
        Attribute iterations = _director.getAttribute("iterations");
        /*    
        if (iterations == null) {
            code.append(_eol + "while (true) {" + _eol);
        }
        else {
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
            }
        }
         */
        if (_isTopGiottoDirector()) {
            code.append("vTaskStartScheduler();"+_eol);
        }
        code.append("/* Will only get here if there is insufficient memory to create the idle"+_eol);
        code.append("task. */");
    
        code.append("return 0;"+_eol);
        code.append("}");
    
        code.append("\\now call fireetc here"+_eol);
    
        // when should I call generate fire code etc??
        code.append(generateFireCode());
        // not sure where to do mode transition code
        code.append("//now for the postfire code"+_eol);
        code.append(generatePostFireCode());     
        // code.append("}"+_eol);
    
        code.append(frequencyTCode);
        return code.toString();
    
    }

    public Set getHeaderFiles()throws IllegalActionException{
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
        return actors;
    }

    private String _getThreadName(int frequencyValue) {
        return "$actorSymbol(frequency)" + frequencyValue;
    }
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // Declare the thread handles.
        HashSet<Integer> frequencies = _getAllFrequencies();

        StringBuffer frequencyTCode = new StringBuffer();

        ArrayList args = new ArrayList();
        args.add("");

        int currentPriorityLevel = 1;
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
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {

            int attributueValue =_getFrequency(actor);

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
        return false;
    }

    public String generateFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        code.append("//fire code should be here. I'm from the openRTOS GiottoDirector "+_eol);

        code.append("static void scheduler(void * pvParameters){"+_eol);
        code.append("portTickType xLastWakeTime;"+_eol);
        code.append("const portTickType xFrequency = ("+_getPeriod()+"*1000)/portTICK_RATE_MS;"+_eol);
        code.append("xLastWakeTime xTaskGetTickCount();"+_eol);
        code.append("for(;;){"+_eol);
        code.append("vTaskDealUntil(&xLastWakeTime,xFrequency);"+_eol);
        code.append("//handle updates, mode switches, and switching the double buffer pointers"+_eol);
        code.append("}"+_eol);

        return code.toString();
    }

    public String generateFrequencyThreadCode(int i) throws IllegalActionException{
        StringBuffer code = new StringBuffer();
        code.append("static void frequency"+i+"(void * pvParameters){"+_eol);
        code.append("portTickType xLastWakeTime;"+_eol);
        code.append("const portTickType xFrequency = ("+_getPeriod()+"/"+i+"*1000)/portTICK_RATE_MS;"+_eol);
        code.append("xLastWakeTime xTaskGetTickCount();"+_eol);
        code.append("for(;;){"+_eol);
        code.append("vTaskDealUntil(&xLastWakeTime,xFrequency);"+_eol);
        code.append("//call the methods for the tasks at this frequency"+_eol);
        code.append("}"+_eol);


        return code.toString();   
    }



    public String generatePostFireCode() throws IllegalActionException{
        StringBuffer code = new StringBuffer();


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



}