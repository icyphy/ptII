/* An actor that sends entries to a Java Space.

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

package ptolemy.actor.lib.jspaces;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;

import java.rmi.RemoteException;
import net.jini.space.JavaSpace;
import net.jini.core.transaction.TransactionException;
import net.jini.core.lease.Lease;

//////////////////////////////////////////////////////////////////////////
//// IndexedPublisher
/**
An actor that sends indexed entries to a Java Space.

@author Jie Liu, Yuhong Xiong
@version $Id$
*/

public class IndexedPublisher extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public IndexedPublisher(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

    	jspaceName = new Parameter(this, "jspaceName",
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);

        entryName = new Parameter(this, "entryName",
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);

        startingSerialNumber = new Parameter(this, "startingSerialNumber",
                new LongToken(0));
        startingSerialNumber.setTypeEquals(BaseType.LONG);

        leaseTime = new Parameter(this, "leaseTime",
                new LongToken(Lease.FOREVER));
        leaseTime.setTypeEquals(BaseType.LONG);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java Space name. The default name is "JavaSpaces" of
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the entries to be published. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    /** The starting serial number of the entries to be published.
     *  The entries will have increasing serial numbers.
     *  The default value is 0 of type LongToken.
     */
    public Parameter startingSerialNumber;

    /** The lease time for entries written into the space. The default
     *  is Least.FOREVER. This parameter must contain a LongToken.
     */
    public Parameter leaseTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        IndexedPublisher newobj = (IndexedPublisher)super.clone(ws);
        newobj.jspaceName = (Parameter)newobj.getAttribute("jspaceName");
        newobj.entryName = (Parameter)newobj.getAttribute("entryName");
        newobj.startingSerialNumber =
            (Parameter)newobj.getAttribute("startingSerialNumber");
        newobj.leaseTime = (Parameter)newobj.getAttribute("leaseTime");
        return newobj;
    }

    /** Find the JavaSpaces according to the jspaceName parameter.
     *  Write the minimum and maximum index token.
     *  At the beginning, the minimum index is larger than maximum by 1,
     *  and the maximum index is the current serial number.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
	String name = ((StringToken)jspaceName.getToken()).stringValue();
	_space = SpaceFinder.getSpace(name);
	_currentSerialNumber =
		((LongToken)startingSerialNumber.getToken()).longValue();
        String entryname = ((StringToken)entryName.getToken()).stringValue();
        try {
            IndexEntry minimum = new IndexEntry(
                    entryname, "minimum", new Long(_currentSerialNumber));
            _space.write(minimum, null, Lease.FOREVER);
            IndexEntry maximum = new IndexEntry(
                    entryname, "maximum", new Long(_currentSerialNumber-1));
            _space.write(maximum, null, Lease.FOREVER);
        } catch (RemoteException re) {
	    throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + re.getMessage());
	} catch (TransactionException te) {
	    throw new IllegalActionException(this, "Cannot write into " +
		"JavaSpace. " + te.getMessage());
	}
        System.out.println("Finish intialization.");
    }


    /** Read at most one input token from each channel of the input
     *  and write an entry into the space for each token read.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
	try {
	    String name = ((StringToken)entryName.getToken()).stringValue();
	    long time = ((LongToken)leaseTime.getToken()).longValue();
            IndexEntry maxtemp= new IndexEntry(
                    name, "maximum", null);
            // FIXME: This may introduce deadlock if maximum is lost
            // in the space.
            IndexEntry maximum =
                (IndexEntry)_space.take(maxtemp, null, Long.MAX_VALUE);

            long serialnumber = maximum.getPosition();
            System.out.println("IndexedPublisher get Max: " + serialnumber);
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);
                    TokenEntry entry = new TokenEntry(name,
                            new Long(++serialnumber), token);
                    _space.write(entry, null, Lease.FOREVER);
                    System.out.println("IndexedPublisher writes " + token);
                    maximum.increment();
                }
            }
            _space.write(maximum, null, Lease.FOREVER);
            _currentSerialNumber = serialnumber;
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private JavaSpace _space;
    private long _currentSerialNumber;

}

