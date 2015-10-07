/*

Copyright (c) 2015 The Regents of the University of California.
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

import de.cau.cs.kieler.kiml.options.Direction;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * Specialized layout configuration dialog for
 * {@link ptolemy.vergil.modal.FSMGraphModel FSMGraphModel}s.
 * 
 * @version $Id$
 * @author Ulf Rueegg
 * @since Ptolemy II 11.0 
 */
public class ModalLayoutConfiguration extends AbstractLayoutConfiguration {

    ///////////////////////////////////////////////////////////////////
    ////                       public parameters                   ////
    
    /** Whether the edges of FSMs should be routed and drawn as splines. */
    public Parameter drawSplines;
    
    /** Default value for useSplines. */
    public static final boolean DEF_USE_SPLINES = true;
    
    /** Specifies the direction into which the "flow" of the layout points. */
    public ChoiceParameter direction;
    
    /** Default direction. */
    public static final Direction DEF_DIRECTION = Direction.DOWN;

    /**
     * Creates an initializes a layout configuration specifically tailored 
     * for {@link ptolemy.vergil.modal.FSMGraphModel FSMGraphModel}s.
     *  
     * @param container The container.
     * @param name The name of this attribute.
     * @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ModalLayoutConfiguration(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        drawSplines = new Parameter(this, "useSplines");
        drawSplines.setDisplayName("Use splines for arcs");
        drawSplines.setTypeEquals(BaseType.BOOLEAN);
        drawSplines.setExpression(Boolean.toString(DEF_USE_SPLINES));

        direction = new ChoiceParameter(this, "direction", Direction.class);
        direction.setDisplayName("Layout direction");
        direction.setExpression(DEF_DIRECTION.toString());
        
    }
}
