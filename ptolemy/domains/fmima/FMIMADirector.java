package ptolemy.domains.fmima;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TimeRegulator;
import ptolemy.actor.util.Time;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

public class FMIMADirector extends SRDirector {

    public FMIMADirector() throws IllegalActionException, NameDuplicationException {
        // TODO Auto-generated constructor stub
        _isFirstFire = true;
    }

    public FMIMADirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _isFirstFire = true;
        // TODO Auto-generated constructor stub
    }

    public FMIMADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _isFirstFire = true;
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void fire() throws IllegalActionException {        
        if (_debugging) {
            _debug("FMIMADirector: invoking fire().");
        }
        
        if (getModelTime().getDoubleValue() > getStopTime()) {
            stop();
        }
        /**
         *  Calling FixedPointDirector.fire()
         *  When super.fire() returns we reached a fixed point:
         *  all FMUs propagated I/O signals.
         */
        super.fire();

        if (_debugging) {
            _debug("FMIMADirector: returned super.fire().");
        }
        
        /** 
         *  We can now compute the step size of the FMU
         *  Consult all actors that implement TimeRegulator interface.
         *  FMUs for example, can implement TimeRegulator interface
         *  to check the acceptance of a step size.
         */
        
        _proposedFmiTime = getModelTime().add(1E-8);//Time.POSITIVE_INFINITY;
        // FIXME: move this class var into this class.
        
        Nameable container = getContainer();
        Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                .iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof TimeRegulator) {
                Time modifiedTime = ((TimeRegulator) actor).proposeTime(_proposedFmiTime);
                if (_proposedFmiTime.compareTo(modifiedTime) > 0) {
                    _proposedFmiTime = modifiedTime;
                }
                if (_debugging) {
                    _debug("FMU " + actor.getFullName() + " proposed: " + modifiedTime.getLongValue() + " at time: " + getModelTime());
                }
            }
        }
        if (_debugging) {
            _debug("Computed future time: " + _proposedFmiTime);
        }
    }
    
    @Override
    public boolean postfire() throws IllegalActionException {

        boolean result=  super.postfire();

        setModelTime(_proposedFmiTime);
        
        return result;
    }

//    @Override
//    public boolean prefire() throws IllegalActionException {
//        if (getModelTime().getDoubleValue() >= getStopTime()) {
//            stop();
//        }
////        _proposedFmiTime = getModelTime().add(1.0);
//        boolean result = super.prefire();
////        setModelTime(_proposedFmiTime);
//        return result;
//    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////
    
    protected boolean _isFirstFire;
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

}
