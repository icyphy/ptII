/* This is a resource scheduler.

@Copyright (c) 2008-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched;

import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 Container for decorator attributes that are provided to actors.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class StaticSchedulingDirectorAttributes extends DecoratorAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public StaticSchedulingDirectorAttributes(NamedObj target,
            GenericCodeGenerator decorator) throws IllegalActionException,
            NameDuplicationException {
        super(target, decorator);
        _init();
    }

    /** Constructor to use when parsing a MoML file.
     *  @param target The object being decorated.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public StaticSchedulingDirectorAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicator of whether to pad buffers.
     *  This is a boolean that defaults to true.
     */
    public Parameter padBuffers;

    /** Indicator of whether to allow dynamic multiport references.
     *  If true, then channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  This is a boolean that defaults to false.
     */
    public Parameter allowDynamicMultiportReference;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the parameters.
     */
    private void _init() {
        try {
            padBuffers = new Parameter(this, "padBuffers");
            padBuffers.setTypeEquals(BaseType.BOOLEAN);
            padBuffers.setExpression("true");

            allowDynamicMultiportReference = new Parameter(this,
                    "allowDynamicMultiportReference");
            allowDynamicMultiportReference.setTypeEquals(BaseType.BOOLEAN);
            allowDynamicMultiportReference.setExpression("false");
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }
}
