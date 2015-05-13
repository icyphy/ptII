/*
@Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.domains.wireless.lib.bluetooth;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * This abstract class defines the parent class for all Bluetooth Token classes, and defines which operations are legal and illegal from the base class Token.
 * <p>
 * This class basically disallows the performing of any arithmatic or algebraic operations on Bluetooth Tokens, as these operations would make no sense on the 
 * Bluetooth Token family
 * <p>
 * 
 * @author Phillip Azar
 * @see BluetoothResponseToken
 * @see BluetoothStatusToken
 */
public abstract class BluetoothToken extends Token {

    @Override
    public BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }
    
    @Override
    public Token add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token addReverse(Token leftArgument) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token multiply(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token pow(int times) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token subtract(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

    @Override
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Action unsupported");
    }

}
