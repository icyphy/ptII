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

package ptolemy.actor.lib.security;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import ptolemy.data.ObjectToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AsymmetricDecryption
/**
This actor takes an unsigned byte array at the input and decrypts the
message.  The resulting output is an unsigned byte array.

<p> Various
ciphers that are implemented by "providers" and installed maybe used
by specifying the algorithm in the <i>algorithm</i> parameter.  The
algorithm specified must be asymmetric.  The mode and padding can also
be specified in the <i>mode</i> and <i>padding</i> parameters. In case
a provider specific instance of an algorithm is needed the provider
may also be specified in the <i>provider</i> parameter.

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

        privateKey = new TypedIOPort(this, "privateKey", true, false);
        privateKey.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** The private key to be used by this actor to dencrypt the data.
     *  The type is an ObjectToken of type java.security.Key.
     */
    public TypedIOPort privateKey;


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
        if (privateKey.hasToken(0)) {
            try {
                ObjectToken objectToken = (ObjectToken)privateKey.get(0);
                //SecretKey key = (SecretKey)objectToken.getValue();
                //java.security.Key key = (java.security.Key)objectToken.getValue(); 
                PrivateKey key = (PrivateKey)objectToken.getValue(); 
                _cipher.init(Cipher.ENCRYPT_MODE, key);
                //_algorithmParameters = _cipher.getParameters();
            } catch (Exception ex) {
                throw new IllegalActionException (this, ex,
                        "Failed to initialize Cipher");
            }
        }
        super.fire();

        //keyOut.send(0,
        //        CryptographyActor.unsignedByteArrayToArrayToken(
        //                CryptographyActor.keyToBytes(_publicKey)));
    }

    /** Outputs the key required for decryption.  The base classes retrieve
     *  the parameters and initialize the cipher.
     *  @exception IllegalActionException If the kyes cannot be created.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        //KeyPair pair = _createAsymmetricKeys();
        //_publicKey = pair.getPublic();
        //_privateKey = pair.getPrivate();
        //keyOut.send(0,
        //        CryptographyActor.unsignedByteArrayToArrayToken(
        //                CryptographyActor.keyToBytes(_publicKey)));
    }

    /** Sets token production for keyOut to 1 and resolves scheduling.
     *
     * @exception IllegalActionException If thrown by base class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
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

    /* The private key to be used for asymmetric decryption.
     */
    //private PrivateKey _privateKey = null;
}
