package ptolemy.domains.de.demo.mems.lib;

import ptolemy.plot.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

abstract class MEMSActor extends DEActor {
  
  public MEMSActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException  {
    super(container, name);
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
  

  /** Returns the debug message header for this class 
   */
  public String getDebugHeader() {
    return _debugHeader;
  }
  public int getID() {
    return myID;
  }

  abstract protected void fireDueEvents() throws IllegalActionException;
  protected String _debugHeader;
  protected int myID;
  protected double prevFireTime = -1.0;
}

