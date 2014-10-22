/*

Copyright (c) 2011-2014 The Regents of the University of California.
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
 */
package ptserver.communication;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptserver.data.AttributeChangeToken;

///////////////////////////////////////////////////////////////////
//// RemoteValueListener

/** A value listener that listens to changes made to a variable widget
 *  and publishes the changes as an AttributeChangeToken.
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ProxyValueListener implements ValueListener {

    /** Initialize the instance with the given token publisher
     *  and enable its listener.
     *  @param tokenPublisher the tokenPublisher used for sending value change events.
     */
    public ProxyValueListener(TokenPublisher tokenPublisher) {
        _tokenPublisher = tokenPublisher;
        setEnabled(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Capture value changes of the settable and send them via the publisher to the remote model.
     *  @param settable The settable whose value changed.
     *  @see ptolemy.kernel.util.ValueListener#valueChanged(ptolemy.kernel.util.Settable)
     */
    @Override
    public synchronized void valueChanged(Settable settable) {
        if (isEnabled()) {
            AttributeChangeToken token = new AttributeChangeToken();
            token.setTargetSettableName(settable.getFullName());
            token.setExpression(settable.getExpression());
            try {
                getTokenPublisher().sendToken(token, null);
            } catch (IllegalActionException e) {
                // FIXME Add logging and exception delegation
                e.printStackTrace();
            }
        }
    }

    /** Return TokenPublisher that would be used to publish
     *  AttributeChange tokens produced by this actor on value change.
     *  @return TokenPublisher the token publisher
     */
    public TokenPublisher getTokenPublisher() {
        return _tokenPublisher;
    }

    /** Set enabled flag of the listener.  If it's true,
     *  the listener would send the attribute value change token.
     *  @param enabled the enabled flag.
     *  @see #isEnabled()
     */
    public synchronized void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /** Return the enabled flag of the listener. If it's true,
     *  the listener would send the attribute value change token.
     *  @return the enabled flag.
     *  @see #setEnabled(boolean)
     */
    public synchronized boolean isEnabled() {
        return _enabled;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Token Publisher is used to publish AttributeChange to a queue for serializing
     *  into a binary.
     */
    private final TokenPublisher _tokenPublisher;

    /** Flag to indicate whether the listener is enabled.
     */
    private boolean _enabled;
}
