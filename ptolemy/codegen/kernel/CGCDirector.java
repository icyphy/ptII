/* FIXME: Class comments.
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** FIXME
 * @author eal
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CGCDirector extends SDFDirector {

	/** FIXME
	 * 
	 */
	public CGCDirector() throws IllegalActionException, NameDuplicationException {
		super();
		// TODO Auto-generated constructor stub
	}

	/** FIXME
	 * @param workspace
	 */
	public CGCDirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
		super(workspace);
		// TODO Auto-generated constructor stub
	}

	/** FIXME
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public CGCDirector(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}

    public void preinitialize() throws IllegalActionException {
    	super.preinitialize();
        
        Schedule schedule = getScheduler().getSchedule();
        
        Iterator actorsToFire = schedule.iterator();
        while (actorsToFire.hasNext()) {
        	Firing firing = (Firing)actorsToFire.next();
            Actor actor = firing.getActor();
            if (!(actor instanceof CodeGeneratingActor)) {
            	throw new IllegalActionException(this,
                        "Cannot generate code for this actor: "
                        + actor
                        + ". It does not implement CodeGeneratingActor.");
            }
            CodeGeneratingActor castActor = (CodeGeneratingActor)actor;
            castActor.generateFireCode();
            // FIXME
            System.out.println(castActor.getCode("default"));
        }
    }
}
