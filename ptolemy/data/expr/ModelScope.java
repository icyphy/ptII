/* An interface used by the expression parser for identifier lookup.

 Copyright (c) 2001-2013 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.LazyComposite;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ModelScope

/**
 An abstract class that is useful for implementing expression language
 scopes for Ptolemy models.

 <p>{@link #getScopedVariable(Variable, NamedObj, String)} is the
 primary entry point, used by the Expression actor and other code
 to look up Variables by name.</p>

 @author Xiaojun Liu, Steve Neuendorffer, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 @see ptolemy.data.expr.PtParser
 */
public abstract class ModelScope implements ParserScope {
    /** Return a list of object names in scope for variables in the
     * given container.
     * @param container The container of this scope.
     */
    public static Set<String> getAllScopedObjectNames(NamedObj container) {
        Set<String> identifiers = new HashSet<String>();
        identifiers.add("this");
        while (container != null) {
            for (Object attribute : container.attributeList()) {
                identifiers.add(((Attribute) attribute).getName());
            }
            if (container instanceof Entity) {
                for (Object port : ((Entity) container).portList()) {
                    identifiers.add(((Port) port).getName());
                }
            }
            if (container instanceof CompositeEntity) {
                for (Object entity : ((CompositeEntity) container).entityList()) {
                    identifiers.add(((Entity) entity).getName());
                }

                for (Object relation : ((CompositeEntity) container)
                        .relationList()) {
                    identifiers.add(((Relation) relation).getName());
                }
            }
            container = container.getContainer();
        }

        return identifiers;
    }

    /** Return a list of variable names in scope for variables in the
     * given container.  Exclude the given variable from being
     * considered in scope.
     * @param exclude  The variable to exclude from the scope.
     * @param container The container of this scope.
     */
    public static Set<String> getAllScopedVariableNames(Variable exclude,
            NamedObj container) {
        List variableList = container.attributeList(Variable.class);
        variableList.remove(exclude);

        Set<String> nameSet = new HashSet<String>();

        for (Iterator variables = variableList.iterator(); variables.hasNext();) {
            Variable variable = (Variable) variables.next();
            nameSet.add(variable.getName());
        }

        // Get variables higher in scope.  Moving up the hierarchy
        // terminates when the container is null.
        NamedObj aboveContainer = container.getContainer();

        if (aboveContainer != null) {
            nameSet.addAll(getAllScopedVariableNames(exclude, aboveContainer));
        }

        // Get variables in scope extenders.  Moving down the scope
        // extenders terminates at hierarchy leaves.
        Iterator extenders = container.attributeList(ScopeExtender.class)
                .iterator();

        while (extenders.hasNext()) {
            ScopeExtender extender = (ScopeExtender) extenders.next();
            // It would be nice if ScopeExtender and NamedObj were common in
            // some way to avoid this cast.

            // This change was necessary for Java Code Generation.
            // We don't want to call getAllScopedVariableNames() here because
            // we will end up in an endless loop.

            // Test 3.1 in ptolemy/actor/parameters/test/ParameterSetModel.tcl
            // will go in an endless loop if the next two lines are on commente:
            //nameSet.addAll(getAllScopedVariableNames(exclude,
            //        (NamedObj) extender));

            // This is safer, but does it include everything?
            for (Iterator attributes = extender.attributeList().iterator(); attributes
                    .hasNext();) {
                Attribute attribute = (Attribute) attributes.next();
                if (attribute instanceof Variable) {
                    nameSet.add(attribute.getName());
                }
            }
        }
        return nameSet;
    }

    /** Get the attribute with the given name in the scope of the given
     *  container.  If the name contains the "::" scoping specifier,
     *  then an attribute more deeply in the hierarchy is searched
     *  for.  The scope of the object includes any container of the
     *  given object, and any attribute contained in a scope extending
     *  attribute inside any of those containers.
     *  @param exclude An attribute to exclude from the search.
     *  @param container The container to search upwards from.
     *  @param name The attribute name to search for.
     *  @return The attribute with the given name or null if the attribute
     *  does not exist.
     */
    public static Attribute getScopedAttribute(Attribute exclude,
            NamedObj container, String name) {
        // getScopedAttribute() is used by the SetVariable actor.
        String insideName = name.replaceAll("::", ".");

        while (container != null) {
            Attribute result = _searchAttributeIn(exclude, container,
                    insideName);

            if (result != null) {
                return result;
            } else {
                List attributes = container
                        .attributeList(ContainmentExtender.class);
                Iterator attrIterator = attributes.iterator();
                NamedObj extendedContainer = null;
                while (extendedContainer == null && attrIterator.hasNext()) {
                    ContainmentExtender extender = (ContainmentExtender) attrIterator
                            .next();
                    try {
                        extendedContainer = extender.getExtendedContainer();
                    } catch (IllegalActionException e) {
                        // Ignore the exception, and try the next extender.
                    }
                }

                if (extendedContainer == null) {
                    container = container.getContainer();
                } else {
                    container = extendedContainer;
                }
            }
        }

        return null;
    }

