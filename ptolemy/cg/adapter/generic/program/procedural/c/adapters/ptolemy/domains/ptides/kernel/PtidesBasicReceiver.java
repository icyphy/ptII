/* Code generator adapter class associated with the PtidesBasicReceiver class.

 Copyright (c) 2005-2009 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////////
////PtidesBasicReceiver

/** The adapter for ptides basic recevier.
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */

public class PtidesBasicReceiver extends ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.Receiver {

    /** Construct a ptides basic receiver.
     *  @throws IllegalActionException 
     */
    public PtidesBasicReceiver (
            ptolemy.domains.ptides.kernel.PtidesBasicReceiver receiver) throws IllegalActionException {
        super(receiver);
    }

    /** Generates code for getting tokens from the receiver.
     *  @return generate get code.
     *  @throws IllegalActionException
     */
    public String generateGetCode(String offset) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort) getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        return "Event_Head_" + getAdapter(port).getName() + "[" + channel + "]->Val."
                + port.getType().toString() + "_Value";
    }

    /** Generates code to check the receiver has token.
     *  @param offset is ignored because it is not applicable in PTIDES.
     *  @return generate hasToken code.
     *  @throws IllegalActionException
     */
    public String generateHasTokenCode(String offset)
            throws IllegalActionException {
        IOPort port = getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        return "Event_Head_" + getAdapter(port).getName() + "[" + channel + "] != NULL";
    }

    /** Generates code for putting tokens from the receiver.
     *  @return generate put code.
     *  @throws IllegalActionException
     */
    public String generatePutCode(IOPort sourcePort, String offset, String token)
            throws IllegalActionException {
        TypedIOPort sinkPort = (TypedIOPort) getComponent().getContainer();
        int sinkChannel = sinkPort.getChannelForReceiver(getComponent());

        Channel source = new Channel(sourcePort, 0);
        Channel sink = new Channel(sinkPort, sinkChannel);
        
        token = ((NamedProgramCodeGeneratorAdapter)getAdapter(
                getComponent().getContainer().getContainer())).getTemplateParser()
                .generateTypeConvertStatement(source, sink, 0, token);

        
        token = _removeSink(token);
        
        Actor actor = (Actor) sinkPort.getContainer();
        Director director = actor.getDirector();
        // Getting depth.
        int depth = ((CausalityInterfaceForComposites) director
                .getCausalityInterface()).getDepthOfActor(actor);
        // Getting deadline.
        Parameter relativeDeadline = (Parameter) sinkPort
                .getAttribute("relativeDeadline");
        String deadlineSecsString = null;
        String deadlineNsecsString = null;
        if (relativeDeadline != null) {
            double value = ((DoubleToken) relativeDeadline.getToken())
                    .doubleValue();
            int intPart = (int) value;
            int fracPart = (int) ((value - intPart) * 1000000000.0);
            deadlineSecsString = Integer.toString(intPart);
            deadlineNsecsString = Integer.toString(fracPart);
        } else {
            deadlineSecsString = new String("0");
            deadlineNsecsString = new String("0");
        }

        // Getting offsetTime.
        Parameter offsetTime = (Parameter) sinkPort.getAttribute("minDelay");
        String offsetSecsString = null;
        String offsetNsecsString = null;
        if (offsetTime != null) {
            double value = ((DoubleToken) offsetTime.getToken()).doubleValue();
            int intPart = (int) value;
            int fracPart = (int) ((value - intPart) * 1000000000.0);
            offsetSecsString = Integer.toString(intPart);
            offsetNsecsString = Integer.toString(fracPart);
        } else {
            offsetSecsString = new String("0");
            offsetNsecsString = new String("0");
        }

        // FIXME: not sure whether we should check if we are putting into an input port or
        // output port.
        // Generate a new event.
        List args = new ArrayList();
        args.add(sinkPort.getType());
        args.add(token);
        args.add((sinkPort.getContainer().getName()));
        args.add("Event_Head_" + sinkPort.getName() + "["
                + sinkPort.getChannelForReceiver(getComponent()) + "]");
        args.add(depth);//depth
        args.add(deadlineSecsString);//deadline
        args.add(deadlineNsecsString);
        args.add(offsetSecsString);//offsetTime
        args.add(offsetNsecsString);
        return _templateParser.generateBlockCode("createEvent", args);
    }

    protected String _generateTypeConvertStatement(Channel source)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }
}
