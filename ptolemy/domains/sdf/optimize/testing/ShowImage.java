package ptolemy.domains.sdf.optimize.testing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

public class ShowImage extends Panel {
    Image  image;
    
    public ShowImage() {
    }

    public void paint(Graphics g) {
      g.drawImage( image, 0, 0, null);
    }
}
