/* Code generator adapter for DEReceiver.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.Receiver;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.lib.ModularCodeGenTypedCompositeActor;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////DEReceiver

/** The adapter for DE receiver.
 * This class generates the DE-specific methods for the director.
 * Those methods are : get, hasToken, put
 * It also deals with the type conversions.
 *
 * Note that for now the generated code is C Code. What we need
 * to do later, is to call some non-languages-specific functions in here
 * which are implemented in a specific C adapter.
 *
 * @author William Lucas, based on SDFReceiver.java by Jia Zou, Man-Kit Leung, Isaac Liu, Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */

public class DEReceiver extends Receiver {
    /** Construct an adapter for an DE receiver.
     *  @param receiver The DEReceiver for which an adapter is constructed.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    public DEReceiver(ptolemy.domains.de.kernel.DEReceiver receiver)
            throws IllegalActionException {
        super(receiver);
        // FIXME: not sure if this is totally correct.
        if (receiver != null
                && receiver.getContainer().getContainer() instanceof CompositeActor) {
            _forComposite = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generates code for getting tokens from the receiver.
     *  @param offset The offset of the port.
     *  @return The generated get code.
     *  @exception IllegalActionException If thrown while getting the component,
     *  getting the adapter, getting the director or getting the port reference.
     */
    @Override
    public String generateGetCode(String offset) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort) getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        //        NamedProgramCodeGeneratorAdapter containingActorAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent()
        //                .getContainer().getContainer());

        //        String result = _getDirectorForReceiver().getReference(port,
        //                new String[] { Integer.toString(channel), offset },
        //                _forComposite, false, containingActorAdapter);
        //        String actorName = CodeGeneratorAdapter.generateName(port.getContainer());
        String type = getCodeGenerator().codeGenType(port.getType());
        String result = "(*(" + port.getName() + "->get))((struct IOPort*) "
                + port.getName() + ", " + channel + ").payload." + type;
        //result = "DEReceiverGet(&(" + result + "))";
        return result;
    }

    /** Generates code to check the receiver has a token.
     *  @param offset The offset of the receiver, ignored in this base
     *  class.
     *  @return The generated hasToken code
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateHasTokenCode(String offset)
            throws IllegalActionException {
        TypedIOPort port = (TypedIOPort) getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        //        NamedProgramCodeGeneratorAdapter containingActorAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent()
        //                .getContainer().getContainer());
        //
        //        String result = _getDirectorForReceiver().getReference(port,
        //                new String[] { Integer.toString(channel), offset },
        //                _forComposite, false, containingActorAdapter);
        //        String actorName = CodeGeneratorAdapter.generateName(port.getContainer());
        String result = "(*(" + port.getName()
                + "->hasToken))((struct IOPort*) " + port.getName() + ", "
                + channel + ")";
        //        String result = "ReceiverHasToken(" + actorName + ".ports[enum_" + actorName + "_" + port.getName() + "].receivers + " + channel + ")";
        return result;
    }

    /** Generate code for putting tokens from the receiver.
     *  @param sourcePort The source port.
     *  @param offset The offset of the port.
     *  @param token The token.
     *  @return The generated put code.
     *  @exception IllegalActionException If thrown while getting the component,
     *  getting the adapter, getting the director or getting the port reference.
     */
    @Override
    public String generatePutCode(IOPort sourcePort, String offset, String token)
            throws IllegalActionException {
        TypedIOPort port = (TypedIOPort) getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        NamedProgramCodeGeneratorAdapter containingActorAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent()
                .getContainer().getContainer());

        // The source's channel as well as the offset is irrelevant here because
        // we use the token as the sourceRef instead.
        // The sink is actually also irrelevant, since we will get rid of it later.
        ProgramCodeGeneratorAdapter.Channel source = new Channel(sourcePort, 0);
        ProgramCodeGeneratorAdapter.Channel sink = new Channel(port, channel);

        token = ((NamedProgramCodeGeneratorAdapter) getAdapter(getComponent()
                .getContainer().getContainer())).getTemplateParser()
                .generateTypeConvertStatement(source, sink, 0, token);
        token = _removeSink(token);
        boolean forComposite = _forComposite;
        if (getComponent().getContainer().getContainer() instanceof ModularCodeGenTypedCompositeActor
                && port.isInput()) {
            // If the container is a ModularCodeGenTypedCompositeActor
            // and the port is an input, then generate a reference
            // assuming that the we are not operating on a composite.
            // This code is needed for
            // $PTII/ptolemy/cg/lib/test/auto/ModularCodeGen2.xml,
            // which has nested ModularCodegen.
            forComposite = false;
        }
        if (port.isInput()
                && ((Actor) sourcePort.getContainer()).getDirector() != ((Actor) port
                        .getContainer()).getDirector()) {
            // Needed for $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/hierarchicalModel_2_2e.xml
            forComposite = false;
        }
        String result = null;
        token = token.trim();
        if (token.charAt(token.length() - 1) == ';') {
            // Remove the trailing semicolon so that we avoid adding
            // a semicolon.
            token = token.substring(0, token.length() - 1);
        }
        if (token.length() > 2 && token.charAt(token.length() - 2) == ';') {
            // Remove the trailing semicolon so that we avoid adding
            // a semicolon.
            token = token.substring(0, token.length() - 2);
        }
        try {
            //                String nameInput = _getDirectorForReceiver().getReference(port,
            //                    new String[] { Integer.toString(channel), offset },
            //                    forComposite, true, containingActorAdapter);
            //                String actorSourceName = CodeGeneratorAdapter.generateName(sourcePort.getContainer());
            //                String actorDestName = CodeGeneratorAdapter.generateName(port.getContainer());
            //                String actorDestNameForArgs = actorDestName;
            //                if (port.getContainer() instanceof CompositeActor)
            //                    actorDestNameForArgs = "(" + actorDestName + ".actor)";
            //                String nameInput = actorSourceName + ".ports[enum_" + actorSourceName + "_" + sourcePort.getName() + "].farReceivers[" +
            //                        actorSourceName + "_" + sourcePort.getName() + "_" + actorDestName + "_" + port.getName() + "_" + channel + "]";
            String type = getCodeGenerator().codeGenType(port.getType());
            //type = type.substring(0, 1).toUpperCase(Locale.getDefault()) + type.substring(1);
            result = "(*(" + port.getName() + "->send))((struct IOPort*) "
                    + port.getName() + ", " + channel + ", ";

            result += "$new(" + type + "(" + token + ")));" + _eol;
            //                result += _eol + "(*(" + actorDestNameForArgs + ".container->director->fireAtFunction))(&"+ actorDestNameForArgs
            //                            +", " + actorDestNameForArgs + ".container->director->currentModelTime, "
            //                            + actorDestNameForArgs + ".container->director->currentMicrostep);" + _eol;
        } catch (Throwable throwable) {
            result = _getExecutiveDirectorForReceiver().getReference(port,
                    new String[] { Integer.toString(channel), offset },
                    forComposite, true, containingActorAdapter)
                    + " = " + token + ";" + _eol;
        }
        return result;
    }

    @Override
    protected String _generateTypeConvertStatement(Channel source)
            throws IllegalActionException {

        Type sourceType = ((TypedIOPort) source.port).getType();
        // TODO : here basically nothing is done, we need to implement this
        //        Type sinkType = ((TypedIOPort) getComponent().getContainer()).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }

        return null;
    }

    /** Each receiver is associated with a director, return that director.
     *  @return The director associated with this receiver.
     *  @exception IllegalActionException
     *
     *  FIXME: this is not exactly correct.
     *  This is probably because the information of the receiver is in the director of
     *  the container?
     */
    @Override
    protected DEDirector _getDirectorForReceiver()
            throws IllegalActionException {
        return (DEDirector) super._getDirectorForReceiver();
    }

    /** Each receiver is associated with a component of some executive director.
     *  @return The executive director if the component associated with this receiver.
     *  @exception IllegalActionException If thrown while getting the adapter.
     */
    protected DEDirector _getExecutiveDirectorForReceiver()
            throws IllegalActionException {
        return (DEDirector) getAdapter(((Actor) getComponent().getContainer()
                .getContainer()).getExecutiveDirector());
    }

    //$send(port#channel) ==> port_channel[writeOffset]
    //$get(port#channel) ==> port_channel[readOffset]

    private boolean _forComposite;

}
