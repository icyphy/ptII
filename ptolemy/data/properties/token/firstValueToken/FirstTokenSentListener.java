/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
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
*/
package ptolemy.data.properties.token.firstValueToken;

import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.IOPortEventListener;
import ptolemy.actor.TokenSentEvent;
import ptolemy.actor.TokenSentListener;
import ptolemy.data.Token;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.properties.token.PropertyTokenHelper;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;

public class FirstTokenSentListener implements TokenSentListener, IOPortEventListener {

    private PropertyTokenSolver _solver;

    public FirstTokenSentListener(PropertyTokenSolver solver) {
        _solver = solver;
    }

    public void tokenSentEvent(TokenSentEvent event) {

        IOPort port = event.getPort();
        Token token = event.getToken();
        if (token == null) {
            token = event.getTokenArray()[0];
        }

        try {
            ((PropertyTokenHelper)_solver.getHelper(port.getContainer())).setEquals(port, new PropertyToken(token));
        } catch (IllegalActionException e) {
            assert false;
        }
    }

    public void portEvent(IOPortEvent event) {
        if (event.getEventType() != IOPortEvent.SEND) {
            return;
        }

        IOPort port = event.getPort();
        Token token = event.getToken();
        if (token == null) {
            token = event.getTokenArray()[0];
        }

        try {
            // prevent of logging an event multiple times (necessary for SampleDelay in combination
            // with value inference for extendedFirstValueToken solver)
            if (_solver.getToken(port) == null) {
                ((PropertyTokenHelper)_solver.getHelper(port.getContainer())).setEquals(port, new PropertyToken(token));
            }
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
