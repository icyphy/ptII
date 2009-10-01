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
 * Name: SearchTest.java Purpose: The purpose of this actor is 
 * to prove that we can use it's output as input for the Loop Actor
 * 
 * input :  String "Search #Loop Number"
 * output : "No Data" or "Results Found"
 * 
 *@author Jim Amrhein/Mark Ruebens Giovannii/GSFC Greenbelt
 * 
 */


public class SearchTest extends TypedAtomicActor {

    /**
     * The Point (Lat,Long) the user selected from the map
     */
    public TypedIOPort search = new TypedIOPort(this,
            "search", true, false);

    /* // Once we get this working, allow the user to change the 
    // match value.
    public TypedIOPort searchMatch = new TypedIOPort(this,
    "search Match", true, false);
    */

    /**
     *  Output whether the download failed or passed.
     * ; acts as an output trigger
     */

    public TypedIOPort resultsOutput = new TypedIOPort(this,
            "results Output", false, true);


    public SearchTest(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        search.setTypeEquals(BaseType.STRING);
        // searchMatch.setTypeEquals(BaseType.STRING);
        resultsOutput.setTypeEquals(BaseType.STRING);
    }

    /**
     *
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire();

    }

    /**
     * Read the search parameter, if it matches "Search 50" then
     * we've "got" data. Let the Loop Actor know it can stop searching
     */
    public void fire() throws IllegalActionException {
        super.fire();            

        if (search.getWidth() > 0 && search.hasToken(0)) {
            String searchStr = ((StringToken) search.get(0)).stringValue();
            if (searchStr.equals("Search 50")) {
                System.out.println("Found DATA!");
                resultsOutput.broadcast(new StringToken("Results Found"));
 
            } else {
                System.out.println("Didn't Match! "+searchStr);
                resultsOutput.broadcast(new StringToken("No Data"));
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
        return super.postfire();
    }

}
