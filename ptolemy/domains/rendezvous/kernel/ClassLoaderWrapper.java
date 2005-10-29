/* The wrapper of context class loader for threads.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.rendezvous.kernel;

import java.io.InputStream;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ClassLoaderWrapper
/**
 The class that wraps the ClassLoader class in Java, and adds a data field to
 it. The data field can store an arbitrary object. To store more than one
 object, Java sets and linked lists can be used. The data field can be accessed
 with a get method and a set method.
 
 An object of this class can be created with the thread's old context class
 loader as a parameter. The new class loader directly calls the old class
 loader's methods when its public methods inherited from the ClassLoader class
 are called. Users may use such a new class loader to replace the class loader
 of a thread (by calling the thread's setContextClassLoader(ClassLoader)
 method.
 
 RendezvousReceiver uses objects of this class to replace the class loader of
 the threads in the rendezvous domain. This replacement is usually transparent
 to the actor designers. However, actor designers should not assume the
 concrete class of the class loader associated with a thread. For example, the
 following piece of code may lead to an error:
 <pre>
    myThread.setContextClassLoader(new MyLoader());
    ... // Do some rendezvous operations
    if (myThread.getContextClassLoader() instanceof MyLoader) {
        ....
    }
 </pre>
 This is because the class loader of the thread may be replaced by a
 RendezvousReceiver.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
*/
class ClassLoaderWrapper extends ClassLoader {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the clearAssertionStatus method of the wrapped class loader.
     */
    public synchronized void clearAssertionStatus() {
        _loader.clearAssertionStatus();
    }

    /** Get the data stored in this object.
     * 
     *  @return The data stored in this object, or null if no data is stored.
     *  @see #setData(Object)
     */
    public Object getData() {
        return _data;
    }
    
    /** Get the wrapped class loader.
     * 
     *  @return The wrapped class loader.
     */
    public ClassLoader getLoader() {
        return _loader;
    }
    
    /** Call the getResource(String) method of the wrapped class loader, and
     *  return its result.
     *  
     *  @param name The name parameter to getResource(String).
     *  @return The result of getResource(String).
     */
    public URL getResource(String name) {
        return _loader.getResource(name);
    }

    /** Call the getResourceAsStream(String) method of the wrapped class
     *  loader, and return its result.
     *  
     *  @param name The name parameter to getResource(String).
     *  @return The result of getResourceAsStream(String).
     */
    public InputStream getResourceAsStream(String name) {
        return _loader.getResourceAsStream(name);
    }

    /** Call the loadClass(String) method of the wrapped class loader, and
     *  return its result.
     *  
     *  @param name The name parameter to loadClass(String).
     *  @return The result of loadClass(String).
     *  @exception ClassNotFoundException If loadClass(String) of the wrapped
     *   class loader throws an ClassNotFoundException.
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        return _loader.loadClass(name);
    }

    /** Call the setClassAssertionStatus(String, boolean) method of the wrapped
     *  class loader.
     *  
     *  @param className The className parameter to
     *   setClassAssertionStatus(String, boolean).
     *  @param enabled The enabled parameter to
     *   setClassAssertionStatus(String, boolean).
     */
    public synchronized void setClassAssertionStatus(String className,
            boolean enabled) {
        _loader.setClassAssertionStatus(className, enabled);
    }

    /** Store a data object in this object.
     * 
     *  @param data The data to be stored in this object, or null if no data is
     *   to be stored.
     *  @see #getData(Object)
     */
    public void setData(Object data) {
        _data = data;
    }
    
    /** Call the setDefaultAssertionStatus(boolean) method of the wrapped class
     *  loader.
     *  
     *  @param enabled The enabled parameter to
     *   setDefaultAssertionStatus(boolean).
     */
    public synchronized void setDefaultAssertionStatus(boolean enabled) {
        _loader.setDefaultAssertionStatus(enabled);
    }

    /** Call the setPackageAssertionStatus(String, boolean) method of the
     *  wrapped class loader.
     *  
     *  @param packageName The packageName parameter to
     *   setPackageAssertionStatus(String, boolean).
     *  @param enabled The enabled parameter to
     *   setPackageAssertionStatus(String, boolean).
     */
    public synchronized void setPackageAssertionStatus(String packageName,
            boolean enabled) {
        _loader.setPackageAssertionStatus(packageName, enabled);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected constructor                 ////

    /** Construct a class loader wrapper with a class loader wrapped in it.
     * 
     *  @param classLoader The class loader to be wrapped in it.
     */
    protected ClassLoaderWrapper(ClassLoader classLoader) {
        _loader = classLoader;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The data object stored in a class loader wrapper. */
    private Object _data;
    
    /** The wrapped class loader. */
    private ClassLoader _loader;
}