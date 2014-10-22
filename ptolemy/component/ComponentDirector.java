/* Base class for directors for component domains.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// ComponentDirector

/**
 FIXME

 @author Yang Zhao and Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating yellow (ellen_zh)
 @Pt.AcceptedRating red (davisj)
 */
public class ComponentDirector implements Component {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the component.  This is invoked once after
     *  preinitialize() and again whenever the component needs
     *  to be reinitialized.
     *  @exception IllegalActionException If initialization
     *   cannot be completed.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // FIXME
    }

    /** Preinitialize the component. This is invoked exactly
     *  once per execution of a model, before any other methods
     *  in this interface are invoked.
     *  @exception IllegalActionException If preinitialization
     *   cannot be completed.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // FIXME
    }

    /** Execute the component. This is invoked after preinitialize()
     *  and initialize(), and may be invoked repeatedly.
     * @exception IllegalActionException If the run cannot be completed.
     */
    @Override
    public void run() throws IllegalActionException {
        // FIXME
    }

    /** Wrap up an execution. This method is invoked exactly once
     *  per execution of a model. It finalizes an execution, typically
     *  closing files, displaying final results, etc. If any other
     *  method from this interface is invoked after this, it must
     *  begin with preinitialize().
     *  @exception IllegalActionException If wrapup fails.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // FIXME
    }
}
