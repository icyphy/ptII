/* An icon that renders the value of all attributes of the container.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.util.Iterator;

import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// BoxedValuesIcon
/**
This icon displays the value of all visible attributes of class Settable
contained by the container of this icon. Visible attributes are those
whose visibility is Settable.FULL. The names and values of the attributes
are displayed in a box that resizes as necessary. If any line is longer
than <i>displayWidth</i> (in characters), then it is truncated.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class BoxedValuesIcon extends BoxedValueIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public BoxedValuesIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the string to render in the icon.  This string is the
     *  expression giving the value of the attribute of the container
     *  having the name <i>attributeName</i>, truncated so that it is
     *  no longer than <i>displayWidth</i> characters.  If it is truncated,
     *  then the string has a trailing "...".  If the string is empty,
     *  then return a string with one space (diva fails on empty strings).
     *  @return The string to display, or null if none is found.
     */
    protected String _displayString() {
        NamedObj container = (NamedObj)getContainer();
        if (container != null) {
            StringBuffer buffer = new StringBuffer();
            Iterator settables = container.attributeList(Settable.class).iterator();
            while (settables.hasNext()) {
                Settable settable = (Settable)settables.next();
                if (settable.getVisibility() != Settable.FULL) {
                    continue;
                }
                String name = settable.getName();
                String value = settable.getExpression();
                String line = name + ": " + value;
                String truncated = line;
                try {
                    int width = ((IntToken)displayWidth.getToken()).intValue();
                    if (line.length() > width) {
                        truncated = line.substring(0, width) + "...";
                    }
                } catch (IllegalActionException ex) {
                    // Ignore... use whole string.
                }
                buffer.append(truncated);
                if (settables.hasNext()) {
                    buffer.append("\n");
                }
            }
            return buffer.toString();
        }
        return null;
    }
}
