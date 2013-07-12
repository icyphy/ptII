/* MetroFSMDirector extends FSMDirector to support Metro semantics.
 
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
package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.FSMDirector;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////MetroFSMDirector

/** 
* MetroFSMDirector extends FSMDirector to support Metro semantics.
*
* @author Liangpeng Guo
* @version $Id: MetroFSMDirector.java 66808 2013-07-03 00:20:58Z glp $
* @since Ptolemy II 9.1
* @Pt.ProposedRating Red (glp)
* @Pt.AcceptedRating Red (glp)
*
*/
public class MetroFSMDirector extends FSMDirector implements
        MetroEventHandler {

    /**
     * Construct a MetroFSMDirector. 
     * 
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public MetroFSMDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
    }

    /**
     * Construct a MetroFSMDirector based on a given container and a name
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MetroFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * YieldAdapter interface
     */
    public YieldAdapterIterable<Iterable<Event.Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }


    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroFSMDirector newObject = (MetroFSMDirector) super
                .clone(workspace);
        newObject.events = new ArrayList<Event.Builder>();
        return newObject;
    }

    /**
     *  Keep proposing the event associated with the current state until it's notified. 
     *  Then call fire() 
     */
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        FSMActor controller = null;
        try {
            controller = getController();
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }

        if (controller == null) {
        } else {
            State currentState = controller.currentState();
            if (_debugging) {
                _debug("*** Firing " + getFullName(), " at time "
                        + getModelTime());
                _debug("Current state is:", currentState.getName());
            }

            Event.Builder eb;
            do {
                eb = MetroEventBuilder.newProposedEvent(MetroEventBuilder.trimModelName(currentState.getFullName()));
                events.add(eb);
                resultHandler.handleResult(events);
                events.remove(events.size() - 1);
            } while (eb.getStatus() != Event.Status.NOTIFIED);

            try {
                super.fire();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////
    
    /**
     * Current MetroII event list
     */
    private ArrayList<Event.Builder> events = new ArrayList<Event.Builder>();
    
}
