package ptolemy.actor.lib.jxta;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.exception.DiscardQueryException;
import net.jxta.exception.NoResponseException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ResendQueryException;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author liuxj, Yang
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 *
 * Use a timer task for peer discovery, and actor query.
 */
public class Peer extends TypedAtomicActor implements QueryHandler, DiscoveryListener {

    public Peer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        trigQuery = new TypedIOPort(this, "trigQuery", true, false);
        trigQuery.setTypeEquals(BaseType.GENERAL);
        queryResult = new TypedIOPort(this, "queryResult", false, true);
        queryResult.setTypeEquals(BaseType.STRING);
        //trigResult = new TypedIOPort(this, "trigResult", true, false);
        //trigResult.setTypeEquals(BaseType.GENERAL);
        /*configDir = new Parameter(this, "configDir",
          new StringToken(""));
          configDir.setTypeEquals(BaseType.STRING);
          actorList = new Parameter(this, "actorList",
          new StringToken(""));
          actorList.setTypeEquals(BaseType.STRING);
        */
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     */
    public TypedIOPort trigQuery;

    //public TypedIOPort trigResult;

    /** The output port with.
     */
    public TypedIOPort queryResult;

    public Parameter configDir;

    public Parameter actorList;





    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        PropertyConfigurator.configure(System.getProperties());
        //if (configDir.hasToken()) {
        //String Dir = ((StringToken)configDir.getToken()).stringValue();
        //} else {
        String Dir = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta";
        //}
        //if (actorList.hasToken()) {
        //String _actorListFileName = ((StringToken)actorList.getToken()).stringValue();
        //} else {
        String _actorListFileName = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta/actors.xml";
        //}
        _properties = new Properties(System.getProperties());
        try
            {
                InputStream configProperties = new FileInputStream(_CONFIG_FILE);
                _properties.load(configProperties);
                configProperties.close();
            }
        catch( IOException e)
            {
                System.out.println( "Warning: Can't find configuration propertiees file. ' " + e.getMessage() + "'");
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
            System.out.println("Error: property undefined - GroupAdvFileName.\n");
        }
        PeerGroupAdvertisement groupAdv = null;
        try {
            groupAdv = (PeerGroupAdvertisement)
                AdvertisementFactory.newAdvertisement(
                        XML_MIME_TYPE,
                        new FileInputStream(Dir + "/" + groupAdvFileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Error: cannot find group adv file.\n"
                    + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error: reading group adv file.\n"
                    + ex.getMessage());
        }
        System.out.println("peer groupAdv: " + groupAdvFileName);
        System.out.println("success before instantiate peer group");
        // instantiate the peer group for actor exchange
        try {
            _group = netPeerGroup.newGroup(groupAdv);
        } catch (PeerGroupException ex) {
            System.out.println("Error: cannot instantiate peer group.\n"
                    + ex.getMessage());
        }

        // join the peer group for actor exchange
        // no authentication is done here
        // modeled after JoinDemo from JXTA Examples
        StructuredDocument identityInfo = null;
        try {
            AuthenticationCredential authCred =
                new AuthenticationCredential(_group, null, identityInfo);
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
        _discoveryService = _group.getDiscoveryService();
        _discoveryService.addDiscoveryListener(this);

        _resolverService = _group.getResolverService();
        // register this as a query handler
        _resolverService.registerHandler(_ACTOR_QUERY_HANDLER_NAME, this);

        // construct the actor query message
        StringBuffer queryTextBuffer = new StringBuffer("<?xml version=\"1.0\"?>\n\n");
        queryTextBuffer = queryTextBuffer.append("<ActorQuery>\n");
        queryTextBuffer = queryTextBuffer.append(
                "What actors do you have?");
        queryTextBuffer = queryTextBuffer.append("\n</ActorQuery>\n");
        _actorQueryMessage = new ResolverQuery(_ACTOR_QUERY_HANDLER_NAME,
                null, null, queryTextBuffer.toString(), 0);
        _actorQueryMessage.setSrc(_group.getPeerID().toString());

        // construct the actor query response message
        if (_actorListFileName != null) {
            StringBuffer actorListText = new StringBuffer();
            try {
                BufferedReader fileReader =
                    new BufferedReader(new FileReader(_actorListFileName));
                String newline = System.getProperty("line.separator");
                while (true) {
                    String line = fileReader.readLine();
                    if (line == null) break;
                    actorListText = actorListText.append(line);
                    actorListText = actorListText.append(newline);
                }
                _actorQueryResponse = new ResolverResponse(
                        _ACTOR_QUERY_HANDLER_NAME,
                        null, 0, actorListText.toString());
            } catch (IOException ex) {
                System.out.println("Warning: error reading actor list file.\n"
                        + ex.getMessage());
            }
        }
    }


    public void fire() throws IllegalActionException {
        super.fire();
        try {
            if ( trigQuery.hasToken(0)) {
                Token token = trigQuery.get(0);

                System.out.println("Send peer discovery message...");
                // send a query message that is
                // - propagated within the group
                // - for peer discovery
                // - no attribute/value matching
                // - each response contains at most 5 peers
                _discoveryService.getRemoteAdvertisements(null,
                        DiscoveryService.PEER, null, null, 5);

                System.out.println("Send actor query message...");
                _actorQueryMessage.setQueryId(_actorQueryMessage.getQueryId() + 1);
                _resolverService.sendQuery(null, _actorQueryMessage);

            }

            if ( _inToken != null) {
                queryResult.send(0, _inToken);
                _inToken = null;
                System.out.println( "send data ");
            }
        }
        catch (Exception e)
            {
                System.out.println("Error : " + e);
            }
    }

    /** Record the most recent output count as the actual count.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        //_discoveryService.removeDiscoveryListener(this) ;
        //_resolverService.unregisterHandler(_ACTOR_QUERY_HANDLER_NAME);
        return super.postfire();
    }

    /**
     * @see net.jxta.resolver.QueryHandler#processQuery(ResolverQueryMsg)
     */
    public ResolverResponseMsg processQuery(ResolverQueryMsg query)
            throws
            NoResponseException,
            ResendQueryException,
            DiscardQueryException,
            IOException {
        System.out.println("Got query from " + query.getSrc()
                + " " + query.getQueryId());
        System.out.println("Query is:\n" + query.getQuery());
        if (_actorQueryResponse == null) {
            throw new DiscardQueryException();
        }
        System.out.println("Send query response...");
        _actorQueryResponse.setQueryId(query.getQueryId());
        return _actorQueryResponse;
    }

    /**
     * @see net.jxta.resolver.QueryHandler#processResponse(ResolverResponseMsg)
     */
    public void processResponse(ResolverResponseMsg response) {
        String responseString = response.getResponse();
        _inToken = new StringToken(responseString);
        System.out.println("Got response:\n" + response.getResponse());
    }

    /**
     * @see net.jxta.discovery.DiscoveryListener#discoveryEvent(DiscoveryEvent)
     */
    public void discoveryEvent(DiscoveryEvent event) {
        // copied from JXTA Examples - PeerDiscovery
        System.out.println("Process discovery event...");

        DiscoveryResponseMsg response = event.getResponse();

        // Get the responding peer's advertisement as a string
        String responderAdvString = response.getPeerAdv();

        try {
            // create a peer advertisement
            InputStream is = new ByteArrayInputStream(responderAdvString.getBytes());
            PeerAdvertisement responderAdv = (PeerAdvertisement)
                AdvertisementFactory.newAdvertisement(
                        XML_MIME_TYPE, is);
            System.out.println(" [  Got a Discovery Response ["
                    + response.getResponseCount() + " elements] from peer: "
                    + responderAdv.getName() + " ]");
        } catch (java.io.IOException e) {
            // bogus peer, skip this message alltogether.
            System.out.println("Warning: cannot parse remote peer's advertisement.\n"
                    + e.getMessage());
            return;
        }

        // now print out each discovered peer
        PeerAdvertisement newAdv = null;

        Enumeration responses = response.getResponses();
        while (responses.hasMoreElements()) {
            try {
                String response = (String)responses.nextElement();

                // create an advertisement object from each element
                newAdv = (PeerAdvertisement)
                    AdvertisementFactory.newAdvertisement(
                            XML_MIME_TYPE,
                            new ByteArrayInputStream(response.getBytes()));
                System.out.println(" Peer name = " + newAdv.getName());
            } catch (java.io.IOException e) {
                // got a bad response. continue to the next response
                System.out.println("Warning: cannot parse response element.\n"
                        + e.getMessage());
                continue;
            }

        } // end while
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Properties _properties;
    private PeerGroup _group;
    private DiscoveryService _discoveryService;
    private ResolverService _resolverService;
    private Authenticator _authenticator;
    private Credential _credential;
    private Timer _peerDiscoveryTimer;
    private Timer _actorQueryTimer;
    private ResolverQueryMsg _actorQueryMessage;
    private ResolverResponseMsg _actorQueryResponse;
    private String _CONFIG_FILE = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta/Peer.properties";
    private String _actorListFileName;
    private String _ACTOR_QUERY_HANDLER_NAME = "ActorQueryHandler";
    private MimeMediaType XML_MIME_TYPE = new MimeMediaType("text/xml");

    private ptolemy.data.Token _inToken = null;


}
