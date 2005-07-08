/* A TM domain specific actor interface.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.domains.tm.kernel;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// TMActor

/**
 An interface that adds a method getExecutionTime() to the Actor interface.
 This allows actors to estimate its execution time on a per iteration basis,
 and maybe in an input-dependent manner.
 @author Jie Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (liuj)
 @Pt.AcceptedRating Yellow (janneck)

 */
public interface TMActor extends Actor {
    /** Return the execution time for this iteration. This method
     *  will be called by the TMDirector after the prefire() method
     *  is called. The reason for this method is to allow the actor
     *  to determine/estimate its execution time on a per iteration
     *  basis.
     */
    public double getExecutionTime();
}
