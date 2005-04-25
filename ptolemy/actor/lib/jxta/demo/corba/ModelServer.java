/* A CORBA server for Ptolemy actors.

Copyright (c) 1998-2005 The Regents of the University of California.
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

*/
package ptolemy.actor.lib.jxta.demo.corba;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;

import net.jxta.exception.DiscardQueryException;
import net.jxta.exception.NoResponseException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ResendQueryException;

import net.jxta.impl.protocol.ResolverResponse;

import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;

import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;

import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


//import ptolemy.domains.ct.demo.Corba.NonlinearServant;
//////////////////////////////////////////////////////////////////////////
//// ModelServer

/**
   A model server register the servant to the name server and wait for calls.
   @author Jie Liu
   @version $Id$
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (cxh)
*/
public class ModelServer implements QueryHandler {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize ORB, create the servant object, and register
     *  it with the name server.
     */
    public static void main(String[] args) {
        try {
            // Initialize the ORB.
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

            //create a GoodDay obj
            NonlinearServant impl = new NonlinearServant();

            // register the servant to ORB
            orb.connect(impl);

            ModelServer server = new ModelServer();
            server._ior = orb.object_to_string(impl);
            System.out.println(server._ior);

            server.startJxta(server._ior);

            //wait for request
            java.lang.Object sync = new java.lang.Object();

            synchronized (sync) {
                sync.wait();
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void startJxta(String ior) {
        _configDir = System.getProperty(_CONFIG_DIR);

        if (_configDir == null) {
            _configDir = System.getProperty("user.dir");
            System.setProperty(_CONFIG_DIR, _configDir);
        }

        /*PropertyConfigurator.configure(System.getProperties());
          String Dir = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta";
          //String _actorListFileName = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta/actors.xml";
          */
        _properties = new Properties(System.getProperties());

        try {
            InputStream configProperties = new FileInputStream(_configDir + "/"
                    + _CONFIG_FILE);
            _properties.load(configProperties);
            configProperties.close();
        } catch (IOException e) {
            System.out.println(
                    "Warning: Can't find configuration propertiees file. ' "
                    + e.getMessage() + "'");
        }

        PeerGroup netPeerGroup = null;

        try {
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();
        } catch (PeerGroupException ex) {
            System.out.println("Error: cannot locate net peer group.\n"
                    + ex.getMessage());
        }

        // load the peer group adv for actor exchange
        String groupAdvFileName = _properties.getProperty("GroupAdvFileName");

        if (groupAdvFileName == null) {
            System.out.println(
                    "Error: property undefined - GroupAdvFileName.\n");
        }

        PeerGroupAdvertisement groupAdv = null;

        try {
            groupAdv = (PeerGroupAdvertisement) AdvertisementFactory
                .newAdvertisement(XML_MIME_TYPE,
                        new FileInputStream(_configDir + "/"
                                + groupAdvFileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Error: cannot find group adv file.\n"
                    + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error: reading group adv file.\n"
                    + ex.getMessage());
        }

        System.out.println("peer groupAdv: " + groupAdvFileName);
        System.out.println("success before instantiate peer group");
        System.out.println("created peer group adv from file " + groupAdv);

        // instantiate the peer group for actor exchange
        try {
            _group = netPeerGroup.newGroup(groupAdv);
        } catch (PeerGroupException ex) {
            System.out.println("Error: cannot instantiate peer group.\n"
                    + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("new peer group created...");

        // join the peer group for actor exchange
        // no authentication is done here
        // modeled after JoinDemo from JXTA Examples
        StructuredDocument identityInfo = null;

        try {
            AuthenticationCredential authCred = new AuthenticationCredential(_group,
                    null, identityInfo);
            MembershipService membershipService = _group.getMembershipService();
            _authenticator = membershipService.apply(authCred);

            if (_authenticator.isReadyForJoin()) {
                _credential = membershipService.join(_authenticator);
                System.out.println("Info: join group successful.");
                _credential.getDocument(XML_MIME_TYPE).sendToStream(System.out);
            } else {
                System.out.println("Error: unable to join group.");
            }
        } catch (Exception ex) {
            System.out.println("Error: failure in authentication.\n"
                    + ex.getMessage());
        }

        _resolverService = _group.getResolverService();

        // register this as a query handler
        _resolverService.registerHandler(_ACTOR_QUERY_HANDLER_NAME, this);

        // construct the response message
        StringBuffer queryTextBuffer = new StringBuffer();

        /*queryTextBuffer = queryTextBuffer.append("<CorbaActorResponse>\n");
          queryTextBuffer = queryTextBuffer.append("<CorbaActor>"
          + _remoteActorName + "</CorbaActor>\n");  */
        queryTextBuffer = queryTextBuffer.append(ior);

        //queryTextBuffer = queryTextBuffer.append("</CorbaActorResponse>\n");
        _actorQueryResponse = new ResolverResponse(_ACTOR_QUERY_HANDLER_NAME,
                null, 0, queryTextBuffer.toString());
    }

    /**
     * @see net.jxta.resolver.QueryHandler#processQuery(ResolverQueryMsg)
     */
    public ResolverResponseMsg processQuery(ResolverQueryMsg query)
            throws NoResponseException, ResendQueryException, DiscardQueryException,
            IOException {
        String qry = query.getQuery();

        if (qry.startsWith("<CorbaActorQuery>")) {
            if (_actorQueryResponse == null) {
                throw new DiscardQueryException();
            }

            System.out.println("Send query response...");
            _actorQueryResponse.setQueryId(query.getQueryId());
            return _actorQueryResponse;
        } else {
            return null;
        }
    }

    /**
     * @see net.jxta.resolver.QueryHandler#processResponse(ResolverResponseMsg)
     */
    public void processResponse(ResolverResponseMsg response) {
        String rp = response.getResponse();
        System.out.println("get response message.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private Properties _properties;
    private PeerGroup _group;
    private ResolverService _resolverService;
    private Authenticator _authenticator;
    private Credential _credential;
    private ResolverQueryMsg _actorQueryMessage;
    private ResolverResponseMsg _actorQueryResponse;
    private String _configDir;
    private static String _CONFIG_DIR = "pae.config.dir";
    private String _CONFIG_FILE = "Peer.properties";
    private String _actorListFileName;
    private String _ACTOR_QUERY_HANDLER_NAME = "ActorQueryHandler";
    private MimeMediaType XML_MIME_TYPE = new MimeMediaType("text/xml");
    private String _ior = null;
    private String _remoteActorName = "Nonlinear";
}
