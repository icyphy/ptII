/* A customized class loader that accepts class paths at run-time.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.backtrack.ast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// LocalClassLoader
/**
   A customized class loader that accepts class paths at run-time. It mimics
   the behavior of loading a class from the within of another class, as the
   following example:
   <pre>    package pkg;
    class A {
        class B {
            class C {
            }
        }
        B.C field;
    }</pre>
   The loading of class <tt>B.C</tt> requires some name resolving, which is
   done in the <tt>javac</tt> compiler, so that the type of <tt>field</tt> is
   always known as <tt>pkg.A$B$C</tt> at run-time.
   <p>
   Class name resolution implemented in this class also takes into account
   importations. In the following example, <tt>Hashtable</tt> is resolved as
   <tt>java.util.Hashtable</tt>:
   <pre>    import java.util.*;
    class A {
        Hashtable table;
    }</pre>
   <p>
   This class loader only loads classes from the specified class paths and
   Java built-in classes. It does not use the <tt>CLASSPATH</tt> environment
   variable.
   <p>
   All the loaded classes are cached in a hash table so that they can be
   loaded efficiently the second time. This also means whenever two {@link
   Class} objects with the same name are returned, they also exist in the
   same memory location.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (tfeng)
*/
public class LocalClassLoader extends URLClassLoader {
    
    ///////////////////////////////////////////////////////////////////
    ////                        constructor                        ////

    /** Construct a class loader with no special class path. This class
     *  loader can only load Java built-in classes. More class paths may
     *  be added after this class loader is created.
     */
    public LocalClassLoader() {
        this(null);
    }
    
