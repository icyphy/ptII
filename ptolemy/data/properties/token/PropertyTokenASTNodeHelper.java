/*
 * A base class for the ASTPtRootNode's PropertyTokenHelper.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2008-2009 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

/**
 * A base class for the ASTPtRootNode's PropertyTokenHelper. By default, it has
 * exactly one taggable object, which is the node itself. It does not have any
 * sub-helpers.
 * 
 * @author Man-Kit Leung, Thomas Mandl
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class PropertyTokenASTNodeHelper extends PropertyTokenHelper {

    /**
     * 
     * @param solver
     * @param component
     */
    public PropertyTokenASTNodeHelper(PropertyTokenSolver solver,
            Object component) {
        super(solver, component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return a list of property-able NamedObj contained by the component. All
     * ports and parameters are considered property-able.
     * @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();
        list.add(getComponent());
        return list;
    }

    /**
     * Return the list of sub-helpers. In this base class, return an empty list.
     * @return The list of sub-helpers.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        return new ArrayList<PropertyHelper>();
    }

    /**
     * 
     * @param attributeList
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public void determineProperty(List<Attribute> attributeList)
            throws IllegalActionException, NameDuplicationException {
        Iterator attributeIterator = attributeList.iterator();
        while (attributeIterator.hasNext()) {
            Attribute attribute = (Attribute) attributeIterator.next();
            //FIXME: take care of all StringParameters and filter them
            //       should not be necessary once proprtyable attributes are filtered (related to kernel exceptions)
            if (attribute instanceof StringAttribute
                    && attribute.getName().equalsIgnoreCase("guardExpression")
                    || attribute instanceof Parameter
                    || attribute instanceof PortParameter) {

                setEquals(attribute, getSolver().getProperty(
                        getParseTree(attribute)));
            }
        }
    }

}
