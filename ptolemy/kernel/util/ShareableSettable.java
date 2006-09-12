/* Base class for objects with a name and a container.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.kernel.util;

import java.util.Collection;

/**
 *  An interface for attributes that are shared.
 *  Ptolemy.moml.SharedParameter implements this class so that
 *  we can optimize NamedObj.validateSettables() so as to avoid
 *  validating multiple instances of the same SharedParameter multiple 
 *  times.

 @author Christopher Brooks, Collaborator: Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface ShareableSettable {

    /** Return a collection of all the shared parameters within the
     *  same model as this parameter.  If there are no such parameters
     *  or if this parameter is deeply contained within an EntityLibrary, then
     *  return an empty collection. The list will include this instance if
     *  this instance.
     *  A shared parameter is one that is an instance of SharedParameter,
     *  has the same name as this one, and is contained by the container
     *  class specified in the constructor.
     *  @return A list of parameters.
     */
    public /*synchronized*/ Collection sharedParameterSet();

    /** Override the base class to also validate the shared instances.
     *  @return A Collection of all the shared parameters with the same
     *  model as this parameter {@link #sharedParameterSet}.
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    public Collection validateShareableSettable() throws IllegalActionException;
}
