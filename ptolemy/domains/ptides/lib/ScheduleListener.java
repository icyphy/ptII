/*
@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.domains.ptides.lib;

import java.util.Hashtable;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// ScheduleListener

/**
 * A schedule listener reacts to given events.
 * 
 * @author Patricia Derler
 */
public interface ScheduleListener {
	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * react to the given event.
	 */
	public void event(Actor node, Actor actor, double time, int scheduleEvent);

	/**
	 * initialize the legend of the display
	 * 
	 * @param nodesActors
	 *            contains platforms and actors running on that platform
	 */
	public void initialize(Hashtable nodesActors);

	static final int START = 0;

	static final int STOP = 1;

	static final int TRANSFEROUTPUT = 2;

	static final int TRANSFERINPUT = 3;

	static final int MISSEDEXECUTION = 4;

}
