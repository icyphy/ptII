/* An attribute that specifies a color.

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

package ptolemy.actor.gui;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

import java.awt.Color;

//////////////////////////////////////////////////////////////////////////
//// ColorAttribute
/**
This is an attribute that specifies a color.  The value of this
attribute, accessed by getExpression(), is a string that represents
a color as an array of four floating point numbers in the form
{red, green, blue, alpha}, where each number is the range of 0.0
to 1.0.  The 'alpha' term represents opacity, where 1.0 is opaque,
and 0.0 is fully transparent (invisible).

@author Edward A. Lee
@version $Id$
*/
public class ColorAttribute extends StringAttribute {

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
    public ColorAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the color as a Color object.
     *  @return The color as a Color object.
     */
    public Color asColor() {
        // NOTE: This same code is in Query.java.
        String spec = getExpression().trim();
        String[] specArray = spec.split("[{},]");
        float red = 0f;
        float green = 0f;
        float blue = 0f;
        float alpha = 1.0f;
        int i = 0;
        // Ignore any blank strings that this simple parsing produces.
        while (specArray[i].trim().equals("")) {
            i++;
        }
        if (specArray.length > i) {
            red = Float.parseFloat(specArray[i]);
        }
        i++;
        while (specArray[i].trim().equals("")) {
            i++;
        }
        if (specArray.length > i) {
            green = Float.parseFloat(specArray[i]);
        }
        i++;
        while (specArray[i].trim().equals("")) {
            i++;
        }
        if (specArray.length > i) {
            blue = Float.parseFloat(specArray[i]);
        }
        i++;
        while (specArray[i].trim().equals("")) {
            i++;
        }
        if (specArray.length > i) {
            alpha = Float.parseFloat(specArray[i]);
        }
        return new Color(red, green, blue, alpha);
    }
}
