/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.actor.gt;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ToplevelTransformer extends TypedCompositeActor {

    /**
     *
     */
    public ToplevelTransformer() {
        try {
            _init();
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Cannot create ports.");
        }
    }

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ToplevelTransformer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     */
    public ToplevelTransformer(Workspace workspace) {
        super(workspace);
        try {
            _init();
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Cannot create ports.");
        }
    }

    public ActorToken getOutputToken() {
        return _outputToken;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _hasToken = true;
    }

    public boolean postfire() throws IllegalActionException {
        super.postfire();
        return _hasToken;
    }

    public void setInputToken(ActorToken inputToken) {
        _inputToken = inputToken;
    }

    public TransformerPort input;

    public TransformerPort output;

    public static class TransformerPort extends TypedIOPort {

        /** Construct a TransformerPort with a container and a name that is
         *  either an input, an output, or both, depending on the third
         *  and fourth arguments. The specified container must implement
         *  the TypedActor interface or an exception will be thrown.
         *
         *  @param container The container actor.
         *  @param name The name of the port.
         *  @param isInput True if this is to be an input port.
         *  @param isOutput True if this is to be an output port.
         *  @exception IllegalActionException If the port is not of an acceptable
         *   class for the container, or if the container does not implement the
         *   TypedActor interface.
         *  @exception NameDuplicationException If the name coincides with
         *   a port already in the container.
         */
        public TransformerPort(ComponentEntity container, String name,
                boolean isInput, boolean isOutput)
                throws IllegalActionException, NameDuplicationException {
            super(container, name, isInput, isOutput);
            setTypeEquals(ActorToken.TYPE);
        }

        public void broadcast(Token token) throws IllegalActionException,
        NoRoomException {
            send(0, token);
        }

        public void broadcast(Token[] tokenArray, int vectorLength)
        throws IllegalActionException, NoRoomException {
            send(0, tokenArray, vectorLength);
        }

        public Token get(int channelIndex) throws NoTokenException,
        IllegalActionException {
            if (isInput() && channelIndex == 0) {
                ToplevelTransformer transformer =
                    (ToplevelTransformer) getContainer();
                if (transformer._hasToken) {
                    transformer._hasToken = false;
                    if (transformer._inputToken == null) {
                        try {
                            return new ActorToken(new TypedCompositeActor());
                        } catch (IllegalActionException e) {
                        }
                    } else {
                        return transformer._inputToken;
                    }
                }
            }
            throw new NoTokenException("No token left.");
        }

        public int getWidth() {
            return 1;
        }

        public boolean hasRoom(int channelIndex) throws IllegalActionException {
            return isOutput() && channelIndex == 0;
        }

        public boolean hasToken(int channelIndex)
        throws IllegalActionException {
            if (isInput() && channelIndex == 0) {
                ToplevelTransformer transformer =
                    (ToplevelTransformer) getContainer();
                return transformer._hasToken;
            } else {
                return false;
            }
        }

        public boolean hasToken(int channelIndex, int tokens) {
            if (isInput() && channelIndex == 0 && tokens == 1) {
                ToplevelTransformer transformer =
                    (ToplevelTransformer) getContainer();
                return transformer._hasToken;
            } else {
                return false;
            }
        }

        public void send(int channelIndex, Token token)
                throws IllegalActionException, NoRoomException {
            if (isOutput() && channelIndex == 0) {
                _checkType(token);
                ToplevelTransformer transformer =
                    (ToplevelTransformer) getContainer();
                transformer._outputToken = (ActorToken) token;
            }
        }

        public void send(int channelIndex, Token[] tokenArray, int vectorLength)
                throws IllegalActionException, NoRoomException {
            if (isOutput() && channelIndex == 0 && vectorLength == 1) {
                Token token = tokenArray[0];
                _checkType(token);
                ToplevelTransformer transformer =
                    (ToplevelTransformer) getContainer();
                transformer._outputToken = (ActorToken) token;
            }
        }

        public void sendClear(int channelIndex) throws IllegalActionException {
            if (isOutput() && channelIndex == 0) {
                ToplevelTransformer transformer =
                    (ToplevelTransformer) getContainer();
                transformer._outputToken = null;
            }
        }
    }

    private void _init() throws IllegalActionException,
            NameDuplicationException {
        setClassName("ptolemy.actor.gt.ToplevelTransformer");
        input = new TransformerPort(this, "input", true, false);
        new Location(input, "_location").setExpression("{20.0, 200.0}");
        output = new TransformerPort(this, "output", false, true);
        new Location(output, "_location").setExpression("{580.0, 200.0}");
    }

    private boolean _hasToken;

    private ActorToken _inputToken;

    private ActorToken _outputToken;
}
