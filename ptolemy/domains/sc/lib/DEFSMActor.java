
package ptolemy.domains.sc.lib;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sc.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.graph.*;
import java.util.Enumeration;
import collections.LinkedList;

public class DEFSMActor extends SCController implements TypedActor {

    public DEFSMActor(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

/*  public double getCurrentTime() throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        if (dir == null) {
            throw new IllegalActionException("No director available");
        }
        return dir.getCurrentTime();
    }

    public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir==null) {
	    throw new IllegalActionException("No director available");
	}
	return dir.getStartTime();
    }

    public double getStopTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir==null) {
	    throw new IllegalActionException("No director available");
	}	
	return dir.getStopTime();
    }

    public void refireAfterDelay(double delay) throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	// FIXME: the depth is equal to zero ???
        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.
        
        dir.fireAfterDelay(this, delay);
    }
*/

    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    public Enumeration typeConstraints()  {
	try {
	    workspace().getReadAccess();

	    LinkedList result = new LinkedList();
	    Enumeration inPorts = inputPorts();
	    while (inPorts.hasMoreElements()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		if (inport.getDeclaredType() == null) {
		    Enumeration outPorts = outputPorts();
	    	    while (outPorts.hasMoreElements()) {
		    	TypedIOPort outport =
				 (TypedIOPort)outPorts.nextElement();

		    	if (outport.getDeclaredType() == null &&
			    inport != outport) {
			    // output also undeclared, not bi-directional port, 
		            Inequality ineq = new Inequality(
				inport.getTypeTerm(), outport.getTypeTerm());
			    result.insertLast(ineq);
			}
		    }
		}
	    }
	    return result.elements();

	}finally {
	    workspace().doneReading();
	}
    }

}


