/* An example to demonstrate the PN Domain Scheduler.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.domains.pn.demo;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// PNInterleavingExample
/**
An example to test the PN domain. This example tests the PN INterleaving
example.
@author Mudit Goel
@version $Id$
*/

class RLEncoding {

    public static void main(String args[]) throws
	    IllegalStateException, IllegalActionException,
            NameDuplicationException {
	CompositeActor c1 = new CompositeActor();
	Manager manager = new Manager();
        // FIXME FIXME FIXME
        c1.setManager(manager);
	BasePNDirector local = new BasePNDirector("Local");
        //local.addProcessListener(new DefaultPNListener());
	c1.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));

        PNImageSource a1 = new PNImageSource(c1, "A1");
        //Parameter p1 = (Parameter)a1.getAttribute("Image_file");
        //p1.setToken(new StringToken("/users/mudit/ptII/ptolemy/domains/pn/lib/test/ptII.pbm"));
	String filename = 
	    "/users/mudit/ptII/ptolemy/domains/pn/lib/test/ptII.pbm";
	try {
	    FileInputStream fis = new FileInputStream(filename);
	    a1.read(fis);
	} catch (FileNotFoundException e) {
	    System.err.println("FileNotFoundException: "+ e.toString());
	}
        //p1.setToken(new StringToken("/users/ptII/ptolemy/domains/pn/lib/test/ptII.pbm"));
        MatrixUnpacker a2 = new MatrixUnpacker(c1, "A2");
        RLEncoder a3 = new RLEncoder(c1, "A3");
        RLDecoder a4 = new RLDecoder(c1, "A4");
        MatrixPacker a5 = new MatrixPacker(c1, "A5");
        PNImageSink a6 = new PNImageSink(c1, "A6");
        Parameter p1 = (Parameter)a6.getAttribute("Output_file");
        p1.setToken(new StringToken("/tmp/image.pbm"));
        ImageDisplay a7 = new ImageDisplay(c1, "dispin");
	p1 = (Parameter)a7.getAttribute("FrameName");
	p1.setToken(new StringToken("InputImage"));
        ImageDisplay a8 = new ImageDisplay(c1, "dispout");
	p1 = (Parameter)a8.getAttribute("FrameName");
	p1.setToken(new StringToken("OutputImage"));

        IOPort portin = (IOPort)a1.getPort("output");
        IOPort portout = (IOPort)a2.getPort("input");
        ComponentRelation rel = c1.connect(portin, portout);
        (a7.getPort("image")).link(rel);

        portin = (IOPort)a2.getPort("output");
        portout = (IOPort)a3.getPort("input");
        c1.connect(portin, portout);

        portin =(IOPort) a2.getPort("dimensions");
        portout = (IOPort)a3.getPort("dimensionsIn");
        c1.connect(portin, portout);

        portin = (IOPort)a3.getPort("dimensionsOut");
        portout = (IOPort)a4.getPort("dimensionsIn");
        c1.connect(portin, portout);

        portin = (IOPort)a3.getPort("output");
        portout = (IOPort)a4.getPort("input");
        c1.connect(portin, portout);

        portin = (IOPort)a4.getPort("dimensionsOut");
        portout = (IOPort)a5.getPort("dimensions");
        c1.connect(portin, portout);

        portin = (IOPort)a4.getPort("output");
        portout = (IOPort)a5.getPort("input");
        c1.connect(portin, portout);

        portin = (IOPort)a5.getPort("output");
        portout = (IOPort)a6.getPort("input");
        rel = c1.connect(portin, portout);        
        (a8.getPort("image")).link(rel);
        
	System.out.println("Connections made");
        Parameter p = (Parameter)local.getAttribute("Initial_queue_capacity");
        p.setToken(new IntToken(500));
 	manager.startRun();
        System.out.println("Bye World\n");
	return;
    }

}


