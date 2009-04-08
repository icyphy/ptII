/* Code generator adapter for IOPort.

 Copyright (c) 2005-2008 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor;

import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy;
import ptolemy.cg.kernel.generic.PortCodeGenerator;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy.Channel;
import ptolemy.cg.lib.EmbeddedCodeActor;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////IOPort

/**
Code generator adapter for {@link ptolemy.actor.IOPort}.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.2
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */

public class IOPort extends CodeGeneratorAdapter implements PortCodeGenerator {

    /** Construct the code generator adapter associated
     *  with the given IOPort.
     *  @param component The associated component.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////

    public String generateCodeForSend(String channel, String dataToken) 
    throws IllegalActionException {
        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = _getDirectorAdapter();
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        int channelNumber = Integer.valueOf(channel);

        return directorAdapter.generateCodeForSend(port, channelNumber, dataToken);
    }

    public String generateCodeForGet(String channel) throws IllegalActionException {
        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = _getDirectorAdapter();
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        int channelNumber = Integer.valueOf(channel);

        return directorAdapter.generateCodeForGet(port, channelNumber);
    }

    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(getComponent().getContainer());

        return adapter.processCode(code.toString());
    }



    public String generatePreFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_isPthread()) {
            code.append("MPI_recv();" + _eol);
        }
        return code.toString();
    }


    public String generatePostFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_isPthread()) {
            code.append("MPI_send();" + _eol);
        }
        return code.toString();
    }


    public ptolemy.actor.Director getDirector() {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        Actor actor = (Actor) port.getContainer();

        if (actor instanceof EmbeddedCodeActor.EmbeddedActor) {
            // ignore the inner SDFDirector.
            actor = (Actor) actor.getContainer();
        }
        Director director = null;

        // FIXME: why are we checking if this is a input port?
        //if (port.isInput() && !port.isOutput() && (actor instanceof CompositeActor)) {
        if (actor instanceof CompositeActor) {
            director = actor.getExecutiveDirector();
        } 

        if (director == null) {
            director = actor.getDirector();
        }
        return director;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

 // FIXME rodiers: reintroduce PN specifics (but somewhere else)
    /*
    private String _generateMPISendCode(int channelNumber, 
            int rate, ptolemy.actor.IOPort sinkPort,
            int sinkChannelNumber, Director director) throws IllegalActionException {
        ptolemy.actor.TypedIOPort port = (ptolemy.actor.TypedIOPort) getComponent();

        StringBuffer code = new StringBuffer();

        code.append("// generateMPISendCode()" + _eol);

        for (int i = 0; i < rate; i++) {

            int sinkRank = MpiPNDirector.getRankNumber((Actor) sinkPort.getContainer());
            int sourceRank = MpiPNDirector.getRankNumber((Actor) port.getContainer());

            code.append("// Initialize send tag value." + _eol);
            code.append("static int " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " = " +
                    MpiPNDirector.getMpiReceiveBufferId(sinkPort, sinkChannelNumber) + ";" + _eol);

            if (MpiPNDirector._DEBUG) {
                code.append("printf(\"" + port.getContainer().getName() + "[" + sourceRank + "] sending msg <" + 
                        sinkRank + ", %d> for " + MpiPNDirector.getBufferLabel(port, channelNumber) +
                        "\\n\", " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + ");" + _eol);
            }

            code.append("MPI_Isend(&");

            String[] channelAndOffset = new String[2];
            channelAndOffset[0] = "" + sinkChannelNumber;
            channelAndOffset[1] = MpiPNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" +
            MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + ".current]";

            String buffer = 
                CodeGeneratorAdapter.generatePortReference(sinkPort, channelAndOffset , false);

            code.append(buffer);

            // count.
            code.append(", 1");

            // FIXME: handle different mpi data types.
            if (port.getType() == BaseType.DOUBLE) {
                code.append(", MPI_DOUBLE");
            } else if (port.getType() == BaseType.INT) {
                code.append(", MPI_INT");
            } else {
                assert false;
            }

            code.append(", " + sinkRank);

            code.append(", " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) +
                    ", " + "comm, &" +
                    MpiPNDirector.generateMpiRequest(sinkPort, sinkChannelNumber) + "[" + 
                    MpiPNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" + 
                    MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                    ".current" + (i == 0 ? "" : i) + "]]" + ");" + _eol);

            if (MpiPNDirector._DEBUG) {
                code.append("printf(\"" + MpiPNDirector.getBufferLabel(port, channelNumber) +
                        ", rank[" + sourceRank + "], sended tag[%d]\\n\", " + 
                        MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + ");" + _eol);
            }            
        }

        // Update the Offset.
        code.append(MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                ".current += " + rate + ";" + _eol);

        MpiPNDirector directorAdapter = (MpiPNDirector) _getAdapter(director);
        code.append(MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " += " + 
                directorAdapter.getNumberOfMpiConnections(true) + ";" + _eol);

        code.append(MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " &= 32767; // 2^15 - 1 which is the max tag value." + _eol);

        return  code + _eol;

    }
    //End FIXME rodiers
     */
    

    private ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director _getDirectorAdapter() throws IllegalActionException {
        Director director = getDirector();
        return (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getCodeGenerator().getAdapter(director);
    }


    private Receiver _getReceiver(String offset, int channel, ptolemy.actor.IOPort port) {
        Receiver[][] receivers = port.getReceivers();
        
        // For output ports getReceivers always returns an empty table.
        if (receivers.length == 0) {
            return null;
        }

	int staticOffset = -1;
        Receiver receiver = null;
	if (offset != null) {
	    try {
		staticOffset = Integer.parseInt(offset);
		receiver = receivers[channel][staticOffset];
	    } catch (Exception ex) {
		staticOffset = -1;
	    }
	}

	if (staticOffset == -1) {
            // FIXME: Assume all receivers are the same type for the channel.
            // However, this may not be true.
            assert (receivers.length > 0);
            receiver = receivers[channel][0];
        }
        return receiver;
    }


//    private boolean _isMpi() {
//        return getCodeGenerator().getAttribute("mpi") != null;
//    }


    private boolean _isPthread() {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        boolean isPN = (((Actor) port.getContainer()).getDirector() 
                instanceof ptolemy.domains.pn.kernel.PNDirector);
        
        /* FIXME rodiers
        return isPN && (null == getCodeGenerator().getAttribute("mpi"))
        && (getCodeGenerator().target.getExpression().equals("default") || 
            getCodeGenerator().target.getExpression().equals("posix"));
        */
        return false;
        //End FIXME rodiers
    }



    // FIXME rodiers: reintroduce PN specifics (but somewhere else)
    /*
    private String _updatePNOffset(int rate, ptolemy.actor.IOPort port, 
            int channelNumber, Director directorAdapter, boolean isWrite)
    throws IllegalActionException {
        // FIXME: this is kind of hacky.
        PNDirector pnDirector = (PNDirector) //directorAdapter; 
        _getAdapter(((Actor) port.getContainer()).getExecutiveDirector());

        String incrementFunction = (isWrite) ? 
                "$incrementWriteOffset" : "$incrementReadOffset";

        if (rate <= 0) {
            assert false;
        }

        String incrementArg = "";
        if (rate > 1) {
            incrementFunction += "By";

            // Supply the increment argument.
            incrementArg += rate + ", ";
        }

        // FIXME: generate the right buffer reference from
        // both input and output ports.

        return incrementFunction + "(" + incrementArg + "&" +
        PNDirector.generatePortHeader(port, channelNumber) + ", &" +
        pnDirector.generateDirectorHeader() + ");" + _eol;
    }
    //end FIXME rodiers
    */


}
