/* Interface for port with notion of input/output.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)

*/

package pt.actors;
import pt.kernel.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// IOPort
/** 
Abstract class for a port that can serve as an input, output, or both.
This class defines the interface for exchanging data between entities.
An IOPort can have width, so that multiple connections to another IOPort
can be acheived via one relation with the same width.
A key method defined in IOPort is deepReceptionist, which plays a role
in the propagation of Receptionist arrays. Create Receptionist array and
propagate these arrays throw Entities is an important part of the data
transfer mechanism. For more information about the data transfer, see
the Design Document.

@author Jie Liu
@version $Id$
*/
public interface IOPort {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Key method for propage Receptionist array.
     *  Implementation may differ for different IOPorts.
     *  An input AtomicIOPort should create/return the Receptionist array
     *  correspoding to the given relation.
     *  An TransparentIOPort should concatinate the Receptionist array
     *  from the other side of the given relation and return the 
     *  sun array corresponding to the given relation.
     *  @see AtomicIOPort
     *  @see TransparentIOPort
     *  @param relation The linked relation ased for Receptionists.
     *  @return The Receptionist array corresponding to the given relation.
     *  @exception If the Port/Relation connection is inconsistent, eg. 
     *             unmatched width.
     */
    public Receptionist[][] deepReceptionists(IORelation relation)
           throws InvalidStateException;

 
    /** Return true if the port is an input. 
     */	
    public boolean isInput(); 

    /** Return true if the port is an output. 
     */	
    public boolean isOutput() ;

    /** If the argument is true, make the port an input port.
     *  If the argument is false,
     *  make the port not an input port.
     */	
    public void makeInput(boolean isinput); 

    /** If the argument is true, make the port an output port.
     *  If the argument is false,
     *  make the port not an output port.
     */	
    public void makeOutput(boolean isoutput);
    
    /** Return the width of the port. It will check the version of the 
     *  workspace. If the version is obsoleted, use updateWidth to find
     *  the new width.
     * @return The width of the port.
     */
    public int getWidth(); 

}





