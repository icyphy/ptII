/* An actor that sends car position to a Java Space.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.CarTracking;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.lib.jspaces.TokenEntry;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;

import java.rmi.RemoteException;
import net.jini.space.JavaSpace;
import net.jini.core.transaction.TransactionException;
import net.jini.core.lease.Lease;

//////////////////////////////////////////////////////////////////////////
//// Publisher
/**
An actor that sends entries to a Java Space. New information will override
the old ones.

@author Jie Liu, Yuhong Xiong
@version $Id$
*/

public class CarInformationPublisher extends TypedAtomicActor 
    implements TimedActor, CTStepSizeControlActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CarInformationPublisher(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

    	jspaceName = new Parameter(this, "jspaceName", 
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);

        entryName = new Parameter(this, "entryName", 
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        
        samplingPeriod = new Parameter(this, "samplingPeriod",
                new DoubleToken(1.0));
        samplingPeriod.setTypeEquals(BaseType.DOUBLE);

        malfunctioning = new Parameter(this, "malfunctioning",
                new BooleanToken(false));
        malfunctioning.setTypeEquals(BaseType.BOOLEAN);

        force = new TypedIOPort(this, "force", true, false);
        force.setMultiport(false);
        
        velocity = new TypedIOPort(this, "velocity", true, false);
        velocity.setMultiport(false);

        position = new TypedIOPort(this, "position", true, false);
        position.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for force, of type DoubleToken.
     */
    public TypedIOPort force;

    /** Input port for velocity, of type DoubleToken.
     */
    public TypedIOPort velocity;

    /** Input port for position, of type DoubleToken.
     */
    public TypedIOPort position;

    /** The Java Space name. The default name is "JavaSpaces" of 
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the entries to be published. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    /** The sampling period. Default is 1.0.
     */
    public Parameter samplingPeriod;

    /** Indicate whether the actor is malfunctioning. Default is false.
     */
    public Parameter malfunctioning;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
	try {
	    CarInformationPublisher newobj = 
                (CarInformationPublisher)super.clone(ws);
	    newobj.jspaceName = (Parameter)newobj.getAttribute("jspaceName");
            newobj.entryName = (Parameter)newobj.getAttribute("entryName");
            newobj.samplingPeriod = (Parameter)newobj.getAttribute(
                    "samplingPeriod");
            newobj.malfunctioning = (Parameter)newobj.getAttribute(
                    "malfunctioning");
            newobj.force = (TypedIOPort)newobj.getPort("force");
            newobj.velocity = (TypedIOPort)newobj.getPort("velocity");
            newobj.position = (TypedIOPort)newobj.getPort("position");
	    return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Return true always.
     *  @return True if the current integration step is acceptable.
     */
    public boolean isThisStepSuccessful() {
        return true;
    }

    /** Return java.lang.Double.MAX_VALUE.
     *  @return java.lang.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return the current step size from the
     *  director.
     *  @return The current step size.
     */
    public double refinedStepSize() {
        return ((CTDirector)getDirector()).getCurrentStepSize();
    }

    /** Find the JavaSpaces according to the jspaceName parameter.
     *  Write the minimum and maximum index token.
     *  At the beginning, the minimum index is larger than maximum by 1,
     *  and the maximum index is the current serial number.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
	String name = ((StringToken)jspaceName.getToken()).toString();
	_space = SpaceFinder.getSpace(name);
        String entryname = ((StringToken)entryName.getToken()).toString();
        TokenEntry tokenTemplate = new TokenEntry(name, null, null);
        try {
            TokenEntry oldEntry;
            do {
                oldEntry = (TokenEntry)_space.takeIfExists(
                        tokenTemplate, null, 1000);
            } while (oldEntry != null);
        } catch (RemoteException re) {
	    throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + re.getMessage());
	} catch (TransactionException te) {
	    throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + te.getMessage());
	} catch (InterruptedException ie) {
            throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + ie.getMessage());
        } catch (net.jini.core.entry.UnusableEntryException ue) {
            throw new IllegalActionException(this, "Unusable Entry " +
                    ue.getMessage());
        }
        //System.out.println("Finish intialization.");
    }

    /** Set the next sampling time.
     */
    public void initialize() throws IllegalActionException {
        _nextSamplingTime = getDirector().getCurrentTime();
    }

    /** Read exactly one input token from each input channel,
     *  put them into an ArrayToken and write to the space.
     *  Replace the old token in the space.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
	try {
	    String name = ((StringToken)entryName.getToken()).toString();
            if(Math.abs(getDirector().getCurrentTime()-_nextSamplingTime)
                    < ((CTDirector)getDirector()).getTimeResolution()){
                _nextSamplingTime += 
                    ((DoubleToken)samplingPeriod.getToken()).doubleValue();
                getDirector().fireAt(this, _nextSamplingTime);
                Token[] tokens = new Token[4];
                if(!((BooleanToken)malfunctioning.getToken()).booleanValue()) {
                    tokens[0] = 
                        new DoubleToken(getDirector().getCurrentTime());
                    tokens[1] = force.get(0);
                    tokens[2] = velocity.get(0);
                    tokens[3] = position.get(0);
                } else {
                    for(int i = 0; i < 4; i++) {
                        tokens[i] = new DoubleToken(Math.random());
                    }
                }
                ArrayToken array = new ArrayToken(tokens);
                TokenEntry template = new TokenEntry(name, null, null);
                _space.takeIfExists(template, null, 100);
                TokenEntry entry = new TokenEntry(name,
                        new Long(0), array);
                _space.write(entry, null, Lease.FOREVER);
            }
	} catch (RemoteException re) {
	    throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + re.getMessage());
	} catch (TransactionException te) {
	    throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + te.getMessage());
	} catch (InterruptedException ie) {
            throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + ie.getMessage());
        } catch (net.jini.core.entry.UnusableEntryException ue) {
            throw new IllegalActionException(this, "Unusable Entry " +
                    ue.getMessage());
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    // The Java space;
    private JavaSpace _space;

    // The next sampling time
    private double _nextSamplingTime;
}

