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
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// ImageSequence
/**
@author Steve Neuendorffer
@version $Id$
*/

public class ImagePartition extends SDFAtomicActor {
    public ImagePartition(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container,name);
    
	new Parameter(this, "X Image Size", new IntToken("176"));
        new Parameter(this, "Y Image Size", new IntToken("144"));
        new Parameter(this, "X Partition Size", new IntToken("4"));
        new Parameter(this, "Y Partition Size", new IntToken("2"));
 
        IOPort outputport = (IOPort) newPort("partitions");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 176*144/4/2);

        IOPort inputport = (IOPort) newPort("image");
        outputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);
    }

    public void initialize() throws IllegalActionException {
	Parameter p;
	p = (Parameter) getAttribute("X Image Size");
        ximagesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("Y Image Size");
        yimagesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("X Partition Size");
        xpartsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("Y Partition Size");
        ypartsize = ((IntToken)p.getToken()).intValue();
 
    }

    public void fire() throws IllegalActionException {
        int part[][] = new int[xpartsize][ypartsize];
        int i, j;
	int x, y;
	IntMatrixToken message = 
	    (IntMatrixToken)((IOPort) getPort("image")).get(0);
	int frame[][] = message.intMatrix();
	
	for(j = 0; j < yimagesize; j += ypartsize)
            for(i = 0; i < ximagesize; i += ypartsize) {
		for(y = 0; y < ypartsize; y++)
		    for(x = 0; x < xpartsize; x++)
			part[y][x] = frame[j + y][i + x];
       		IntMatrixToken omessage = new IntMatrixToken(part);
		((IOPort) getPort("output")).send(0, omessage);
	    }
    }
    int ximagesize;
    int yimagesize;
    int xpartsize;
    int ypartsize;

}
