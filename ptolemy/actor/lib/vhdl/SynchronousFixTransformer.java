/* A base class for actors that transform an input stream into an output stream.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.vhdl;

import ptolemy.data.FixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SynchronousFixPointTransformer

/**
 This is an abstract base class for actors that transform
 an input stream into an output stream with a specified latency
 parameter. The default latency is initially set to 0.
 
 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class SynchronousFixTransformer extends FixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SynchronousFixTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        latency = new Parameter(this, "latency");
        latency.setExpression("0");
    }

    public void attributeChanged(Attribute attribute) 
            throws IllegalActionException {
        if (attribute == latency) {
            int latencyValue = ((ScalarToken) latency.getToken()).intValue();
            for (int i = 0; i < output.getWidth(); i++) {
                // clear all output channels.
                while (output.get(i) != null) ;
                
                // sending dummy tokens to create latency.
                for (int j = 0; j < latencyValue; j++) {
                    output.send(i, FixToken.NIL);
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number cycle delay of the output data. 
     */
    public Parameter latency;
}
