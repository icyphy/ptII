/* A base class for cipher actors.

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


import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CipherActor
/**

This is a base class that implements general functions used by cipher
actors.  Cipher actors are any actors which perform encryption or
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

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/products/jca/" target="_top">http://java.sun.com/products/jca/">.
Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.


@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class CipherActor extends CryptographyActor {

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

        mode = new Parameter(this, "mode");
        mode.setTypeEquals(BaseType.STRING);
        mode.setExpression("\"\"");

        padding = new Parameter(this, "padding");
        padding.setTypeEquals(BaseType.STRING);
        padding.setExpression("\"\"");
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The padding scheme used by the cipher during encryption.  The padding
     *  is specified as a string.  Names for modes and modes implemented vary
     *  based on the provider.  For detail on this information refer to the
     *  provider specifications or use the implementation of this actor based
     *  on the BouncyCastle provider that list the options.  If left
     *  blank the default setting for the algorithm is used.
     */
    public Parameter padding;

    /** The mode of the block cipher that was for used encryption.  Names
     *  for padding schemes and padding schemes implemented vary
     *  based on the provider.  For detail on this information refer to the
     *  provider specifications or use the implementation of this actor based
     *  on the BouncyCastle provider that list the options.  If left
     *  blank, the default setting for the algorithm used.
     */
    public Parameter mode;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method retrieves the <i>algorithm</i>, <i>provider</i>,
     *  <i>mode</i>, <i>keySize</i> and <i>padding</i>.  The cipher is also
     *  initialized.  If provider is left as "SystemDefault" the system
     *  chooses the provider based on the JCE.
     *
     * @exception IllegalActionException If the algorithm cannot be found,
     * the padding scheme is illegal for the the given algorithm or
     * if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _padding = ((StringToken)padding.getToken()).stringValue();
        _mode = ((StringToken)mode.getToken()).stringValue();
        _keyAlgorithm = _algorithm;
        try {
            if (_provider.equalsIgnoreCase("default")) {
                _cipher = Cipher.getInstance(
                        _algorithm + "/" + _mode + "/" + _padding);
            } else {
                _cipher = Cipher.getInstance(
                        _algorithm + "/" + _mode + "/" + _padding, _provider);
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to initialize Cipher with algorithm: '"
                    + _algorithm + "', padding: '"
                    + _padding + "', provider: '"
                    + _provider + "'");
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
