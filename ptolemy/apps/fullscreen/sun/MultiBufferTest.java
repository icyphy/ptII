/**
 * This test takes a number up to 13 as an argument (assumes 2 by
 * default) and creates a multiple buffer strategy with the number of
 * buffers given.  This application enters full-screen mode, if available,
 * and flips back and forth between each buffer (each signified by a different
 * color).
 */

import java.awt.*;
import java.awt.image.BufferStrategy;

public class MultiBufferTest {
    
    private static Color[] COLORS = new Color[] {
        Color.red, Color.blue, Color.green, Color.white, Color.black,
        Color.yellow, Color.gray, Color.cyan, Color.pink, Color.lightGray,
        Color.magenta, Color.orange, Color.darkGray };
    private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] {
        new DisplayMode(640, 480, 32, 0),
        new DisplayMode(640, 480, 16, 0),
        new DisplayMode(640, 480, 8, 0)
    };
    
    Frame mainFrame;
    
    public MultiBufferTest(int numBuffers, GraphicsDevice device) {
        try {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            mainFrame = new Frame(gc);
            mainFrame.setUndecorated(true);
            mainFrame.setIgnoreRepaint(true);
            device.setFullScreenWindow(mainFrame);
            if (device.isDisplayChangeSupported()) {
                chooseBestDisplayMode(device);
            }
            Rectangle bounds = mainFrame.getBounds();
            mainFrame.createBufferStrategy(numBuffers);
            BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();
            for (float lag = 2000.0f; lag > 0.00000006f; lag = lag / 1.33f) {
                for (int i = 0; i < numBuffers; i++) {
                    Graphics g = bufferStrategy.getDrawGraphics();
                    if (!bufferStrategy.contentsLost()) {
                        g.setColor(COLORS[i]);
                        g.fillRect(0,0,bounds.width, bounds.height);
                        bufferStrategy.show();
                        g.dispose();
                    }
                    try {
                        Thread.sleep((int)lag);
                    } catch (InterruptedException e) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            device.setFullScreenWindow(null);
        }
    }
    
    private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
        for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
            DisplayMode[] modes = device.getDisplayModes();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
                   && modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
                   && modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()
                   ) {
                    return BEST_DISPLAY_MODES[x];
                }
            }
        }
        return null;
    }
    
    public static void chooseBestDisplayMode(GraphicsDevice device) {
        DisplayMode best = getBestDisplayMode(device);
        if (best != null) {
            device.setDisplayMode(best);
        }
    }
    
    public static void main(String[] args) {
        try {
            int numBuffers = 2;
            if (args != null && args.length > 0) {
                numBuffers = Integer.parseInt(args[0]);
                if (numBuffers < 2 || numBuffers > COLORS.length) {
                    System.err.println("Must specify between 2 and "
                        + COLORS.length + " buffers");
                    System.exit(1);
                }
            }
            GraphicsEnvironment env = GraphicsEnvironment.
                getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            MultiBufferTest test = new MultiBufferTest(numBuffers, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
