/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red
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
 * This actor deterministically splits its input token stream into two
 * streams.
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class SDFSplit extends SDFAtomicActor {
    public SDFSplit(TypedCompositeActor container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try{
            TypedIOPort inputport = (TypedIOPort)newPort("input");
            inputport.setInput(true);
            setTokenConsumptionRate(inputport, 2);
            inputport.setDeclaredType(IntToken.class);

            TypedIOPort outputport1 = (TypedIOPort)newPort("output1");
            outputport1.setOutput(true);
            setTokenProductionRate(outputport1, 1);
            outputport1.setDeclaredType(IntToken.class);
            
            TypedIOPort outputport2 = (TypedIOPort)newPort("output2");
            outputport2.setOutput(true);
            setTokenProductionRate(outputport2, 1);
            outputport2.setDeclaredType(IntToken.class);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFSplit: constructor error");
        }
    }

    public void fire() throws IllegalActionException {
        IntToken message;
        TypedIOPort inputport = (TypedIOPort)getPort("input");
        TypedIOPort outputport1 = (TypedIOPort)getPort("output1");
        TypedIOPort outputport2 = (TypedIOPort)getPort("output2");

        message = (IntToken)inputport.get(0);
        System.out.print("Split1 - ");
        System.out.println(message.intValue());
        outputport1.send(0, message);
        message = (IntToken)inputport.get(0);
        System.out.print("Split2 - ");
        System.out.println(message.intValue());
        outputport2.send(0, message);
    }
}






