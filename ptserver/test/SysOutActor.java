/*

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptserver.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * SysOutActor class.
 *
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class SysOutActor extends TypedAtomicActor implements PortablePlaceable {

    public interface TokenDelegator {
        public void getToken(Token token);

    }

    public SysOutActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new TypedIOPort(this, "input", true, false);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Token token = _input.get(0);

        if (_delegator != null) {
            _delegator.getToken(token);
        } else {
            System.out.println(token);
        }
        _implementation.printToken(token);
    }

    /**
     * @param delegator the delegator to set
     */
    public void setDelegator(TokenDelegator delegator) {
        _delegator = delegator;
    }

    /**
     * @return the delegator
     */
    public TokenDelegator getDelegator() {
        return _delegator;
    }

    @Override
    public void place(PortableContainer container) {
        _implementation.place(container);
    }

    private TokenDelegator _delegator;
    private final TypedIOPort _input;
    private final SysOutActorInterface _implementation = PtolemyInjector
            .getInjector().getInstance(SysOutActorInterface.class);
}
