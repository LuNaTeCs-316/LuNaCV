package org.lunatecs316.frc2014.vision;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 * Utility to capture sample images from the camera and save them to the disk
 * @author Domenic
 */
public class AcquireSampleImages {
    public static final String OPENCV_LIB_PATH = "C:\\OpenCV\\build\\java\\x64\\opencv_java247.dll";
    public static final String TEAM_IP = "10.3.16.11";
    
    private VideoCapture camera;
    private Mat frame;
    
    private int saveCount = 0;
    
    public void run() {
        System.load(OPENCV_LIB_PATH);
        camera = new VideoCapture("http://" + TEAM_IP + "/mjpg/video.mjpg");
        frame = new Mat();
        
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
        
        CVMatPanel imagePanel = new CVMatPanel(640, 480);
        window.getContentPane().add(imagePanel);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                saveImage();
            }
        });
        window.getContentPane().add(saveButton);
        
        window.pack();
        window.setVisible(true);
        
        while (true) {
            if (camera.isOpened()) {
                if (camera.read(frame)) {
                    imagePanel.showMat(frame);
                } else {
                    System.err.println("Error: unable to read image from camera");
                }
            } else {
                System.err.println("Error: camera not open");
            }
        }
    }
    
    public void saveImage() {
        Highgui.imwrite("image" + ++saveCount + ".jpg", frame);
    }
    
    public static void main(String[] args) {
        AcquireSampleImages program = new AcquireSampleImages();
        program.run();
    }
}
