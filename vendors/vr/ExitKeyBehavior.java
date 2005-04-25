/*
 *	@(#)ExitKeyBehavior.java 1.2 99/09/15 13:44:10
 *
 * Copyright (c) 1996-1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package vendors.vr;

import javax.media.j3d.*;
import java.util.*;
import java.awt.Event;
import java.awt.Point;
import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import javax.vecmath.*;

/**
 * A KeyBehavior which called exit when the ESC key is pressed
 */

public class ExitKeyBehavior extends Behavior {

    // look for released events since sometimes ESCAPE only generates a
    // RELEASE event
    WakeupCriterion criterion[] = {
    		new WakeupOnAWTEvent( Event.KEY_PRESS ),
    		new WakeupOnAWTEvent( Event.KEY_RELEASE ),
		};

    WakeupCondition conditions = new WakeupOr( criterion );

    public void initialize() {
        wakeupOn(conditions);
    }

    public void processStimulus( Enumeration criteria ) {
	WakeupCriterion wakeup;
	AWTEvent[] evt=null;

	while( criteria.hasMoreElements() ) {
	    wakeup = (WakeupCriterion)criteria.nextElement();
	    if (wakeup instanceof WakeupOnAWTEvent) {
		 evt=((WakeupOnAWTEvent)wakeup).getAWTEvent();
	    }

	    if (evt!=null) {
		for(int i=0; i<evt.length; i++) {
		    if (evt[i] instanceof KeyEvent)  {
			processKeyEvent( (KeyEvent)evt[i] );
		    }
		}
	    }
	}
	wakeupOn( conditions );
    }

    private void processKeyEvent( KeyEvent evt ) {
	int key = evt.getKeyCode();
	switch (key) {
	  case KeyEvent.VK_ESCAPE:
	    System.exit(0);
	    break;
	}
    }
}
