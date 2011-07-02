package ptolemy.homer;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.widgets.NamedObjectWidget;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class WidgetLoader {

    public static Widget loadWidget(Scene scene, NamedObj namedObject,
            Class<?> targetType) {

        return null;
    }

    private static Widget getObjectWidget(Scene scene, NamedObj namedObject,
            Class<?> targetType) throws IllegalActionException {
        if (targetType == null) {
            return null;
        }
        if (!OBJECT_WIDGET_BUNDLE.containsKey(targetType.getName())) {
            return getObjectWidget(scene, namedObject,
                    targetType.getSuperclass());
        }
        String widgetTypeName = OBJECT_WIDGET_BUNDLE.getString(targetType
                .getName());
        try {
            Class<NamedObjectWidget> widgetType = (Class<NamedObjectWidget>) WidgetLoader.class
                    .getClassLoader().loadClass(widgetTypeName);
            return widgetType.getConstructor(Scene.class, NamedObj.class)
                    .newInstance(scene, namedObject);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        } catch (IllegalArgumentException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        } catch (SecurityException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        } catch (InstantiationException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        } catch (InvocationTargetException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(namedObject, e,
                    "Problem loading widget " + widgetTypeName);
        }

    }

    private static Widget getImageWidget(NamedObj namedObject,
            Class<?> targetType) {
        return null;
    }

    private static Widget getIconWidget(NamedObj namedObject,
            Class<?> targetType) {
        return null;
    }

    private static final ResourceBundle OBJECT_WIDGET_BUNDLE = ResourceBundle
            .getBundle("ptolemy.homer.widgets.ObjectWidgets");
    private static final ResourceBundle OBJECT_IMAGE_BUNDLE = ResourceBundle
            .getBundle("ptolemy.homer.widgets.ObjectImages");
}
