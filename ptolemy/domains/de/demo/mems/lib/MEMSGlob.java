package ptolemy.domains.de.demo.mems.lib;


import ptolemy.domains.de.demo.mems.gui.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

/*
@author Allen Miu
@version $Id$
*/

public class MEMSGlob extends DEActor {


    public MEMSGlob(TypedCompositeActor container, String name, MEMSPlot plot)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
	this.plot = plot;
	Debug.log(0, "MEMSGlob instance created");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    /** Schedules the next sampling event for all sensors and 
     *  processes pending tokens at the msgIO and sysIO ports
     *
     *  @exception CloneNotSupportedException If there is more than one
     *   destination and the output token cannot be cloned.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
      Debug.log(0, "MEMSGlob flushes");
      plot.flush();
      
      synchronized (this) {
	try {
	  wait(100);
	} catch (InterruptedException e) {}
      }

      fireAfterDelay(1.0);
    }

    /** Produce the initializer event that will cause the generation of
     *  the first output at time zero.
     *
     *  FIXME: What to do if the initial current event is less than zero ?
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        // FIXME: This should be just DEDirector
        // FIXME: This class should be derived from DEActor, which should
        // ensure that this cast is valid.
        super.initialize();
        double curTime = getCurrentTime();
        // The delay parameter maybe negative, but it's permissible in the
        // director because the start time is not initialized yet.
        fireAfterDelay(0.0-curTime);
    }

    private MEMSPlot plot;
}

