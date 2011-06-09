/* A parameter that is in string mode and has a list of choices.

 Copyright (c) 2003-2009 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ChoiceParameter

/**
 This parameter contains a string value and has a list of acceptable choices.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ChoiceParameter extends StringParameter {

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
     *  @param enumClass The enum class that contains the choices.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public ChoiceParameter(NamedObj container, String name, Class<?> enumClass)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        if (!enumClass.isEnum()) {
            throw new IllegalActionException("Only a Java enum class is "
                    + "accepted as parameter to enumClass.");
        }
        int i = 0;
        for (Object enumObject : enumClass.getEnumConstants()) {
            String value = enumObject.toString();
            addChoice(value);
            if (i++ == 0) {
                setExpression(value);
            }
        }
        _enumClass = enumClass;
    }

    /** Get the choice as a member of the enum class. The string value of that
     *  member (returned with its toString() method) is equal to the string
     *  value contained by this parameter.
     *
     *  @return The chosen member of the enum class.
     */
    public Object getChosenValue() {
        String expression = getExpression();
        for (Object enumObject : _enumClass.getEnumConstants()) {
            if (expression.equals(enumObject.toString())) {
                return enumObject;
            }
        }
        return null;
    }

    private Class<?> _enumClass;
}
