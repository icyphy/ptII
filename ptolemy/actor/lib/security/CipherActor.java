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
import ptolemy.kernel.util.Settable;

import javax.crypto.Cipher;

//////////////////////////////////////////////////////////////////////////
//// CipherActor
/**
A base class for actors that encrypt and decrypt data.

<p>Cipher actors are any actors which perform encryption or
decryption based on the Java Cryptography Extension (JCE).
See the
{@link ptolemy.actor.lib.security.CryptographyActor} documentation for
resources about the JCE.

<p> Actors extending this class take in an unsigned byte array at the
<i>input</i>, process the data based on the <i>algorithm</i> parameter
and send a unsigned byte array to the <i>output</i>.  The algorithms
that may be implemented are limited to those that are implemented
by "providers" following the JCE specifications and installed in the
machine being run. The mode and padding of the algorithm can also be
specified in the <i>mode</i> and <i>padding</i> parameters.
In case a provider specific instance of an algorithm is needed,
the provider may also be specified in the <i>provider</i> parameter.
The <i>keySize</i> parameter allows implementations of algorithms
using various key sizes.

<p>Concrete actors derived from this base class must implement the
{@link ptolemy.actor.lib.security.CryptographyActor#_process(byte[])} method.
The initialize() method of this actor sets _cipher to the
value of javax.crypt.Cipher.getInstance() with an argument that is
created from the values of the <i>algorithm</i>, <i>padding</i> and
<i>keySize</i> parameters. Derived classes should call _cipher.init()
with the value of the key in the fire() method.  The_process() method
in a derived class usually calls _cipher.doFinal().

@author Christopher Hylands Brooks, Contributor: Rakesh Reddy
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
        mode.setVisibility(Settable.EXPERT);
        mode.setExpression("");
        mode.addChoice("");
        // The meaning of these is covered in the documentation of mode
        // in the ports and parameters section.
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
     *  Algorithms can be run in several different modes.
     *  The mode is specified as a string.
     *  Names for modes and modes implemented vary based on the provider.
     *  Possible values include
     * <dl>
     * <dt><code></code> (<i>The empty string</i>)
     * <dd>Use the default setting for the algorithm.
     *
     * <dt><code>NONE</code>
     * <dd>No mode, meaning that the algorithm does not use a mode.
     *
     * <dt><code>CBC</code>
     * <dd>Cipher Block Chaining Mode, as defined in FIPS PUB 81.
     * CBC is usually the mode that is used.
     *
     * <dt><code>CFB</code>
     * <dd>Cipher Feedback Mode, as defined in FIPS PUB 81.
     *
     * <dt><code>ECB</code>
     * <dd>Electronic Codebook Mode, as defined in: The National
     * Institute of Standards and Technology (NIST) Federal
     * Information Processing Standard (FIPS) PUB 81, "DES Modes of
     * Operation," U.S. Department of Commerce, Dec 1980.
     * ECM is best for encrypting small pieces of data.  If possible,
     * use CBC instead.
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
     *  In cryptography, padding is used to handle situations where the input
     *  data must be an exact multiple of the block size for the algorithm
     *  <a href="http://www.di-mgt.com.au/cryptopad.html#whennopadding" target="_top">http://www.di-mgt.com.au/cryptopad.html#whennopadding</a> says:
     *  <blockquote>
     *  Block cipher algorithms like DES and Blowfish in Electronic Code Book
     *  (ECB) and Cipher Block Chaining (CBC) mode require their input to be
     *  an exact multiple of the block size. If the plaintext to be encrypted
     *  is not an exact multiple, you need to pad before encrypting by adding
     *  a padding string. When decrypting, the receiving party needs to know
     *  how to remove the padding, if any.
     *  </blockquote>
     * 
     *  <p>The padding is specified as a string.
     *  Names for parameter and parameters implemented vary based on the
     *  provider.
     *  Possible values include
     * <dl>
     * <dt><code></code> (<i>The empty string</i>)
     * <dd>Use the default setting for the algorithm.
     *
     * <dt><code>NoPadding</code>
     * <dd>No padding (do not use padding).
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
            _updateCipherNeeded = true;
            _mode = ((StringToken)mode.getToken()).stringValue();
        } else if (attribute == padding) {
            _updateCipherNeeded = true;
            _padding = ((StringToken)padding.getToken()).stringValue();
        } else if (attribute == algorithm) {
            _updateCipherNeeded = true;
            super.attributeChanged(attribute);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Update _cipher if an attribute has changed and then invoke
     *  super.fire() to transform the input data.
     *
     *  @exception IllegalActionException If thrown by the base class or
     *  if there is a problem processing the data.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_updateCipherNeeded) {
            // If the user changed a parameter, reinitialize _cipher.  
            _updateCipher();
            _updateCipherNeeded = false;
        }
    }

    /** Retrieve the values of the parameters and set up
     *  javax.crypto.Cipher.
     *
     * @exception IllegalActionException If the algorithm cannot be found,
     * the padding scheme is illegal for the the given algorithm or
     * if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_updateCipherNeeded) {
            _updateCipher();
            _updateCipherNeeded = false;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                    Protected Methods                      ////

    /** The value of _cipher is updated by calling 
     * javax.crypt.Cipher.getInstance() with an argument that is
     * created from the values of the _algorithm, _mode and _padding.
     */
    protected void _updateCipher() throws IllegalActionException {
        // Usually, this method is called from initialize().
        // This method may end up being called in fire() if
        // the user changed attributes while the model is running.

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

    ///////////////////////////////////////////////////////////////////
    ////                    Private Variables                      ////

    // Set to true if one of the parameters changed and we need to
    // call _updateCipher().
    private boolean _updateCipherNeeded = true;
}
