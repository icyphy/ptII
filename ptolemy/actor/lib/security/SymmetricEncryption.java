/* Encrypt an unsigned byte array using a symmetric algorithm.

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
import ptolemy.kernel.util.Settable;

import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;

//////////////////////////////////////////////////////////////////////////
//// SymmetricEncryption
/**
Encrypt an unsigned byte array using a symmetric algorithm.

<p>In cryptography, a symmetric algorithm is an algorithm that uses
the same key for encryption and decryption.  An asymmetric algorithm uses
two different keys: a public key and a private key.  Sun's documentation 
says that asymmetric algorithms are usually much slower than
symmetric algorithms.  The initial default set of algorithms that
comes with the Sun JDK does not include an asymmetric encryption algorithm,
though other algorithms may be installed by the system administrator.

<p>This actor reads an unsigned byte array at the <i>input<i> port,
encrypts the data using the data from the <i>key</i> port and then
writes the unsigned byte array results to the <i>output</i> port.

<p>The <i>key</i> should be the same for both the
SymmetricDecryption actor and this actor.  The <i>key</i> should not
be visible to users as the security of the encrypted message relies on
the secrecy of this key.

<p>The <i>algorithm</i> parameter determines which algorithm is used.
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

@author Christopher Hylands Brooks, Contributor: Rakesh Reddy
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

        algorithm.setVisibility(Settable.NOT_EDITABLE);
        algorithm.setPersistent(false);
        // Hide the algorithm parameter.
        algorithm.setVisibility(Settable.EXPERT);

        key = new TypedIOPort(this, "key", true, false);
        key.setTypeEquals(KeyToken.KEY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The key to be used by this actor to encrypt the data.
     *  The type is an KeyToken containing a java.security.Key.
     *  Usually the output of the {@link ptolemy.actor.lib.security.Key}
     *  actor is connected to this port
     */
    public TypedIOPort key;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and encrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and <i>padding</i>
     *  using the key read in from the <i>key</i> port.
     *  This processed data is then sent on the <i>output</i> port.
     *  All parameters should be the same as the corresponding
     *  decryption actor.  This method calls javax.crypto.Cipher.init()
     *  with the value of the <i>key</i>.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        if (key.hasToken(0)) {
            try {
                KeyToken keyToken = (KeyToken)key.get(0);
                // FIXME: do we really want to initialize the key each time?
                java.security.Key securityKey =
                    (java.security.Key)keyToken.getValue();
                if (!_algorithm.equals(securityKey.getAlgorithm())) { 
                    // We have the name of the algorithm from the Key,
                    // so we reinitialize the cipher
                    _algorithm = securityKey.getAlgorithm();
                    algorithm.setExpression(_algorithm);
                    _updateCipherNeeded = true;
                    _updateCipher();
                }
                _cipher.init(Cipher.ENCRYPT_MODE, securityKey);
            } catch (Exception ex) {
                throw new IllegalActionException (this, ex,
                        "Failed to initialize Cipher with "
                        + "algorithm: '"+ _algorithm
                        + "', padding: '" + _padding
                        + "', provider: '" + _provider + "'");
            }
        }
        super.fire();
    }

    /** Encrypt the data using the javax.crypto.Cipher.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @exception IllegalActionException If error occurs in
     * ByteArrayOutputStream, if the key is invalid, if the padding is bad
     * or if the block size is illegal.
     */
    protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException{
        // FIXME: should this method try to stream the data and
        // have a wrapup method that calls _cipher.doFinal() instead?
        ByteArrayOutputStream byteArrayOutputStream
            = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(_cipher.doFinal(dataBytes));
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem processing " + dataBytes.length + " bytes.");
        }
        return byteArrayOutputStream.toByteArray();
    }
}
