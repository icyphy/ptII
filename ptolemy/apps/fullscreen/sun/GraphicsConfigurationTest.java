// Print information about all of the GraphicsConfigurations
// for all of the devices.
import java.awt.*;


public class GraphicsConfigurationTest {
    public static void main(String[] args) {
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();

        for (int i = 0; i < graphicsDevices.length; i++) {
            String graphicsDeviceType = "UNKNOWN";

            switch (graphicsDevices[i].getType()) {
            case GraphicsDevice.TYPE_RASTER_SCREEN:
                graphicsDeviceType = "TYPE_RASTER_SCREEN";
                break;

            case GraphicsDevice.TYPE_PRINTER:
                graphicsDeviceType = "TYPE_PRINTER";
                break;

            case GraphicsDevice.TYPE_IMAGE_BUFFER:
                graphicsDeviceType = "TYPE_IMAGE_BUFFER";
                break;
            }

            System.out.println("GraphicsDevices[" + i + "]: "
                + graphicsDeviceType + " " + graphicsDevices[i]);

            GraphicsConfiguration[] graphicsConfigurations = graphicsDevices[i]
                        .getConfigurations();

            for (int j = 0; j < graphicsConfigurations.length; j++) {
                System.out.println("GraphicsConfigurations[" + j + "]: "
                    + graphicsConfigurations[j]);
                System.out.println("GraphicsConfigurations[" + j + "]"
                    + graphicsConfigurations[j]);
                System.out.println("  ColorModel: "
                    + graphicsConfigurations[j].getColorModel());
                System.out.println("  Bounds: "
                    + graphicsConfigurations[j].getBounds());
                virtualBounds = virtualBounds.union(graphicsConfigurations[j]
                                .getBounds());

                BufferCapabilities bufferCapabilities = graphicsConfigurations[j]
                            .getBufferCapabilities();
                System.out.println("BufferCapabilities: " + bufferCapabilities
                    + " getFlipContents:"
                    + bufferCapabilities.getFlipContents()
                    + " isFullScreenRequired:"
                    + bufferCapabilities.isFullScreenRequired()
                    + " isPageFlipping:" + bufferCapabilities.isPageFlipping()
                    + " isMultiBufferAvailable:"
                    + bufferCapabilities.isMultiBufferAvailable());
            }
        }
    }
}
