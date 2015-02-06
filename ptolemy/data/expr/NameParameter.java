/* A string parameter that changes the name of its container to its value.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.util.Collection;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// NameParameter

/**
 <p>This subclass of StringParameter uses its value to set the name of
 its container.

 @author Elaine Cheong
 @version $Id$
@since Ptolemy II 6.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class NameParameter extends StringParameter {
    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public NameParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If this variable is not lazy (the default) then evaluate
     *  the expression contained in this variable, and notify any
     *  value dependents. If those are not lazy, then they too will
     *  be evaluated.  Also, if the variable is not lazy, then
     *  notify its container, if there is one, by calling its
     *  attributeChanged() method.
     *  <p>
     *  If this variable is lazy, then mark this variable and any
     *  of its value dependents as needing evaluation and for any
     *  value dependents that are not lazy, evaluate them.
     *  Note that if there are no value dependents,
     *  or if they are all lazy, then this will not
     *  result in evaluation of this variable, and hence will not ensure
     *  that the expression giving its value is valid.  Call getToken()
     *  or getType() to accomplish that.
     *  <p>
     *  This method differs from its base class implementation in that
     *  if its string value is not empty, then it also attempts to set
     *  the name of its container to the string value.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     *   Also thrown if there is a NameDuplicationException when setting
     *   the name of the container.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        Collection result = null;
        result = super.validate();
        NamedObj container = getContainer();
        if (container != null) {
            try {
                String newName = stringValue();
                if (newName != null && !newName.equals("")) {
                    container.setName(stringValue());
                }
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(this, ex,
                        "Could not set name of container.");
            }
        }
        return result;
    }

}
