/** An actor that slices the input bits and output a consecutive subset
  of the input bits. 

 Copyright (c) 1998-2006 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Multiplexor

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the slicing of the bits of the input token value. The bit width of
 the output token value is determined by taking the difference of parameters
 start and end. The width parameter specifies the bit width of the input 
 value. The output FixPoint value is unsigned, and all its bits are integer
 bits. The input can have any scalar type. 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Multiplexor extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Multiplexor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        input = new TypedIOPort(this,"input",true,false);
        input.setMultiport(true);  
        input.setTypeEquals(BaseType.FIX);
        
        select = new TypedIOPort(this,"select",true,false);
        select.setTypeEquals(BaseType.FIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Input for different data tokens.  This is a multiport of fix 
     *  point type.
     */
    public TypedIOPort input;
 
    /** Input for select one of the inputs.  This port has int type.
     */
    public TypedIOPort select;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    
    /** output a consecutive subset of the input bits. 
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if( select.isKnown() && input.isKnown() ) {
            if (select.hasToken(0)) {
                FixToken channel = (FixToken) select.get(0);
                _checkFixMaxValue(channel,input.getWidth()-1);
                _channel = channel.fixValue().getUnscaledValue().intValue();            
            }
    
            for (int i = 0; i < input.getWidth(); i++) {
    
                if (input.hasToken(i)) {
                    Token token = input.get(i);
    
                    if (i == _channel) {
                        sendOutput(output,0, token);
                    }
                }
            }
    
        }
        else {
            output.resend(0);
        }
    }
    
    /** Initialize to the default, which is to use channel zero. */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _channel = 0;
    }
    
    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(input, output);
        removeDependency(select, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The most recently read select input. */
    private int _channel = 0;
    
}
