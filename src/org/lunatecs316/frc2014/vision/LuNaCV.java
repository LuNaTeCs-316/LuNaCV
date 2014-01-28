package org.lunatecs316.frc2014.vision;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * 2014 FRC Vision Processing
 * @author Domenic Rodriguez
 */
public class LuNaCV {
    private NetworkTable table;
    private JFrame frame;
    private CVMatPanel originalPanel;
    private CVMatPanel processedPanel;
    private VideoCapture camera;
    
    private JSlider minHueSlider;
    private JSlider minSatSlider;
    private JSlider minValSlider;
    private JSlider maxHueSlider;
    private JSlider maxSatSlider;
    private JSlider maxValSlider;

    private boolean done = false;
    
    public static void main(String[] args) {
        new LuNaCV().run();
    }
    
    public void run() {
        System.load("C:\\OpenCV\\build\\java\\x64\\opencv_java247.dll");
        
        //NetworkTable.setClientMode();
        //NetworkTable.setIPAddress("10.3.16.2");
        //table = NetworkTable.getTable("vision-data");
        
        // Setup GUI
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                done = true;
            }
        });
        frame.getContentPane().setLayout(new GridLayout(2, 2));
        
        // Add Image panels
        originalPanel = new CVMatPanel(640, 480);
        frame.getContentPane().add(originalPanel);
        processedPanel = new CVMatPanel(640, 480);
        frame.getContentPane().add(processedPanel);
        
        // Add sliders
        JPanel sliders = new JPanel();
        sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
        JLabel minHueLabel = new JLabel("minHue");
        minHueSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 116);
        JLabel minSatLabel = new JLabel("minSat");
        minSatSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 73);
        JLabel minValLabel = new JLabel("minVal");
        minValSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
        JLabel maxHueLabel = new JLabel("maxHue");
        maxHueSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 360);
        JLabel maxSatLabel = new JLabel("maxSat");
        maxSatSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);
        JLabel maxValLabel = new JLabel("maxVal");
        maxValSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 138);
        sliders.add(minHueLabel);
        sliders.add(minHueSlider);
        sliders.add(minSatLabel);
        sliders.add(minSatSlider);
        sliders.add(minValLabel);
        sliders.add(minValSlider);
        sliders.add(maxHueLabel);
        sliders.add(maxHueSlider);
        sliders.add(maxSatLabel);
        sliders.add(maxSatSlider);
        sliders.add(maxValLabel);
        sliders.add(maxValSlider);
        frame.getContentPane().add(sliders);
        
        // Display the frame
        frame.pack();
        frame.setVisible(true);
        
        //camera = new VideoCapture("http://10.3.16.11/mjpg/video.mjpg");
        camera = new VideoCapture(0);
        Mat originalImage = new Mat();
        
        // Main camera loop
        while (!done) {
            if (camera.isOpened()) {
                if (camera.read(originalImage)) {
                    originalPanel.showMat(originalImage);
                    Mat processed = processImage(originalImage);
                    processedPanel.showMat(processed);
                } else {
                    System.err.println("Error: unable to read image");
                }
            } else {
                System.out.println("Error: camera is not open");
            }
        }
    }
    
    private Mat processImage(Mat image) {
        double startTime = System.currentTimeMillis();
        
        // Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
        
        // Apply color threshold
        Mat thresh = new Mat();
        Scalar lowerBound = new Scalar(minHueSlider.getValue(), minSatSlider.getValue(), minValSlider.getValue());
        Scalar upperBound = new Scalar(maxHueSlider.getValue(), maxHueSlider.getValue(), maxHueSlider.getValue());
        Core.inRange(hsv, lowerBound, upperBound, thresh);
        
        // Morph operations
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(5, 5));
        Imgproc.erode(thresh, thresh, morphKernel);
        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(5, 5));
        Imgproc.dilate(thresh, thresh, morphKernel);
        
        // Find contours
        //List<MatOfPoint> contours = new ArrayList<>();
        //Mat heirarchy = new Mat();
        //Imgproc.findContours(thresh, contours, heirarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        
        // Draw contours
        //Mat drawing = Mat.zeros(thresh.size(), thresh.type());
        //for (int i = 0; i < contours.size(); i++) {
        //    Imgproc.drawContours(drawing, contours, i, new Scalar(255, 255, 255));
        //}
        
        System.out.println("Image processed in " + (System.currentTimeMillis() - startTime) + "ms");
        
        // Send data to the robot
        //table.putBoolean("goalIsHot", true);
        
        return thresh;
    }
}
