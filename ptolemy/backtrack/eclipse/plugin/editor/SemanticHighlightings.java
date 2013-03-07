/* Definition of a set of semantic highlightings.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.editor;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

import ptolemy.backtrack.eclipse.plugin.preferences.PreferenceConstants;

///////////////////////////////////////////////////////////////////
//// SemanticHighlightings

/**
 Definition of a set of semantic highlightings. Each semantic highlighting is
 a subclass of {@link SemanticHighlighting} and handles a special type of
 semantic elements.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SemanticHighlightings {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Test whether the event in store affects the enablement of semantic
     *  highlighting.
     *
     *  @param store The preference store where the event was observed.
     *  @param event The property change under examination.
     *  @return true if the event in store affects the enablement of semantic
     *   highlighting.
     */
    public static boolean affectsEnablement(IPreferenceStore store,
            PropertyChangeEvent event) {
        String relevantKey = null;
        SemanticHighlighting[] highlightings = getSemanticHighlightings();

        for (int i = 0; i < highlightings.length; i++) {
            if (event.getProperty().equals(
                    highlightings[i].getEnabledPreferenceKey())) {
                relevantKey = event.getProperty();
                break;
            }
        }

        if (relevantKey == null) {
            return false;
        }

        for (int i = 0; i < highlightings.length; i++) {
            String key = highlightings[i].getEnabledPreferenceKey();

            if (key.equals(relevantKey)) {
                continue;
            }

            if (store.getBoolean(key)) {
                return false; // another is still enabled or was enabled before
            }
        }

        // All others are disabled, so toggling relevantKey affects the
        // enablement.
        return true;
    }

    /** Return the complete list of semantic highlightings to be used in the
     *  Ptolemy editor.
     *
     *  @return The array of semantic highlightings.
     */
    public static SemanticHighlighting[] getSemanticHighlightings() {
        if (_semanticHighlightings == null) {
            _semanticHighlightings = new SemanticHighlighting[] {
                    new MethodHighlighting(), new StateVariableHighlighting() };
        }

        return _semanticHighlightings;
    }

    /** Test whether semantic highlightings are enabled in the preference store.
     *
     *  @param store The preference store.
     *  @return true if semantic highlightings are enabled.
     */
    public static boolean isEnabled(IPreferenceStore store) {
        return store
                .getBoolean(PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   protected inner classes                 ////

    ///////////////////////////////////////////////////////////////////
    //// MethodHighlighting
    /**
     Semantic highlighting for methods.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    protected static class MethodHighlighting extends SemanticHighlighting {

        /** Test whether a semantic token can be consumed.
         *
         *  @param token The token to be tested.
         *  @return true if the token can be consumed.
         */
        public boolean consumes(SemanticToken token) {
            IBinding binding = token.getBinding();

            if ((binding == null) || (binding.getKind() != IBinding.METHOD)) {
                return false;
            }

            IMethodBinding methodBinding = (IMethodBinding) binding;

            return _testMethod(methodBinding);
        }

        /** Get the key of the bold face preference.
         *
         *  @return The key for the bold face preference.
         */
        public String getBoldPreferenceKey() {
            return PreferenceConstants.EDITOR_ACTOR_METHOD_BOLD;
        }

        /** Get the key of the color preference.
         *
         *  @return The key of the color preference.
         */
        public String getColorPreferenceKey() {
            return PreferenceConstants.EDITOR_ACTOR_METHOD_COLOR;
        }

        /** Get the key of the enabled preference.
         *
         *  @return The key of the enabled preference.
         */
        public String getEnabledPreferenceKey() {
            return PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED;
        }

        /** Get the key of the italic font preference.
         *
         *  @return The key of the italic font preference.
         */
        public String getItalicPreferenceKey() {
            return PreferenceConstants.EDITOR_ACTOR_METHOD_ITALIC;
        }

        /** The special methods to be highlighted. Keys are names of the root
         *  classes that define the special methods; values are the special
         *  methods defined as {@link PtolemyMethod} objects.
         */
        protected static final Hashtable<String, PtolemyMethod[]> _METHODS = new Hashtable<String, PtolemyMethod[]>();

        //////////////////////////////////////////////////////////////////////////
        //// PtolemyMethod
        /**
         Wrapper for Ptolemy methods to be highlighted.

         @author Thomas Feng
         @version $Id$
         @since Ptolemy II 5.1
         @Pt.ProposedRating Red (tfeng)
         @Pt.AcceptedRating Red (tfeng)
         */
        protected static class PtolemyMethod {

            /** Get the names of the method's argument types.
             *
             *  @return The names of the method's argument types.
             */
            protected String[] _getArgumentTypes() {
                return _argumentTypes;
            }

            /** Get the modifier of the method (a constant defined in
             *  <tt>org.eclipse.jdt.core.dom.Modifier</tt>).
             *
             *  @return The modifier.
             */
            protected int _getModifier() {
                return _modifier;
            }

            /** Get the name of the method.
             *
             *  @return The name of the method.
             */
            protected String _getName() {
                return _name;
            }

            /** Get the name of the method's return type.
             *
             *  @return The name of the method's return type.
             */
            protected String _getReturnType() {
                return _returnType;
            }

            /** Test whether the method binding matches this method description.
             *
             *  @param binding The method binding.
             *  @return true if the method binding matches this method
             *   description.
             */
            protected boolean _testMethod(IMethodBinding binding) {
                boolean preTest = (binding.getModifiers() == _modifier)
                        && binding.getReturnType().getName()
                                .equals(_returnType)
                        && binding.getName().equals(_name);

                if (preTest) {
                    ITypeBinding[] types = binding.getParameterTypes();

                    if (types.length == _argumentTypes.length) {
                        for (int i = 0; i < types.length; i++) {
                            if (!types[i].getName().equals(_argumentTypes[i])) {
                                return false;
                            }
                        }

                        return true;
                    }
                }

                return false;
            }

            /** Construct a Ptolemy special method description.
             *
             *  @param modifier Modifier of the method (a constant defined in
             *   <tt>org.eclipse.jdt.core.dom.Modifier</tt>).
             *  @param returnType Name of the method's return type.
             *  @param name The method name.
             *  @param argumentTypes Names of the method's argument types.
             */
            PtolemyMethod(int modifier, String returnType, String name,
                    String[] argumentTypes) {
                _modifier = modifier;
                _returnType = returnType;
                _name = name;

                if (argumentTypes == null) {
                    _argumentTypes = new String[0];
                } else {
                    _argumentTypes = argumentTypes;
                }
            }

            /** The names of the method's argument types.
             */
            private String[] _argumentTypes;

            /** The modifier of the method.
             */
            private int _modifier;

            /** The name of the method.
             */
            private String _name;

            /** The name of the method's return type.
             */
            private String _returnType;
        }

        /** Test whether the method binding matches any of the pre-defined
         *  special methods.
         *
         *  @param binding The method binding.
         *  @return true if the method binding matches any of the pre-defined
         *   special methods.
         */
        private boolean _testMethod(IMethodBinding binding) {
            Enumeration<String> keys = _METHODS.keys();

            while (keys.hasMoreElements()) {
                String typeName = keys.nextElement();
                PtolemyMethod[] methods = _METHODS.get(typeName);

                ITypeBinding type = binding.getDeclaringClass();
                boolean classFound = false;
                List<ITypeBinding> workList = new LinkedList<ITypeBinding>();
                Set<ITypeBinding> handledSet = new HashSet<ITypeBinding>();
                workList.add(type);

                while (workList.size() > 0) {
                    type = workList.remove(0);

                    if (type.getQualifiedName().equals(typeName)) {
                        classFound = true;
                        break;
                    }

                    handledSet.add(type);

                    ITypeBinding nextType = type.getSuperclass();

                    if ((nextType != null) && !handledSet.contains(nextType)) {
                        workList.add(nextType);
                    }

                    ITypeBinding[] interfaces = type.getInterfaces();

                    for (int i = 0; i < interfaces.length; i++) {
                        if (!handledSet.contains(interfaces[i])) {
                            workList.add(interfaces[i]);
                        }
                    }
                }

                if (!classFound) {
                    return false;
                }

                for (int i = 0; i < methods.length; i++) {
                    if (methods[i]._testMethod(binding)) {
                        return true;
                    }
                }
            }

            return false;
        }

        // Initialize the array of special methods.
        static {
            PtolemyMethod[] executableMethods = new PtolemyMethod[] {
                    new PtolemyMethod(Modifier.PUBLIC, "void", "fire", null),
                    new PtolemyMethod(Modifier.PUBLIC, "boolean", "postfire",
                            null),
                    new PtolemyMethod(Modifier.PUBLIC, "boolean", "prefire",
                            null) };
            _METHODS.put("ptolemy.actor.Executable", executableMethods);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// StateVariableHighlighting
    /**
     Semantic highlighting for state variables.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    protected static class StateVariableHighlighting extends
            SemanticHighlighting {

        /** Test whether a semantic token can be consumed.
         *
         *  @param token The token to be tested.
         *  @return true if the token can be consumed.
         */
        public boolean consumes(SemanticToken token) {
            IBinding binding = token.getBinding();

            if ((binding == null) || (binding.getKind() != IBinding.VARIABLE)) {
                return false;
            }

            IVariableBinding variableBinding = (IVariableBinding) binding;
            return variableBinding.isField()
                    && Modifier.isPrivate(variableBinding.getModifiers())
                    && !Modifier.isStatic(variableBinding.getModifiers());
        }

        /** Get the key of the bold face preference.
         *
         *  @return The key for the bold face preference.
         */
        public String getBoldPreferenceKey() {
            return PreferenceConstants.EDITOR_STATE_BOLD;
        }

        /** Get the key of the color preference.
         *
         *  @return The key of the color preference.
         */
        public String getColorPreferenceKey() {
            return PreferenceConstants.EDITOR_STATE_COLOR;
        }

        /** Get the key of the enabled preference.
         *
         *  @return The key of the enabled preference.
         */
        public String getEnabledPreferenceKey() {
            return PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED;
        }

        /** Get the key of the italic font preference.
         *
         *  @return The key of the italic font preference.
         */
        public String getItalicPreferenceKey() {
            return PreferenceConstants.EDITOR_STATE_ITALIC;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The array of semantic highlightings.
     */
    private static SemanticHighlighting[] _semanticHighlightings;
}
