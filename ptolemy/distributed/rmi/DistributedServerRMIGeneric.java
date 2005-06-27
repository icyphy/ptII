/* A distributed server to execute ptolemy actors in a distributed manner.

@Copyright (c) 2005 The Regents of Aalborg University.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

*/

package ptolemy.distributed.rmi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;

import ptolemy.kernel.util.KernelException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.NoSuchEntryException;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.Exporter;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;

//////////////////////////////////////////////////////////////////////////
//// DistributedServerRMIGeneric

/**
   A distributed server to execute ptolemy actors in a distributed manner.
   It uses Jini as discovery protocol. It performs the following tasks:
   <ul>
   <li>Prepares for discovery of a service locator.
   <li>Loading of various settings as Unicast locators and service class.
   <li>Discovers the service locator (unicast, multicast or both).
   <li>Creates and exports the service proxy (that allows for RMI calls).
   <li>Stays alive.
   </ul>
   @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (kapokasa)
   @Pt.AcceptedRating Red (cxh)
*/

public class DistributedServerRMIGeneric implements ServiceIDListener,
                                                    DiscoveryListener {

    /** Construct a DistributedServerRMIGeneric with a configuration file.
     *  It performs the following tasks:
     * <ul>
     * <li>Prepares for discovery of a service locator.
     * <li>Loading of various settings as Unicast locators and service class.
     * <li>Discovers the service locator (unicast, multicast or both).
     * <li>Creates and exports the service proxy (that allows for RMI calls).
     * <li>Stays alive.
     * </ul>
     *  - Prepares for discovery of a service locator:
     *     - Loading of various settings as Unicast locators and service class.
     *  - Discovers the service locator (unicast, multicast or both).
     *  - Creates and exports the service proxy (that allows for RMI calls).
     *
     *  @param configFileName The configuration file.
     */

    public DistributedServerRMIGeneric(String configFileName) {

        try {
            System.out.println("Starting server in: ");
            System.out.println("    " +
                               InetAddress.getLocalHost().getHostName() +
                               " (" +
                               InetAddress.getLocalHost().getHostAddress() +
                               ")");
        } catch (UnknownHostException e) {
            KernelException.stackTraceToString(e);
        }

        getConfiguration(configFileName);

        System.out.println("Setting codebase property " + codebase );
        System.setProperty("java.rmi.manager.codebase", codebase);

        System.out.println("Exporting service " + service );
        try {
            proxy = exporter.export(service);
        } catch(java.rmi.server.ExportException e) {
            KernelException.stackTraceToString(e);
        }

        // install suitable security manager
        System.setSecurityManager(new RMISecurityManager());

        tryRetrieveServiceId(serviceIdFile);

        JoinManager joinMgr = null;
        try {
            LookupDiscoveryManager mgr = new LookupDiscoveryManager(groups,
                    unicastLocators,  // unicast locators
                    this); // DiscoveryListener
            if (serviceID != null) {
                joinMgr = new JoinManager(proxy, // service proxy
                        entries,  // attr sets
                        serviceID,  // ServiceID
                        mgr,   // DiscoveryManager
                        new LeaseRenewalManager());
            } else {
                joinMgr = new JoinManager(proxy, // service proxy
                        entries,  // attr sets
                        this,  // ServiceIDListener
                        mgr,   // DiscoveryManager
                        new LeaseRenewalManager());
            }
        } catch(Exception e) {
            KernelException.stackTraceToString(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called when one or more lookup service registrars has been discarded.
     *  The method should return quickly; e.g., it should not make remote
     *  calls.
     *
     *  @param evt The event that describes the discovered registrars.
     */

    public void discarded(DiscoveryEvent evt) {

    }

    /** Called when one or more lookup service registrars has been discovered.
     *  The method should return quickly; e.g., it should not make remote
     *  calls.
     *
     *  @param evt The event that describes the discovered registrars.
     */

    public void discovered(DiscoveryEvent evt) {
        ServiceRegistrar[] registrars = evt.getRegistrars();

        for (int n = 0; n < registrars.length; n++) {
            ServiceRegistrar registrar = registrars[n];

            try {
                System.out.println("Found a service locator at " +
                                   registrar.getLocator().getHost());
            } catch(RemoteException e) {
                KernelException.stackTraceToString(e);
            }
        }
    }

    /** Create a new instance of this application, passing it the first
     *  command-line argument (configuration file). It stays alive.
     *  @param args The command-line arguments.
     */

    public static void main(String args[]) {
        if (args.length == 0) {
            System.err.println("No configuration specified");
            System.exit(1);
        }
        new DistributedServerRMIGeneric(args[0]);

        // stay around forever
        Object keepAlive = new Object();
        synchronized(keepAlive) {
            try {
                keepAlive.wait();
            } catch(InterruptedException e) {
                // do nothing
            }
        }
    }

    /** Required by the ServiceIDListener interface.
     *  Called when the JoinManager gets a valid ServiceID from a lookup
     *  service.
     *
     *  @param serviceID the service ID assigned by the lookup service.
     */

    public void serviceIDNotify(ServiceID serviceID) {
        // called as a ServiceIDListener
        // Should save the id to permanent storage
        System.out.println("Got service ID " + serviceID.toString());

        // try to save the service ID in a file
        /*
          if (serviceIdFile != null) {
          DataOutputStream dout = null;
          try {
          dout = new DataOutputStream(new FileOutputStream(serviceIdFile));
          serviceID.writeBytes(dout);
          dout.flush();
          dout.close();
          System.out.println("Service id saved in " +  serviceIdFile);
          } catch(Exception e) {
          // ignore
          }
          }
        */
    }

    /** Try to load the service ID from file. It isn't an error if we
     *  can't load it, because maybe this is the first time this service
     *  was run.
     *
     *  @param serviceIdFile name of the file where the serviceID is stored.
     */

    public void tryRetrieveServiceId(File serviceIdFile) {
        System.out.print("Trying to retrieve ServiceID from: " +
                         serviceIdFile.getAbsolutePath() + "... ");
        DataInputStream din = null;
        try {
            din = new DataInputStream(new FileInputStream(serviceIdFile));
            serviceID = new ServiceID(din);
            System.out.println("Found service ID in file " + serviceIdFile);
            din.close();
        } catch(Exception e) {
            System.out.println("Not Found.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Loads various settings from a configuration file.
     *  This file contains information about:
     *  - codebase: location of the code.
     *  - exporter: export to be used.
     *  - groups: groups to join.
     *  - unicast locators: Know service locators can be specified here.
     *  - entries: Other info e.g. name and comments
     *  - service: The service to be located.
     *
     *  @param configFileName The configuration file.
     */

    private void getConfiguration(String configFileName) {
        System.out.println("Opening configuration file: " + configFileName);
        Configuration configuration = null;

        // We have to get a configuration file or we can't continue
        try {
            configuration = ConfigurationProvider.getInstance(
                                new String[] {configFileName});
        } catch(ConfigurationException e) {
            System.err.println(e.toString());
            KernelException.stackTraceToString(e);
        }

        // The config file must have an exporter, a service and a codebase
        try {
            System.out.print("Reading exporter: ");
            exporter = (Exporter) configuration.getEntry(SERVER,
                    "exporter",
                    Exporter.class);
            System.out.println(exporter);
            System.out.print("Reading service: ");
            service = (Remote) configuration.getEntry(SERVER,
                    "service",
                    Remote.class);
            System.out.println(service);
            System.out.print("Reading codebase: ");
            codebase = (String) configuration.getEntry(SERVER,
                    "codebase",
                    String.class);
            System.out.println(codebase);
        } catch(NoSuchEntryException  e) {
            System.err.println("No config entry for " + e);
        } catch(Exception e) {
            System.err.println(e.toString());
            KernelException.stackTraceToString(e);
        }

        // These fields can fallback to a default value
        try {
            System.out.println("Reading unicastLocators: ");
            unicastLocators = (LookupLocator[])
                configuration.getEntry( SERVER,
                        "unicastLocators",
                        LookupLocator[].class,
                        null); // default
            for (int i = 0; i < unicastLocators.length; i++) {
                System.out.println("    " + unicastLocators[i]);
            }

            System.out.println("Reading entries: ");
            entries = (Entry[])
                configuration.getEntry( SERVER,
                        "entries",
                        Entry[].class,
                        null); // default
            for (int i = 0; i < entries.length; i++) {
                System.out.println("    " + entries[i]);
            }
            System.out.print("Reading serviceIdFile: ");
            serviceIdFile = (File)
                configuration.getEntry( SERVER,
                        "serviceIdFile",
                        File.class,
                        null); // default
            System.out.println(serviceIdFile);
            System.out.println("Reading groups: ");
            groups = (String[]) configuration.getEntry( SERVER,
                    "groups",
                    String[].class,
                    null); // default
            if (groups.length != 0) {
                for (int i = 0; i < groups.length; i++) {
                    System.out.println("    " + groups[i]);
                }
            } else {
                groups = LookupDiscovery.ALL_GROUPS;
                System.out.println("    No groups specified, using" +
                                   " LookupDiscovery.ALL_GROUPS.");
            }

        } catch(Exception e) {
            KernelException.stackTraceToString(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Entry of the configuration file we want ot load. */
    private static final String SERVER = "DistributedServerRMIGeneric";

    /** Proxy that allows for RMI calls to the service. */
    private Remote proxy;

    /** Service provided. */
    private Remote service;

    /** An abstraction for exporting a single remote object such that it
     *  can receive remote method invocations */
    private Exporter exporter;

    /** Groups loaded from the config file. */
    private String[] groups;

    /** Entries loaded from the config file. */
    private Entry[] entries;

    /** UnicastLocators loaded from the config file. */
    private LookupLocator[] unicastLocators;

    /**  File that stores the serviceID. */
    private File serviceIdFile;

    /** Codebase. */
    private String codebase;

    /** ID of the service. */
    private ServiceID serviceID;
}
