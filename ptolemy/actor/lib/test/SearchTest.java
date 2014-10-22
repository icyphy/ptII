/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2014 The Regents of the University of California.
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
 */
//package gov.nasa.gsfc.giovanni;
package ptolemy.actor.lib.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
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
 * @author Jim Amrhein/Mark Ruebens Giovannii/GSFC Greenbelt
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class SearchTest extends TypedAtomicActor {

    /**
     * The Point (Lat,Long) the user selected from the map
     */
    public TypedIOPort search = new TypedIOPort(this, "search", true, false);

    /* // Once we get this working, allow the user to change the
    // match value.
    public TypedIOPort searchMatch = new TypedIOPort(this,
    "search Match", true, false);
     */

    /**
     *  Output whether the download failed or passed.
     * ; acts as an output trigger
     */

    public TypedIOPort resultsOutput = new TypedIOPort(this, "results Output",
            false, true);

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
    @Override
    public boolean prefire() throws IllegalActionException {
        return super.prefire();

    }

    /**
     * Read the search parameter, if it matches "Search 50" then
     * we've "got" data. Let the Loop Actor know it can stop searching
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (search.getWidth() > 0 && search.hasToken(0)) {
            String searchStr = ((StringToken) search.get(0)).stringValue();
            if (searchStr.equals("Search 50")) {
                System.out.println("Found DATA!");
                resultsOutput.broadcast(new StringToken("Results Found"));

            } else {
                System.out.println("Didn't Match! " + searchStr);
                resultsOutput.broadcast(new StringToken("No Data"));
            }
        }
    }

    /**
     * Post fire the actor. Return false to indicate that the process has
     * finished. If it returns true, the process will continue indefinitely.
     *
     *@return The value returned by the parent method.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return super.postfire();
    }

}
