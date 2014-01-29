package org.lunatecs316.frc2014.vision;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
    private VideoCapture camera;
    private Mat frame;
    
    private int saveCount = 0;
    
    public void run() {
        System.loadLibrary("opencv_java247");
        camera = new VideoCapture("http://10.3.16.11/mjpg/video.mjpg");
        frame = new Mat();
        
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
        
        CVMatPanel imagePanel = new CVMatPanel(320, 240);
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
        File f = null;
        String filename = null;
        do {
            filename = "sample_images/image" + ++saveCount + ".jpg";
            System.out.println(filename);
            f = new File(filename);
        } while (f.isFile());
        Highgui.imwrite(filename, frame);
    }
    
    public static void main(String[] args) {
        new AcquireSampleImages().run();
    }
}
