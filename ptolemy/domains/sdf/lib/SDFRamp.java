/*
@Copyright (c) 1998 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@PropsedRating Red
@AcceptedRating Red
*/
package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;


/**
 * Create an increasing sequence of integer tokens,
 * starting with value zero, and incrementing by one.
 * This actor is aware of the rate that is set on its port and
 * will create the proper number of tokens with every firing.
 *
 * @version $Id$
 * @author Steve Neuendorffer
 */
public class SDFRamp extends SDFAtomicActor {
    public SDFRamp(CompositeActor container, String name)
            throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        try{
            IOPort outputport = (IOPort) newPort("output");
            outputport.setOutput(true);
            setTokenProductionRate(outputport, 1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFRamp: constuctor error");
        }
        value = 0;

    }

    public void fire() throws IllegalActionException {
        int i;
        IOPort outputport = (IOPort) getPort("output");
        int tokens = getTokenProductionRate(outputport);
        for(i = 0; i < tokens; i++) {
            Token message = new IntToken(value);
            value = value + 1;
            outputport.send(0, message);
        }
    }

    public void initialize() {
        value = 0;
    }

    public boolean prefire() throws IllegalActionException {
        return true;
    }

    private int value;

}

