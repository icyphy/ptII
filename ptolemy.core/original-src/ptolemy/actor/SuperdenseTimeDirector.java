/* Interface for directors that use a super-dense model of time.

 Copyright (c) 2009-2011 The Regents of the University of California.
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

 */
package ptolemy.actor;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// SuperdenseTimeDirector

/**
 This is an interface for directors that use a superdense model of time.
 Actors can determine the index, also called the microstep,
 of current time by calling getIndex().
 <p>
 Superdense time is defined by Haiyang Zheng as:
 <blockquote>
 <p>"The interactions between CT and DE subsystems and between DE
 subsystems themselves are captured by discontinuities in
 continuous-time signals and simultaneous discrete events in
 discrete-event signals."</p>

 <p>"In order to precisely represent them in compute execution
 results, a two-dimension domain, called "superdense time," is used
 as the domain for defining signals. This domain allows a signal to
 have multiple values at the same time point while keeping the values
 ordered."</p>
 </blockquote>
 <p>See: Haiyang Zheng.
<a href="https://ptolemy.berkeley.edu/projects/chess/pubs/303.html#in_browser" target="_top"><i>Operational Semantics of Hybrid Systems</i></a>, PhD thesis,
 University of California, Berkeley, May, 2007.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public interface SuperdenseTimeDirector {
    /** Return a superdense time index for the current time.
     *  @return A superdense time object.
     *  @see #setIndex(int)
     */
    public int getIndex();

    /** Set the superdense time index. This should only be
     *  called by an enclosing director.
     *  @param index The index of the superdense time object.
     *  Events that occur at the same time have different indicies.
     *  @exception IllegalActionException If the specified index is invalid.
     *  @see #getIndex()
     */
    public void setIndex(int index) throws IllegalActionException;
}
