/* A memory address used in Cache Aware Scheduler

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

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.apps.cacheAwareScheduler.kernel;
 
///////////////////////////////////////////////////////////////////////////
//// MemoryAddress
/**

   This class represents a memory address for data produced and consumed by
   SDF actors. It is primarily used by cache aware scheduler during schedule
   generation.

   This class doesn't have any member functions, its simply a collection of 
   two data items namely the actor id that produced this data and the 
   index of the produced token.

   @author Sanjeev Kohli
   @version $Id$
   @since Ptolemy II 2.0

 */

public class MemoryAddress {

    /** Default constructor.
     */
    public MemoryAddress() {
        actorID = 0;
        tokenNumber = 0;
    }

    /** Parameterized constructor.
     *
     *  @param ID The id of the actor that produces this data.
     *  @param tokenNo The token number of data produced by the associated 
     *   actor.
     */
    public MemoryAddress(int ID, int tokenNo) {
        actorID = ID;
        tokenNumber = tokenNo;
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////
    
    /** Check if the passed object is equal to this memory address 
     *  object. If the actorId and token number of the passed objects
     *  is same as this object, then its considered to be equal to this
     *  object.
     *
     *  @param  memoryAddress The MemoryAddress object we want to compare
     *   with this object.
     *  @return boolean True iff equal else False.
     */
    public boolean equals(Object memoryAddress) {
        if(((MemoryAddress)memoryAddress).actorID == actorID) {
            if(((MemoryAddress)memoryAddress).tokenNumber == tokenNumber){
                return true;
            } else return false;
        } else return false;
    }
        

    /** Returns a hashcode for this object. This function overrides the
     *  hashcode function of the Object class.
     * 
     *  @return The hascode of this object.
     */
    public int hashCode() {
        return (actorID*1000 + tokenNumber);
    }
    
    /** This method sets the actorId and tokenNumber equal to the specified
     *  parameters.
     *
     *  @param ID The id of the actor that produces this data.
     *  @param tokenNo The token number of data produced by the associated 
     *   actor.
     */
    public void setFields(int ID, int tokenNo) {
        actorID = ID;
        tokenNumber = tokenNo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public variables                     ////
    
    // ID of the actor that produced this data token
    public int actorID;
    // Index of the produced token
    public int tokenNumber;
}
        
