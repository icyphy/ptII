/* An attribute that offers a fixed set of choices.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.attributes;

import java.util.ArrayList;
import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;


//////////////////////////////////////////////////////////////////////////
//// ChoiceAttribute
/**
This is an attribute that offers a fixed set of choices.
<p>
@author Edward A. Lee
@version $Id$
@see URIAttribute
*/
public class ChoiceAttribute extends StringAttribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ChoiceAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a choice.
     *  @param choice A choice to offer to the user.
     */
    public void addChoice(String choice) {
        _choices.add(choice);
    }
    
    /** Get choices.
     *  @return An array of choices.
     */
    public String[] getChoices() {
        return (String [])_choices.toArray(new String[_choices.size()]);
    }

    /** Return true if setEditable(true) has been called.
     *  @return True to indicate that the choice list is not exhaustive,
     *   and therefore the user should be allowed to enter something else.
     */
    public boolean isEditable() {
        return _editable;
    }

    /** Specify whether the choice list is not exhaustive,
     *   and therefore whether the user should be allowed
     *  to enter something else.
     *  @param editable True to indicate that the choices are editable.
     */
    public void setEditable(boolean editable) {
        _editable = editable;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The List of choices. */
    private List _choices = new ArrayList();
    
    /** An indicator of whether the choices are exhaustive. */
    private boolean _editable;
}
