/* BlockingFireTest is a unit test for BlockingFire.

 Copyright (c) 2012-2013 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel.test.junit;

import java.util.LinkedList;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.test.TestActor;
import ptolemy.domains.metroII.kernel.BlockingFire;
import ptolemy.domains.metroII.kernel.FireMachine;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import junit.framework.TestCase;

///////////////////////////////////////////////////////////////////
//// BlockingFireTest

/**
* BlockingFireTest is a unit test for BlockingFire. It tests
* <ol>
* <li> whether the state of actor is set correctly</li>
* <li> whether the events are proposed correctly</li>
* </ol>
*
* @author Liangpeng Guo
* @version $Id$
* @since Ptolemy II 10.0
* @Pt.ProposedRating Red (glp)
* @Pt.AcceptedRating Red (glp)
*
*/
public class BlockingFireTest extends TestCase {

    /**
     * Execution sequence and expected states and events:
     * <ol>
     * <li> initialization: expect the state is set to START. </li>  
     * <li> startOrResume(): expect the state is set to BEGIN and FIRE_BEGIN is proposed. </li>  
     * <li> startOrResume(): expect the state is set to BEGIN and FIRE_BEGIN is proposed. </li>  
     * <li> notify FIRE_BEGIN and startOrResume(): expect the state is set to END and FIRE_END is proposed. </li>  
     * <li> startOrResume(): expect the state is set to END and FIRE_END is proposed. </li>  
     * <li> notify FIRE_END and startOrResume(): expect the state is set to FINAL. </li>  
     * <li> startOrResume(): expect the state is set to FINAL. </li>  
     * <li> reset(): expect the state is set to START. </li>  
     * </ol>
     * 
     */
    @org.junit.Test
    public void test() {
        TestActor actor;
        try {
            actor = new TestActor(new CompositeActor(), "TestActor");
            BlockingFire firing = new BlockingFire(actor); 
            
            assertEquals(firing.getState(), FireMachine.State.START);
            
            LinkedList<Builder> events = new LinkedList<Builder>();

            firing.startOrResume(events);
            assertEquals(firing.getState(), FireMachine.State.BEGIN);
            assertEquals(events.size(), 1);
            System.out.println(events.getFirst().getName()); 
            assertEquals(events.getFirst().getStatus(), Event.Status.PROPOSED);
            
            firing.startOrResume(events);
            assertEquals(firing.getState(), FireMachine.State.BEGIN);
            assertEquals(events.size(), 1);
            System.out.println(events.getFirst().getName()); 
            assertEquals(events.getFirst().getStatus(), Event.Status.PROPOSED);

            events.getFirst().setStatus(Event.Status.NOTIFIED); 
            firing.startOrResume(events);
            assertEquals(events.size(), 1);
            assertEquals(firing.getState(), FireMachine.State.END);
            System.out.println(events.getFirst().getName()); 
            assertEquals(events.getFirst().getStatus(), Event.Status.PROPOSED);
            
            firing.startOrResume(events);
            assertEquals(events.size(), 1);
            assertEquals(firing.getState(), FireMachine.State.END);
            System.out.println(events.getFirst().getName()); 
            assertEquals(events.getFirst().getStatus(), Event.Status.PROPOSED);

            events.getFirst().setStatus(Event.Status.NOTIFIED); 
            firing.startOrResume(events);
            assertEquals(events.size(), 0);
            assertEquals(firing.getState(), FireMachine.State.FINAL);
            
            firing.startOrResume(events);
            assertEquals(events.size(), 0);
            assertEquals(firing.getState(), FireMachine.State.FINAL);

            firing.reset();
            assertEquals(firing.getState(), FireMachine.State.START);
            
        } catch (IllegalActionException e) {
            e.printStackTrace();
        } catch (NameDuplicationException e) {
            e.printStackTrace();
        } 
    }

}
