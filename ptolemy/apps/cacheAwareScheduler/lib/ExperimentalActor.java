/* Used by CacheAwareScheduler to conduct experiments.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.apps.cacheAwareScheduler.lib;

import ptolemy.actor.Director;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ExperimenalActor
/**
This actor consumes the tokens available at the input ports according to
their respective consumption rates and produces token at the output ports
according to the their respective production rates.

This actor is data polymorphic. It can accept any token
type on the input.

This actor contains a parameter "codeSize" that describes the code size
of this actor in terms of the instruction count.

@author Sanjeev Kohli
@version $Id$
@since Ptolemy II 1.0
*/

public class ExperimentalActor extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ExperimentalActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.INT);
        output.setTypeEquals(BaseType.INT);
        // Set parameter codeSize.
        codeSize = new Parameter(this, "codeSize");
        codeSize.setExpression("1 + roundToInt(random()*(iSPMSize-1))");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The code size of the given actor in terms of instruction count.
     */
    public Parameter codeSize;

     ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>codeSize</i> parameter, then
     *  set the codeSize,  and invalidate the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == codeSize) {
            int codeSizeValue = ((IntToken)codeSize.getToken()).intValue();
            if (codeSizeValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid codeSize: " + codeSizeValue);
            }
        } 
        else {
            super.attributeChanged(attribute);
        }
    }

    /** Doesn't do anything at this time as its not required for schedule
     *  generation.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }
}
