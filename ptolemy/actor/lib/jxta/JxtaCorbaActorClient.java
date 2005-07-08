package ptolemy.actor.lib.jxta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.exception.DiscardQueryException;
import net.jxta.exception.NoResponseException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ResendQueryException;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import org.apache.log4j.PropertyConfigurator;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.corba.util.CorbaActor;
import ptolemy.actor.corba.util.CorbaActorHelper;
import ptolemy.actor.corba.util.CorbaIllegalActionException;
import ptolemy.actor.corba.util.CorbaIllegalValueException;
import ptolemy.actor.corba.util.CorbaIndexOutofBoundException;
import ptolemy.actor.corba.util.CorbaUnknownPortException;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;

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
public class JxtaCorbaActorClient extends TypedAtomicActor implements
        QueryHandler {
    public JxtaCorbaActorClient(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInit");
        ORBInitProperties.setToken(new StringToken(""));
        remoteActorName = new Parameter(this, "RemoteActorName");
        remoteActorName.setToken(new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the remote actor. The type of the Parameter
     *  is StringToken.
     */
    public Parameter remoteActorName;

    public Parameter ORBInitProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void initialize() throws IllegalActionException {
        super.initialize();

        _remoteActorName = ((StringToken) remoteActorName.getToken())
                .stringValue();

        // String tokenize the parameter ORBInitProperties
        StringTokenizer st = new StringTokenizer(
                ((StringToken) ORBInitProperties.getToken()).stringValue());
        String[] args = new String[st.countTokens()];
        int i = 0;

        while (st.hasMoreTokens()) {
            args[i] = st.nextToken();
            _debug("ORB initial argument: " + args[i]);
            i++;
        }

        PropertyConfigurator.configure(System.getProperties());

        //String Dir = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta";
        //String _actorListFileName = "c:/Cygwin/home/ellen_zh/ptII/ptolemy/actor/lib/jxta/actors.xml";
        _properties = new Properties(System.getProperties());
        _configDir = System.getProperty(_CONFIG_DIR);

        if (_configDir == null) {
            _configDir = System.getProperty("user.dir");
            System.setProperty(_CONFIG_DIR, _configDir);
        }

        InputStream configProperties = null;
        String configFile = _configDir + "/" + _CONFIG_FILE;

        try {
            configProperties = new FileInputStream(configFile);
            _properties.load(configProperties);
        } catch (IOException e) {
            System.out
                    .println("Warning: Can't find configuration propertiees file. ' "
                            + e.getMessage() + "'");
        } finally {
            if (configProperties != null) {
                try {
                    configProperties.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on " + configFile + "'");
                    throwable.printStackTrace();
                }
            }
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
            System.out
                    .println("Error: property undefined - GroupAdvFileName.\n");
        }

        PeerGroupAdvertisement groupAdv = null;

        try {
            groupAdv = (PeerGroupAdvertisement) AdvertisementFactory
                    .newAdvertisement(XML_MIME_TYPE, new FileInputStream(
                            _configDir + "/" + groupAdvFileName));
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
            AuthenticationCredential authCred = new AuthenticationCredential(
                    _group, null, identityInfo);
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

        // construct the actor query message
        StringBuffer queryTextBuffer = new StringBuffer();
        queryTextBuffer = queryTextBuffer.append("<CorbaActorQuery>\n");
        queryTextBuffer = queryTextBuffer.append("<CorbaActor>"
                + _remoteActorName + "</CorbaActor>");
        queryTextBuffer = queryTextBuffer.append("\n</CorbaActorQuery>\n");
        _actorQueryMessage = new ResolverQuery(_ACTOR_QUERY_HANDLER_NAME, null,
                null, queryTextBuffer.toString(), 0);
        _actorQueryMessage.setSrc(_group.getPeerID().toString());
        _resolverService.sendQuery(null, _actorQueryMessage);

        synchronized (this) {
            System.out
                    .println("send out corba actor query message, and wait for response... ");

            try {
                wait();
                System.out.println("get response and wake up.");
            } catch (InterruptedException ex) {
            }
        }

        try {
            ORB orb = ORB.init(args, null);
            _debug(getName(), " ORB initialized");
            System.out.println("try to get an object from ior:" + _ior);

            org.omg.CORBA.Object obj = orb.string_to_object(_ior);
            _remoteActor = CorbaActorHelper.narrow(obj);
            System.out.println("narrow to a corbaActor object from the ior.");

            if (_remoteActor == null) {
                throw new IllegalActionException(this,
                        " can not find the remote actor.");
            }
        } catch (SystemException ex) {
            _debug(getName(), " CORBA set up failed " + ex.getMessage());
            ex.printStackTrace();
            throw new IllegalActionException(this, "CORBA set up faliar"
                    + ex.getMessage());
        }

        try {
            //check the corespondence of parameters and ports.
            Iterator attributes = attributeList().iterator();

            while (attributes.hasNext()) {
                Attribute att = (Attribute) attributes.next();

                if ((att != ORBInitProperties) && (att != remoteActorName)
                        && (att instanceof Parameter)) {
                    _debug(getName(), " check remote parameter: ", att
                            .getName());

                    if (!_remoteActor.hasParameter(att.getName())) {
                        throw new IllegalActionException(this, "Parameter: "
                                + att.getName()
                                + " not found on the remote side.");
                    }
                }
            }

            Iterator ports = portList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();
                _debug(getName(), " check remote port: ", p.getName());

                if (!_remoteActor.hasPort(p.getName(), p.isInput(), p
                        .isOutput(), p.isMultiport())) {
                    _debug("Port: " + p.getName()
                            + " not found on the remote side"
                            + " or has wrong type.");
                    throw new IllegalActionException(this, "Port: "
                            + p.getName() + " not found on the remote side"
                            + " or has wrong type.");
                }

                try {
                    _remoteActor
                            .setPortWidth(p.getName(), (short) p.getWidth());
                } catch (UserException ex) {
                    _debug("Port: " + p.getName() + " does not support width");
                    throw new IllegalActionException(this, "Port: "
                            + p.getName() + " does not support width "
                            + p.getWidth());
                }
            }
        } catch (SystemException ex) {
            _debug(getName(), " CORBA set up failed " + ex.getMessage());
            throw new IllegalActionException(this, "CORBA set up faliar"
                    + ex.getMessage());
        }

        _debug("Finished initializing " + getName());
    }

    /** Transfer the input tokens to the remote actor, fire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    public void fire() throws IllegalActionException {
        Director dir = getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }

        try {
            _transferInputs();

            try {
                _remoteActor.fire();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            }

            _transferOutputs();
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Comminication Failiar."
                    + ex.getMessage());
        }
    }

    /** Transfer the input tokens to the remote actor, postfire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        Director dir = getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }

        try {
            _transferInputs();

            try {
                /* result = */_remoteActor.postfire();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            }

            _transferOutputs();
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Comminication Failiar."
                    + ex.getMessage());
        }

        // FIXME: why don't we return the value of result?
        return true;
    }

    /** Transfer the input tokens to the remote actor, prefire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        Director dir = getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }

        boolean result;

        try {
            _transferInputs();

            try {
                result = _remoteActor.prefire();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            }

            _transferOutputs();
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Comminication Failiar."
                    + ex.getMessage());
        }

        return result;
    }

    /** wrapup the remote actor.
     */
    public void wrapup() throws IllegalActionException {
        try {
            _remoteActor.prefire();
        } catch (CorbaIllegalActionException ex) {
            throw new IllegalActionException(this,
                    "remote actor throws IllegalActionException"
                            + ex.getMessage());
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Comminication Failiar."
                    + ex.getMessage());
        }
    }

    /**
     * @see net.jxta.resolver.QueryHandler#processQuery(ResolverQueryMsg)
     */
    public ResolverResponseMsg processQuery(ResolverQueryMsg query)
            throws NoResponseException, ResendQueryException,
            DiscardQueryException, IOException {
        //do nothing.
        return null;
    }

    /**
     * @see net.jxta.resolver.QueryHandler#processResponse(ResolverResponseMsg)
     */
    public synchronized void processResponse(ResolverResponseMsg response) {
        _ior = response.getResponse();

        //_ior = r.substring(4) ;
        //System.out.println("the response is: " + _ior);
        /*StructuredTextDocument doc = (StructuredTextDocument)
         response.getDocument(XML_MIME_TYPE);
         Enumeration rps = doc.getChildren("Response");
         //TextElement rrr = (TextElement) rps.nextElement();
         //System.out.println("the name of the element is: " + rrr.getName());
         while (rps.hasMoreElements()) {
         TextElement rp = (TextElement) rps.nextElement();
         Enumeration cbrps = rp.getChildren();
         TextElement rrr = (TextElement) cbrps.nextElement();
         System.out.println("the name of the element is: " + rrr.getName());

         while (cbrps.hasMoreElements()) {
         TextElement cbrp = (TextElement) cbrps.nextElement();
         System.out.println("the name of the element is: " + cbrp.getName());
         Enumeration  acts = cbrp.getChildren("CorbaActor");
         while (acts.hasMoreElements()) {
         TextElement act = (TextElement) acts.nextElement();
         System.out.println("the name of the actor element is: " + act.getName());
         if (act.getTextValue() == _remoteActorName) {
         Enumeration  iors = cbrp.getChildren("CorbaActorIOR");
         while (iors.hasMoreElements()) {
         TextElement ior = (TextElement) iors.nextElement();
         _ior = ior.getTextValue();

         }
         }
         }
         }
         }
         */
        System.out.println("the IOR is: " + _ior);
        notifyAll();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Transfer the input tokens to the remote actor.
     *  This is done by converting the local token to String and
     *  transfer the string to the remote actor.
     *  FIXME: This operation is not type-save. Consider better
     *         ways to do it.
     *  @exception IllegalActionException If the port names do not
     *      match, or the tokens values are invalid.
     *  FIXME: How to set width?
     */
    protected void _transferInputs() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) (inputPorts.next());
            String inputName = port.getName();

            for (short i = 0; i < port.getWidth(); i++) {
                if (port.hasToken(i)) {
                    Token inputToken = port.get(0);

                    try {
                        _remoteActor.transferInput(inputName, i, inputToken
                                .toString());
                    } catch (SystemException ex) {
                        throw new InvalidStateException(this,
                                "Communication failiar." + ex.getMessage());
                    } catch (CorbaIllegalActionException ex1) {
                        throw new IllegalActionException(this,
                                "Illegal Action on remote actor. "
                                        + ex1.getMessage());
                    } catch (CorbaUnknownPortException ex2) {
                        throw new IllegalActionException(this,
                                "Wrong port name. " + ex2.getMessage());
                    } catch (CorbaIndexOutofBoundException ex3) {
                        throw new IllegalActionException(this, port,
                                "Channel index out of bound. "
                                        + ex3.getMessage());
                    } catch (CorbaIllegalValueException ex4) {
                        throw new IllegalActionException(port,
                                "contains illegal token value. "
                                        + ex4.getMessage());
                    }
                }
            }
        }
    }

    /** Transfer the output data from the remote actor.
     *  The remote output data is transferred in the form of
     *  String. This method converts the String back to
     *  Token and sends them.
     *  FIXME: This operation is not type-save. Consider better
     *         ways to do it.
     *  @exception IllegalActionException If the port names do not
     *      match, or the tokens values are invalid.
     *  FIXME: how to set width?
     */
    protected void _transferOutputs() throws IllegalActionException {
        Iterator outputPorts = outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) (outputPorts.next());
            String portName = port.getName();

            for (short i = 0; i < port.getWidth(); i++) {
                try {
                    if (_remoteActor.hasData(portName, i)) {
                        String returndata = _remoteActor.transferOutput(
                                portName, i);

                        //FIXME: type?
                        DoubleToken outputToken = new DoubleToken(returndata);
                        port.send(i, outputToken);
                    }
                } catch (SystemException ex) {
                    throw new InvalidStateException(this,
                            "Communication failiar." + ex.getMessage());
                } catch (CorbaIllegalActionException ex1) {
                    throw new IllegalActionException(this,
                            "Illegal Action on remote actor. "
                                    + ex1.getMessage());
                } catch (CorbaUnknownPortException ex2) {
                    throw new IllegalActionException(this, "Unknow port name"
                            + portName + ex2.getMessage());
                } catch (CorbaIndexOutofBoundException ex3) {
                    throw new IllegalActionException(this, port,
                            "channel index out of bound. " + ex3.getMessage());
                }
            }
        }
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

    private String _remoteActorName;

    private CorbaActor _remoteActor;
}
