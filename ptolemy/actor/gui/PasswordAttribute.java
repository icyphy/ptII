/* A password attribute.

 Copyright (c) 2004-2005 The Regents of the University of California.
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

 @ProposedRating Green (eal@eecs.berkeley.edu)
 @AcceptedRating Green (bilung@eecs.berkeley.edu)
 */
package ptolemy.actor.gui;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// PasswordAttribute

/**
 An attribute that represents a password. The value of this attribute is
 a string that represents the password in an encrypted form. To access the
 password in unencrypted form, call the getPassword() method. To set the
 password in unencrypted form, call the setPassword() method.

 FIXME: we need to support the persistence and encryption.

 @author Edward Lee, Yang Zhao
 @version $Id$
 @since Ptolemy II 4.1
 */
public class PasswordAttribute extends StringParameter {
    /** Construct a password attribute with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name.
     */
    public PasswordAttribute(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the password contained by this attribute. If the password
     *  hasn't been set, then open a dialog and wait for the user to
     *  set the password.
     *  @return The password.
     */
    public char[] getPassword() {
        if (_password == null) {
            //FIXME: this need to be done in the swing thread...
            new EditParametersDialog(null, this);
        }

        return _password;
    }

    /** Set the password contained by this attribute.
     *  @param password The password.
     */
    public void setPassword(char[] password) {
        _password = password;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////
    private char[] _password = null;
}
