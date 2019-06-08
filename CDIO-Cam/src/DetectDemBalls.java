import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
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
            /*Mat frameHSV = new Mat();
            Imgproc.cvtColor(imgCapture, frameHSV, Imgproc.COLOR_BGR2HSV);
            Mat imgThresh = new Mat();
            Core.inRange(frameHSV, lower, upper, imgThresh);
            
            //Core.addWeighted(imgCapture, 1.5, imgCapture, -0.5, 0, imgCapture);
            
            //Core.addWeighted(imgThresh, 1.5, imgThresh, -0.5, 0, imgThresh);

            Imgproc.adaptiveThreshold(imgThresh, imgThresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
            */
            

    		Mat dst = new Mat();
    		Mat edges = new Mat();
    		List<MatOfPoint> contoursWalls = new ArrayList<MatOfPoint>();
    		
    		Imgproc.GaussianBlur(imgCapture, dst, new Size(3,3), 0);
    		
    		Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * 8 + 1, 2 * 8 + 1), new Point(8, 8));
    		
    		int lowThresh = 90;

    		Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, element);
    		Imgproc.Canny(dst, edges, lowThresh, lowThresh*3, 3, true);

    		
    		Imgproc.findContours(edges, contoursWalls, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

    		double areaLast = 0;
    		Point[] verticesLast = null;
    		RotatedRect rectLast = null;
    		
    		for (int i = 0; i < contoursWalls.size(); i++) {
    			
    			MatOfPoint2f temp = new MatOfPoint2f(contoursWalls.get(i).toArray());
    			MatOfPoint2f approxCurve = new MatOfPoint2f();
    			Imgproc.approxPolyDP(temp, approxCurve, Imgproc.arcLength(temp, true) * 0.04, true);
    			
    			RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contoursWalls.get(i).toArray()));
    			Point[] vertices = new Point[4];  
    	        rect.points(vertices);
    	        
    			double area = rect.size.width * rect.size.height;
    			
    			if(area > areaLast) {
    	        	verticesLast = vertices;
    		        rectLast = rect;
    				areaLast = area;
    			}
    			
    		}
    		
    		if(verticesLast != null && rectLast != null) {
    			for(int j = 0; j < 4; j++) {
    				Imgproc.line(imgCapture, verticesLast[j], verticesLast[(j+1)%4], new Scalar(0,255,0));
    				Imgproc.putText(imgCapture, "Corner", verticesLast[j], 2, 0.5, new Scalar(250,250,250));
    			}
    			Imgproc.putText(imgCapture, "wall", new Point(rectLast.center.x, rectLast.center.y), 0, 1.5, new Scalar(0, 255, 0));
    		}
            
            imgCaptureLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(imgCapture)));
            imgDetectionLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(dst)));
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
