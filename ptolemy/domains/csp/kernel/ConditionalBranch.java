/* Parent class of conditional rendezvous branches.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// ConditionalBranch
/**
Base class of both conditional communication classes (send and receive). 
For rendezvous, the receiver is the key synchronization point. 
Conditional branches are designed to be used once. Upon instantiation, 
they are given the receiver to try to rendezvous with, the parent 
object they are performing the conditional rendezvous for, and the 
identification number of the branch according to the parent.
<p>
A conditional branch is created to perfrom a single conditional communication.
The information it contains in its private memebers is immutable and 
fixed upon creation.
<p>
FIXME: does this class want/need to have a notion of workspace?
 
@author  Neil Smyth
@version $Id$

*/

public abstract class ConditionalBranch {

  /** Create a conditional branch. 
   * @param rec The receiver the branch must try to rendezvous with.
   * @param par The CSPActor that is executing a conditional 
   *  communication construct(CIF or CDO).
   * @param branch The identification number assigned to this branch
   *   upon creation by the CSPActor. 
   */
  public ConditionalBranch(CSPReceiver rec, CSPActor par, int branch){
    _receiver = rec;
    _parent = par;
    _branchNumber = branch;
  }
  
  ////////////////////////////////////////////////////////////////////////
  ////                         public methods                         ////

  /** Returns the identification number of this branch(according to its
   *  parent).
   *  @return The identification number of this branch.
   */
  public int getBranchNumber() {
    return _branchNumber;
  }

  /** Return the CSPActor that created this branch.
   * @return The CSPActor that created this branch.
   */
  public CSPActor getParent() {
    return _parent;
  }
  
  /** Return the CSPReceiver this branch is trying to rendezvous with.
   *  @return The CSPReceiver this branch is trying to rendezvous with.
   */
  public CSPReceiver getReceiver() {
    return _receiver;
  }
  
  /** Boolean indicating if this branch is still alive. If it is false, it 
   *  indicates another conditional branch was able to rendezvous before 
   *  this branch. The branch should stop trying to rendezvous with 
   *  its receiver and terminate
   *  @return boolean indicating if this branch is still alive(needed).
   */
  public boolean isAlive() {
    return _alive;
  }

  /** Set whether this branch is still alive.
   * FIXME: this method is only ever called with a false value, perhaps
   * should modify method to public void finishUp ??
   *
   * @param value boolean indicating whether this branch is still alive.
   */
  public void setAlive(boolean value) {
    _alive = value;
  }

  ////////////////////////////////////////////////////////////////////////
  ////                         private variables                      ////

  // Has another branch successfully rendezvoused? 
  private boolean _alive = true;

  // The receiver this thread is trying to rendezvous with. It is immutable.
  private CSPReceiver _receiver;

  // The parent this thread is trying to perform a conditional rendezous for.
  private CSPActor _parent;

  // The identification number of this branch (according to its parent)
  private int _branchNumber;
}
	  
	       
	 

	    
