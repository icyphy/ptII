/* Creates and sends a key to an AsymmetricEncryption actor and decrypts
   incoming data based on a given asymmetric algorithm.

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

package ptolemy.domains.sdf.lib.security;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AsymmetricDecryption
/**
This actor takes an unsigned byte array at the input and decrypts the
message.  The resulting output is an unsigned byte array. Various
ciphers that are implemented by "providers" and installed maybe used
by specifying the algorithm in the <i>algorithm</i> parameter.  The
algorithm specified must be asymmetric.  The mode and padding can also
be specified in the <i>mode</i> and <i>padding</i> parameters. In case
a provider specific instance of an algorithm is needed the provider
may also be specified in the <i>provider</i> parameter.  This actor
creates a private key for decryption use and a public key which is
sent on the <i>keyOut</i> port to an encryption actor for encryption
purposes.  Key creation is done in pre-initialization and is put on
the <i>keyOut</i> port during initialization so the encryption actor
has a key to use when it is first fired.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).


TODO: include sources of information on JCE cipher and algorithms

@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class AsymmetricDecryption extends CipherActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AsymmetricDecryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyOut = new SDFIOPort(this, "keyOut", false, true);
        keyOut.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port outputs the key to be used by the AsymmetricEncryption actor
     *  as an unsigned byte array.
     */
    public SDFIOPort keyOut;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and decrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and  <i>padding</i>
     *  using the created private key.  This is then sent on the
     *  <i>output</i>.  The public key is also sent out on the <i>keyOut</i>
     *  port.  All parameters should be the same as the corresponding
     *  encryption actor.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        keyOut.send(0,
                _unsignedByteArrayToArrayToken(_keyToBytes(_publicKey)));
    }

    /** Outputs the key required for decryption.  The base classes retrieve
     *  the parameters and initialize the cipher.
     *  @exception IllegalActionException If the kyes cannot be created.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        KeyPair pair = _createAsymmetricKeys();
        _publicKey = pair.getPublic();
        _privateKey = pair.getPrivate();
        keyOut.send(0,
                _unsignedByteArrayToArrayToken(_keyToBytes(_publicKey)));
    }

    /** Sets token production for keyOut to 1 and resolves scheduling.
     *
     * @exception IllegalActionException If thrown by base class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        keyOut.setTokenInitProduction(1);
        getDirector().invalidateResolvedTypes();
    }

    /** Decrypt the data with the private key.  Receives the data to be
     *  decrypted as a byte array and returns a byte array.
     *
     *  @param dataBytes the data to be decrypted.
     *  @return byte[] the decrypted data.
     *  @exception IllegalActionException If exception below is thrown.
     */
    protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            _cipher.init(Cipher.DECRYPT_MODE, _privateKey);
            int blockSize = _cipher.getBlockSize();
            int length = 0;
            for (int i = 0; i < dataBytes.length; i += blockSize) {
                if (dataBytes.length-i <= blockSize) {
                    length = dataBytes.length-i;
                } else {
                    length = blockSize;
                }
                byteOutputStream.write(_cipher.doFinal(dataBytes, i, length));
            }
            byteOutputStream.flush();
            byteOutputStream.close();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem decrypting "+ dataBytes.length
                    + "bytes data with the private key");
        }
        return byteOutputStream.toByteArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The public key to be used for asymmetric encryption.
     */
    private PublicKey _publicKey = null;

    /* The private key to be used for asymmetric decryption.
     */
    private PrivateKey _privateKey = null;
}
