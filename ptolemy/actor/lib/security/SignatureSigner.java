/* Sign the input data using a public key.

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
/** Sign the input data using a public key.

<p>In cryptography, digital signatures can be used to verify that the
data was not modified in transit.  However, the data itself is passed
in cleartext.

<p> In this case, the SignatureSigner actor generates private and
public keys. 

<p>The public key is passed as an unsigned byte array on the
<i>publicKey</i> port to the SignatureVerifier actor to verify that
the data has not been tampered with.

<p>Each time fire() is called, the private key is used to create a
signature for each block of unsigned byte array read from the
<i>input</i> port.  The signed data is passed to a SignatureVerifier
actor on the <i>output</i> port as an unsigned byte array.

<p>The <i>input</i> data itself is passed to in <b>cleartext</b>
on the <i>data</i> port.

<p>In this actor, the <i>algorithm<ii> parameter is used to set
the Signature and the <i>keyPairGenerator</i> parameter is used
to set the KeyPairGenerator.
<br>Common settings are:
<br><i>algorithm</i>: <code>SHA1WITHDSA</code>
<br><i>keyPairGenerator</i>:<code>DSA</code>
<br>or
<br><i>algorithm</i>: <code>MD5WITHRSA</code>
<br><i>keyPairGenerator</i>:<code>RSA</code>
Other possible values can be found in the JCE documentation below.

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
        // Get the possible choices.
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


    /** The original data in cleartext to be verified with the the
     *  signed digest.  The type is unsigned byte array.
     */
    public TypedIOPort data;

    /** The public key to be used by the SignatureVerifier actor
     *  to verify the data on the <i>output</i> port.
     *  The type is an ObjectToken containin a java.security.Key.
     */
    public TypedIOPort publicKey;

    /** The algorithm to be used to generate the key pair.  For
     *  example, using RSAwithMD5 as the signature algorithm in the
     *  <i>algorithm</i> parameter, RSA would be used for the
     *  <i>keyPairGenerator</i> parameter.  If the
     *  <i>algorithm</i>parameter was SHA1WITHDSA, then the value of
     *  this parameter would be DSA.
     */
    public StringParameter keyPairGenerator;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>keyPairGenerator</i> parameter is changed.
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

    /** Created a signature for the input data and the signature to the
     *  output.   
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        // This method merely calls the super class which eventually
        // calls CryptographicActor.fire() which in turn calls 
        // SignatureSigner_process().
        super.fire();
    }

    /** Create asymmetric keys and place the output the public key.
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

    /** Calculate a signature.
     *
     * @return byte[] the data to calculate a signature for.
     * @exception IllegalActionException If the key or padding is invalid.
     */
    protected byte[] _process(byte[] dataBytes) throws IllegalActionException{
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
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
