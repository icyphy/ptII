/*
 This class loads Netbeans Visual Library widgets for a provided named object
 and target type.

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
package ptolemy.homer.kernel;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.widgets.NamedObjectIconWidget;
import ptolemy.homer.widgets.NamedObjectImageWidget;
import ptolemy.homer.widgets.NamedObjectWidget;
import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// WidgetLoader

/**
 * This class loads Netbeans Visual Library widgets for a provided named object
 * and target type.  It uses three strategies to load widgets for the target type: custom widget class,
 *  widget consisting of an image of an actor, or widget consisting of a Ptolemy icon.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class WidgetLoader {

    /** Hide the constructor of the utility class.
     */
    private WidgetLoader() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Load widget of the given PositionableElement and targetType.  If there is no widget mapped for the
     * the target type, it would look for a widget for the target type's parent recursively.
     * The method would first try finding custom widget class for the targetType.  The mappings is
     * defined in the ObjectWidgets.properties file within widget package. If this fails, it would look for
     * image of the targetType based on mapping in ImageWidgets.properties file within images package.  If this fails too,
     * it would try loading a Ptolemy icon for the provided NamedObject.
     *
     * @param scene The scene where the widget belongs.
     * @param element The element for which widget is loaded.
     * @param targetType The targetType used to finding appropriate widget mapped to it.
     * Usually targetType is the same as namedObject's type.
     * @return A new widget instance for the targetType and namedObject.  The
     * returned instance implements {@link NamedObjectWidgetInterface}.
     * @exception IllegalActionException if there is a problem loading a object widget or icon.
     * @exception NameDuplicationException if there is a problem loading an icon.
     */
    public static Widget loadWidget(Scene scene, PositionableElement element,
            Class<?> targetType) throws IllegalActionException,
            NameDuplicationException {
        Widget widget = _getObjectWidget(scene, element, targetType);
        if (widget != null) {
            return widget;
        }
        widget = _getImageWidget(scene, element, targetType);
        if (widget != null) {
            return widget;
        }
        widget = new NamedObjectIconWidget(scene, element);
        //assert widget instanceof NamedObjectWidgetInterface : "The widget must implement NamedObjectWidgetInterface";
        return widget;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return custom object widget for the provided type if available.  Null otherwise.
     * @param scene The scene where the widget belongs.
     * @param element The element for which widget is loaded.
     * @param targetType The targetType used to finding appropriate widget mapped to it.
     * Usually targetType is the same as namedObject's type.
     * @return A new widget instance for the targetType and namedObject.  The
     * returned instance implements {@link NamedObjectWidgetInterface}.
     * @exception IllegalActionException if there is a problem loading a object widget.
     */
    private static Widget _getObjectWidget(Scene scene,
            PositionableElement element, Class<?> targetType)
            throws IllegalActionException {
        if (targetType == null) {
            return null;
        }
        if (!_OBJECT_WIDGET_BUNDLE.containsKey(targetType.getName())) {
            return _getObjectWidget(scene, element, targetType.getSuperclass());
        }
        String widgetTypeName = _OBJECT_WIDGET_BUNDLE.getString(targetType
                .getName());
        try {
            Class<NamedObjectWidget> widgetType = (Class<NamedObjectWidget>) WidgetLoader.class
                    .getClassLoader().loadClass(widgetTypeName);
            return widgetType.getConstructor(Scene.class,
                    PositionableElement.class).newInstance(scene, element);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        } catch (IllegalArgumentException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        } catch (SecurityException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        } catch (InstantiationException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        } catch (InvocationTargetException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(element.getElement(), e,
                    "Problem loading widget " + widgetTypeName);
        }

    }

    /**
     * Return image widget for the provided element if available, null otherwise.
     * @param scene The scene where the widget belongs.
     * @param element The element for which widget is loaded.
     * @param targetType The targetType used to finding appropriate widget mapped to it.
     * Usually targetType is the same as namedObject's type.
     * @return A new widget instance for the targetType and namedObject.  The
     * returned instance implements {@link NamedObjectWidgetInterface}.
     */
    private static Widget _getImageWidget(Scene scene,
            PositionableElement element, Class<?> targetType) {
        if (targetType == null) {
            return null;
        }
        if (!_IMAGE_WIDGET_BUNDLE.containsKey(targetType.getName())) {
            return _getImageWidget(scene, element, targetType.getSuperclass());
        }
        String imageName = _IMAGE_WIDGET_BUNDLE.getString(targetType.getName());
        return new NamedObjectImageWidget(scene, element,
                WidgetLoader.class.getResource("../images/" + imageName));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The bundle containing mappings from the named object types to widgets visualizing them.
     */
    private static final ResourceBundle _OBJECT_WIDGET_BUNDLE = ResourceBundle
            .getBundle("ptolemy.homer.widgets.ObjectWidgets");
    /**
     * The bundle containing mappings from the named object types to images depicting them.
     */
    private static final ResourceBundle _IMAGE_WIDGET_BUNDLE = ResourceBundle
            .getBundle("ptolemy.homer.images.ImageWidgets");
}
