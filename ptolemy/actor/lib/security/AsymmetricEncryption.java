/* Encrypt data using a public key.

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
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AsymmetricEncryption
/**
Encrypt data using a public key. 
The cleartext input is an unsigned byte array, the encrypted output
is an unsigned byte array.

<p>Various ciphers that are implemented by "providers" and installed on
the system maybe used by specifying the algorithm in the
<i>algorithm</i> parameter. The algorithm specified must be
asymmetric. The mode and padding can also be specified in the
<i>mode</i> and <i>padding</i> parameters. In case a provider specific
instance of an algorithm is needed the provider may also be specified
in the <i>provider</i> parameter.

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
public class AsymmetricEncryption extends CipherActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AsymmetricEncryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        publicKey = new TypedIOPort(this, "publicKey", true, false);
        publicKey.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The public key to be used by this actor to encrypt the data.
     *  The type is an ObjectToken of type java.security.Key.
     */
    public TypedIOPort publicKey;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there are tokens on the <i>input</i> and <i>keyIn</i> ports, they
     *  are consumed.  This method takes the data from the <i>input</i> and
     *  encrypts the data based on the <i>algorithm</i>, <i>provider</i>,
     *  <i>mode</i> and <i>padding</i> using the public key from the decryption
     *  actor.  This is then sent on the <i>output</i>.  All parameters should
     *  be the same as the corresponding decryption actor.
     *
     */
    public void fire() throws IllegalActionException {
        if (publicKey.hasToken(0)) {
            try {
                ObjectToken objectToken = (ObjectToken)publicKey.get(0);
                //SecretKey key = (SecretKey)objectToken.getValue();
                //java.security.Key key = (java.security.Key)objectToken.getValue(); 
                PublicKey key = (PublicKey)objectToken.getValue(); 
                System.out.println("AsymmetricEncryption: "
                        + key.getClass().toString() + " " + key);
                _cipher.init(Cipher.ENCRYPT_MODE, key);
                //_algorithmParameters = _cipher.getParameters();
            } catch (Exception ex) {
                throw new IllegalActionException (this, ex,
                        "Failed to initialize Cipher");
            }
            //_publicKey = (PublicKey)_bytesToKey(
            //        _arrayTokenToUnsignedByteArray((ArrayToken)keyIn.get(0)));
        }
        //if (_publicKey != null) {
            super.fire();
            //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Receives the data to be encrypted as a byte array and returns
     *  a byte array.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @exception IllegalActionException if an error occurs while
     * creating the output stream, the key is invalid, the padding is pad
     * or if there is an illegal block size
     */
    protected byte[] _process(byte[] dataBytes)throws IllegalActionException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            //_cipher.init(Cipher.ENCRYPT_MODE, _publicKey);
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
                    "Problem processing " + dataBytes.length + " bytes. "
                    + "Cipher was " + _cipher);
        }
        return byteOutputStream.toByteArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //The public key to be used for asymmetric encryption.
    //private PublicKey _publicKey = null;
}
