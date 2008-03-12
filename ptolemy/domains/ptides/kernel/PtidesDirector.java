package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.domains.ptides.lib.SchedulePlotter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * @author Patricia Derler
 */
public class PtidesDirector extends CompositeProcessDirector implements
		TimedDirector {

	public PtidesDirector() throws IllegalActionException,
			NameDuplicationException {
		super();
		 
		_initialize();
	}

	public PtidesDirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
		super(workspace);

		_initialize();
	}

	public PtidesDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);

		_initialize();
	}
	
	
	public Parameter calculateMinDelays;
	public Parameter clockSyncError;
	public Parameter networkDelay;
	public Parameter usePtidesExecutionSemantics;
	public Parameter stopTime;
	
	
	public double getClockSyncError() {
		return _clockSyncError;
	}
	public double getNetworkDelay() {
		return _networkDelay;
	}
	public boolean usePtidesExecutionSemantics() {
		return _usePtidesExecutionSemantics;
	}
	
	 public void attributeChanged(Attribute attribute)
	     throws IllegalActionException {
		 if (attribute == clockSyncError) {
		     _clockSyncError = ((DoubleToken) clockSyncError.getToken()).doubleValue();
		 } else if (attribute == networkDelay) {
		     _networkDelay = ((DoubleToken) networkDelay.getToken()).doubleValue();
		 } else if (attribute == usePtidesExecutionSemantics) { 
			 _usePtidesExecutionSemantics = ((BooleanToken)usePtidesExecutionSemantics.getToken()).booleanValue();
		 } else if (attribute == calculateMinDelays) {
			 _calculateMinDelays = ((BooleanToken)calculateMinDelays.getToken()).booleanValue();
		 } else 
		     super.attributeChanged(attribute);
	}
	
	
	
	public void addScheduleListener(SchedulePlotter plotter) {
		_scheduleListeners.add(plotter);
	}
	
    /**
     * The stopTime parameter specifies the completion time of a model's
     * execution. During the initialize() method the value of this parameter is
     * passed to all receivers governed by this director. The default value of
     * stopTime is <I>PrioritizedTimedQueue.ETERNITY</I> indicating that
     * execution will continue indefinitely.
     */
    
	@Override
	public void initialize() throws IllegalActionException {
		super.initialize();
		_completionTime = Time.POSITIVE_INFINITY;
        
		_nextFirings = new TreeSet<Time>();
    	_completionTime = new Time(this, ((DoubleToken) stopTime.getToken()).doubleValue());
    	if (!_completionTime.equals(Time.POSITIVE_INFINITY))
    		_nextFirings.add(_completionTime);
    	
    	if (_calculateMinDelays) {
    		PtidesGraphUtilities utilities = new PtidesGraphUtilities(this.getContainer());
    		utilities.calculateMinDelays();
    	}
    	
    	Hashtable<Actor, List> table = new Hashtable<Actor, List>();
    	for (Iterator it = ((CompositeActor)getContainer()).entityList().iterator(); it.hasNext(); ) {
    		Object obj = it.next();
    		if (obj instanceof CompositeActor) {
	    		CompositeActor actor = (CompositeActor) obj;
	    		if (actor.getDirector() instanceof PtidesEmbeddedDirector) {
	    			PtidesEmbeddedDirector dir = (PtidesEmbeddedDirector) actor.getDirector();
	    			dir.setUsePtidesExecutionSemantics(_usePtidesExecutionSemantics);
	    			dir.setClockSyncError(_clockSyncError);
	    			dir.setNetworkDelay(_networkDelay);
	    		}
	    		List<Actor> actors = new ArrayList<Actor>();
	    		for (Iterator it2 = actor.entityList().iterator(); it2.hasNext(); ) {
	    			Object o = it2.next();
	    			if (o instanceof Actor)
	    				actors.add((Actor)o);
	    		}
	    		table.put(actor, actors);
    		}
    	}
    	synchronized (this) {
            if (_scheduleListeners != null) {
                Iterator listeners = _scheduleListeners.iterator();

                while (listeners.hasNext()) {
                    ((ScheduleListener) listeners.next()).initialize(table);
                }
            }
        }
    	
	}


	public Receiver newReceiver() {
        PtidesDDEReceiver receiver = new PtidesDDEReceiver();
        double timeValue;

        try {
            timeValue = ((DoubleToken) stopTime.getToken()).doubleValue() + 1;
            receiver._setCompletionTime(new Time(this, timeValue));
            receiver._lastTime = new Time(this);
        } catch (IllegalActionException e) {
            // If the time resolution of the director or the stop
            // time is invalid, it should have been caught before this.
            throw new InternalErrorException(e);
        }

        return receiver;
    }
    
    public synchronized Time getModelTime() {
//        Thread thread = Thread.currentThread();

//        if (thread instanceof DDEThread4Ptides) {
//            TimeKeeper timeKeeper = ((DDEThread4Ptides) thread).getTimeKeeper();
//            return timeKeeper.getModelTime();
//        } else {
            return _currentTime;
//        }
    }
    
    
    @Override
    public synchronized void setModelTime(Time newTime) throws IllegalActionException {
    	_currentTime = newTime;
    }
    
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
//        double ETERNITY = PrioritizedTimedQueue.ETERNITY;
//        PtidesPlatformThread ddeThread = null;
//        Thread thread = Thread.currentThread();
//
//        if (thread instanceof PtidesPlatformThread) {
//            ddeThread = (PtidesPlatformThread) thread;
//        }
//        if ((_completionTime.getDoubleValue() != ETERNITY) && (time.compareTo(_completionTime) > 0)) {
//            return;
//        }
//        Actor threadActor = ddeThread.getActor();
//        if (threadActor != actor) {
//            throw new IllegalActionException("Actor argument of DDEDirector.fireAt() must be contained by the DDEThread that calls fireAt()");
//        }
    	if (time.compareTo(getModelTime()) > 0)
			_nextFirings.add(time);

    }
    
    @Override
    public void wrapup() throws IllegalActionException {
    	super.wrapup();
    	_waitingForPhysicalTime.clear();
    	_nextFirings.clear();
    	setModelTime(new Time(this, 0.0));
    	
    	for (Iterator it = ((CompositeActor)getContainer()).entityList().iterator(); it.hasNext(); ) {
    		Object obj = it.next();
    		if (obj instanceof CompositeActor) {
	    		CompositeActor actor = (CompositeActor) obj;
	    		if (actor.getDirector() instanceof PtidesEmbeddedDirector) {
	    			PtidesEmbeddedDirector dir = (PtidesEmbeddedDirector) actor.getDirector();
	    			dir.setModelTime(getModelTime());
	    		}
    		}
    	}
	    		
    }
    
    @Override
    public boolean prefire() throws IllegalActionException {
    	
    	return super.prefire();
    }
    
    public void notifyWaitingThreads() {
		Set set = null;
		try { // TODO wrong
			set = (Set) _waitingForPhysicalTime.clone();
		} catch (Exception ex) {
			return;
		}
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Thread thread = (Thread) it.next();
			System.out.println(",,,,,, unblock: " + thread.getName() + " " ); 
			threadUnblocked(thread, null);
		}
		_waitingForPhysicalTime.clear();
	}
    
    public synchronized Time waitForFuturePhysicalTime() throws IllegalActionException {
		System.out.println("/// wait on " + ((PtidesPlatformThread)Thread.currentThread()).getActor().getName() + " " +  _getActiveThreadsCount() + " " + _getBlockedThreadsCount()  + " " + _waitingForPhysicalTime.size());
		if ((!_waitingForPhysicalTime.contains(Thread.currentThread()) 
				&& _getActiveThreadsCount() - _waitingForPhysicalTime.size() == 1)) { // increase physical time when all threads are blocked
			
			_increasePhysicalTime();
			return getModelTime();
		}
		if (_stopFireRequested)
			return getModelTime();
		_waitingForPhysicalTime.add(Thread.currentThread());
		
		threadBlocked(Thread.currentThread(), null);
		
		try {
			workspace().wait(this);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!_stopRequested)
		//threadUnblocked(Thread.currentThread(), null);
		_waitingForPhysicalTime.remove(Thread.currentThread());
		return getModelTime();
	}
    
    protected final void _displaySchedule(Actor node, Actor actor, double time,
            int scheduleEvent) {
        if (_scheduleListeners != null) {
            Iterator listeners = _scheduleListeners.iterator();

            while (listeners.hasNext()) {
                ((ScheduleListener) listeners.next()).event(node, actor,
                        time, scheduleEvent);
            }
        }
    }
    
    protected ProcessThread _newProcessThread(Actor actor,
            ProcessDirector director) throws IllegalActionException {
        return new PtidesPlatformThread(actor, director);
    }
    
    @Override
	protected synchronized boolean _resolveDeadlock() throws IllegalActionException {
		System.out.println("********* resolveDeadlock");
//		increasePhysicalTime();
		notifyWaitingThreads();
		//stop();
		return true;
	}
    
    @Override
	protected boolean _transferOutputs(IOPort port) throws IllegalActionException {
		Token token = null;
		boolean result = false;
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.hasTokenInside(i)) {
                    token = port.getInside(i);

                    if (_debugging) {
                        _debug(getName(), "transferring output from "
                                + port.getName());
                    }

                    Receiver[][] outReceivers = port.getRemoteReceivers();
                    for (int k = 0; k < outReceivers.length; k++) {
                        for (int l = 0; l < outReceivers[k].length; l++) {
                            PtidesDDEReceiver outReceiver = (PtidesDDEReceiver) outReceivers[k][l];
                            Thread thread = Thread.currentThread();

                            if (thread instanceof PtidesPlatformThread) {
                                Time time = ((PtidesPlatformThread) thread).getActor().getDirector().getModelTime();
                                outReceiver.put(token, time);
                            }
                        }
                    }
                    result = true;
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return result;       
	}

	
	
	

	
	
	
	
	private synchronized void _increasePhysicalTime() throws IllegalActionException {
		if (_nextFirings.size() == 0)
			return;
		Time time = (Time) _nextFirings.first();
		_nextFirings.remove(time);
		if (time.compareTo(_completionTime) > 0) {
			//notifyWaitingThreads();
			System.out.println("STOP !!!!!!!!");
			stopFire();
			stop();
			return;
		}
		_currentTime = time;
		System.out.println("\nphysical time: " + time + " set by " + Thread.currentThread().getName());
		notifyWaitingThreads();
	}

    private void _initialize() throws IllegalActionException, NameDuplicationException {
		double value = PrioritizedTimedQueue.ETERNITY;
		stopTime = new Parameter(this, "stopTime", new DoubleToken(value));
        timeResolution.setVisibility(Settable.FULL);
        
        try {
			clockSyncError = new Parameter(this, "clockSyncError");
			clockSyncError.setExpression("0.1");
			clockSyncError.setTypeEquals(BaseType.DOUBLE);
			
			networkDelay = new Parameter(this, "networkDelay");
			networkDelay.setExpression("0.1");
			networkDelay.setTypeEquals(BaseType.DOUBLE);
			
			usePtidesExecutionSemantics = new Parameter(this, "usePtidesExecutionSemantics");
			usePtidesExecutionSemantics.setExpression("true");
			usePtidesExecutionSemantics.setTypeEquals(BaseType.BOOLEAN);
			
			calculateMinDelays = new Parameter(this, "calculateMinDelays");
			calculateMinDelays.setExpression("true");
			calculateMinDelays.setTypeEquals(BaseType.BOOLEAN);
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        } 
	}
	

	

	
	
	private HashSet<Thread> _waitingForPhysicalTime = new HashSet<Thread>();
	private TreeSet<Time> _nextFirings;

	private Collection<ScheduleListener> _scheduleListeners = new LinkedList<ScheduleListener>();

	private double _clockSyncError;
	private double _networkDelay;
	private boolean _usePtidesExecutionSemantics;
	
	/** calcuate minimum delays or use specified minimum delays in the model */
	private boolean _calculateMinDelays;

	
	
   
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    /** The completion time. Since the completionTime is a constant,
     *  we do not convert it to a time object.
     */
    private Time _completionTime;


}
