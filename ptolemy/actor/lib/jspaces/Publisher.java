/* An actor that sends entries to a Java Space.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;

import java.rmi.RemoteException;
import net.jini.space.JavaSpace;
import net.jini.core.transaction.TransactionException;
import net.jini.core.lease.Lease;

//////////////////////////////////////////////////////////////////////////
//// Publisher
/**
An actor that publishes instances of TokenEntry to a JavaSpace.  The
JavaSpace that the entries are published to is identified by the
<i>jspaceName</i> parameter. TokenEntries in the JavaSpace has a name,
a serial number, and a Ptolemy II token. This actor has a single input
port.  When the actor is fired, it consumes at most one token from the
input port and publishes an instance of token entry, which contains
the token, to the JavaSpace with the name specified by the
<i>entryName</i> parameter. In this class, the serial number of the
token entry is not used, and is always set to 0. Derived class may use
the serial number to keep track of the order of the published
tokens. If there is already an entry in the JavaSpace with the entry
name, the new token will override the existing one. In theory, an entry
only exists in the JavaSpace for a limited amount of time, denoted
as the <i>lease time</i>. If the lease time expires, the JavaSpace
can freely remote the entry from it. The lease time of an entry
published by this publisher is specified by the <i>leaseTime</i>
parameter in milliseconds. The default value LEASE_FOREVER will
keep the entry as long as the JavaSpace exists.

@see TokenEntry
@author Jie Liu, Yuhong Xiong
@version $Id$
@since Ptolemy II 1.0
*/

public class Publisher extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Publisher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        jspaceName = new Parameter(this, "jspaceName",
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);

        entryName = new Parameter(this, "entryName",
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);

        leaseTime = new Parameter(this, "leaseTime",
                new LongToken(Lease.FOREVER));
        leaseTime.setTypeEquals(BaseType.LONG);

        input.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The JavaSpace name. The type of the parameter is string.
     *  The default name is "JavaSpaces".
     *
     */
    public Parameter jspaceName;

    /** The name for the entries to be published. The type of the
     *  parameter is string. The default value is
     *  an empty string.
     */
    public Parameter entryName;

    /** The lease time for entries written into the space.
     *  This parameter must contain a LongToken. The default
     *  is Lease.FOREVER.
     */
    public Parameter leaseTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Find the JavaSpace according to the <i>jspaceName</i> parameter.
     *  If there are already entries in the Java Space with the
     *  specified entry name, then remove all the old entries.
     *  @exception IllegalActionException If the removal
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        String name = ((StringToken)jspaceName.getToken()).stringValue();
        _lookupThread = Thread.currentThread();
        _space = SpaceFinder.getSpace(name);
        _lookupThread = null;

        String entry = ((StringToken)entryName.getToken()).stringValue();
        TokenEntry tokenTemplate = new TokenEntry(entry, null, null);
        try {
            TokenEntry oldEntry;
            do {
                oldEntry = (TokenEntry)_space.takeIfExists(
                        tokenTemplate, null, 1000);
            } while (oldEntry != null);
        } catch (RemoteException ex) {
            throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + ex.getMessage());
        } catch (TransactionException ex) {
            throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + ex.getMessage());
        } catch (InterruptedException ex) {
            throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + ex.getMessage());
        } catch (net.jini.core.entry.UnusableEntryException ex) {
            throw new IllegalActionException(this, "Unusable Entry " +
                    ex.getMessage());
        }
        if (_debugging) {
            _debug(getName(), "Finished preinitialization.");
        }
    }


    /** Read one input token, if there is one, from the input
     *  and publish an entry into the space for the token read.
     *  Do nothing if there's no token in the input port.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    public void fire() throws IllegalActionException {
        try {
            if (input.hasToken(0)) {
                Token token = input.get(0);
                String name =
                    ((StringToken)entryName.getToken()).stringValue();
                long time = ((LongToken)leaseTime.getToken()).longValue();

                TokenEntry template = new TokenEntry(name, null, null);
                _space.takeIfExists(template, null, 500);
                TokenEntry entry = new TokenEntry(name,
                        new Long(0), token);
                _space.write(entry, null, Lease.FOREVER);
                if (_debugging) {
                    _debug(getName(), "Publisher writes " + token);
                }
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
    }

    /** Interrupt the lookup thread if it is still alive. The lookup
     *  thread is a thread created to find the JavaSpace.
     */
    public void stopFire() {
        if (_lookupThread != null) {
            _lookupThread.interrupt();
        }
        super.stopFire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // The JavaSpace.
    private JavaSpace _space;

    // The thread that finds jini.
    private Thread _lookupThread;

}

