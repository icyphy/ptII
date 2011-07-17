/*
 TODO
 
 Copyright (c) 2011 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// WidgetLoader

/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class WidgetLoader {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * TODO
     * @param scene
     * @param namedObject
     * @param targetType
     * @return
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public static Widget loadWidget(Scene scene, PositionableElement element,
            Class<?> targetType) throws IllegalActionException,
            NameDuplicationException {
        Widget widget = getObjectWidget(scene, element, targetType);
        if (widget != null) {
            return widget;
        }
        widget = getImageWidget(scene, element, targetType);
        if (widget != null) {
            return widget;
        }
        return new NamedObjectIconWidget(scene, element);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * TODO
     * @param scene
     * @param namedObject
     * @param targetType
     * @return
     * @throws IllegalActionException
     */
    private static Widget getObjectWidget(Scene scene,
            PositionableElement element, Class<?> targetType)
            throws IllegalActionException {
        if (targetType == null) {
            return null;
        }
        if (!OBJECT_WIDGET_BUNDLE.containsKey(targetType.getName())) {
            return getObjectWidget(scene, element, targetType.getSuperclass());
        }
        String widgetTypeName = OBJECT_WIDGET_BUNDLE.getString(targetType
                .getName());
        try {
            Class<NamedObjectWidget> widgetType = (Class<NamedObjectWidget>) WidgetLoader.class
                    .getClassLoader().loadClass(widgetTypeName);
            return widgetType.getConstructor(Scene.class, PositionableElement.class)
                    .newInstance(scene, element);
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
     * TODO
     * @param scene
     * @param namedObject
     * @param targetType
     * @return
     */
    private static Widget getImageWidget(Scene scene, PositionableElement element,
            Class<?> targetType) {
        if (targetType == null) {
            return null;
        }
        if (!IMAGE_WIDGET_BUNDLE.containsKey(targetType.getName())) {
            return getImageWidget(scene, element,
                    targetType.getSuperclass());
        }
        String imageName = IMAGE_WIDGET_BUNDLE.getString(targetType.getName());
        return new NamedObjectImageWidget(scene, element,
                WidgetLoader.class.getResource("../images/" + imageName));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * TODO
     */
    private static final ResourceBundle OBJECT_WIDGET_BUNDLE = ResourceBundle
            .getBundle("ptolemy.homer.widgets.ObjectWidgets");
    /**
     * TODO
     */
    private static final ResourceBundle IMAGE_WIDGET_BUNDLE = ResourceBundle
            .getBundle("ptolemy.homer.images.ImageWidgets");
}
