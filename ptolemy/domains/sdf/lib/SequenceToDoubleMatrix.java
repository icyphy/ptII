/* Bundle a sequence of n x m input tokens into an DoubleMatrixToken.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFAtomicActor;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SequenceToDoubleMatrix
/**
<p>
This actor bundles a certain number of input tokens into an DoubleMatrixToken.
The number of tokens to be bundled into an DoubleMatrixToken is determined
by the product of the parameters <i>rows</i> and <i>columns</i>.
</p>
<p>
The output matrix is read by filling up row 0 with columns 0 .. n-1
then filling up row 1 with columns 0 .. n-1, until row m-1.
</p>

@author Jeff Tsay
@version $Id$
*/

public class SequenceToDoubleMatrix extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceToDoubleMatrix(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        input.setTypeAtMost(BaseType.DOUBLE);

        output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);

        // the number of rows is defaulted to 1
        rows = new Parameter(this, "rows", new IntToken(1));
        
        // the number of columns is defaulted to 1
        columns = new Parameter(this, "columns", new IntToken(1));

        // set the token consumption rate 
        attributeChanged(columns);

        output.setTokenProductionRate(1);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input receiving the tokens to bundle. */
    public SDFIOPort input;

    /** Output for sending the DoubleMatrixToken. */
    public SDFIOPort output;


    /** The number of rows of the output matrix. This parameter must
     *  evaluate to an IntToken.
     */
    public Parameter rows;

    /** The number of columns of the output matrix. This parameter must
     *  evaluate to an IntToken.
     */
    public Parameter columns;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if ((attribute == rows) || (attribute == columns)) {           
           int iRows = ((IntToken) rows.getToken()).intValue();
           int iColumns = ((IntToken) columns.getToken()).intValue();        
                
           input.setTokenConsumptionRate(iRows * iColumns);
           Director dir = getDirector();

           if (dir != null) {
              dir.invalidateSchedule();
           }                
        } else {
           super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
        throws CloneNotSupportedException {
        SequenceToDoubleMatrix newobj =
            (SequenceToDoubleMatrix) super.clone(ws);
        newobj.input = (SDFIOPort) newobj.getPort("input");
        newobj.output = (SDFIOPort) newobj.getPort("output");

        newobj.rows = (Parameter) newobj.getAttribute("rows");
        newobj.columns = (Parameter) newobj.getAttribute("columns");
        return newobj;
    }

    /** Consume the inputs and produce the output DoubleMatrixToken.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public void fire() throws IllegalActionException {    
        int iRows = ((IntToken) rows.getToken()).intValue();
        int iColumns = ((IntToken) columns.getToken()).intValue();        

        double[][] matrix = new double[iRows][iColumns];

        for (int i = 0; i < iRows; i++) {        
            for (int j = 0; j < iColumns; j++) { 
                matrix[i][j] = ((ScalarToken) input.get(0)).doubleValue();
            }
        }

        // could be optimized not to copy matrix on token construction
        output.send(0, new DoubleMatrixToken(matrix));
    }
}
