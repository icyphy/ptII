/* An OpenModelica Director by using the content of LeftRightDirector
  
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.domains.openmodelica.kernel;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.openmodelica.lib.core.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCLogger;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// OpenModelicaDirector

// FIXME: This comment looks to be from the left-to-right director?
// FIXME: What does this Director do? 
// FIXME: Briefly describe OpenModelica in a paragraph.  The issue
// is that a reader might not know.
/** Fire actors in left-to-right order. This director is a simple
 *  illustration of how to construct schedules for firing. It examines
 *  the location of the actor in a Vergil window, and on each
 *  invocation of the fire() method, fires each actor once,
 *  starting with the leftmost one and ending with the rightmost one.
 *  If two actors have the same horizontal position, then the order
 *  of their firings is arbitrary.
 *  <p>
 *  Note that this director will fire the actors forever. It may
 *  be difficult to stop the model executing.
 *  FIXME: Edward is not the author,
 *  @author Edward A. Lee
 *  FIXME: I added these JavaDoc tags, please add them to all your files
 *  For version use DollarIdDollar, where Dollar is '$', it will get substituted.
 *  @version $Id$
 *  @since Ptolemy II 9.1
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 *  
 */
public class OpenModelicaDirector extends StaticSchedulingDirector {

    // FIXME: Look at other directors and the style guide.  See SDFDirector.
    // What does this constructor do?
    /** Constructor. A director is an Attribute.
     *  @param container The container for the director.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the container cannot contain 
     *   this director.
     *  @exception NameDuplicationException If the container already contains an
     *   Attribute with this name.
     */
    public OpenModelicaDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Set the scheduler.
        setScheduler(new OpenModelicaScheduler(this, "OpenModelicaScheduler"));

        iterations = new Parameter(this, "iterations");
        iterations.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    // FIXME: Should be "The Parameter", not "Parameter"
    /** Parameter specifying the number of iterations.
     *  If the value is 0 or less, then the model does not stop
     *  executing on its own.
     *  This is an int that defaults to 1.
     */
    public Parameter iterations;
    // FIXME: Place an empty line between variable declarations.
    // FIXME: public variables do not start with underscore.
    // FIXME: Be sure to say what OMC stands for?
    // FIXME: Do these need to be public?  If they are only set in
    // in this class, then should they be private and have just get?  
    // FIXME: Javadoc comments end in a period.  These comments do
    // not tell me what these variables are used for, please expand
    // the comments.
    // FIXME: What is a Pr?  In Ptolemy, we don't use abbeviations.
    // Maybe change this to _omcProxy, make it private, move it
    // below and add a getOMCProxy() method?
    /** Object of the OMCProxy */
    public static OMCProxy _omcPr;

    // FIXME: See above.
    /** Object of the OMCLogger */
    public static OMCLogger _ptLogger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                ////

