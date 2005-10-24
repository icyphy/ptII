/* An actor that receives packets forwarded by terminals including the group and
 * outputs the percentage of packets received per group.

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
package ptolemy.domains.wireless.demo.CooperativeTerminals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////DETransformer

/**
 /* An actor that receives packets forwarded by terminals including the group and
 * outputs the percentage of packets received per group.

 @author Daniel Lazaro Cuadrado
 @version
 @since
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating
 */
public class CooperationEvaluator extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CooperationEvaluator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        String[] labels = { "group", "packet" };
        Type[] types = { BaseType.INT, BaseType.INT };
        input.setTypeEquals(new RecordType(labels, types));

        String[] labels2 = { "1", "2", "3" };
        Type[] types2 = { BaseType.DOUBLE, BaseType.DOUBLE, BaseType.DOUBLE };
        output.setTypeEquals(new RecordType(labels2, types2));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                    ////

    /** Initialize the groupMap.
     *
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        groupMap = new HashMap();
        groupMap.put("1", new TreeSet());
        groupMap.put("2", new TreeSet());
        groupMap.put("3", new TreeSet());
    }

    /** Gets the new packet and updates the groupMap.
     *  Calculate the precentage of packets received by group and
     *  sent a token with the information.
     *
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        RecordToken token = null;

        if (input.hasToken(0)) {
            token = (RecordToken) input.get(0);
        }

        String group = ((IntToken) token.get("group")).toString();
        Integer packet = new Integer(((IntToken) token.get("packet"))
                .intValue());

        TreeSet auxSet;

        if ((groupMap.keySet()).contains(group)) {
            auxSet = (TreeSet) groupMap.get(group);
            auxSet.add(packet);
        }

        String[] labels = new String[3];
        Token[] values = new Token[3];

        Iterator groups = groupMap.keySet().iterator();

        int i = 0;

        while (groups.hasNext()) {
            group = (String) groups.next();
            labels[i] = group;
            auxSet = (TreeSet) groupMap.get(group);

            double auxSetSize = auxSet.size();
            double auxPacketNumber = packet.doubleValue();
            double aux = (auxSetSize / auxPacketNumber) * 100;
            DoubleToken tokenAux = new DoubleToken(aux);
            values[i] = tokenAux;
            i++;
        }

        output.send(0, new RecordToken(labels, values));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** Map that contains the packets received per group. */
    private Map groupMap = new HashMap();
}
