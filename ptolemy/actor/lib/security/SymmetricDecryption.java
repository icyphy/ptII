/* Receives a key from a SymmetricEncryption actor and uses it to decrypt
   a data input based on a given symmetric algorithm.

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

@ProposedRating Yellow (rnreddy@andrew.cmu.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ptolemy.data.ArrayToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SymmetricDecryption
/**
This actor takes an unsigned byte array at the input and decrypts the
message.  The resulting output is an unsigned byte array.  The shared
secret key is received from the SymmetricEncryption actor on the
<i>keyIn</i> and is used to decrypt the message.  Certain algorithms
may also require extra parameters generated during encryption to
decrypt the message.  These are received on the <i>parameters</i>
port.  Various ciphers that are implemented by "providers" and
installed maybe used by specifying the algorithm in the
<i>algorithm</i> parameter.  The algorithm specified must be
symmetric. The mode and padding can also be specified in the
<i>mode</i> and <i>padding</i> parameters.  In case a provider
specific instance of an algorithm is needed the provider may also be
specified in the provider parameter.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html" target="_top">http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html">.
<br>Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class SymmetricDecryption extends CipherActor {

    // TODO: include sources of information on JCE cipher and algorithms
    // TODO: Use cipher streaming to allow for easier file input reading.

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

        keyIn = new TypedIOPort(this, "keyIn", true, false);
        keyIn.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        parameters = new TypedIOPort(this, "parameters", true, false);
        parameters.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This port receives the key to be used from AsymmetricDecryption actor
     *  as an unsigned byte array.  This key is used to decrypt data from the
     *  <i>input</i> port.
     */
    public TypedIOPort keyIn;

    /** This port receives any parameters that may have generated during
     *  encryption if parameters were generated during encryption.
     */
    public TypedIOPort parameters;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there are tokens on the <i>input</i>, <i>keyIn</i> and
     *  <i>parameters</i> ports, they are consumed. This method takes
     *  the data from the <i>input</i> and decrypts the data based
     *  on the <i>algorithm</i>, <i>provider</i>, <i>mode</i> and
     *  <i>padding</i> using the secret key.  This is then
     *  sent on the <i>output</i>.  All parameters should be the same as the
     *  corresponding encryption actor.
     *
     *  @exception IllegalActionException If retrieving parameters fails,
     *  the algorithm does not exist or if the provider does not exist.
     */
    public void fire() throws IllegalActionException {
        try {
            if (keyIn.hasToken(0)) {
                _secretKey =
                    (SecretKey)_bytesToKey(_arrayTokenToUnsignedByteArray(
                            (ArrayToken)keyIn.get(0)));
            }

            if (parameters.hasToken(0)) {
                if (_provider.equalsIgnoreCase("SystemDefault")) {
                    _algorithmParameters = AlgorithmParameters.getInstance(_algorithm);
                } else {
                    _algorithmParameters =
                        AlgorithmParameters.getInstance(_algorithm, _provider);
                }
                byte [] encodedAP =
                    _arrayTokenToUnsignedByteArray(
                            (ArrayToken)parameters.get(0));

                _algorithmParameters.init(encodedAP);
            }

            if (_secretKey != null) {
                super.fire();
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "fire() failed");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////


    /** Decrypt the data with the secret key.  Receives the data to be
     *  decrypted as a byte array and returns a byte array.
     *
     * @param dataBytes the data to be decrypted.
     * @return byte[] the decrypted data.
     * @exception IllegalActionException If an error occurs in
     * ByteArrayOutputStream, a key is invalid, padding is bad,
     * or if the block size is illegal.
     */
    protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException {
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        try {

            _cipher.init(Cipher.DECRYPT_MODE, _secretKey, _algorithmParameters);
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
    private SecretKey _secretKey = null;

    // The initialization parameter used in a block ciphering mode.
    private IvParameterSpec _spec;

    // The algorithm parameters to be used if they exist.
    private AlgorithmParameters _algorithmParameters;
}