    /** Get the NamedObj with the given name in the scope of the given
     *  container.  If the name contains the "::" scoping specifier,
     *  then an attribute more deeply in the hierarchy is searched
     *  for. If the specified container is lazy (implements
     *  LazyComposite), then references to its contained entities
     *  or relations will not resolve, so such references are disallowed.
     *  @param container The container to search upwards from.
     *  @param name The object name to search for.
     *  @return The NamedObj with the given name or null if the NamedObj
     *  does not exist.
     */
    public static NamedObj getScopedObject(NamedObj container, String name) {
        if (name.equals("this")) {
            return container;
        }

        String[] parts = name.replaceAll("::", ".").split("\\.");
        NamedObj result = null;
        boolean lookup = true;
        for (String part : parts) {
            result = null;
            while (container != null) {
                //Attribute attribute = container.getAttribute(part);
                Attribute attribute = _searchAttributeIn(null, container, part);
                if (attribute != null) {
                    result = attribute;
                } else {
                    if (container instanceof Entity) {
                        Port port = ((Entity) container).getPort(part);
                        if (port != null) {
                            result = port;
                        } else if (container instanceof CompositeEntity) {
                            // NOTE: Lazy composites cannot have references to their
                            // contained entities or relations. This would defeat the
                            // lazy mechanism, forcing the actor to populate its
                            // contents.
                            if (!(container instanceof LazyComposite)) {
                                ComponentEntity entity = ((CompositeEntity) container)
                                        .getEntity(part);
                                if (entity != null) {
                                    result = entity;
                                } else {
                                    ComponentRelation relation = ((CompositeEntity) container)
                                            .getRelation(part);
                                    if (relation != null) {
                                        result = relation;
                                    }
                                }
                            }
                        }
                    }
                }
                if (lookup && result == null) {
                    List attributes = container
                            .attributeList(ContainmentExtender.class);
                    Iterator attrIterator = attributes.iterator();
                    NamedObj extendedContainer = null;
                    while (extendedContainer == null && attrIterator.hasNext()) {
                        ContainmentExtender extender = (ContainmentExtender) attrIterator
                                .next();
                        try {
                            extendedContainer = extender.getExtendedContainer();
                        } catch (IllegalActionException e) {
                            // Ignore the exception, and try the next extender.
                        }
                    }

                    if (extendedContainer == null) {
                        container = container.getContainer();
                    } else {
                        container = extendedContainer;
                    }
                } else {
                    break;
                }
            }
            if (result == null) {
                break;
            }
            container = result;
            lookup = false;
        }

        return result;
    }

    /** Get the variable with the given name in the scope of the given
     *  container.  If the name contains the "::" scoping specifier,
     *  then an attribute more deeply in the hierarchy is searched
     *  for.  The scope of the object includes any container of the
     *  given object, and any variable contained in a scope extending
     *  attribute inside any of those containers.
     *  @param exclude A variable to exclude from the search.
     *  @param container The container to search upwards from.
     *  @param name The variable name to search for.
     *  @return The variable with the given name or null if the variable
     *  does not exist.
     */
    public static Variable getScopedVariable(Variable exclude,
            NamedObj container, String name) {
        // This is the primary entry point for this class, used
        // by the Expression actor and others.
        String insideName = name.replaceAll("::", ".");

        while (container != null) {
            Variable result = _searchVariableIn(exclude, container, insideName);

            if (result != null) {
                return result;
            } else {
                List attributes = container
                        .attributeList(ContainmentExtender.class);
                Iterator attrIterator = attributes.iterator();
                NamedObj extendedContainer = null;
                while (extendedContainer == null && attrIterator.hasNext()) {
                    ContainmentExtender extender = (ContainmentExtender) attrIterator
                            .next();
                    try {
                        extendedContainer = extender.getExtendedContainer();
                    } catch (IllegalActionException e) {
                        // Ignore the exception, and try the next extender.
                    }
                }

                if (extendedContainer == null) {
                    container = container.getContainer();
                } else {
                    container = extendedContainer;
                }
            }
        }

        return null;
    }

    /** Check to see whether a preference of the specified name is
     *  defined in the specified context, and if it is, return its value.
     *  Note that if there is an error in the expression for the preference,
     *  then this method will return null and report the error to standard out.
     *  This is done because we assume the error will normally be caught
     *  before this method is called.
     *  @param context The context for the preference.
     *  @param preferenceName The name of the preference.
     *  @return The value of the preference, or null if it is not set.
     */
    public static Token preferenceValue(NamedObj context, String preferenceName) {
        Variable result = ModelScope.getScopedVariable(null, context,
                preferenceName);

        if (result != null) {
            try {
                return result.getToken();
            } catch (IllegalActionException ex) {
                System.out.println("Warning: Invalid preference: " + ex);
            }
        }

        // If no scoped variable is found, try for a defined constant.
        return Constants.get(preferenceName);
    }

    // Search in the container for an attribute with the given name.
    // Search recursively in any instance of ScopeExtender in the
    // container.
    private static Attribute _searchAttributeIn(Attribute exclude,
            NamedObj container, String name) {
        Attribute result = container.getAttribute(name);

        if (result != null && result != exclude) {
            return result;
        } else {
            Iterator extenders = container.attributeList(ScopeExtender.class)
                    .iterator();

            while (extenders.hasNext()) {
                ScopeExtender extender = (ScopeExtender) extenders.next();
                result = extender.getAttribute(name);

                if (result != null && result != exclude) {
                    return result;
                }
            }
        }

        return null;
    }

    // Search in the container for a variable with the given name.
    // Search recursively in any instance of ScopeExtender in the
    // container.
    private static Variable _searchVariableIn(Variable exclude,
            NamedObj container, String name) {
        Attribute result = container.getAttribute(name);

        if (result != null && result instanceof Variable && result != exclude) {
            return (Variable) result;
        } else {
            Iterator extenders = container.attributeList(ScopeExtender.class)
                    .iterator();

            while (extenders.hasNext()) {
                ScopeExtender extender = (ScopeExtender) extenders.next();
                result = extender.getAttribute(name);

                if (result != null && result instanceof Variable
                        && result != exclude) {
                    return (Variable) result;
                }
            }
        }

        return null;
    }
}
