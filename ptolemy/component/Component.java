/* Interface for defining how an object can be invoked.

Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.component;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Component
/**
   This interface defines the interface for component. It contains a sub
   set of methods of the executable interface, i.e. it is more coarse 
   grained.
   FIXME: should this only keep initialize or preinitialize?
   should it containe the fire method?

   @author Yang Zhao
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating yellow (ellen_zh)
   @Pt.AcceptedRating red (davisj)
*/
public interface Component {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Begin execution of the component.  This is invoked exactly once
     *  after the preinitialization phase.  Since type resolution is done
     *  in the preinitialization phase, along with topology changes that
     *  may be requested by higher-order function actors, an actor
     *  can produce output data and schedule events in the initialize()
     *  method.
     *
     *  @exception IllegalActionException If execution is not permitted.
     */
    public void initialize() throws IllegalActionException;


    /** This method should be invoked exactly once per execution
     *  of a model, before any of these other methods are invoked.
     *  For actors, this is invoked prior to type resolution and
     *  may trigger changes in the topology, changes in the
     *  type constraints.
     *
     *  @exception IllegalActionException If initializing is not permitted.
     */
    public void preinitialize() throws IllegalActionException;


    /** This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.  It finalizes an execution, typically closing
     *  files, displaying final results, etc.  When this method is called,
     *  no further execution should occur.
     *
     *  @exception IllegalActionException If wrapup is not permitted.
     */
    public void wrapup() throws IllegalActionException;
}
