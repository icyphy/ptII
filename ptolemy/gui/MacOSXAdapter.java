/* Support for Mac OS X Specific key bindings.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.gui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Support for Mac OS X specific features such as key bindings.
 *
 * There is no public constructor.  Instead, call stati set*Method() methods.
 *
 *  @author Christopher Brooks, Based on OSXAdapter.java, downloaded from: <a href="http://developer.apple.com/library/mac/#samplecode/OSXAdapter/Listings/src_OSXAdapter_java.html">http://developer.apple.com/library/mac/#samplecode/OSXAdapter/Listings/src_OSXAdapter_java.html</a> on July 26, 2011.
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class MacOSXAdapter implements InvocationHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Invoke a method.  This method is part of the java.lang.reflect.InvocationHandler
     *  interface.
     *  @param proxy The object upon which the method is invoked.
     *  @param method The method to be invoked.
     *  @param args The arguments to the method, which must be non-null.
     *  @exception Throwable If the method does not exist or is not accessible or if
     *  thrown while invoking the method.
     *  @return the result, which in this case is always null because
     *  the com.apple.eawt.ApplicationListener methods are all void.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (_topMethod == null || !_proxySignature.equals(method.getName())
                || args.length != 1) {
            return null;
        }
        boolean handled = true;
        Object result = _topMethod.invoke(_top, (Object[]) null);
        if (result != null) {
            handled = Boolean.valueOf(result.toString()).booleanValue();
        }

        try {
            Method setHandledMethod = args[0].getClass().getDeclaredMethod(
                    "setHandled", new Class[] { boolean.class });
            setHandledMethod.invoke(args[0],
                    new Object[] { Boolean.valueOf(handled) });
        } catch (Exception ex) {
            _top.report("The Application event \"" + args[0]
                    + "\" was not handled.", ex);
        }
        return null;
    }

    /** Set the about menu handler for a Top window.
     *  Under Mac OS X, the About menu may be selected from the application
     *  menu, which is in the upper left, next to the apple.
     *  @param top the Top level window to perform the operation.
     *  @param aboutMethod The method to invoke in Top, typically
     *  {@link ptolemy.gui.Top#about()}.
     */
    public static void setAboutMethod(Top top, Method aboutMethod) {
        _setHandler(top, new MacOSXAdapter("handleAbout", top, aboutMethod));
        if (_macOSXApplication == null) {
            // _setHandler should set _macOSXApplication, so perhaps
            // we are running as Applet or under -sandbox.
            return;
        }
        try {
            Method enableAboutMethod = _macOSXApplication.getClass()
                    .getDeclaredMethod("setEnabledAboutMenu",
                            new Class[] { boolean.class });
            enableAboutMethod.invoke(_macOSXApplication,
                    new Object[] { Boolean.TRUE });
        } catch (SecurityException ex) {
            if (!_printedSecurityExceptionMessage) {
                _printedSecurityExceptionMessage = true;
                System.out.println("Warning: Failed to enable "
                        + "the about menu. "
                        + "(applets and -sandbox always causes this)");
            }
        } catch (Exception ex) {
            top.report("The about menu could not be set.", ex);
        }
    }

    /** Set the quit handler (Command-q) for a Top window.
     *  @param top the Top level window to perform the operation.
     *  @param quitMethod The method to invoke in Top, typically
     *  {@link ptolemy.gui.Top#exit()}.
     */
    public static void setQuitMethod(Top top, Method quitMethod) {
        _setHandler(top, new MacOSXAdapter("handleQuit", top, quitMethod));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create an adapter that has the name of a method to be listened for,
     *  the Top window that performs the task and the method in Top.
     *  @param proxySignature  A method name from com.apple.eawt.ApplicationListener,
     *  for example "handleQuit".
     *  @param top the Top level window to perform the operation.
     *  @param topMethod The Method in Top to be called.
     */
    private MacOSXAdapter(String proxySignature, Top top, Method topMethod) {
        _proxySignature = proxySignature;
        _top = top;
        _topMethod = topMethod;
    }

    /** Create a proxy object from the adapter and add it as an ApplicationListener.
     *  @param top the Top level window to perform the operation.
     *  @param adapter The adapter.
     */
    private static void _setHandler(Top top, MacOSXAdapter adapter) {
        try {
            Class applicationClass = null;
            String applicationClassName = "com.apple.eawt.Application";
            try {
                applicationClass = Class.forName(applicationClassName);
            } catch (NoClassDefFoundError ex) {
                if (!_printedNoClassDefFoundMessage) {
                    System.out.println("Warning: Failed to find the \""
                            + applicationClassName + "\" class: " + ex
                            + " (applets and -sandbox always causes this)");
                    _printedNoClassDefFoundMessage = true;
                }
                return;
            } catch (ExceptionInInitializerError ex) {
                if (ex.getCause() instanceof SecurityException) {
                    if (!_printedSecurityExceptionMessage) {
                        System.out.println("Warning: Failed to create new "
                                + "instance of \"" + applicationClassName
                                + "\": " + ex
                                + "(applets and -sandbox always causes this)");
                    }
                    return;
                }
            }

            if (applicationClass == null) {
                throw new NullPointerException("Internal Error!  class "
                        + applicationClassName + " was not found "
                        + "and the exception was missed?");
            } else {
                if (_macOSXApplication == null) {
                    try {
                        _macOSXApplication = applicationClass.getConstructor(
                                (Class[]) null).newInstance((Object[]) null);
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        if (ex.getCause() instanceof SecurityException) {
                            if (!_printedSecurityExceptionMessage) {
                                System.out
                                        .println("Warning: Failed to get the"
                                                + "constructor of \""
                                                + applicationClassName
                                                + "\" ("
                                                + applicationClass
                                                + "): "
                                                + ex
                                                + "(applets and -sandbox always causes this)");
                            }
                        }
                        return;
                    }
                }
                Class applicationListenerClass = Class
                        .forName("com.apple.eawt.ApplicationListener");
                Method addListenerMethod = applicationClass.getDeclaredMethod(
                        "addApplicationListener",
                        new Class[] { applicationListenerClass });

                // Create a proxy object around this handler that can be
                // reflectively added as an Apple ApplicationListener
                Object osxAdapterProxy = Proxy.newProxyInstance(
                        MacOSXAdapter.class.getClassLoader(),
                        new Class[] { applicationListenerClass }, adapter);
                addListenerMethod.invoke(_macOSXApplication,
                        new Object[] { osxAdapterProxy });
            }
        } catch (ClassNotFoundException ex) {
            top.report("The a com.apple.eawt class was not found?", ex);
        } catch (Exception ex2) {
            top.report(
                    "There was a problem invoking the addApplicationListener method",
                    ex2);
        }
    }

    /** An instance of com.apple.eawt.Application, upon which methods are invoked.
     *  We use Object here instead of com.apple.eawt.Application so as to avoid
     *  compile-time dependencies on Apple-specific code.
     *  The _setHandler() method sets macOSXApplication.
     *  If we are running as an unsigned applet or using -sandbox, then
     *  this variable will be null.
     */
    private static Object _macOSXApplication;

    /** True if we have printed the securityException message. */
    private static boolean _printedSecurityExceptionMessage = false;

    /** True if we have printed the NoClassDefFound message. */
    private static boolean _printedNoClassDefFoundMessage = false;

    /**  The name of a method in com.apple.eawt.ApplicationListener,
     *  for example "handleQuit".
     */
    private String _proxySignature;

    /** The Top level window to perform the operation.  This window is also
     *  used to report errors.
     */
    private Top _top;

    /** The Method in Top to be called. */
    private Method _topMethod;
}
