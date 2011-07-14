/*
 Class containing helper methods used by the ptserver and/or Homer.
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ServerUtility

/**
 * Class containing helper methods used by the ptserver and/or Homer.
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ServerUtility {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return the deep attribute list of the container.
     * @param container the container to process.
     * @return the deep attribute list of the container.
     */
    public static List<Attribute> deepAttributeList(NamedObj container) {
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        _deepAttributeList(container, attributeList);
        return attributeList;
    }

    /**
     * Find all remote attributes of the model and add them to the
     * remoteAttributeMap.
     * @param attributeList the attribute list to search
     * @param remoteAttributeMap the map where the attributes need to be added.
     */
    public static void findRemoteAttributes(List<Attribute> attributeList,
            HashMap<String, Settable> remoteAttributeMap) {
        for (Attribute attribute : attributeList) {
            if (ServerUtility.isRemoteAttribute(attribute)) {
                remoteAttributeMap.put(attribute.getFullName(),
                        (Settable) attribute);
            }
        }
    }

    /**
     * Return true if the attribute is marked as remote attribute, false otherwise.
     * @param attribute the child attribute of the attribute to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isRemoteAttribute(Attribute attribute) {
        if (attribute instanceof Settable) {
            Attribute isRemoteAttribute = attribute
                    .getAttribute(ProxyModelBuilder.REMOTE_OBJECT_TAG);
            if (isRemoteAttribute instanceof Parameter) {
                if (((Parameter) isRemoteAttribute).getExpression().equals(
                        ProxyModelBuilder.REMOTE_ATTRIBUTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return true if the attribute is marked as remote sink, false otherwise.
     * @param attribute the child attribute the source actor to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isTargetProxySink(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            if (parameter.getExpression().equals(
                    ProxyModelBuilder.PROXY_SINK_ATTRIBUTE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the attribute is marked as remote source, false otherwise.
     * @param attribute the child attribute of the source actor to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isTargetProxySource(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            if (parameter.getExpression().equals(
                    ProxyModelBuilder.PROXY_SOURCE_ATTRIBUTE)) {
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * Recursively find all attributes of the container.
     * @param container the container to check.
     * @param attributeList the attributeList that would contain attributes.
     */
    private static void _deepAttributeList(NamedObj container,
            List<Attribute> attributeList) {
        for (Object attributeObject : container.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;
            attributeList.add(attribute);
            _deepAttributeList(attribute, attributeList);
        }
    }
}
