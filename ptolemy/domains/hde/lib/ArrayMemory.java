
/** Array Storage with serial or parallel read or write.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (celaine@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/
package ptolemy.domains.hde.lib;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ArrayMem
/**
Alter or Extract the ith element from an internal array.
Read serial:  read the ith element from the internal array and send it to
the serial output port.
Read parallel: read the entire array and send it to the parallel output
port.
Write serial: write the serial data input to the ith element of an internal
 array.
Write parallel: write the parallel  data input to the internal array.
It is required that the value of the input index be less than or equal to the
length parameter.
@see ptolemy.actor.lib.LookupTable
@see ptolemy.actor.lib.RecordDisassembler
@author  Jim Armstrong
@version $Id$
*/

public class ArrayMemory extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayMemory(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Set type constraints.
        index = new TypedIOPort(this, "index", true, false);
        index.setTypeEquals(BaseType.INT);

        dataInParallel = new TypedIOPort(this, "dataInParallel", true, false);
        dataInParallel.setTypeEquals(new ArrayType(BaseType.INT));
        ArrayType dataInParallelType = (ArrayType)dataInParallel.getType();
        InequalityTerm elementTerm = dataInParallelType.getElementTypeTerm();

        dataOutParallel =
            new TypedIOPort(this, "dataOutParallel", false, true);
        dataOutParallel.setTypeEquals(dataInParallelType);


        dataInSerial  = new TypedIOPort(this, "dataInSerial", true, false);
        dataInSerial.setTypeEquals(BaseType.INT);


        dataOutSerial = new TypedIOPort(this, "dataOutSerial", false, true);
        dataOutSerial.setTypeEquals(BaseType.INT);

        serialOrParallel =
            new TypedIOPort(this, "serialOrParallel", true, false);
        serialOrParallel.setTypeEquals(BaseType.BOOLEAN);

        read = new TypedIOPort(this, "read", true, false);
        read.setTypeEquals(BaseType.BOOLEAN);

        write  = new TypedIOPort(this, "write", true, false);
        write.setTypeEquals(BaseType.BOOLEAN);

        // Set parameters.
        length = new Parameter(this, "length");
        length.setExpression("1");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         Inputs                      ////

    /** The index into the input array.  This is an integer that is required to
     * be less than or equal to the  length of the input array.
     */
    public TypedIOPort index;
    /** The read input*/
    public TypedIOPort read;
    /** The write input*/
    public TypedIOPort write;
    /**The ser/par control for reading and writing**/
    public TypedIOPort serialOrParallel;
    /** The serial data input*/
    public  TypedIOPort dataInSerial;
    /** The serial data output*/
    public  TypedIOPort dataOutSerial;
    /** The parallel  data output*/
    public TypedIOPort dataOutParallel;
    /** The parallel input data*/
    public TypedIOPort dataInParallel;

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The length of the input array.  This is an integer that
     * defaults to 1;
     */
    public Parameter length;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Get the value of the length parameter
     *  Initialize the memory array
     */

    public void initialize() throws IllegalActionException{
        _arrayLength = ((IntToken)length.getToken()).intValue();
        _memory = new Token[_arrayLength];
    }
    /** If serialOrParallel = true: If read has a token, copy the
     *  ith element of the memory to
     *  dataOutSerial.  If write has a token, copy the dataInSerial input to
     *  the ith element of memory.
     *  If serialOrParallel = false: If read has a token, copy the contents
     *   of  the memory to  dataOutParallel.  If write has a token, copy the
     *   dataInParallel  input to  the  memory.
     *  @exception IllegalActionException If the index input
     *   is out of range.
     */
    public void fire() throws IllegalActionException {
        BooleanToken yes = BooleanToken.TRUE;
        /**Read the Serial/Parallel Control*/
        if (serialOrParallel.hasToken(0)){
            _serialOrParallel =
                ((BooleanToken)serialOrParallel.get(0)).booleanValue();
        }

        /** Read the Index*/
        if (index.hasToken(0)) {
            _index = ((IntToken)index.get(0)).intValue();
            if ((_index < 0) || (_index >= _arrayLength)) {
                throw new IllegalActionException(this,
                        "index " + _index + " is out of range for the memory "
                        + "array, which has length " + _arrayLength);
            }
        }

        /** Write to the array*/
        if (write.hasToken(0)){
            _write = (BooleanToken)write.get(0);

            if (_write.isEqualTo(yes).booleanValue()){
                if (_serialOrParallel){
                    _memory[_index] = (Token)dataInSerial.get(0);
                }  else {if (dataInParallel.hasToken(0)){
                    ArrayToken token = (ArrayToken)dataInParallel.get(0);
                    for (int i = 0; i < _arrayLength; i++) {
                        _memory[i] = (token.getElement(i));
                    }
                }
                }
            }

            /** Read from the array*/
            if (read.hasToken(0)){
                _read  = (BooleanToken)read.get(0);
                if (_read.isEqualTo(yes).booleanValue()){
                    if (_serialOrParallel){
                        dataOutSerial.send(0, _memory[_index]);
                    } else{
                        dataOutParallel.send(0, new ArrayToken(_memory));
                    }
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The most recently read index token.
    private int _index = 0;
    private BooleanToken  _read;
    private BooleanToken  _write;
    private boolean  _serialOrParallel;

    //The linear array in which the data is stored. The length of the
    // array is specified by the length Parameter;

    private  Token[]  _memory;
    private int _arrayLength;
}








