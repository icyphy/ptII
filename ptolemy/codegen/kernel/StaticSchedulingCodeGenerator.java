/* Base class for code generators for static scheduling models of computation.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.codegen.kernel;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// StaticSchedulingCodeGenerator

/** FIXME
 * 
 *  @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Rachel Zhou
 *  @version $Id$
 *  @since Ptolemy II 5.0
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class StaticSchedulingCodeGenerator extends CodeGenerator {

	/** Create a new instance of the C code generator.
	 *  @param container The container.
	 *  @param name The name.
	 *  @throws IllegalActionException
	 *  @throws NameDuplicationException
	 */
	public StaticSchedulingCodeGenerator(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}
    
    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    public void generateFireCode(CodeStream stream) 
            throws IllegalActionException {
        
        CompositeEntity model = (CompositeEntity)getContainer();
        
        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        Director director = ((Actor)model).getDirector();
         
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
        
        String packageName = generatorPackage.stringValue();
        
        Iterator actorsToFire = schedule.iterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing)actorsToFire.next();
            Actor actor = firing.getActor();
            
            // FIXME: Before looking for a helper class, we should
            // check to see whether the actor contains a code generator
            // attribute. If it does, we should use that as the helper.
            
            String actorClassName = actor.getClass().getName();
            String helperClassName = actorClassName
                    .replaceFirst("ptolemy", packageName);
            
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
			} catch (Exception e) {
                throw new IllegalActionException((NamedObj)actor, e,
                        "Failed to create helper class code generator.");
			}
            
            if (!(helperObject instanceof ActorCodeGenerator)) {
                throw new IllegalActionException(this,
                        "Cannot generate code for this actor: "
                        + actor
                        + ". Its helper class does not implement CodeFactory.");
            }
            ActorCodeGenerator castHelperObject = (ActorCodeGenerator)helperObject;
            castHelperObject.generateFireCode(stream);
        }
    }
    
    // FIXME: Override setContainer to ensure that the container is an Actor.
}
