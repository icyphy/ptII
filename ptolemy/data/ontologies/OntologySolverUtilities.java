/** A class representing shared utilities for ontology solvers in the ontologies package.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.kernel.util.Attribute;

//////////////////////////////////////////////////////////////////////////
//// SharedUtilities

/**
 A class representing shared utilities for ontology solvers in the ontologies package.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class OntologySolverUtilities {

    /**
     * Construct a new SharedUtilities object.
     */
    public OntologySolverUtilities() {
        // Since this is a shared (singleton) object per model,
        // it is important that all model-specific references
        // need to be reset when cloned. Otherwise, it will lead
        // to bugs that are hard to detect, and inconsistency
        // will occur.
        _id = _count++;
    }

    /**
     * Record the given error message.
     * @param error The error message to record.
     */
    public void addErrors(String error) {
        _errors.add(error);
    }

    /**
     * Return the map that maps root ast node (keys) to the corresponding
     * attribute (values).
     * @return The mappings for root ast nodes to attributes.
     */
    public Map<ASTPtRootNode, Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Return the list of error strings.
     * @return The list of error strings.
     */
    public List<String> getErrors() {
        Collections.sort(_errors);
        return _errors;
    }

    /**
     * Return the map that maps attributes (keys) to their root ast nodes
     * (values).
     * @return The mappings for attributes to their root ast nodes.
     */
    public Map<Attribute, ASTPtRootNode> getParseTrees() {
        return _parseTrees;
    }

    /**
     * Clear and return the previously recorded errors.
     * @return The list of previously recorded errors.
     */
    public List<String> removeErrors() {
        List<String> result = new ArrayList<String>(_errors);
        _errors.clear();
        return result;
    }

    /**
     * Clear the states of this shared object. The states include all previously
     * recorded information.
     */
    public void resetAll() {
        _parseTrees = new HashMap<Attribute, ASTPtRootNode>();
        _attributes = new HashMap<ASTPtRootNode, Attribute>();
        _errors = new ArrayList<String>();
    }

    /**
     * Return the representation for the SharedUtilities object.
     *
     * @return The string representation of the SharedUtilities object
     */
    @Override
    public String toString() {
        String result = "sharedUtilities#" + _id;
        return result;
    }

    /**
     * Record the association between the given ast node and the given
     * attribute.
     * @param node The given ast node.
     * @param attribute The given attribute.
     */
    protected void putAttribute(ASTPtRootNode node, Attribute attribute) {
        _attributes.put(node, attribute);
    }

    /** The map of attributes to the Ptolemy expression root nodes they contain. */
    private Map<Attribute, ASTPtRootNode> _parseTrees = new HashMap<Attribute, ASTPtRootNode>();

    /** The map of Ptolemy expression root nodes to the attributes that contain them. */
    private Map<ASTPtRootNode, Attribute> _attributes = new HashMap<ASTPtRootNode, Attribute>();

    /** The list of errors that have been collected from the ontology solvers. */
    private ArrayList<String> _errors = new ArrayList<String>();

    /** The number of ontology solvers using this a shared utilities object. */
    private static int _count = 0;

    /** The id number for the ontology solver using this utilities object. */
    private final int _id;
}
