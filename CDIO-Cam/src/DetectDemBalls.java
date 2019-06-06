import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class DetectDemBalls {
	
	private VideoCapture cap;
    private Mat matFrame = new Mat();
    private JFrame frame;
    private JLabel imgCaptureLabel;
    private JLabel imgDetectionLabel;
    private CaptureTask captureTask;
    private Scalar lower = new Scalar(0, 0, 250);
    private Scalar upper = new Scalar(180, 100, 255);
	
	private class CaptureTask extends SwingWorker<Void, Mat> {
        @Override
        protected Void doInBackground() {
            Mat matFrame = new Mat();
            while (!isCancelled()) {
                if (!cap.read(matFrame)) {
                    break;
                }
                publish(matFrame.clone());
            }
            return null;
        }
        @Override
        protected void process(List<Mat> frames) {
            Mat imgCapture = frames.get(frames.size() - 1);
            Mat frameHSV = new Mat();
            Imgproc.cvtColor(imgCapture, frameHSV, Imgproc.COLOR_BGR2HSV);
            Mat imgThresh = new Mat();
            Core.inRange(frameHSV, lower, upper, imgThresh);
            
            //Core.addWeighted(imgCapture, 1.5, imgCapture, -0.5, 0, imgCapture);
            
            //Core.addWeighted(imgThresh, 1.5, imgThresh, -0.5, 0, imgThresh);

            Imgproc.adaptiveThreshold(imgThresh, imgThresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
            
            imgCaptureLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(imgCapture)));
            imgDetectionLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(imgThresh)));
            frame.repaint();
        }
    }
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	new DetectDemBalls(args);
            }
        });
	}
	
	public DetectDemBalls(String[] args) {
		
        cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            System.err.println("Cannot open camera");
            System.exit(0);
        }
        if (!cap.read(matFrame)) {
            System.err.println("Cannot read camera stream.");
            System.exit(0);
        }
        // Create and set up the window.
        frame = new JFrame("Video");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(matFrame);
        
        JPanel framePanel = new JPanel();
        imgCaptureLabel = new JLabel(new ImageIcon(img));
        framePanel.add(imgCaptureLabel);
        imgDetectionLabel = new JLabel(new ImageIcon(img));
        framePanel.add(imgDetectionLabel);
        frame.getContentPane().add(framePanel, BorderLayout.CENTER);
        
        frame.pack();
        frame.setVisible(true);
        captureTask = new CaptureTask();
        captureTask.execute();
	}	
}
