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
//// ImagePartition
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class ImagePartition extends SDFAtomicActor {
    public ImagePartition(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
    
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "XPartitionSize", new IntToken("4"));
        new Parameter(this, "YPartitionSize", new IntToken("2"));
 
        SDFIOPort outputport = (SDFIOPort) newPort("partition");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 3168);

        SDFIOPort inputport = (SDFIOPort) newPort("image");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);
    }

    public void initialize() throws IllegalActionException {
	Parameter p;
	p = (Parameter) getAttribute("XFramesize");
        xframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YFramesize");
        yframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("XPartitionSize");
        xpartsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YPartitionSize");
        ypartsize = ((IntToken)p.getToken()).intValue();

        partition = ((SDFIOPort) getPort("partition"));
        image = ((SDFIOPort) getPort("image"));
        part = new int[xpartsize*ypartsize];
        partitions = new ImageToken[3168];
    }

    public void fire() throws IllegalActionException {
        int i, j;
	int x, y;
        int a;
        int numpartitions = xframesize * yframesize / xpartsize / ypartsize;

        message = (ImageToken) image.get(0);
        frame = message.intArrayRef();

	//for(j = 0, a = numpartitions - 1 ; j < yframesize; j += ypartsize)
        //    for(i = 0; i < xframesize; i += xpartsize, a--) {
        for(j = 0, a = 0 ; j < yframesize; j += ypartsize)
            for(i = 0; i < xframesize; i += xpartsize, a++) {
                for(y = 0; y < ypartsize; y++)
                    System.arraycopy(frame, (j + y) * xframesize + i,
                            part, y * xpartsize, xpartsize);
                partitions[a] = new ImageToken(part, ypartsize, xpartsize);
            }
	
        partition.sendArray(0, partitions);
	
    }

    ImageToken message;
    SDFIOPort partition;
    SDFIOPort image;
    private ImageToken partitions[];
    private int part[];
    private int frame[];
    private int xframesize;
    private int yframesize;
    private int xpartsize;
    private int ypartsize;

}
