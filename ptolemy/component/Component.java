/* Interface with basic method for initializing and executing components.

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
package ptolemy.component;

import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// Component

/**
   This interface defines the basic methods for initializing and
   executing components. The intended usage is that preintialize()
   is invoked exactly once in an execution of a model, before any
   static analysis such as scheduling or type resolution is done.
   The initialize() method may be invoked more than once to
   initialize() a component, and it is invoked after static analysis.
   The run() method may be invoked multiple times after initialize().
   The wrapup() method is invoked exactly once at the end of an
   execution of a model. It is important that an implementor ensure
   that wrapup() is called even if an exception occurs.

   @author Yang Zhao and Edward A. Lee
   @version $Id$
   @since Ptolemy II 5.0
   @Pt.ProposedRating yellow (ellen_zh)
   @Pt.AcceptedRating red (davisj)
*/
public interface Component {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the component.  This is invoked once after
     *  preinitialize() and again whenever the component needs
     *  to be reinitialized.
     *  @exception IllegalActionException If initialization
     *   cannot be completed.
     */
    public void initialize() throws IllegalActionException;

    /** Preinitialize the component. This is invoked exactly
     *  once per execution of a model, before any other methods
     *  in this interface are invoked.
     *  @exception IllegalActionException If preinitialization
     *   cannot be completed.
     */
    public void preinitialize() throws IllegalActionException;

    /** Execute the component. This is invoked after preinitialize()
     *  and initialize(), and may be invoked repeatedly.
     * @exception IllegalActionException If the run cannot be completed.
     */
    public void run() throws IllegalActionException;

    /** Wrap up an execution. This method is invoked exactly once
     *  per execution of a model. It finalizes an execution, typically
     *  closing files, displaying final results, etc. If any other
     *  method from this interface is invoked after this, it must
     *  begin with preinitialize().
     *  @exception IllegalActionException If wrapup fails.
     */
    public void wrapup() throws IllegalActionException;
}
