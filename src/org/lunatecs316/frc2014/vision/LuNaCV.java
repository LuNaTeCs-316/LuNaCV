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
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * 2014 FRC Vision Processing
 * @author Domenic Rodriguez
 */
public class LuNaCV {
    public static final String kCameraAddress = "http://10.3.16.11/mjpg/video.mjpg";

    public static final int kImageWidth = 320;
    public static final int kImageHeight = 240;

    public static final int kMinHue = 80;
    public static final int kMinSat = 55;
    public static final int kMinVal = 95;
    public static final int kMaxHue = 180;
    public static final int kMaxSat = 255;
    public static final int kMaxVal = 255;
    public static final int kErodeSize = 2;
    public static final int kDilateSize = 2;

    public static final int kMinTargetArea = 75;            // px
    public static final int kMaxTargetArea = 500;           // px
    public static final double kApproxPolyTolerance = 0.0275;

    public static final double kStaticTargetWidth = 4;      // Inches
    public static final double kStaticTargetHeight = 32;    // Inches
    public static final double kDynamicTargetWidth = 4;     // Inches
    public static final double kDynamicTargetLength = 23.5; // Inches
    public static final int kCameraViewAngle = 47;          // Degrees
    // Compute once instead of every loop
    public static final double kTanTheta = Math.tan(((kCameraViewAngle / 2) * Math.PI) / 180);
    
    private NetworkTable table;
    private JFrame frame;
    private CVMatPanel originalPanel;
    private CVMatPanel processedPanel;
    
    private JSlider minHueSlider;
    private JSlider minSatSlider;
    private JSlider minValSlider;
    private JSlider maxHueSlider;
    private JSlider maxSatSlider;
    private JSlider maxValSlider;
    private JSlider erodeSlider;
    private JSlider dilateSlider;

    private boolean done = false;
    private boolean debug;
    
    public LuNaCV() {
        this(false);
    }
    
    public LuNaCV(boolean debug) {
        this.debug = debug;
    }
    
    public static void main(String[] args) {
        new LuNaCV().run();
    }
    
