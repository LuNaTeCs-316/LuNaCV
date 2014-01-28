package org.lunatecs316.frc2014.vision;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

/**
 * JPanel to display an OpenCV Mat
 * @author Domenic
 */
public class CVMatPanel extends JPanel {
    private BufferedImage image;
    
    public CVMatPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
    }
    
    public void showMat(Mat mat) {
        image = matToBufferedImage(mat);
        repaint();
    }
   
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
    
    /**
     * Convert an OpenCV Mat to an AWT BufferedImage
     * @param mat the OpenCV matrix
     * @return the equivalent BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte bytemat = new MatOfByte();
        Highgui.imencode(".jpg", mat, bytemat);
        byte[] bytes = bytemat.toArray();
        BufferedImage im = null;
        try {
            im = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException ex) {
            Logger.getLogger(AcquireSampleImages.class.getName()).log(Level.SEVERE, null, ex);
        }
        return im;
    }
}
