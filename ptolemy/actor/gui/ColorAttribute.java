/* An attribute that specifies a color.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Color;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ColorAttribute

/**
 This is an attribute that specifies a color.  The value of this
 attribute is an array of four doubles in the form
 {red, green, blue, alpha}, where each number is the range of 0.0
 to 1.0.  The 'alpha' term represents opacity, where 1.0 is opaque,
 and 0.0 is fully transparent (invisible).

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColorAttribute extends Parameter {

    /** Construct an attribute with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ColorAttribute(String name) throws IllegalActionException,
    NameDuplicationException {
        super();
        setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

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
        setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the color as a Color object.
     *  @return The color as a Color object.
     */
    public Color asColor() {
        // NOTE: This also has to be handled by Query.java.
        try {
            ArrayToken spec = (ArrayToken) getToken();
            int length = 0;

            if (spec != null) {
                length = spec.length();
            }

            // Default values allow us to tolerate incomplement specs.
            float red = 0f;
            float green = 0f;
            float blue = 0f;
            float alpha = 1.0f;

            if (length > 0) {
                red = (float) ((DoubleToken) spec.getElement(0)).doubleValue();
            }

            if (length > 1) {
                green = (float) ((DoubleToken) spec.getElement(1))
                        .doubleValue();
            }

            if (length > 2) {
                blue = (float) ((DoubleToken) spec.getElement(2)).doubleValue();
            }

            if (length > 3) {
                alpha = (float) ((DoubleToken) spec.getElement(3))
                        .doubleValue();
            }

            return new Color(red, green, blue, alpha);
        } catch (IllegalActionException ex) {
            // getToken() failed for some reason.
            return Color.black;
        }
    }
}
