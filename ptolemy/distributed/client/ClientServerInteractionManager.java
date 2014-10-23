/* Manager that handles and eases the discovery of services using JINI.

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
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
package ptolemy.distributed.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.NoSuchEntryException;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import ptolemy.distributed.common.DistributedActor;
import ptolemy.kernel.util.KernelException;

///////////////////////////////////////////////////////////////////
////ClientServerInteractionManager

/**
 Manager that handles and eases the discovery of services using JINI.
 Helps the client service discovery. It discovers the lookup service.
 A configuration file can be provided in the constructor to specify unicast
 locators, groups to join and the service to be located. After discovering
 a lookup service it queries for the given service and filters the dead
 services.

 @author Daniel L&#225;zaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see "Jini Documentation"
 */
public class ClientServerInteractionManager implements DiscoveryListener,
ServiceDiscoveryListener {
    /** Construct a ClientServerInteractionManager initializing it with a given
     *  VERBOSE option.
     *
     *  @param verbose If true, flag messages will be printed in the standard
     *  output.
     */
    public ClientServerInteractionManager(boolean verbose) {
        VERBOSE = verbose;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Required by DiscoveryListener interface.
     *  Called when one or more lookup service registrars has been discarded.
     *  The method should return quickly; e.g., it should not make remote
     *  calls.
     *
     *  @param evt The event that describes the discarded registrars
     */
    @Override
    public void discarded(DiscoveryEvent evt) {
        if (VERBOSE) {
            System.out.println("Registrars discarded: " + evt);
        }
    }

    /** Required by DiscoveryListener interface.
     *  Called when one or more lookup service registrars has been discovered.
     *  The method should return quickly; e.g., it should not make remote
     *  calls.
     *
     *  It prints the locator of the found Registrars.
     *
     *  @param evt The event that describes the discovered registrars
     */
    @Override
    public void discovered(DiscoveryEvent evt) {
        ServiceRegistrar[] serviceRegistrars = evt.getRegistrars();

        for (ServiceRegistrar serviceRegistrar : serviceRegistrars) {
            try {
                if (true) {
                    System.out.println("Found a service locator at: "
                            + serviceRegistrar.getLocator());
                }
            } catch (RemoteException e) {
                KernelException.stackTraceToString(e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  methods                   ////

    /** Returns the list of alive services.
     *
     *  @return LinkedList of registrars containing the alive services.
     */
    public LinkedList getServices() {
        return aliveServices;
    }

    /** Initializes the ClientServerInteractionManager. It loads the
     *  configuration file and creates a ServiceDiscoveryManager in
     *  order to help locate the service. It searches for the service
     *  specified in the configuration file and it filters the dead
     *  services.
     *
     *  @param configFileName String containing the name and/or path of the
     *  configuration file to be loaded.
     */
    public void init(String configFileName) {
        if (VERBOSE) {
            try {
                System.out.println("Starting ClientServerInteractionManager "
                        + "in: ");
                System.out.println("    "
                        + InetAddress.getLocalHost().getHostName() + " ("
                        + InetAddress.getLocalHost().getHostAddress() + ")");
            } catch (UnknownHostException e) {
                KernelException.stackTraceToString(e);
            }
        }

        getConfiguration(configFileName);

        ServiceDiscoveryManager clientMgr = null;

        System.setSecurityManager(new RMISecurityManager());

        try {
            LookupDiscoveryManager mgr = new LookupDiscoveryManager(groups,
                    unicastLocators, // unicast locators
                    this); // DiscoveryListener
            clientMgr = new ServiceDiscoveryManager(mgr,
                    new LeaseRenewalManager());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize "
                    + "ClientServerInteractionManager using \""
                    + configFileName + "\"", ex);
        }

        // This is the class of the service we are looking for...
        Class[] classes = new Class[] { DistributedActor.class };
        ServiceTemplate template = new ServiceTemplate(null, classes, null);

        try {
            cache = clientMgr.createLookupCache(template, null, // no filter
                    this); // no listener
        } catch (Exception e) {
            KernelException.stackTraceToString(e);
        }

        filterCacheServices();

        while (aliveServices.size() < requiredServices) {
            if (true) {
                System.out.println("Not enough services yet, found: "
                        + aliveServices.size() + " required: "
                        + requiredServices);
            }

            try {
                Thread.sleep(WAITFOR);
            } catch (java.lang.InterruptedException e) {
                // do nothing
            }

            filterCacheServices();
        }
    }

    /** Required by ServiceDiscoveryListener interface.
     *  When the cache receives from one of the managed lookup services, an
     *  event signaling the registration of a service of interest for the
     *  first time (or for the first time since the service has been
     *  discarded), the cache invokes the serviceAdded method on all instances
     *  of ServiceDiscoveryListener that are registered with the cache; doing
     *  so notifies the entity that a service of interest has been discovered.
     *  It just notifies of the event in the standard output.
     *
     *  @param evt a ServiceDiscoveryEvent object containing references to the
     *  service item corresponding to the event, including representations of
     *  the service's state both before and after the event.
     */
    @Override
    public void serviceAdded(ServiceDiscoveryEvent evt) {
        ServiceItem postItem = evt.getPostEventServiceItem();

        if (VERBOSE) {
            System.out.println("Service appeared: "
                    + postItem.service.getClass().toString() + " with ID: "
                    + postItem.serviceID.toString());
        }
    }

    /** Required by ServiceDiscoveryListener interface.
     *  When the cache receives, from a managed lookup service, an event
     *  signaling the unique modification of the attributes of a service of
     *  interest (across the attribute sets of all references to the service),
     *  the cache invokes the serviceChanged  method on all instances of
     *  ServiceDiscoveryListener  that are registered with the cache; doing so
     *  notifies the entity that the state of a service of interest has
     *  changed.
     *  It just notifies of the event in the standard output.
     *
     *  @param evt a ServiceDiscoveryEvent object containing references to the
     *  service item corresponding to the event, including representations of
     *  the service's state both before and after the event.
     */
    @Override
    public void serviceChanged(ServiceDiscoveryEvent evt) {
        ServiceItem postItem = evt.getPostEventServiceItem();

        if (VERBOSE) {
            System.out.println("Service changed: "
                    + postItem.service.getClass().toString() + " with ID: "
                    + postItem.serviceID.toString());
        }
    }

    /** Required by ServiceDiscoveryListener interface.
     *  When the cache receives, from a managed lookup service, an event
     *  signaling the removal of a service of interest from the last such
     *  lookup service with which it was registered, the cache invokes the
     *  serviceRemoved method on all instances of ServiceDiscoveryListener
     *  that are registered with the cache; doing so notifies the entity that
     *  a service of interest has been discarded.
     *  It just notifies of the event in the standard output.
     *
     *  @param evt a ServiceDiscoveryEvent object containing references to the
     *  service item corresponding to the event, including representations of
     *  the service's state both before and after the event.
     */
    @Override
    public void serviceRemoved(ServiceDiscoveryEvent evt) {
        ServiceItem preItem = evt.getPreEventServiceItem();

        if (VERBOSE) {
            System.out.println("Service removed: "
                    + preItem.service.getClass().toString() + " with ID: "
                    + preItem.serviceID.toString());
        }
    }

    /** Specify the number of required services.
     *
     * @param requiredServices The number of requiredServices.
     */
    public void setRequiredServices(int requiredServices) {
        this.requiredServices = requiredServices;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Construct the list of aliveServices.
     */
    private void filterCacheServices() {
        ServiceItem[] items = cache.lookup(null, Integer.MAX_VALUE);
        aliveServices.clear();

        for (ServiceItem item : items) {
            if (VERBOSE) {
                System.out.print("Service: " + item.serviceID);
            }

            try {
                if (VERBOSE) {
                    System.out.println(" is alive in: "
                            + ((DistributedActor) item.service).getAddress());
                }

                aliveServices.add(item);
            } catch (RemoteException e) {
                if (VERBOSE) {
                    System.out.println(" is dead.");
                }
            }
        }
    }

    /** Loads the configuration file.
     *  This file contains information about:
     *  codebase: location of the code.
     *  exporter: export to be used.
     *  groups: groups to join.
     *  unicast locators: Know service locators can be specified here.
     *  entries: Other info e.g. name and comments
     *  service: The service to be located.
     *
     *  @param configFileName A string containing the name and/or path of the
     *  configuration file.
     */
    private void getConfiguration(String configFileName) {
        if (VERBOSE) {
            System.out.println("Opening configuration file: " + configFileName);
            System.out.println("Entry: " + CLIENT);
        }

        Configuration configuration = null;

        // We have to get a configuration file or we can't continue
        try {
            configuration = ConfigurationProvider
                    .getInstance(new String[] { configFileName });
        } catch (ConfigurationException e) {
            KernelException.stackTraceToString(e);
        }

        // The config file must have an exporter, a service and a codebase
        try {
            if (VERBOSE) {
                System.out.print("Reading service: " + CLIENT);
            }

            service = (Remote) configuration.getEntry(CLIENT, "service",
                    Remote.class);

            if (VERBOSE) {
                System.out.println(service);
            }
        } catch (NoSuchEntryException e) {
            System.err.println("No config entry for " + e);
        } catch (Exception e) {
            KernelException.stackTraceToString(e);
        }

        // These fields can fallback to a default value
        try {
            if (VERBOSE) {
                System.out.println("Reading unicastLocators: ");
            }

            unicastLocators = (LookupLocator[]) configuration.getEntry(CLIENT,
                    "unicastLocators", LookupLocator[].class, null); // default

            if (VERBOSE) {
                for (LookupLocator unicastLocator : unicastLocators) {
                    System.out.println("    " + unicastLocator);
                }

                System.out.println("Reading entries: ");
            }

            entries = (Entry[]) configuration.getEntry(CLIENT, "entries",
                    Entry[].class, null); // default

            if (VERBOSE) {
                for (Entry entrie : entries) {
                    System.out.println("    " + entrie);
                }

                System.out.println("Reading groups: ");
            }

            groups = (String[]) configuration.getEntry(CLIENT, "groups",
                    String[].class, null); // default

            if (groups.length != 0) {
                if (VERBOSE) {
                    for (String group : groups) {
                        System.out.println("    " + group);
                    }
                }
            } else {
                groups = LookupDiscovery.ALL_GROUPS;

                if (VERBOSE) {
                    System.out.println("    No groups specified, "
                            + "using LookupDiscovery.ALL_GROUPS.");
                }
            }
        } catch (ConfigurationException e) {
            KernelException.stackTraceToString(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private   variables               ////

    /** Number of required services. */
    private int requiredServices = 0;

    /** Key to be loaded from the configuration file. */
    private static final String CLIENT = "ClientServerInteractionManager";

    /** Waiting time to receive responses when finding services.*/
    private static final long WAITFOR = 1000L;

    /** Service that we look for. */
    private Remote service;

    /**  Array of unicastLocators. */
    private LookupLocator[] unicastLocators;

    /** Information entries. */
    private Entry[] entries;

    /** Groups. */
    private String[] groups;

    /** Cache of registrars. */
    private LookupCache cache = null;

    /** List of alive services discovered and filtered. */
    private LinkedList aliveServices = new LinkedList();

    /** Shows debug messages when true. */
    private boolean VERBOSE = false;
}
