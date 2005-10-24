/* License
 *
 * Copyright 1994-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *  * Redistribution in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
import java.awt.*;
import java.awt.event.*;


public class HelloPP extends Canvas {
    public void paint(Graphics g) {
        Dimension d = getSize();
        int cx = d.width / 2;
        int cy = d.height / 2;

        for (int x = 0; x < d.width; x++) {
            if ((x % 2) == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }

            g.drawLine(cx, cy, x, 0);
            g.drawLine(cx, cy, x, d.height);
        }

        for (int y = 0; y < d.height; y++) {
            if ((y % 2) == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }

            g.drawLine(cx, cy, 0, y);
            g.drawLine(cx, cy, d.width, y);
        }
    }

    public static void main(String[] args) {
        final Frame f = new Frame("HelloPP");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(0, 0);
        f.setSize(d.width, d.height);

        Component c = new HelloPP();
        f.add(c);
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    f.dispose();
                    System.exit(0);
                }
            });
        f.setVisible(true);
    }
}