    public void run() {
        System.loadLibrary("opencv_java247");
        
        //NetworkTable.setClientMode();
        //NetworkTable.setIPAddress("10.3.16.2");
        //table = NetworkTable.getTable("vision-data");
        
        // Setup the GUI
        setupAndCreateGUI();
      
        // Open the camera
        VideoCapture camera = new VideoCapture(kCameraAddress);
        //VideoCapture camera = new VideoCapture(0);
        Mat originalImage = new Mat();
        
        // Main loop
//        while (!done) {
//            if (camera.isOpened()) {
//                if (camera.read(originalImage)) {
//                    Mat processed = processImage(originalImage);
//                    originalPanel.showMat(originalImage);
//                    processedPanel.showMat(processed);
//                } else {
//                    System.err.println("Error: unable to read image");
//                }
//            } else {
//                System.out.println("Error: camera is not open");
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ex) {
//                }
//            }
//        }
        
        for (int i = 1; i <= 11; i++) {
            System.out.println("Image " + i);
            originalImage = Highgui.imread("sample_images/image" + i + ".jpg");
            Mat processed = processImage(originalImage);
            originalPanel.showMat(originalImage);
            processedPanel.showMat(processed);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex){
            }
        }
        
//        while (true) {
//            originalImage = Highgui.imread("sample_images/image1.jpg");
//            Mat processed = processImage(originalImage);
//            originalPanel.showMat(originalImage);
//            processedPanel.showMat(processed);
//       }
    }
    
    /**
     * Create the GUI elements
     */
    private void setupAndCreateGUI() {
        // Setup GUI
        frame = new JFrame("LuNaCV");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                done = true;
            }
        });
        frame.getContentPane().setLayout(new GridLayout(2, 2));
        
        // Add Image panels
        originalPanel = new CVMatPanel(kImageWidth, kImageHeight);
        frame.getContentPane().add(originalPanel);
        processedPanel = new CVMatPanel(kImageWidth, kImageHeight);
        frame.getContentPane().add(processedPanel);
        
        // Add sliders
        JPanel sliders = new JPanel();
        sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
        JLabel minHueLabel = new JLabel("minHue");
        minHueSlider = new JSlider(JSlider.HORIZONTAL, 0, 180, kMinHue);
        JLabel minSatLabel = new JLabel("minSat");
        minSatSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMinSat);
        JLabel minValLabel = new JLabel("minVal");
        minValSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMinVal);
        JLabel maxHueLabel = new JLabel("maxHue");
        maxHueSlider = new JSlider(JSlider.HORIZONTAL, 0, 180, kMaxHue);
        JLabel maxSatLabel = new JLabel("maxSat");
        maxSatSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMaxSat);
        JLabel maxValLabel = new JLabel("maxVal");
        maxValSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, kMaxVal);
        JLabel erodeLabel = new JLabel("erodeSize");
        erodeSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, kErodeSize);
        JLabel dilateLabel = new JLabel("dilateSize");
        dilateSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, kDilateSize);
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
        sliders.add(erodeLabel);
        sliders.add(erodeSlider);
        sliders.add(dilateLabel);
        sliders.add(dilateSlider);
        frame.getContentPane().add(sliders);
        
        // Display the frame
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Process the image and search for the targets
     * @param image the original image
     * @return the processed image
     */
    private Mat processImage(Mat image) {
        double startTime = System.currentTimeMillis();
        
        // Convert image to HSV color space
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
        
        // Apply color threshold
        Mat thresh = new Mat();
        Scalar lowerBound = new Scalar(minHueSlider.getValue(), minSatSlider.getValue(), minValSlider.getValue());
        Scalar upperBound = new Scalar(maxHueSlider.getValue(), maxSatSlider.getValue(), maxValSlider.getValue());
        Core.inRange(hsv, lowerBound, upperBound, thresh);
        //System.out.println(lowerBound + " " + upperBound);
        
        // Morph operations to filter out small blobs
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(erodeSlider.getValue(), erodeSlider.getValue()));
        Imgproc.erode(thresh, thresh, morphKernel);
        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(dilateSlider.getValue(), dilateSlider.getValue()));
        Imgproc.dilate(thresh, thresh, morphKernel);
        
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat heirarchy = new Mat();
        Imgproc.findContours(thresh.clone(), contours, heirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        // Contours / Convex Hull / Polygon Approximation / Bounding Rectangles
        MatOfPoint2f approx = new MatOfPoint2f();
        List<MatOfPoint> polygons = new ArrayList<>();
        Rect boundingRect;
        Mat drawing = Mat.zeros(thresh.size(), thresh.type());
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);

            // Skip any contours too big or small to be the target
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea < kMinTargetArea || contourArea > kMaxTargetArea) {
                polygons.add(new MatOfPoint());
                continue;
            }
            
            // Approximate polygon from contour
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double contourLength = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approx, contourLength * kApproxPolyTolerance, true);
            MatOfPoint approxPointMat = new MatOfPoint(approx.toArray());
            polygons.add(approxPointMat);
            
            // Find the bounding rectangle
            boundingRect = Imgproc.boundingRect(approxPointMat);
            
            // Print information on targets
            System.out.print("Target " + i + ": Vertices " + approxPointMat.total() + " ");
            System.out.print("Position: (" + boundingRect.x + "," + boundingRect.y + ") ");
            System.out.print("Width: " + boundingRect.width + " Height: " + boundingRect.height + " ");
            System.out.print("Area: " + (boundingRect.width * boundingRect.height) + " ");
            boolean horizontal = boundingRect.width > boundingRect.height;
            System.out.print("Distance: " + getTargetDistance(boundingRect.width, horizontal) + " ");
            if (horizontal) {
                System.out.println("Goal is hot?");
            } else {
                System.out.println();
            }
            
            // Overlay the bounding rectangle on the original image
            Core.rectangle(image, boundingRect.tl(), boundingRect.br(), new Scalar(255, 0, 0));
            
//            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 255, 255));
            Imgproc.drawContours(drawing, polygons, i, new Scalar(255, 255, 255));
        }
        
        System.out.println("Image processed in " + (System.currentTimeMillis() - startTime) + "ms");
        
        // Send data to the robot
        //table.putBoolean("goalIsHot", true);
        
        System.out.println();
        return drawing;
    }
    
    /**
     * Get the distance to the specified target
     * @param pxWidth
     * @param horizontal
     * @return the distance in inches to the target
     */
    private double getTargetDistance(double pxWidth, boolean dynamic) {
        double result;
        if (dynamic) {
            result = (kDynamicTargetLength * kImageWidth) / (2 * kTanTheta * pxWidth);
        } else {
            result = (kStaticTargetWidth * kImageWidth) / (2 * kTanTheta * pxWidth);
        }
        return result;
    }
}
