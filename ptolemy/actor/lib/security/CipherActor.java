/* A base class for actors that encrypt and decrypt data.

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

@ProposedRating Yellow (rnreddy@ptolemy.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.crypto.Cipher;

//////////////////////////////////////////////////////////////////////////
//// CipherActor
/** 
A base class that implements general functions used by cipher
actors.

<p>Cipher actors are any actors which perform encryption or
decryption based on the Java JCE.  Actors extending this class take in
an unsigned byte array at the <i>input</i>, perform the transformation
specified in the <i>algorithm</i> parameter and send a unsigned byte
array on the <i>output</i>.  The algorithms that maybe implemented are
limited to the ciphers that are implemented by "providers" following
the JCE specifications and installed in the machine being run. The
mode and padding can also be specified in the <i>mode</i> and
<i>padding</i> parameters.  In case a provider specific instance of an
algorithm is needed, the provider may also be specified in the
<i>provider</i> parameter. The <i>keySize</i> parameter allows
implementations of algorithms using various key sizes.

<p>Derived classes should implement the abstract 
{@link #CryptographyActor._process(byte[])} method.

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
abstract public class CipherActor extends CryptographyActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CipherActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        mode = new StringParameter(this, "mode");
        mode.setExpression("");
        mode.addChoice("");
        mode.addChoice("NONE");
        mode.addChoice("CBC");
        mode.addChoice("CFB");
        mode.addChoice("ECB");
        mode.addChoice("OFB");
        mode.addChoice("PCBC");

        padding = new StringParameter(this, "padding");
        padding.setExpression("");
        padding.addChoice("");
        padding.addChoice("NoPadding");
        padding.addChoice("OAEPWithMD5AndMGF1Padding");
        padding.addChoice("PKCS5Padding");
        padding.addChoice("SSL3Padding");
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The mode component when the Cipher is instantiated.
     *  The mode is specified as a string.
     *  Names for modes and modes implemented vary based on the provider.
     *  Possible values include
     * <dl>
     * <dt><code></code> (<i>The empty string</i>)
     * <dd>Use the default setting for the algorithm
     *
     * <dt><code>NONE</code>
     * <dd>No mode.
     *
     * <dt><code>CBC</code>
     * <dd>Cipher Block Chaining Mode, as defined in FIPS PUB 81.
     *
     * <dt><code>CFB</code>
     * <dd>Cipher Feedback Mode, as defined in FIPS PUB 81.
     *
     * <dt><code>ECB</code>
     * <dd>Electronic Codebook Mode, as defined in: The National
     * Institute of Standards and Technology (NIST) Federal
     * Information Processing Standard (FIPS) PUB 81, "DES Modes of
     * Operation," U.S. Department of Commerce, Dec 1980.
     *
     * <dt><code>OFB</code>
     * <dd>Output Feedback Mode, as defined in FIPS PUB 81.
     *
     * <dt><code>PCBC</code>
     * <dd>Propagating Cipher Block Chaining, as defined by Kerberos V4.
     * </dl>
     *
     *  The initial default is the empty string, which indicates that
     *  the default setting for the algorithm should be used.
     *  <p>
     *  See the
     *  <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jce/JCERefGuide.html#AppA">Java Cryptography Extension (JCE) Reference Guide</a>
     *  for details.
     */
    public StringParameter mode;

    /** The padding scheme used by the cipher during encryption.
     *  The padding is specified as a string.
     *  Names for parameter and parameters implemented vary based on the
     *  provider.
     *  Possible values include
     * <dl>
     * <dt><code></code> (<i>The empty string</i>)
     * <dd>Use the default setting for the algorithm.
     *
     * <dt><code>NoPadding</code>
     * <dd>No padding.
     *
     * <dt><code> OAEPWith<digest>And<mgf>Padding</code>
     * <dd>Optimal Asymmetric Encryption Padding scheme defined in
     * PKCS #1, where <digest> should be replaced by the message
     * digest and <mgf> by the mask generation function. Example:
     * OAEPWithMD5AndMGF1Padding.
     *
     * <dt><code>PKCS5Padding</code>
     * <dd>The padding scheme described in: RSA Laboratories, "PKCS
     * #5: Password-Based Encryption Standard," version 1.5, November
     * 1993.
     *
     * <dt><code>SSL3Padding</code>
     * <dd>The padding scheme defined in the SSL Protocol Version 3.0,
     * November 18, 1996, section 5.2.3.2 (CBC block cipher):
     * </dt>
     *
     *  The initial default is the empty string, which indicates that
     *  the default setting for the algorithm should be used.
     *  <p>
     *  See the
     *  <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jce/JCERefGuide.html#AppA">Java Cryptography Extension (JCE) Reference Guide</a>
     *  for details.
     */
    public StringParameter padding;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if the
     *  the <i>mode</i>, or <i>padding</i>parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mode) {
            _mode = ((StringToken)mode.getToken()).stringValue();
        } else if (attribute == padding) {
            _padding = ((StringToken)padding.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Retrieve the values of the parameters and initialize the Cipher.
     *
     * @exception IllegalActionException If the algorithm cannot be found,
     * the padding scheme is illegal for the the given algorithm or
     * if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _mode = ((StringToken)mode.getToken()).stringValue();
        _padding = ((StringToken)padding.getToken()).stringValue();
        try {
            // If the mode or padding parameters are the empty
            // string, then we use the default for the algorithm
            // If they are not empty
            String modeArgument = (_mode.length() > 0) ?
                "/" + _mode : "";
            String paddingArgument = (_padding.length() > 0) ?
                "/" + _padding : "";
            if (_mode.length() == 0
                    && _padding.length() > 0) {
                modeArgument = "/";
            }

            if (_provider.equalsIgnoreCase("SystemDefault")) {
                _cipher = Cipher.getInstance(
                        _algorithm + modeArgument + paddingArgument);
            } else {
                _cipher = Cipher.getInstance(
                        _algorithm + modeArgument + paddingArgument,
                        _provider);
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to initialize Cipher with "
                    + "algorithm: '"+ _algorithm
                    + "', padding: '" + _padding
                    + "', provider: '" + _provider + "'");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                    Protected Variables                    ////

    /** The cipher that will be used to process the data.
     */
    protected Cipher _cipher;

    /** The mode to be used to process the data.
     */
    protected String _mode;

    /** The padding scheme to be used process the data.
     */
    protected String _padding;
}
