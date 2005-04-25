/*
 *      @(#)FirstFramesBehavior.java 1.1 00/09/20 15:49:35
 *
 * Copyright (c) 1996-1998 Sun Microsystems, Inc. All Rights Reserved.
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
import java.util.Enumeration;

/**
 * A Behavior waits for the initial frame to be displayed, calls
 * VolRend.eyePtChanged() and stops running
 */

public class FirstFramesBehavior extends Behavior {

    VolRendListener volRend;
    int		numFrames = 0;

    FirstFramesBehavior(VolRendListener volRend) {
	this.volRend = volRend;
    }

    WakeupCriterion criterion[] = {
    		new WakeupOnElapsedFrames(0),
		};

    WakeupCondition conditions = new WakeupOr( criterion );

    public void initialize() {
        wakeupOn(conditions);
    }

    public void processStimulus( Enumeration criteria ) {
	//System.out.println("frame");
	volRend.eyePtChanged();

	// don't wakeup after the first couple frame
	if (numFrames++ < 5) {
	    wakeupOn( conditions );
	}
    }
}
