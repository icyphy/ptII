/*

Copyright (c) 2011 The Regents of the University of California.
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
/**
* A value listener that listens to changes made to a variable widget
* and publishes the changes as an AttributeChangeToken.
* @author Peter Foldes
* @version $Id$
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (pdf)
* @Pt.AcceptedRating Red (pdf)
*/
public class RemoteValueListener implements ValueListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Capture value changes of the settable and send them via the publisher to the remote model.
     * @param settable The settable whose value changed.
     * @see ptolemy.kernel.util.ValueListener#valueChanged(ptolemy.kernel.util.Settable)
     */
    public void valueChanged(Settable settable) {
        AttributeChangeToken token = new AttributeChangeToken();
        token.setTargetSettableName(settable.getName());
        token.setExpression(settable.getExpression());
        try {
            getTokenPublisher().sendToken(token);
        } catch (IllegalActionException e) {
            // FIXME Add logging and exception delegation
            e.printStackTrace();
        }
    }

    /**
     * Return TokenPublisher that would be used to publish
     * AttributeChange tokens produced by this actor on value change.
     * @return TokenPublisher the token publisher
     * @see #setTokenPublisher(TokenPublisher)
     */
    public TokenPublisher getTokenPublisher() {
        return tokenPublisher;
    }

    /**
     * Set the token publisher that would be used to send
     * AttributeChange tokens.
     * @param tokenPublisher the token publisher used to send attribute change messages.
     * @see #getTokenPublisher()
     */
    public void setTokenPublisher(TokenPublisher tokenPublisher) {
        this.tokenPublisher = tokenPublisher;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Token Publisher is used to publish AttributeChange to a queue for serializing
     * into a binary.
     */
    private TokenPublisher tokenPublisher;
}
