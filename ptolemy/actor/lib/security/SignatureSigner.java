/* Creates a signature for the given piece of data and creates the key
to be sent to the signature verifier.

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


import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// SignatureSigner
/**

This actor takes an unsigned byte array at the input creates a
signature for it.  The resulting output is an unsigned byte array.
The Signature class in Java is based on calculating the message digest
of some data and encrypting it.  Various signature algorithms that are
implemented by "providers" and installed maybe used by specifying the
algorithm in the <i>algorithm</i> parameter.  In case a provider
specific instance of an algorithm is needed, the provider may also be
specified in the <i>provider</i> parameter.  This actor creates 
a private key to sign data and a public key which is sent on the
<i>keyOut</i> port to verify the signature.

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
public class SignatureSigner extends SignatureActor {

    // TODO: Use cipher streaming to allow for easier file input reading.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SignatureSigner(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyPairGenerator = new StringParameter(this, "keyPairGenerator");
        // Get the possible choices
        Set algorithms = Security.getAlgorithms("KeyPairGenerator");
        Iterator algorithmsIterator = algorithms.iterator();
        for(int i = 0; algorithmsIterator.hasNext(); i++) {
            String algorithmName = (String)algorithmsIterator.next();
            if (i == 0) {
                keyPairGenerator.setExpression(algorithmName);
            }
            keyPairGenerator.addChoice(algorithmName);
        }

        publicKey = new TypedIOPort(this, "publicKey", false, true);
        publicKey.setTypeEquals(BaseType.OBJECT);

        data = new TypedIOPort(this, "data", false, true);
        data.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port sends out the original data to be verified with the
     *  encrypted digest
     */
    public TypedIOPort data;

    /** This port outputs the key to be used by the
     *  AsymmetricEncryption actor as an unsigned byte array.
     */
    public TypedIOPort publicKey;

    /** The algorithm to be used to generate the key pair.  For
     *  example, using RSAwithMD5 as the signature algorithm, RSA
     *  would be used for the <i>keyPairGenerator</i> parameter.
     */
    public StringParameter keyPairGenerator;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>algorith</i>, <i>provider</i>, or <i>keysize</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == keyPairGenerator) {
            _keyPairGenerator =
                ((StringToken)keyPairGenerator.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
    /** If there is a token on the <i>input</i> port, this method
     *  takes the data from the <i>input</i> and decrypts the data
     *  based on the <i>algorithm</i>, <i>provider</i>, <i>mode</i>
     *  and <i>padding</i> using the created private key.  This is
     *  then sent on the <i>output</i>.  The public key is also sent
     *  out on the <i>publicKey</i> port.  All parameters should be the
     *  same as the corresponding encryption actor.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        System.out.println("SignatureSigner.fire()");
        super.fire();
        //publicKey.send(0, (new ObjectToken(_publicKey)));
    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException If thrown by base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        KeyPair pair = _createAsymmetricKeys();
        _publicKey = pair.getPublic();
        _privateKey = pair.getPrivate();
        publicKey.send(0, new ObjectToken(_publicKey));
    }

    /** Takes the data and calculates a message digest for it.
     *
     * @return byte[] the decrypted data.
     * @exception IllegalActionException If the key or padding is invalid.
     */
    protected byte[] _process(byte[] dataBytes) throws IllegalActionException{
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        System.out.println("SignatureSigner._process()");
        try {
            // The data port contains the unsigned data.
            data.send(0,
                    CryptographyActor.unsignedByteArrayToArrayToken(dataBytes));
            _signature.initSign(_privateKey);
            _signature.update(dataBytes);
            return _signature.sign();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem sending " + dataBytes.length + " bytes.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The private key to be used for asymmetric decryption.  This key is null
     *  for symmetric decryption.
     */
    private PrivateKey _privateKey = null;
}
