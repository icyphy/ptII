/* Encrypt data using a symmetric algorithm.

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


import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;

//////////////////////////////////////////////////////////////////////////
//// SymmetricEncryption
/**
Decrypt an unsigned byte array using a symmetric algorithm.

<p>In cryptography, a symmetric algorithm is an algorithm that uses
the same key for encryption and decryption.  An asymmetric algorithm uses
a two different keys: a public key and private key.  Asymmetric algorithms
are usually much slower than symmetric algorithms.  The initial
default set of algorithms that comes with the Sun JDK does
not include a symmetric encryption algorithm.

<p>This actor reads an unsigned byte array at the <i>input<i> port,
encrypts the data using the data from the <i>key</i> port and then
writes the unsigned byte array results to the <i>output</i> port.

<p>The <i>key</i> is should be the same for both the
SymmetricDecryption actor and this actor.  The <i>key</i> should not
be visible to users as the security of the encrypted message relies on
the secrecy of this key.

<p>The <i>algorithm</i> parameter determines which algorithm is used.
algorithm specified must be symmetric. The mode and padding can also
be specified in the <i>mode</i> and <i>padding</i> parameters.  In
case a provider specific instance of an algorithm is needed the
provider may also be specified in the <i>provider</i> parameter.

<p>Note that for simplicity, this actor does not support the
notion of algorithm parameters, so the algorithm must not require
that algorithm parameters be transmitted separately from the key.

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
public class SymmetricEncryption extends CipherActor {


    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SymmetricEncryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        key = new TypedIOPort(this, "key", true, false);
        key.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** The key to be used by this actor to encrypt the data.
     *  The type is an ObjectToken of type java.security.Key.
     */
    public TypedIOPort key;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and encrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and <i>padding</i>
     *  using the created secret key.  This is then sent on the
     *  <i>output</i>.  The public key is also sent out on the <i>keyOut</i>
     *  port.  All parameters should be the same as the corresponding
     *  decryption actor.  The call for encryption is done in the base class.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        if (key.hasToken(0)) {
            try {
                ObjectToken objectToken = (ObjectToken)key.get(0);
                //SecretKey key = (SecretKey)objectToken.getValue();
                java.security.Key key =
                    (java.security.Key)objectToken.getValue();
                _cipher.init(Cipher.ENCRYPT_MODE, key);
            } catch (Exception ex) {
                throw new IllegalActionException (this, ex,
                        "Failed to initialize Cipher");
            }
        }

        super.fire();
    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException If thrown by base class or
     *  if the algorithm is not found, or if the padding scheme is illegal,
     *  or if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /** Sets token production for initialize to one and resolves scheduling.
     *
     * @exception IllegalActionException If thrown by base class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        getDirector().invalidateResolvedTypes();
    }

    /** Encrypt the data with the specified key.  Receives the data to be
     *  encrypted as a byte array and returns a byte array.  Also creates
     *  and sends an initialization vector if necessary.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @exception IllegalActionException If error occurs in
     * ByteArrayOutputStream, if the key is invalid, if the padding is bad
     * or if the block size is illegal.
     */
    protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException{
        // ByteArrayOutputStream byteArrayOutputStream =
        //      new ByteArrayOutputStream();
        _byteArrayOutputStream = new ByteArrayOutputStream();
        _byteArrayOutputStream.reset();
        // byteArrayInputStream = new ByteArrayInputStream(initialData);
        // int length = 0;
        // byte [] buffer = new byte [BUFFER_SIZE];
        // try {
        //     while ((length = byteArrayInputStream.read(buffer)) != -1) {
        //         _cos.write(buffer, 0, length);
        //     }
        //     _cos.flush();
        // } catch (IOException e) {
        //      throw new IllegalActionException(this, ex);
        // }

        try {
            _byteArrayOutputStream.write(_cipher.doFinal(dataBytes));
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem processing " + dataBytes.length + " bytes.");
        }
        return _byteArrayOutputStream.toByteArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ByteArrayOutputStream _byteArrayOutputStream;
}
