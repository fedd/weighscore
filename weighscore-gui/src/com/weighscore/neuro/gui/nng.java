package com.weighscore.neuro.gui;

import java.awt.Toolkit;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import sun.awt.image.*;
import sun.awt.*;
//import java.awt.*;

public class nng {
    boolean packFrame = false;

    /**
     * Construct and show the application.
     */
    public nng() {
        Editor frame = new Editor(true);
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

        /*// Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }*/
        //frame.setLocation((screenSize.width - frameSize.width) / 2,
        //                  (screenSize.height - frameSize.height) / 2);

        frame.setLocation(50, 20);


        frame.setIconImage((new ImageIcon(Editor.class.getResource(
                "arrange.png"))).getImage());
        frame.setVisible(true);
    }

    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.
                                             getSystemLookAndFeelClassName());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                new nng();
            }
        });
    }
}
