/* Decrypt data using a symmetric algorithm.

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
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;



//////////////////////////////////////////////////////////////////////////
//// SymmetricDecryption
/**
Decrypt an unsigned byte array using a symmetric algorithm.

Read an unsigned byte array at the <i>input<i>, decrypt
the data using the data from the <i>key</i> port and
write the unsigned byte array results to the <i>output</i> port.

<p>The <i>key</i> is should be the same for both the SymmetricEncryption
actor and this actor.

The <i>algorithm</i> parameter determines which algorithm is used.
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

        key = new TypedIOPort(this, "key", true, false);
        key.setTypeEquals(BaseType.OBJECT);

        parameters = new TypedIOPort(this, "parameters", true, false);
        parameters.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The key port.  This port contains the key that is used to
     *  decrypt data from the <i>input</i> port.  The type is ObjectToken
     *  that wraps a java.security.key
     */
    public TypedIOPort key;

    /** This port receives any parameters that may have generated during
     *  encryption if parameters were generated during encryption.
     */
    public TypedIOPort parameters;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there are tokens on the <i>input</i>, <i>key</i> and
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
            if (key.hasToken(0)) {
                ObjectToken objectToken = (ObjectToken)key.get(0);
                _key = (java.security.Key)objectToken.getValue();
            }

            if (_key != null) {
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