    // FIXME: The comment is a little unclear.  It looks like
    // the _iterationCount is set to zero.  See SDFDirector for
    // wording.  I'm not sure if it is necessary to use the word
    // "Override".
    // FIXME: See SDFDirector.#initialize() for suggested
    // text for the exception tag.
    /** Override to initialize the iteration count. */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
    }

    // FIXME: Add an @return tag that states what this method does
    // See SDFDirector for suggested text.
    // FIXME: See SDFDirector#postfire() for suggested
    // text for the exception tag.
    /** Override to check the number of iterations. */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        _iterationCount++;
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        if (iterationsValue > 0 && _iterationCount >= iterationsValue) {
            return false;
        }
        return result;
    }

    // FIXME: Remove the @author tag, it will cause warnings.
    // FIXME: In JavaDoc, the first sentence ends with a period.
    // FIXME: See SDFDirector.#preinitialize() for suggested
    // text for the exception tag.
    /** Initialize the communication with OMC
     *  @exception IllegalActionException TODO ....
     *  @author Mana Mirzaei
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            _ptLogger = new OMCLogger();
            _omcPr = new OMCProxy();
            _omcPr.init();
        } catch (ConnectException ex) {
            throw new IllegalActionException(this, ex,
                    "Unable to start the server!");
        }
    }

    // FIXME: Remove the @author tag, it will cause warnings.
    // FIXME: In JavaDoc, the first sentence ends with a period.
    // FIXME: See SDFDirector#wrapup() for suggested
    // text for the exception tag.
    /** Leave and quit OpenModelica environment
     *  @exception IllegalActionException Not thrown in this base class.
     *  @author Mana Mirzaei
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            _omcPr.quit();
            
            /*Set the created object to null*/
            _omcPr = null;
            _ptLogger = null;
        } catch (ConnectException ex) {
            _ptLogger
                    .getInfo("Ignore this exception for quit(), it is already quited!: "
                            + ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    /** Count of the number of iterations. */
    private int _iterationCount;

    /** Inner class defining the scheduler.
     */
    public static class OpenModelicaScheduler extends Scheduler {

        // FIXME: Don't write "Constructor", write what this constructor
        // constructs.
        /** Constructor. A Scheduler is an Attribute,
         *  normally contained by a director.
         *  @param director The director that will use this scheduler.
         *  @param name The name of the scheduler.
         *  @exception IllegalActionException If the director cannot use
         *   this scheduler.
         *  @exception NameDuplicationException If the director already
         *   contains an Attribute with this name.
         */
        public OpenModelicaScheduler(OpenModelicaDirector director, String name)
                throws IllegalActionException, NameDuplicationException {
            super(director, name);
        }

        // FIXME: Copy and Paste?  Does this really return a left-to-right
        // schedule?
        /** Return a left-to-right schedule. */
        protected Schedule _getSchedule() throws IllegalActionException,
                NotSchedulableException {
            // Get the director.
            NamedObj director = getContainer();
            // Get the container of the director.
            CompositeActor compositeActor = (CompositeActor) director
                    .getContainer();
            // Get the actors to be fired by the director.
            List<Actor> actors = compositeActor.deepEntityList();
            // Create a sorted list of actors, sorted by
            // a specialized comparator.
            TreeSet<Actor> sortedActors = new TreeSet(new LeftRightComparator());
            sortedActors.addAll(actors);
            // Construct a Schedule from the sorted list.
            Schedule schedule = new Schedule();
            for (Actor actor : sortedActors) {
                Firing firing = new Firing(actor);
                schedule.add(firing);

            }
            return schedule;
        }

        // FIXME: Hmm, I guess it really does return left-to-right.
        // You should clearly state why this is done in the constructor
        // for the entire class.  Also, add Edward A. Lee as a contributor 
        // in the class wide javadoc:
        //  @author Mana Mirzaei, Based on LeftToRightDirector by Edward A. Lee
        /** Inner class that implements a specialized comparator
         *  that compares the horizontal positions of the two
         *  arguments, which are assumed to actors.
         */
        public static class LeftRightComparator implements Comparator {
            public int compare(Object o1, Object o2) {
                // In case there is no location for an actor,
                // provide a default.
                double[] location1 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
                double[] location2 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
                // The location of the actor in Vergil is stored in an
                // Attribute that implements the Locatable interface.
                // Get a list of all such attributes, and use the first one
                // (normally there will be only one).
                List locations = ((Entity) o1).attributeList(Locatable.class);
                if (locations.size() > 0) {
                    location1 = ((Locatable) locations.get(0)).getLocation();
                }
                locations = ((Entity) o2).attributeList(Locatable.class);
                if (locations.size() > 0) {
                    location2 = ((Locatable) locations.get(0)).getLocation();
                }
                if (location1[0] < location2[0]) {
                    return -1;
                } else if (location1[0] > location2[0]) {
                    return 1;
                } else {
                    // NOTE: It is not correct to return 0 if the x
                    // locations are the same because the actors may
                    // not be the same actor. A comparator has to be
                    // consistent with equals. We arbitrarily return -1,
                    // unless they are equal.
                    if (o1.equals(o2)) {
                        return 0;
                    }
                    return -1;
                }
            }
        }
    }
}
