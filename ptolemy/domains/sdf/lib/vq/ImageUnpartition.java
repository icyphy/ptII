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
@AcceptedRating Red
@ProposedRating Red
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
//// ImageUnpartition
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class ImageUnpartition extends SDFAtomicActor {
    public ImageUnpartition(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

	imageColumns = 
            new Parameter(this, "imageColumns", new IntToken("176"));
        imageRows = 
            new Parameter(this, "imageRows", new IntToken("144"));
        partitionColumns = 
            new Parameter(this, "partitionColumns", new IntToken("4"));
        partitionRows = 
            new Parameter(this, "partitionRows", new IntToken("2"));

        input = (SDFIOPort) newPort("input");
        input.setInput(true);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(IntMatrixToken.class);
        output.setTokenProductionRate(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

    /** The width of the input matrices */
    public Parameter imageColumns;

    /** The height of the input matrices */
    public Parameter imageRows;

    /** The width of the input partitions */
    public Parameter partitionColumns;

    /** The height of the input partitions */
    public Parameter partitionRows;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            ImageUnpartition newobj = (ImageUnpartition)(super.clone(ws));
            newobj.output = (SDFIOPort)newobj.getPort("output");
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.imageRows = 
                (Parameter)newobj.getAttribute("imageRows");
            newobj.imageColumns = 
                (Parameter)newobj.getAttribute("imageColumns");
            newobj.partitionRows = 
                (Parameter)newobj.getAttribute("partitionRows");
            newobj.partitionColumns = 
                (Parameter)newobj.getAttribute("partitionColumns");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Initialize this actor
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _imageColumns = ((IntToken)imageColumns.getToken()).intValue();
        _imageRows = ((IntToken)imageRows.getToken()).intValue();
        _partitionColumns = ((IntToken)partitionColumns.getToken()).intValue();
        _partitionRows = ((IntToken)partitionRows.getToken()).intValue();

        image = new int[_imageRows * _imageColumns];
        int partitionCount = _imageColumns * _imageRows
                / _partitionColumns / _partitionRows;
        partitions = new IntMatrixToken[partitionCount];
        input.setTokenConsumptionRate(partitionCount);
    }

    /**
     * Fire this actor
     * Consume IntMatrixTokens on the input port corresponding to the
     * partitions of an image.  Reassemble the image and produce a
     * single IntMatrixToken on the output port.
     *
     * @exception IllegalActionException If the ports are not connected.
     */
    public void fire() throws IllegalActionException {
        int i, j;
	int x, y;
        int a;

        input.getArray(0, partitions);
        
        for(j = 0, a = 0; j < _imageRows; j += _partitionRows)
            for(i = 0; i < _imageColumns; i += _partitionColumns, a++) {
                part = partitions[a].intArray();
                for(y = 0; y < _partitionRows; y++)
                    System.arraycopy(part, y * _partitionColumns,
                            image, (j + y) * _imageColumns + i,
                            _partitionColumns);
            }
        
        output.send(0, 
                new IntMatrixToken(image, _imageRows, _imageColumns));
    }

    private IntMatrixToken partitions[];

    private int part[];
    private int image[];
    private int _imageColumns;
    private int _imageRows;
    private int _partitionColumns;
    private int _partitionRows;
}
