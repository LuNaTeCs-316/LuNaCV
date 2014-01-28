package org.lunatecs316.frc2014.vision;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JSlider;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Domenic
 */
public class LuNaCVWidget extends StaticWidget {
    public static final String OPENCV_LIB_PATH = "C:\\OpenCV\\build\\java\\x64\\opencv_java247.dll";
    private VideoCapture camera;
    private Mat frame;
    private Mat threshold;
    private JSlider minHue;
    private JSlider minSat;
    private JSlider minVal;
    private JSlider maxHue;
    private JSlider maxSat;
    private JSlider maxVal;

    @Override
    public void init() {
        camera = new VideoCapture("http://10.3.16.11/mjpg/video.mjpg");
        frame = new Mat();
        threshold = new Mat();
        
        minHue = new JSlider(0, 360, 116);
        minSat = new JSlider(0, 255, 73);
        minVal = new JSlider(0, 255, 0);
        maxHue = new JSlider(0, 360, 360);
        maxSat = new JSlider(0, 255, 255);
        maxVal = new JSlider(0, 255, 138);
        
        add(minHue);
        add(minSat);
        add(minVal);
        add(maxHue);
        add(maxSat);
        add(maxVal);
    }

    @Override
    public void propertyChanged(Property prprt) {
    }
    
    public void processImage() {
        if (camera.isOpened()) {
            // Get the latest image from the camera
            boolean didReadNewImage = camera.read(frame);
            if (didReadNewImage) {
                // Convert to HSV color space
                Mat hsv = new Mat();
                Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
                
                // Apply threshold
                Core.inRange(frame,
                             new Scalar(minHue.getValue(), minSat.getValue(), minVal.getValue()),
                             new Scalar(maxHue.getValue(), maxHue.getValue(), maxVal.getValue()),
                             threshold);
                System.out.println("" + minHue.getValue() + " " + minSat.getValue() + " " + minVal.getValue()
                                    + " " + maxHue.getValue() + " " + maxSat.getValue() + " " + maxVal.getValue());
                
                // Morph operations
                Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(3, 3));
                Imgproc.erode(threshold, threshold, morphKernel);
                morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3));
                Imgproc.dilate(threshold, threshold, morphKernel);
                
                // Find contours
                List<MatOfPoint> contours = new ArrayList<>();    
                Mat hierarchy = new Mat();
                Imgproc.findContours(threshold.clone(), contours, hierarchy, Imgproc.CHAIN_APPROX_SIMPLE, Imgproc.RETR_TREE);
                
                // Draw contours
                //for (int i = 0; i < contours.size(); i++) {
                //    Imgproc.drawContours(frame, contours, i, new Scalar(255, 255, 255));
                //}
            }
        } else {
            System.err.println("Error: camera is not open");
        }
        repaint();
    }
    
    protected BufferedImage matToBufferedImage(Mat img) {
        MatOfByte bytemat = new MatOfByte();
        Highgui.imencode(".jpg", img, bytemat);
        byte[] bytes = bytemat.toArray();
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException ex) {
            Logger.getLogger(LuNaCVWidget.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(matToBufferedImage(frame), 0, 0, null);
        g.drawImage(matToBufferedImage(threshold), 320, 0, null);
    }
    
    public static void main(String[] args) {
        System.load(OPENCV_LIB_PATH);
        //DashboardFrame frame = new DashboardFrame(true);
        LuNaCVWidget widget = new LuNaCVWidget();
        widget.init();
        JFrame window = new JFrame();
        window.getContentPane().add(widget);
        window.setSize(640, 480);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        while (true) {
            widget.processImage();
        }
    }
}
