/* An attribute that has a string value that names a class to be instantiated.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ClassFactoryAttribute
/**
An attribute that has a string value that names a class to be
instantiated.

<p>We use this class to avoid requiring that all classes be available at
when a MoML file is read in.  In particular, some Ptolemy II models
may contain IconControllers or AnnotationEditors that are part of
vergil.  If the model is run in a non-graphical environment, and the
vergil classes are not available, then instantiation of these classes
is deferred.

<p>For example, NodeControllerFactory could look like:
<pre>
    public NodeControllerFactory extends ClassFactoryAttribute {

        public NamedObjController create(GraphController controller)
               throws IllegalActionException {
            Object[] args = new Object[1];
            args[1] = controller;
            return instantiate(args);
        }
    }
</pre>
And the moml to instantiate this would look like
<pre>
     <property name="_controllerClass"
         class="ptolemy.kernel.util.ClassFactoryAttribute">
         value="ptolemy.vergil.basic.NodeController">
     </property>
</pre>

<p>Use setExpression() to define the value, as in for example
<pre>
  classFactoryAttribute.setExpression("ptolemy.vergil.basic.NodeController");
</pre>
<p>The default value of the string contained by this attribute is the empty
string.

<p>By default, an instance of this class is not visible
a user interface.  The invisibility is indicated to the user
interface when the user interface calls the getVisibility() method
of this class and the value Settable.NONE is returned to the user
interface.
<p>
@see Settable#NONE
@author Christopher Hylands, Edward A. Lee
@version $Id$
*/

public class ClassFactoryAttribute extends StringAttribute
    implements Settable {

    //   ... settable methods just like StringAttribute (or alternatively,
    //   extend StringAttribute and replicate the methods of
    //   SingletonAttribute to make it a singleton...
    // some code duplication is inevitable...

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public ClassFactoryAttribute() {
	super();
	// System.out.println("ClassFactorAttribute()  after super();");
        setVisibility(Settable.NONE);
    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ClassFactoryAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	// System.out.println("ClassFactorAttribute(" + container + ","
	//		   + name + ") after super();");
        setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    public Object instantiate(Object[] constructorArgs)
            throws IllegalActionException {

        // We can't return NamedObj here because
        // vergil.basic.NodeControllerFactory.create calls this method
        // and returns a NamedObjController, which is not a NamedObj.

        //... put all reflection code in here, and translate all the
        // various exceptions to IllegalActionException...
        // ... use getExpression to get the class name to instantiate...
        String className = getExpression();
	System.out.println("ClassFactorAttribute.instantiate(): '"
			   + className + "'");
        try {
            Class newClass = Class.forName(className);

	    // The way to make this change backward compatible in the
	    // code would be to modify ClassFactoryAttribute so that
	    // when it is called upon to create an instance, it also
	    // populates it with any attributes it contains (same
	    // name, same value, that is).

            Object instance =  _createInstance(newClass, constructorArgs);

            if (instance instanceof NamedObj) {
                NamedObj namedObjInstance = (NamedObj)instance;
                // For each attribute that this object has copy the attribute
                // into instance.
                for (Iterator i = attributeList().iterator(); i.hasNext();) {
                    Attribute attribute = (Attribute)i.next();
                    // FIXME: call exportMoML here so that we
                    // can save the changes???
                    Attribute clone =
                        (Attribute)attribute.clone(namedObjInstance
                                .workspace());
                    System.out.println("instantiate:\n\t\tattribute: "
                            + attribute
                            + "\n\t\tclone: " + clone
                            + "\n\t\tinstance: " + namedObjInstance );
                    //clone.setContainer(instance);
                }
            }

	    return instance;

        } catch (NoClassDefFoundError noClassDefFound) {
            throw new IllegalActionException(this, noClassDefFound,
                    "Could not find class '" + className +"'");
        } catch (InvocationTargetException invocation) {
            throw new IllegalActionException(this, invocation,
                    "Could not invoke class '" + className +"'");
        } catch (ClassNotFoundException ex) {
            throw new IllegalActionException(this, ex,
                    "Could not find class '" + className +"'");
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "className was '" + className + "'");
        }
    }

    public Object instantiateWithDefaultContainerAndName()
            throws IllegalActionException {
	// Invoke the constructor.
	Object[] constructorArguments = new Object[2];
	NamedObj container = (NamedObj)getContainer();
	constructorArguments[0] = container;
	constructorArguments[1] = container.uniqueName(getName());
	System.out.println("ClassFactorAttribute.instantiateWithDefault"
			   + "ContainerAndName():\n\t"
			   + constructorArguments[0] + "\n\t"
			   + constructorArguments[1]);
	return instantiate(constructorArguments);
    }

    /** Remove any previous attribute in the container that has
     *  class SingletonAttribute and the same name as this
     *  attribute, and then call the base class method to set the container.
     *  If the container already contains an attribute with the same name
     *  that is not of class SingletonAttribute, then throw an
     *  exception and do not make any changes.  Similarly, if the container
     *  is not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove this attribute from its
     *  container. It is not added to the workspace directory, so this
     *  could result in this object being garbage collected.
     *  <p>
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.
     *  <p>
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  <p>
     *  This method is precisely the same as the
     *  SingletonAttribute.setContainer() method.
     *
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute that is of class
     *   SingletonAttribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        Attribute previous = null;
        if (container != null) {
            previous = container.getAttribute(getName());
            if (previous instanceof SingletonAttribute) {
                previous.setContainer(null);
            }
        }
        try {
            super.setContainer(container);
        } catch (IllegalActionException ex) {
            // Restore previous.
            if (previous instanceof SingletonAttribute) {
                previous.setContainer(container);
            }
            throw ex;
        } catch (NameDuplicationException ex) {
            // Restore previous.
            if (previous instanceof SingletonAttribute) {
                previous.setContainer(container);
            }
            throw ex;
        }
    }

    // FIXME: This can go away
    public void setExpression(String expression)
	throws IllegalActionException {
	System.out.println("ClassFactorAttribute.setExpression("
			   + expression + ")");
	super.setExpression(expression);
    }

    // FIXME: this is from MoMLParser, we should make it static.
    //
    // Create an instance of the specified class name by finding a
    // constructor that matches the specified arguments.  The specified
    // class must be NamedObj or derived, or a ClassCastException will
    // be thrown.
    // @param newClass The class.
    // @param arguments The constructor arguments.
    // @exception Exception If no matching constructor is found, or if
    //  invoking the constructor triggers an exception.
    private Object _createInstance(Class newClass, Object[] arguments)
            throws Exception {
        Constructor[] constructors = newClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != arguments.length) continue;
            boolean match = true;
            for (int j = 0; j < parameterTypes.length; j++) {
                if (!(parameterTypes[j].isInstance(arguments[j]))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return /*(NamedObj)*/ constructor.newInstance(arguments);
            }
        }
        // If we get here, then there is no matching constructor.

	// Generate a StringBuffer containing what we were looking for.
	StringBuffer argumentBuffer = new StringBuffer();
	for (int i = 0; i < arguments.length; i++) {
	    argumentBuffer.append(arguments[i].getClass() + " = \""
				  + arguments[i].toString() + "\" " );
	}

        throw new Exception("Cannot find a suitable constructor ("
			       + arguments.length + " args) ( "
			       + argumentBuffer + ") for'"
                               + newClass.getName() + "'");

    }

}
