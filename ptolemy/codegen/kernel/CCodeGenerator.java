/*
 * Created on Feb 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.kernel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * @author zgang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CCodeGenerator extends Attribute {

	/**
	 * 
	 */
	public CCodeGenerator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param workspace
	 */
	public CCodeGenerator(Workspace workspace) {
		super(workspace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public CCodeGenerator(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}

    public String generateCCode(CompositeActor model) 
            throws IllegalActionException {
        
        StringBuffer buffer = new StringBuffer();
        
        Director director = model.getDirector();
         
        if (director == null) {
            throw new IllegalActionException(this, 
                    "The model " + model.getName()
                    + " does not have a director.");   
        }
        
        if (!(director instanceof StaticSchedulingDirector)) {
            throw new IllegalActionException(this, 
                    "The director of the model " + model.getName()
                    + " is not a StaticSchedulingDirector.");        
        }
        
        StaticSchedulingDirector castDirector = 
            (StaticSchedulingDirector)director;
        Schedule schedule = castDirector.getScheduler().getSchedule();
        
        Iterator actorsToFire = schedule.iterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing)actorsToFire.next();
            Actor actor = firing.getActor();
            String actorClassName = actor.getClass().getName();
            String helperClassName = actorClassName
                    .replaceFirst("ptolemy", "ptolemy.codegen.c");
            
            Class helperClass = null;
            try {
                helperClass = Class.forName(helperClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalActionException(this, e, 
                        "Cannot find helper class " 
                        + helperClassName);   
            }
            
            Constructor constructor = null;
            try {
                constructor = helperClass
                        .getConstructor(new Class[]{actor.getClass()});
            } catch (NoSuchMethodException e) {
                throw new IllegalActionException(this, e,
                        "There is no constructor in " 
                        + helperClassName
                        + " which accepts an instance of "
                        + actorClassName
                        + " as the argument.");
            }
            
            Object helperObject = null;
            try {
				helperObject = constructor.newInstance(new Object[]{actor});
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            if (!(helperObject instanceof CodeFactory)) {
                throw new IllegalActionException(this,
                        "Cannot generate code for this actor: "
                        + actor
                        + ". Its helper class does not implement CodeFactory.");
            }
            CodeFactory castHelperObject = (CodeFactory)helperObject;
            castHelperObject.generateFireCode();
            buffer.append(castHelperObject.getCode("default"));
        }
        return buffer.toString();
    }
    

}
