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
*/
package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFJoin
/**
@author Stephen Neuendorffer
@version $Id$
*/

public class SDFJoin extends SDFAtomicActor {
    public IOPort inputport1;
    public IOPort inputport2;
    public IOPort outputport;

    public SDFJoin(CompositeActor container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport1=(IOPort)newPort("input1");
            inputport1.setInput(true);
            setTokenConsumptionRate(inputport1,1);
            inputport2=(IOPort)newPort("input2");
            inputport2.setInput(true);
            setTokenConsumptionRate(inputport2,1);
            outputport=(IOPort)newPort("output");
            outputport.setOutput(true);
            setTokenProductionRate(outputport,2);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFJoin: constructor error");
        }
    }

    public void fire() throws IllegalActionException {
        IntToken message;


        message=(IntToken)inputport1.get(0);
        System.out.print("Join1 - ");
        System.out.println(message.intValue());
        outputport.send(0,message);
        message=(IntToken)inputport2.get(0);

        System.out.print("Join2 - ");
        System.out.println(message.intValue());
        outputport.send(0,message);

    }
}






