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

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/**
 * Specialized layout configuration dialog for 
 * {@link ptolemy.vergil.actor.ActorGraphModel ActorGraphModel}s.
 * 
 * @version $Id$
 * @author Ulf Rueegg
 * @since Ptolemy II 11.0
 */
public class ActorLayoutConfiguration extends AbstractLayoutConfiguration {

    ///////////////////////////////////////////////////////////////////
    ////                       public parameters                   ////

    /** Whether bends are minimized.  The default value is true. */
    public Parameter minimizeBends;
    
    /** Default value for minimizeBends. */
    public static final boolean DEF_MINIMIZE_BENDS = true;
    
    /**
     * Creates an initializes a layout configuration specifically tailored 
     * for {@link ptolemy.vergil.actor.ActorGraphModel ActorGraphModel}s.
     * 
     * @param container The container.
     * @param name The name of this attribute.
     * @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ActorLayoutConfiguration(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        minimizeBends = new Parameter(this, "minimizeBends");
        minimizeBends.setDisplayName("Minimize edge bends");
        minimizeBends.setTypeEquals(BaseType.BOOLEAN);
        minimizeBends.setExpression(Boolean.toString(DEF_MINIMIZE_BENDS));
        minimizeBends.setVisibility(Settable.EXPERT);

    }
}
