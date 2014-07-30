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
 * Name: LoopTest.java Purpose: The purpose of this actor is
 *   prove that output from another actor can be input to this actor.
 *
 * input : counter : start of the loop, will loop until it's stopped (# 75)
 xmlResults :
 * output : .
 *
 *@author Jim Amrhein/Mark Ruebens Giovannii/GSFC Greenbelt
@version $Id$
@since Ptolemy II 8.0
 *
 */

public class LoopTest2 extends TypedAtomicActor {

    boolean continueLooping = true;
    /**
     * The Point (Lat,Long) the user selected from the map
     */
    public TypedIOPort counter = new TypedIOPort(this, "counter", true, false);

    public TypedIOPort xmlResults = new TypedIOPort(this, "XML Results", true,
            false);

    /**
     */

    public TypedIOPort searchOutput = new TypedIOPort(this, "searchOutput",
            false, true);

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
    @Override
    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    /**
     *
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (counter.getWidth() > 0 && counter.hasToken(0)) {
            String counterStr = ((StringToken) counter.get(0)).stringValue();
            int counterInt = -1;
            try {
                counterInt = Integer.parseInt(counterStr);
            } catch (NumberFormatException ex) {
                throw new IllegalActionException(this, ex,
                        "Could not convert \"" + counterStr
                                + "\" to an integer.");
            }
            System.out.println("counter input  is " + counterInt);

            // if (xmlResults.getWidth() <= 0) { }

            // see if I can have the loop stop itself before it reaches
            // the limit of 75 "searches"
            if (xmlResults.getWidth() > 0 && xmlResults.hasToken(0)) {
                String xmlResultsStr = ((StringToken) xmlResults.get(0))
                        .stringValue();
                System.out.println("#" + xmlResultsStr + "# \n");
                if (xmlResultsStr.equals("Results Found")) {
                    System.out.println("Got data, set to " + xmlResultsStr);
                    foundResultsOutput
                    .broadcast(new StringToken(xmlResultsStr));
                    continueLooping = false;
                }
            } else {
                System.out.println("counter is " + counterInt);
                searchOutput.broadcast(new StringToken("Search " + counterInt));
                if (counterInt == 75) {
                    System.out.println("Ending this " + counterInt);
                    continueLooping = false;
                }

            }

        }
    }

    /**
     * Post fire the actor. Return false to indicate that the process has
     * finished. If it returns true, the process will continue indefinitely.
     *
     *@return the value of the continueLooping flag.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return continueLooping;
        //                return super.postfire();
    }

}
