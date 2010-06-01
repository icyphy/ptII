package ptolemy.domains.sdf.optimize.testing;

import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JFrame;
import processing.core.PImage;

public class TestImageWindow extends JFrame {
    private JFrame _frame;

    public TestImageWindow(PImage img){
        _frame = new JFrame("TestImageWindow");
//        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ShowImage ji = new ShowImage();

        Image awkimage;
        MemoryImageSource mis = new MemoryImageSource(
                                    img.width, img.height, img.pixels, 0, img.width);
        awkimage = _frame.createImage(mis);
        
        ji.image = awkimage;
        _frame.getContentPane().add(ji);
        _frame.setSize(640, 480);

        _frame.setVisible(true);    }

}
