/* Decrypt an unsigned byte array using a symmetric algorithm.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Green (cxh@eecs.berkeley.edu) 
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;



//////////////////////////////////////////////////////////////////////////
//// SymmetricDecryption
/**
Decrypt an unsigned byte array using a symmetric algorithm.

<p>See {@link ptolemy.actor.lib.security.SymmetricEncryption} for a
description of symmetric vs. asymmetric algorithms.

<p>This actor reads an unsigned byte array at the <i>input</i> port,
decrypts the data using the data from the <i>key</i> port and then
writes the unsigned byte array results to the <i>output</i> port.

<p>The <i>key</i> should be the same for both the SymmetricEncryption
actor and this actor.

The <i>algorithm</i> parameter determines which algorithm is used.
The algorithm specified must be symmetric. The mode and padding can also
be specified in the <i>mode</i> and <i>padding</i> parameters.  In
case a provider specific instance of an algorithm is needed the
provider may also be specified in the <i>provider</i> parameter.

<p>Note that for simplicity, this actor does not support the
notion of algorithm parameters, so the algorithm must not require
that algorithm parameters be transmitted separately from the key.
If the user selects an algorithm that uses algorithm parameters, then
an exception will likely be thrown.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).  See the
{@link ptolemy.actor.lib.security.CryptographyActor} documentation for
resources about JCA and JCE.

@author Christopher Hylands Brooks, Contributor: Rakesh Reddy
@version $Id$
@since Ptolemy II 3.1
*/
public class SymmetricDecryption extends CipherActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SymmetricDecryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        algorithm.setVisibility(Settable.NOT_EDITABLE);
        algorithm.setPersistent(false);
        // Hide the algorithm parameter.
        algorithm.setVisibility(Settable.EXPERT);

        key = new TypedIOPort(this, "key", true, false);
        key.setTypeEquals(KeyToken.KEY);

        // Hide the keySize parameter, it is not used.
        keySize.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The key port.  This port contains the key that is used to
     *  decrypt data from the <i>input</i> port.  The type is KeyToken
     *  that contains a java.security.key
     */
    public TypedIOPort key;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the <i>input</i> and <i>key</i>, and send the decrypted data
     *  to the <i>output</i> port.
     *  The algorithm is obtained from the <i>key</i>, and if it
     *  is different than the current value of the <i>algorithm</i>
     *  parameter, we reinitialize _cipher.
     *
     *  @exception IllegalActionException If retrieving parameters fails,
     *  the algorithm does not exist or if the provider does not exist.
     */
    public void fire() throws IllegalActionException {
        try {
            if (key.hasToken(0)) {
                KeyToken keyToken = (KeyToken)key.get(0);
                _key = (java.security.Key)keyToken.getValue();
                if (!_algorithm.equals(_key.getAlgorithm())) {
                    // We have the name of the algorithm from the Key,
                    // so we reinitialize the cipher
                    _algorithm = _key.getAlgorithm();
                    algorithm.setExpression(_algorithm);
                    _updateCipherNeeded = true;
                    _updateCipher();
                }
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "fire() failed");
        }
        super.fire();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Decrypt the data with the secret key by using javax.crypto.Cipher.
     *  @param dataBytes the data to be decrypted.
     *  @return byte[] the decrypted data.
     *  @exception IllegalActionException If an error occurs in
     *  ByteArrayOutputStream, a key is invalid, padding is bad,
     *  or if the block size is illegal.
     */
    protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException {
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        try {
            _cipher.init(Cipher.DECRYPT_MODE, _key);
            byteArrayOutputStream.write(_cipher.doFinal(dataBytes));
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem processing " + dataBytes.length + " bytes.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The secret key to be used for symmetric encryption and decryption.
     * This key is null for asymmetric decryption.
     */
    private java.security.Key _key = null;
}