    /** Construct a class loader with a set of class paths specified as
     *  a string array. The class loader looks for classes in those
     *  paths in order.
     *  
     *  @param classPaths The array of class paths to be searched in
     *   order.
     */
    public LocalClassLoader(String[] classPaths) {
        super(_stringsToUrls(classPaths), null, null);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Add a class path to the list of class paths to be searched
     *  from.
     *  
     *  @param path The path to be added.
     */
    public void addClassPath(String path) {
        try {
            super.addURL(new File(path).toURL());
        } catch (MalformedURLException e) {
            throw new UnknownASTException();
        }
    }
    
    /** Import a class. This function mimics the <tt>import</tt>
     *  declaration used in a Java program. It adds a class to the
     *  import list to be considered at a later call to {@link
     *  #loadClass(String)}.
     *  <p>
     *  Class importations "<tt>import packageName.className</tt>"
     *  takes precedence over package importations
     *  "<tt>import packageName.*</tt>".
     *  
     *  @param classFullName The full name of the class to be
     *   imported. It muct contain the package name as its prefix.
     *   Nested classes are separated with ".", not "$" as used as
     *   name separator at run-time. It has exactly the same syntax
     *   as importing a class in the source code.
     */
    public void importClass(String classFullName) {
        int lastDotPos = classFullName.lastIndexOf(".");
        _importedClasses.add(
                new ClassImport(classFullName.substring(0, lastDotPos),
                        classFullName.substring(lastDotPos + 1)));
    }
    
    /** Import a package. This function mimics the <tt>import</tt>
     *  declaration used in a Java program. It adds a package to
     *  the import list to be considered at a later call to {@link
     *  #loadClass(String)}.
     *  <p>
     *  Class importations "<tt>import packageName.className</tt>"
     *  takes precedence over package importations
     *  "<tt>import packageName.*</tt>".
     *  
     *  @param packageName The full name of the package to be
     *   imported.
     */
    public void importPackage(String packageName) {
        _importedPackages.add(packageName);
    }
    
    /** Set the current class within which class names are to be
     *  solved with {@link #loadClass(String)}. This class can be
     *  an interface, a nested class, and an anonymous class
     *  (created at instantiation), as well as any normal class.
     *  <p>
     *  This function also sets the current package (see {@link
     *  #setCurrentPackage(String)}) to the package which the
     *  class belongs to.
     *  
     *  @param c The class to be set as the current class. When it
     *   is <tt>null</tt>, both the current class and the current
     *   package are set undefined.
     */
    public void setCurrentClass(Class c) {
        _currentClass = c;
        if (c == null)
            _packageName = null;
        else
            _packageName = c.getPackage().getName();
    }
    
    /** Set the current package. The current package is the package
     *  that the current class belongs to. {@link #loadClass(String)}
     *  searches for classes in the current package.
     *  
     *  @param packageName The package name to be set as the current
     *   package. When it is <tt>null</tt>, the current package is
     *   set undefined.
     */
    public void setCurrentPackage(String packageName) {
        _packageName = packageName;
    }
    
    /** Load a class from the specified class. This function is
     *  equivalent to first setting the current class with {@link
     *  #setCurrentClass(Class)} (which also sets the current package),
     *  then loading the class with the given name with {@link
     *  #loadClass(String)}, and then restoring the old current class
     *  before this function is called.
     *  
     *  @param name The name of the class to be loaded. It need not be
     *   as full name, as required by the superclasses {@link
     *   ClassLoader} and {@link URLClassLoader}. This function
     *   searches for the partial class name according to a set of
     *   rules.
     *  @param currentClass The new current class to be used.
     *  @return The class loaded with the given name in the scope.
     *  @throws ClassNotFoundException If the class cannot be found.
     *  @see #loadClass(String, boolean)
     */
    public Class loadClass(String name, Class currentClass) throws ClassNotFoundException {
        Class oldCurrentClass = _currentClass;
        setCurrentClass(currentClass);
        try {
            return loadClass(name);
        } finally {
            setCurrentClass(oldCurrentClass);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    
    /** Load a class with a given name. It is called in {@link
     *  #loadClass(String name)}, the function that user uses to
     *  dynamically resolve classes.
     *  <p>
     *  The class name may be partial. This function searches for the
     *  name with {@link #searchForClass(StringBuffer, boolean, Class)}.
     *  If <tt>resolve</tt> is true, it uses {@link #resolveClass(Class)}
     *  to further resolve the class. Resolving a class is a step in
     *  preparing the class for use. It is not always required. When
     *  Java virtual machine merely uses the signature of the loaded
     *  class to perform dynamic verification, {@link #loadClass(String)}
     *  calls this function with <tt>resolve</tt> equal to
     *  <tt>false</tt> when this is the case.
     *  
     *  @param name The partial name of the class to be loaded.
     *  @param resolve Whether {@link #resolveClass(Class)} should be
     *   called.
     *  @return The class loaded with the given name in the scope.
     *  @throws ClassNotFoundException If the class cannot be found.
     */
    // Not supporting anonymous classes like "Class$1".
    // Do not try to load nested classes in enclosing classes of
    // currentClass.
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (_loadedClasses.containsKey(name))
            return (Class)_loadedClasses.get(name);
        
        // Try to search for the name with a list of rules.
        Class c = searchForClass(new StringBuffer(name), resolve, _currentClass);
        if (resolve)
            resolveClass(c);
        return c;
    }
    
    /** Search for a class with a partial name in the current scope.
     *  It is called in {@link #loadClass(String, boolean)}.
     *  <p>
     *  It takes the following steps in this name resolving:
     *  <ol>
     *    <li>Check if the name represents an array. An array type
     *      is treated as a special class in Java. The name of an
     *      array is either like "<tt>char[]</tt>" (source
     *      representation) or like "<tt>[C</tt>" (JVM internal
     *      representation). If this is the case, it recursively
     *      calls itself with the type of the elements in the array
     *      (if not primitive), and then loads the array class and
     *      return.
     *    </li>
     *    <li>Check if the name corresponds to a nested class in
     *      the current class. In JVM representation, nested class
     *      names are separated with "$" from their containers. If
     *      so, the nested class is returned.
     *    </li>
     *    <li>Check if the name is already a full class name, e.g.,
     *      "<tt>java.lang.Class</tt>". If so, the class is loaded
     *      with {@link URLClassLoader#loadClass(String, boolean)}
     *      and returned. Nested classes (case 2) take precedence
     *      over this full name resolution.
     *    </li>
     *    <li>For every class explicitly imported with {@link
     *      #importClass(String)}, check if its simple class name
     *      (the last part) is the same as the first part of the
     *      class name to be searched for. If so, the imported
     *      class is loaded, and if necessary, its nested classes
     *      are searched.
     *    </li>
     *    <li>Check if the partial name is relative to the current
     *      package.
     *    </li>
     *    <li>Check if the partial name is relative to any package
     *      explicitly imported with {@link #importPackage(String)}.
     *      The implicitly imported package "<tt>java.lang</tt>" is
     *      also searched.
     *    </li>
     *  </ol>
     *  
     *  @param name The partial name of the class to be loaded.
     *  @param resolve Whether {@link #resolveClass(Class)} should be
     *   called.
     *  @param currentClass The current class.
     *  @return The class loaded with the given name in the scope.
     *  @throws ClassNotFoundException If the class cannot be found.
     *  @see #importClass(String)
     *  @see #importPackage(String)
     *  @see #loadClass(String, boolean)
     */
    // Not supporting anonymous classes like "Class$1".
    protected Class searchForClass(StringBuffer name, boolean resolve, Class currentClass)
            throws ClassNotFoundException {
        // Nested classes requires "$" separator between classes.
        StringBuffer dollarName = new StringBuffer(name.toString().replace('.', '$'));

        // Check if the name represents an array.
        // If c == null and no exception, it means "name" is not an array class.
        Class c = _checkArrayClass(name, resolve);
        
        // Check for nested classes in the current class.
        if (c == null)
            c = _checkNestedClass(dollarName, resolve, currentClass);
        
        if (c == null)
            c = _checkFullClassName(name, resolve);
        
        if (c == null) {
            Iterator importedClassesIter = _importedClasses.iterator();
            while (c == null && importedClassesIter.hasNext())
                c = _checkClassNameWithImportClass(dollarName, resolve,
                        (ClassImport)importedClassesIter.next());
        }

        if (c == null && _packageName != null)
            c = _checkClassNameWithImportPackage(dollarName, resolve, _packageName);
        
        if (c == null) {
            Iterator importedPackagesIter = _importedPackages.iterator();
            while (c == null && importedPackagesIter.hasNext())
                c = _checkClassNameWithImportPackage(dollarName, resolve,
                        (String)importedPackagesIter.next());
        }
        
        if (c == null)
            c = _checkClassNameWithImportPackage(dollarName, resolve, "java.lang");
        
        // If still no success, fall back to tries by replacing "."'s with "$"'s.
        if (c == null) {
            int lastDotPos = name.length();
            while (c == null && lastDotPos != -1) {
                lastDotPos = name.lastIndexOf(".", lastDotPos);
                if (lastDotPos >= 0) {
                    name.setCharAt(lastDotPos, '$');
                    try {
                        c = super.loadClass(name.toString(), resolve);
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }
        
        if (c == null)
            throw new ClassNotFoundException(name.toString());
        else
            return c;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////
    
    //----------------------------------------------------------------
    // All these private methods return null if no class is found.
    // They throw ClassNotFoundException only when an error is detected.
    
    /** Check if the given name refers to an array class. If so, load
     *  the array class.
     * 
     *  @param name The name of the class to be loaded.
     *  @param resolve Whether to resolve the class.
     *  @return The class loaded if not <tt>null</tt>. If the name does
     *   not refer to an array class, the return value is <tt>null</tt>.
     *  @throws ClassNotFoundException Thrown when the name refers to
     *   an array class but the class cannot be loaded.
     *  @see #searchForClass(StringBuffer, boolean, Class)
     */
    private Class _checkArrayClass(StringBuffer name, boolean resolve)
            throws ClassNotFoundException {
        boolean isPrimitiveArray;
        int nameStart = 0;
        int nameEnd = name.length() - 1;    // Inclusive.
        
        // Count dimensions.
        if (name.charAt(nameStart) == '[') {
            while (name.charAt(nameStart) == '[')
                nameStart++;
            if (name.charAt(nameStart) == 'L') {
                nameStart++;
                nameEnd--;  // Remove the ending ";".
                isPrimitiveArray = false;
            } else
                isPrimitiveArray = true;
        } else if (name.charAt(nameEnd) == ']') {
            while (name.charAt(nameEnd) == ']')
                nameEnd -= 2;   // Remove "[]".
            isPrimitiveArray = Type.isPrimitive(name.substring(nameStart, nameEnd + 1));
        } else
            return null;    // Not array.
        
        if (isPrimitiveArray) {
            String typeName = Type.toArrayType(name.toString());
            Class c = super.loadClass(typeName, resolve);
            _loadedClasses.put(typeName, c);
            return c;
        } else {
            // Try to load the object class.
            Class c = loadClass(name.substring(nameStart, nameEnd + 1));
            name.delete(nameStart, nameEnd + 1);
            name.insert(nameStart, c.getName());
            
            // FIXME: Class.forName uses the default ClassLoader, not the
            // bootstrap one.
            String typeName = Type.toArrayType(name.toString());
            c = Class.forName(typeName);
            _loadedClasses.put(typeName, c);
            return c;
        }
    }
    
    /** Check if a class is explicitly imported, or is any nested class
     *  in the explicitly imported class. The class looked for can be
     *  nested, when "$" appears in its name.
     * 
     *  @param dollarName The name of the class to be loaded. "$" is used
     *   as the separator between a nested class name and the name of its
     *   enclosing class.
     *  @param resolve Whether to resolve the class.
     *  @param importedClass The data structure that specifies an imported
     *   class.
     *  @return The class loaded if found. If no such class is found,
     *   <tt>null</tt> is returned.
     */
    private Class _checkClassNameWithImportClass(StringBuffer dollarName,
            boolean resolve, ClassImport importedClass) {
        int dotPos = dollarName.indexOf("$");
        if (dotPos == -1)
            dotPos = dollarName.length();
        if (importedClass.className.equals(dollarName.substring(0, dotPos))) {
            dollarName.insert(0, '.');
            dollarName.insert(0, importedClass.packageName);
            try {
                String className = dollarName.toString();
                Class c = super.loadClass(className, resolve);
                _loadedClasses.put(className, c);
                return c;
            } catch (ClassNotFoundException e) {
                return null;
            } finally {
                dollarName.delete(0, importedClass.packageName.length() + 1);
            }
        }
        else
            return null;
    }

    /** Check if a class with the given name can be found relative to the
     *  given package. The class can be nested, when "$" appears in its
     *  name.
     *  
     *  @param dollarName The name of the class to be loaded. "$" is used
     *   as the separator between a nested class name and the name of its
     *   enclosing class.
     *  @param resolve Whether to resolve the class.
     *  @param packageName The name of the package (may contain ".") in
     *   which the class is searched for.
     *  @return The class loaded if found. If no such class is found,
     *   <tt>null</tt> is returned.
     */
    private Class _checkClassNameWithImportPackage(StringBuffer dollarName,
            boolean resolve, String packageName) {
        dollarName.insert(0, '.');
        dollarName.insert(0, packageName);
        try {
            String className = dollarName.toString();
            Class c = super.loadClass(className, resolve);
            _loadedClasses.put(className, c);
            return c;
        } catch (ClassNotFoundException e) {
            return null;
        } finally {
            dollarName.delete(0, packageName.length() + 1);
        }
    }
    
    /** Check if the name is a full name corresponds to a class. A full name
     *  contains the package name as its prefix and the simple class name as
     *  its postfix, separated with a ".".
     * 
     *  @param name The name of the class to be loaded.
     *  @param resolve Whether to resolve the class.
     *  @return The class loaded if found. If no such class is found,
     *   <tt>null</tt> is returned.
     */
    private Class _checkFullClassName(StringBuffer name, boolean resolve) {
        String nameString = name.toString();
        try {
            Class c = super.loadClass(nameString, resolve);
            _loadedClasses.put(nameString, c);
            return c;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** Check if the name corresponds to a nested class in a specified current
     *  class. If so, load the nested class and return it. This function does
     *  not load anonymous classes with names like "Class$1". Users should not
     *  dynamically resolve anonymous classes, because they cannot be
     *  instantiated without knowing the enclosing method and the enclosing
     *  class.
     * 
     *  @param dollarName The name of the nested class to be loaded. "$" is
     *   used as the separator between a nested class name and the name of its
     *   enclosing class.
     *  @param resolve Whether to resolve the class.
     *  @param currentClass The current class from whose scope the nested class
     *   is searched for.
     *  @return The nested class loaded if found. If no such class is found or
     *   error occurs while loading, the return value is <tt>null</tt>.
     *  @see #searchForClass(StringBuffer, boolean, Class)
     */
    private Class _checkNestedClass(StringBuffer dollarName, boolean resolve, Class currentClass) {
        if (currentClass == null)
            return null;
        
        Set handledSet = new HashSet();
        List workList = new LinkedList();
        workList.add(currentClass);
        
        while (!workList.isEmpty()) {
            Class c = (Class)workList.remove(0);
            dollarName.insert(0, "$");
            dollarName.insert(0, c.getName());
            try {
                String className = dollarName.toString();
                Class classLoaded = super.loadClass(className, resolve);
                _loadedClasses.put(className, classLoaded);
                return classLoaded;
            } catch (ClassNotFoundException e) {
            } finally {
                dollarName.delete(0, c.getName().length() + 1);
            }
            
            handledSet.add(c);
            
            Class declaring = c.getDeclaringClass();
            if (declaring != null && !handledSet.contains(declaring))
                workList.add(declaring);
            
            Class superClass = c.getSuperclass();
            if (superClass != null && !handledSet.contains(superClass))
                workList.add(superClass);
            
            Class[] interfaces = c.getInterfaces();
            for (int i=0; i<interfaces.length; i++)
                if (!handledSet.contains(interfaces[i]))
                    workList.add(interfaces[i]);
        }
        return null;
    }

    //----------------------------------------------------------------
    
    /** Convert an array of strings to an array of {@link URL}s with
     *  {@link File#toURL()}.
     *  
     *  @param strings The array of strings.
     *  @return The array of urls.
     */
    private static URL[] _stringsToUrls(String[] strings) {
        if (strings == null)
            return new URL[0];
        
        URL[] urls = new URL[strings.length];
        for (int i=0; i<strings.length; i++)
            try {
                urls[i] = new File(strings[i]).toURL();
            } catch (MalformedURLException e) {
                throw new UnknownASTException();
            }
        return urls;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private nested class                   ////

    /** The data structure that represent class importation. It divides
     *  each imported class as a package name and a class name, and
     *  store them as fields visible in {@link LocalClassLoader}.
     *  
     *  @author Thomas Feng
     */
    private class ClassImport {
        
        /** Construct a class importation.
         * 
         *  @param packageName The package name, possibly with "." in it.
         *  @param className The simple class name, possibly with "." in
         *   it.
         */
        protected ClassImport(String packageName, String className) {
            this.packageName = packageName;
            this.className = className;
        }
        
        /** The package name of the importation.
         */
        protected String packageName;

        /** The simple class name of the importation.
         */
        protected String className;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private fields                     ////

    /** The list of imported packages. Each element is a {@link String},
     *  possibly with "." in it.
     */
    private List _importedPackages = new LinkedList();
    
    /** The list of imported classes. Each element is a {@link
     *  ClassImport}.
     */
    private List _importedClasses = new LinkedList();
    
    /** The current class in whose scope class names are to be resolved.
     */
    private Class _currentClass;
    
    /** The name of the current package, possibly with "." in it.
     */
    private String _packageName;
    
    /** The cache of all the loaded classes. Keys are class names while
     *  values are {@link Class} objects.
     */
    private static Hashtable _loadedClasses = new Hashtable();
}
