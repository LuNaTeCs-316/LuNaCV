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
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
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

    public static final int kMinHue = 70;
    public static final int kMinSat = 53;
    public static final int kMinVal = 93;
    public static final int kMaxHue = 180;
    public static final int kMaxSat = 255;
    public static final int kMaxVal = 255;
    public static final int kMorphKernelSize = 2;

    public static final int kMinTargetArea = 75;            // px
    public static final int kMaxTargetArea = 750;           // px
    public static final double kApproxPolyTolerance = 0.0295;

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
    private VideoCapture camera;

    private JSlider minHueSlider;
    private JSlider minSatSlider;
    private JSlider minValSlider;
    private JSlider maxHueSlider;
    private JSlider maxSatSlider;
    private JSlider maxValSlider;

    private boolean done = false;
    private boolean debug = false;

    public void run() {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Initialize NetworkTables
        if (!debug) {
            NetworkTable.setClientMode();
            NetworkTable.setIPAddress("10.3.16.2");
            table = NetworkTable.getTable("visionData");
        }

        // Setup the GUI
        setupGUI();

        if (debug) {
            processSampleImages();
            //while (true) {
            //    processSampleImage("sample_images/image1.jpg");
            //}
        } else {
            // Open the camera feed
            camera = new VideoCapture(kCameraAddress);

            // Process images from the camera
            processCameraFeed();
        }
    }

    /**
     * Create the GUI elements
     */
    private void setupGUI() {
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
    }

    /**
     * Process a continuous feed of images from the camera
     */
    private void processCameraFeed() {
        Mat original = new Mat();
        while (!done) {
            if (camera.isOpened()) {// && table.getBoolean("enabled", true)) {
                if (camera.read(original)) {
                    Mat processed = processImage(original);
                    originalPanel.showMat(original);
                    processedPanel.showMat(processed);
                } else {
                    System.err.println("Error: unable to read image");
                }
            } else {
                System.out.println("Error: camera is not open");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    /**
     * Process the sample images. Used for testing
     */
    private void processSampleImages() {
        for (int i = 1; i <= 10; i++) {
            System.out.println("Image " + i);
            processSampleImage("sample_images/image" + i + ".jpg");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex){
            }
        }
    }

    /**
     * Process a single sample image
     * @param filepath the path to the image
     */
    private void processSampleImage(String filepath) {
        Mat original = Highgui.imread(filepath);
        Mat processed = processImage(original);
        originalPanel.showMat(original);
        processedPanel.showMat(processed);
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
        System.out.println("Threshold: " + lowerBound + " " + upperBound);

        // Morph operations to filter out small blobs
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kMorphKernelSize, kMorphKernelSize));
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_OPEN, morphKernel);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat heirarchy = new Mat();
        Imgproc.findContours(thresh.clone(), contours, heirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Check the number of contours
        if (contours.size() > 2) {
            System.out.println("Warning: more than 2 contours found! Proceed with caution...");
        }

        // Process contours for targets
        MatOfInt hullIndices = new MatOfInt();
        List<Point> hullPointList = new ArrayList<>();
        MatOfPoint hull = new MatOfPoint();
        MatOfPoint2f approx2f = new MatOfPoint2f();
        List<MatOfPoint> polygons = new ArrayList<>();
        Rect target;
        List<Rect> horizontalTargets = new ArrayList<>();
        List<Rect> verticalTargets = new ArrayList<>();
        double distanceSum = 0;
        for (int i = 0; i < contours.size(); i++) {
            // Get the next contour
            MatOfPoint contour = contours.get(i);

            // Skip any contours too big or small to be the target
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea < kMinTargetArea || contourArea > kMaxTargetArea) {
                continue;
            }

            // Convex Hull
            Imgproc.convexHull(contour, hullIndices);   // hullIndices is the indices of the points
                                                        // in the original contour that form the convex hull

            List<Integer> hullIndexList = hullIndices.toList();
            hullPointList.clear();
            for (int j = 0; j < hullIndexList.size(); j++) {
                // Build the required points from the contour into a list
                hullPointList.add(contour.toList().get(hullIndexList.get(j)));
            }
            hull.fromList(hullPointList);   // Create a MatOfPoint from the list we built

            // Approximate a polygon from the convex hull
            MatOfPoint2f contour2f = new MatOfPoint2f(hull.toArray());
            double contourLength = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approx2f, contourLength * kApproxPolyTolerance, true);
            MatOfPoint polygon = new MatOfPoint(approx2f.toArray());
            polygons.add(polygon);

            // Find the bounding rectangle of the polygon
            target = Imgproc.boundingRect(polygon);

            // Draw the rectangle over the original image
            Core.rectangle(image, target.tl(), target.br(), new Scalar(255, 0, 0));

            // Check the orientation of the target
            boolean isHorizontal = target.width > target.height;
            if (isHorizontal) {
                horizontalTargets.add(target);
            } else {
                verticalTargets.add(target);
            }

            // Display information about target
            System.out.print("Target " + i + ": Vertices " + polygon.total() + " ");
            System.out.print("Position: (" + target.x + "," + target.y + ") ");
            System.out.print("Width: " + target.width + " Height: " + target.height + " ");
            System.out.print("Area: " + (target.width * target.height) + " ");
            double distance = getTargetDistance(target.width, isHorizontal);
            distanceSum += distance;
            System.out.print("Distance: " + distance + " ");
            if (isHorizontal) {
                System.out.println("Horizontal");
            } else {
                System.out.println("Vertical");
            }
        }

        // Draw the polygons in the output image
        Mat drawing = Mat.zeros(thresh.size(), thresh.type());
        for (int i = 0; i < polygons.size(); i++)
            Imgproc.drawContours(drawing, polygons, i, new Scalar(255, 255, 255), -1);

        // Estimate the distance to the target
        double distance = distanceSum / polygons.size();
        System.out.println("Estimated distance: " + distance);

        // Determine if the goal is hot or not
        System.out.println("Horizontal Targets: " + horizontalTargets.size() + " Vertical Targets: " + verticalTargets.size());
        boolean goalIsHot;
        if (verticalTargets.size() == 1 && horizontalTargets.size() == 1) {
            System.out.println("Goal is Hot");
            goalIsHot = true;
        } else if (verticalTargets.size() == 1 && horizontalTargets.isEmpty()) {
            System.out.println("Goal is not hot");
            goalIsHot = false;
        } else {
            System.out.println("Unsure if goal is hot or not...");
            goalIsHot = false;
        }

        System.out.println("Image processed in " + (System.currentTimeMillis() - startTime) + "ms");

        // Send data to the robot
        if (!debug) {
            table.putBoolean("goalIsHot", goalIsHot);
            table.putNumber("distance", distance);
        }

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

    public static void main(String[] args) {
        new LuNaCV().run();
    }
}
