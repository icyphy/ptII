/** Alter of Extract the ith element from an array.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
import java.util.List;
import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
//import ptolemy.kernel.util.IllegalActionException;
//import ptolemy.kernel.util.NameDuplicationException;
//import ptolemy.kernel.util.InternalErrorException;
//import ptolemy.kernel.util.Workspace;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ArrayMem
/**
Alter or Extract the ith element from an internal array. 
Read:  read the ith element from the internal array and send it to the output
port.  
Write: write the data input to the ith element of an internal array.
Initialize: write the initializing data input to the internal array.
It is required that the value of the input index be less than or equal to the 
length parameter.
@see LookupTable
@see RecordDisassembler
@authors Edward A. Lee, Elaine Cheong,Jim Armstrong
@version $Id$
*/

public class ArrayMem extends TypedAtomicActor {
    
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayMem(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	// Set type constraints.
	index = new TypedIOPort(this, "index", true, false);
      index.setTypeEquals(BaseType.INT);
      init = new TypedIOPort(this, "init", true, false);
      init.setTypeEquals(BaseType.BOOLEAN);
      initData = new TypedIOPort(this, "initData", true, false);
      initData.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
      ArrayType initDataType = (ArrayType)initData.getType();
      InequalityTerm elementTerm = initDataType.getElementTypeTerm();
      read = new TypedIOPort(this, "read", true, false);
      read.setTypeEquals(BaseType.BOOLEAN);
      dataIn = new TypedIOPort(this, "dataIn", true, false);
      write  = new TypedIOPort(this, "write", true, false);
      write.setTypeEquals(BaseType.BOOLEAN);
      dataOut = new TypedIOPort(this, "dataOut", false, true);
      dataIn.setTypeAtLeast(elementTerm);
      dataOut.setTypeAtLeast(elementTerm);


       // Set parameters.
        length = new Parameter(this, "length");
        length.setExpression("1");
        
        

        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Inputs                      ////

    /** The index into the input array.  This is an integer that is required to
     * be less than or equal to the
     *  length of the input array.
     */
    public TypedIOPort index;
   /** The read input*/
    public TypedIOPort read;
   /** The write input*/
    public TypedIOPort write;
   /** The data input*/
    public  TypedIOPort dataIn;
   /** The data output*/
    public  TypedIOPort dataOut;
   /** The initialize control input*/
    public TypedIOPort init;
   /** The initialization data*/
    public TypedIOPort initData;

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The length of the input array.  This is an integer that
      * defaults to 1; 
     */
    public Parameter length;
    ///////////////////////////////////////////////////////////////////
    ////                         variable                       //////
    public int alength;

     

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Get the value of the lenght parameter
     *  Initialize the memory array
     */
        
    public void initialize() throws IllegalActionException{
	 alength =((IntToken)length.getToken()).intValue();
         _mem = new Token[alength];
    }  
  /** If read has a token, copy the ith elment of the memory to dataOut.
     *  If write has a token, copy the dataIn input to the ith element of memory.
     *  If init has a token, copy the initData input to the internal array*
     *  @exception IllegalActionException If the index input
     *   is out of range.
     */
    public void fire() throws IllegalActionException {
	BooleanToken yes=BooleanToken.TRUE;
      
      /** Initialize the Array*/
      if(init.hasToken(0)){
	  _init = (BooleanToken)init.get(0);
          if (_init.isEqualTo(yes).booleanValue()){
	    if(initData.hasToken(0)){
              ArrayToken token = (ArrayToken)initData.get(0);
               for(int i = 0; i < alength; i++) {
               _mem[i]=(token.getElement(i));
               }
             }
           }
      }
      /** Read the Index*/	  
      if (index.hasToken(0)) {
          _index = ((IntToken)index.get(0)).intValue();
            if ((_index < 0) || (_index >= alength)) {
		throw new IllegalActionException(this,
		"index " + _index + " is out of range for the memory "
		+ "array, which has length " + alength);
	     }
       } 
      /** Write to the array*/
       if(write.hasToken(0)){
	  _write = (BooleanToken)write.get(0);
       
       if (_write.isEqualTo(yes).booleanValue()){
              
	     _mem[_index]=(Token)dataIn.get(0);
        }
       }
      /** Read from the array*/
      if(read.hasToken(0)){ 
          _read  = (BooleanToken)read.get(0);
	 if(_read.isEqualTo(yes).booleanValue()) {
              	    dataOut.send(0,_mem[_index]);
       
	}
      }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The most recently read index token.
    private int _index = 0;
    private BooleanToken  _read;
    private BooleanToken  _write; 
    private BooleanToken  _init;

   //The linear array in which the data is stored. The length of the
   // array is specified by the length Parameter;

    private  Token[]  _mem;
    
    
}

