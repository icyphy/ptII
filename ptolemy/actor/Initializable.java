/* Interface for objects that can be initialized.

 Copyright (c) 2007-2012 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// Initializable

/**
 This interface defines a subset of the <i>action methods</i> for initialization
 and wrapup. It should be implemented by objects that do not need to invoked
 during runtime, but should be invoked in preinitialize() (exactly once,
 prior to type resolution), in initialize() (which could happen repeatedly during
 execution), or wrapup() (exactly once, at the end of execution).

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public interface Initializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the set of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     */
    public void addInitializable(Initializable initializable);

    /** Begin execution of the actor.  This is invoked exactly once
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

    /** Remove the specified object from the set of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     */
    public void removeInitializable(Initializable initializable);

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
