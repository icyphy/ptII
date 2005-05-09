/* 

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.plugin.editor;

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

import ptolemy.backtrack.plugin.preferences.PreferenceConstants;

//////////////////////////////////////////////////////////////////////////
//// SemanticHighlightings
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SemanticHighlightings {

    static class MethodHighlighting extends SemanticHighlighting {

        public boolean consumes(SemanticToken token) {
            IBinding binding = token.getBinding();
            if (binding == null || binding.getKind() != IBinding.METHOD)
                return false;
            
            IMethodBinding methodBinding =
                ((IMethodBinding)binding).getMethodDeclaration();
            
            return _testMethod(methodBinding);
        }

        public String getPreferenceKey() {
            return PreferenceConstants.EDITOR_STATE_COLOR;
        }
        
        private boolean _testMethod(IMethodBinding binding) {
            Enumeration keys = METHODS.keys();
            while (keys.hasMoreElements()) {
                String typeName = (String)keys.nextElement();
                PtolemyMethod[] methods =
                    (PtolemyMethod[])METHODS.get(typeName);
                
                if (binding.getName().equals("fire")) {
                    int i = 0;
                }
                
                ITypeBinding type = binding.getDeclaringClass();
                boolean classFound = false;
                List workList = new LinkedList();
                Set handledSet = new HashSet();
                workList.add(type);
                while (workList.size() > 0) {
                    type = (ITypeBinding)workList.remove(0);
                    if (type.getQualifiedName().equals(typeName)) {
                        classFound = true;
                        break;
                    }
                    handledSet.add(type);
                    ITypeBinding nextType = type.getSuperclass();
                    if (nextType != null && !handledSet.contains(nextType))
                        workList.add(nextType);
                    ITypeBinding[] interfaces = type.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++)
                        if (!handledSet.contains(interfaces[i]))
                            workList.add(interfaces[i]);
                }
                if (!classFound)
                    return false;
                
                for (int i = 0; i < methods.length; i++)
                    if (methods[i]._test(binding))
                        return true;
            }
            return false;
        }
        
        private static class PtolemyMethod {
            
            PtolemyMethod(int modifier, String returnType, String name,
                    String[] argumentTypes) {
                _modifier = modifier;
                _returnType = returnType;
                _name = name;
                if (argumentTypes == null)
                    _argumentTypes = new String[0];
                else
                    _argumentTypes = argumentTypes;
            }
            
            int _getModifier() {
                return _modifier;
            }
            
            String _getReturnType() {
                return _returnType;
            }
            
            String _getName() {
                return _name;
            }
            
            String[] _getArgumentTypes() {
                return _argumentTypes;
            }
            
            boolean _test(IMethodBinding binding) {
                boolean preTest =
                    binding.getModifiers() == _modifier &&
                    binding.getReturnType().getName().equals(_returnType) &&
                    binding.getName().equals(_name);
                    
                if (preTest) {
                    ITypeBinding[] types = binding.getParameterTypes();
                    if (types.length == _argumentTypes.length) {
                        for (int i = 0; i < types.length; i++)
                            if (!types[i].getName().equals(_argumentTypes[i]))
                                return false;
                        return true;
                    }
                }
                
                return false;
            }
            
            private int _modifier;
            
            private String _returnType;
            
            private String _name;
            
            private String[] _argumentTypes;
        }
        
        public static final Hashtable METHODS = new Hashtable();
        
        static {
            PtolemyMethod[] executableMethods = new PtolemyMethod[] {
                    new PtolemyMethod(
                            Modifier.PUBLIC,
                            "void",
                            "fire",
                            null),
                    new PtolemyMethod(
                            Modifier.PUBLIC,
                            "boolean",
                            "postfire",
                            null),
                    new PtolemyMethod(
                            Modifier.PUBLIC,
                            "boolean",
                            "prefire",
                            null)
            };
            METHODS.put("ptolemy.actor.Executable", executableMethods);
        }
    }
    
    static class StateVariableHighlighting extends SemanticHighlighting {

        public boolean consumes(SemanticToken token) {
            IBinding binding = token.getBinding();
            if (binding == null || binding.getKind() != IBinding.VARIABLE)
                return false;
            IVariableBinding variableBinding = (IVariableBinding)binding;
            return variableBinding.isField() &&
                    Modifier.isPrivate(variableBinding.getModifiers()) &&
                    !Modifier.isStatic(variableBinding.getModifiers());
        }

        public String getPreferenceKey() {
            return PreferenceConstants.EDITOR_STATE_COLOR;
        }
    }
    
    public static SemanticHighlighting[] getSemanticHighlightings() {
        if (_semanticHighlightings == null)
            _semanticHighlightings = new SemanticHighlighting[] {
                new MethodHighlighting(),
                new StateVariableHighlighting()
        };
        return _semanticHighlightings;
    }
    
    public static String getColorPreferenceKey(SemanticHighlighting semanticHighlighting) {
        return semanticHighlighting.getPreferenceKey();
    }
    
    public static String getEnabledPreferenceKey(SemanticHighlighting semanticHighlighting) {
        return PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED;
    }
    
    public static boolean isEnabled(IPreferenceStore store) {
        return store.getBoolean(PreferenceConstants.EDITOR_HIGHLIGHTING_ENABLED);
    }
    
    public static boolean affectsEnablement(IPreferenceStore store, PropertyChangeEvent event) {
        String relevantKey= null;
        SemanticHighlighting[] highlightings= getSemanticHighlightings();
        for (int i= 0; i < highlightings.length; i++) {
            if (event.getProperty().equals(getEnabledPreferenceKey(highlightings[i]))) {
                relevantKey= event.getProperty();
                break;
            }
        }
        if (relevantKey == null)
            return false;

        for (int i= 0; i < highlightings.length; i++) {
            String key= getEnabledPreferenceKey(highlightings[i]);
            if (key.equals(relevantKey))
                continue;
            if (store.getBoolean(key))
                return false; // another is still enabled or was enabled before
        }
        
        // all others are disabled, so toggling relevantKey affects the enablement
        return true;
    }
    
    private static SemanticHighlighting[] _semanticHighlightings;
}
