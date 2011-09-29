/*

Copyright (c) 2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.vergil.basic.layout;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * An attribute for parameters of automatic layout. This is read by the KIELER
 * layout action to generate a configuration for the layout algorithm.
 *
 * @see ptolemy.vergil.basic.layout.kieler.KielerLayoutAction
 * @author Miro Spoenemann
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class LayoutConfiguration extends Attribute {
    
    /**
     * Create and initialize a layout configuration.
     * 
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public LayoutConfiguration(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        spacing = new Parameter(this, "spacing");
        spacing.setDisplayName("Spacing");
        spacing.setTypeEquals(BaseType.DOUBLE);
        spacing.setExpression("10.0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public parameters                   ////

    /** The overall spacing between graph elements. */
    public Parameter spacing;

}
