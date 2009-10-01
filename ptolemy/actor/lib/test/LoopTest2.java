//package gov.nasa.gsfc.giovanni;
package ptolemy.actor.lib.test;

import java.io.*;
import java.util.*;
import java.lang.StringBuilder;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.*;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.IntToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Name: LoopTest.java Purpose: The purpose of this actor is 
 *   prove that output from another actor can be input to this actor. 
 * 
 * input : counter : start of the loop, will loop until it's stopped (# 75)
 xmlResults :  
 * output : .
 * 
 *@author Jim Amrhein/Mark Ruebens Giovannii/GSFC Greenbelt
 * 
 */


public class LoopTest2 extends TypedAtomicActor {

    boolean continueLooping = true;
    /**
     * The Point (Lat,Long) the user selected from the map
     */
    public TypedIOPort counter = new TypedIOPort(this,
            "counter", true, false);

    public TypedIOPort xmlResults = new TypedIOPort(this,
            "XML Results", true, false);

    /**
     */

    public TypedIOPort searchOutput = new TypedIOPort(this,
            "searchOutput", false, true);


    public TypedIOPort foundResultsOutput = new TypedIOPort(this,
            "found Results Output", false, true);


    public LoopTest2(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {

        super(container, name);
        counter.setTypeEquals(BaseType.STRING);
        searchOutput.setTypeEquals(BaseType.STRING);
        foundResultsOutput.setTypeEquals(BaseType.STRING);
        xmlResults.setTypeEquals(BaseType.STRING);
    }

    /**
     *
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    /**
     *
     */
    public void fire() throws IllegalActionException {
        super.fire();            

        if (counter.getWidth() > 0 && counter.hasToken(0)) {
            String counterStr = ((StringToken) counter.get(0)).stringValue();
            int counterInt  = new Integer(counterStr).intValue();
            System.out.println("counter input  is "+counterInt);

            // if (xmlResults.getWidth() <= 0) { }

            // see if I can have the loop stop itself before it reaches
            // the limit of 75 "searches"
            if (xmlResults.getWidth() > 0 && xmlResults.hasToken(0)) {
                String xmlResultsStr = ((StringToken) xmlResults.get(0)).stringValue();
                System.out.println("#"+xmlResultsStr+"# \n");
                if (xmlResultsStr.equals("Results Found")) {    
                    System.out.println("Got data, set to "+ xmlResultsStr);
                    foundResultsOutput.broadcast(new StringToken(xmlResultsStr));
                    continueLooping = false;
                }
            } else {
                System.out.println("counter is "+counterInt);
                searchOutput.broadcast(new StringToken("Search "+counterInt));
                if (counterInt == 75 ) {
                    System.out.println("Ending this "+counterInt);
                    continueLooping = false;
                }


            }

        }
    }

    /**
     * Post fire the actor. Return false to indicate that the process has
     * finished. If it returns true, the process will continue indefinitely.
     * 
     *@return
     */
    public boolean postfire() throws IllegalActionException {
        return continueLooping;
        //		return super.postfire();
    }

}
