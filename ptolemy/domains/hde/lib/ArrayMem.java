
/** Alter of Extract the ith element from an array.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
port
Read parallel: read the entire array and send it to the parallel output
port  
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
      
      dataInPar = new TypedIOPort(this, "dataInPar", true, false);
      dataInPar.setTypeEquals(new ArrayType(BaseType.INT));
      ArrayType dataInParType = (ArrayType)dataInPar.getType();
      InequalityTerm elementTerm = dataInParType.getElementTypeTerm();

      dataOutPar = new TypedIOPort(this, "dataOutPar", false, true);
      dataOutPar.setTypeEquals(dataInParType);
     

       dataInSer  = new TypedIOPort(this, "dataInSer", true, false);
       dataInSer.setTypeEquals(BaseType.INT);
       

       dataOutSer = new TypedIOPort(this, "dataOutSer", false, true);
       dataOutSer.setTypeEquals(BaseType.INT);

       serPar = new TypedIOPort(this, "serPar", true, false);
       serPar.setTypeEquals(BaseType.BOOLEAN);
      
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
     * be less than or equal to the
     *  length of the input array.
     */
    public TypedIOPort index;
   /** The read input*/
    public TypedIOPort read;
   /** The write input*/
    public TypedIOPort write;
    /**The ser/par control for reading and writing**/
    public TypedIOPort serPar; 
   /** The serieal data input*/
    public  TypedIOPort dataInSer;
   /** The serial data output*/
    public  TypedIOPort dataOutSer;
    /** The parallel  data output*/
    public TypedIOPort dataOutPar;
     /** The parallel input data*/
    public TypedIOPort dataInPar;

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
  /** If read has a token, copy the ith elment of the memory to dataOutSer.
     *  If write has a token, copy the dataInSer input to the ith element of memory.
     *  If init has a token, copy the dataInPar input to the internal array*
     *  @exception IllegalActionException If the index input
     *   is out of range.
     */
    public void fire() throws IllegalActionException {
	BooleanToken yes=BooleanToken.TRUE;
      /**Read the Serial/Parallel Control*/
        if(serPar.hasToken(0)){
           _serPar = ((BooleanToken)serPar.get(0)).booleanValue();
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
	   if(_serPar){
	     _mem[_index]=(Token)dataInSer.get(0);
            }
	   else {if(dataInPar.hasToken(0)){
              ArrayToken token = (ArrayToken)dataInPar.get(0);
               for(int i = 0; i < alength; i++) {
               _mem[i]=(token.getElement(i));
               }
             }
	   }
	 }   
      /** Read from the array*/
      if(read.hasToken(0)){ 
          _read  = (BooleanToken)read.get(0);
	 if(_read.isEqualTo(yes).booleanValue()){
	     if(_serPar){
             dataOutSer.send(0,_mem[_index]);
       	      }
	     else{

	     dataOutPar.send(0,new ArrayToken(_mem));
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
    private boolean  _serPar;

   //The linear array in which the data is stored. The length of the
   // array is specified by the length Parameter;

     private  Token[]  _mem;
}
