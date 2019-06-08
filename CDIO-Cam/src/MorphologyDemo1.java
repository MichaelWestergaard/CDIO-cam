import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
public class MorphologyDemo1 {
    private static final String[] ELEMENT_TYPE = { "Rectangle", "Cross", "Ellipse" };
    private static final String[] MORPH_OP = { "Erosion", "Dilatation" };
    private static final int MAX_KERNEL_SIZE = 21;
    private Mat matImgSrc;
    private Mat matImgDst = new Mat();
    private int elementType = Imgproc.CV_SHAPE_RECT;
    private int kernelSize = 0;
    private boolean doErosion = true;
    private JFrame frame;
    private JLabel imgLabel;
    public MorphologyDemo1(String[] args) {
        String imagePath = args.length > 0 ? args[0] : "map15.jpg";
        matImgSrc = Imgcodecs.imread(imagePath);
        if (matImgSrc.empty()) {
            System.out.println("Empty image: " + imagePath);
            System.exit(0);
        }
        // Create and set up the window.
        frame = new JFrame("Erosion and dilatation demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(matImgSrc);
        addComponentsToPane(frame.getContentPane(), img);
        // Use the content pane's default BorderLayout. No need for
        // setLayout(new BorderLayout());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    private void addComponentsToPane(Container pane, Image img) {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        JComboBox<String> elementTypeBox = new JComboBox<>(ELEMENT_TYPE);
        elementTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> cb = (JComboBox<String>)e.getSource();
                if (cb.getSelectedIndex() == 0) {
                    elementType = Imgproc.CV_SHAPE_RECT;
                } else if (cb.getSelectedIndex() == 1) {
                    elementType = Imgproc.CV_SHAPE_CROSS;
                } else if (cb.getSelectedIndex() == 2) {
                    elementType = Imgproc.CV_SHAPE_ELLIPSE;
                }
                update();
            }
        });
        sliderPanel.add(elementTypeBox);
        sliderPanel.add(new JLabel("Kernel size: 2n + 1"));
        JSlider slider = new JSlider(0, MAX_KERNEL_SIZE, 0);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                kernelSize = source.getValue();
                update();
            }
        });
        sliderPanel.add(slider);
        JComboBox<String> morphOpBox = new JComboBox<>(MORPH_OP);
        morphOpBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> cb = (JComboBox<String>)e.getSource();
                doErosion = cb.getSelectedIndex() == 0;
                update();
            }
        });
        sliderPanel.add(morphOpBox);
        pane.add(sliderPanel, BorderLayout.PAGE_START);
        imgLabel = new JLabel(new ImageIcon(img));
        pane.add(imgLabel, BorderLayout.CENTER);
    }
    private void update() {
    	Mat edges = new Mat();
    	Mat copy = new Mat();
    	matImgSrc.copyTo(copy);
		List<MatOfPoint> contoursWalls = new ArrayList<MatOfPoint>();
		contoursWalls.clear();
    	
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        if (doErosion) {
            Imgproc.erode(matImgSrc, matImgDst, element);
        } else {
            Imgproc.dilate(matImgSrc, matImgDst, element);
        }
        
        //Imgproc.GaussianBlur(matImgSrc, matImgDst, new Size(3,3), 0);
				
		int lowThresh = 90;
		
		Imgproc.Canny(matImgDst, edges, lowThresh, lowThresh*3, 3, true);
		
		Imgproc.findContours(edges, contoursWalls, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		Imgproc.drawContours(copy, contoursWalls, -1, new Scalar(255,0,0));
		
		double areaLast = 0;
		Point[] verticesLast = null;
		RotatedRect rectLast = null;
		
		for (int i = 0; i < contoursWalls.size(); i++) {
			
			double area = Imgproc.contourArea(contoursWalls.get(i));
			MatOfPoint2f temp = new MatOfPoint2f(contoursWalls.get(i).toArray());
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			Imgproc.approxPolyDP(temp, approxCurve, Imgproc.arcLength(temp, true) * 0.04, true);
			
			System.out.println(approxCurve.total());
			
			if(area > 0.0) {
				System.out.println(area + " - " + areaLast);
				if(area > areaLast) {
					RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contoursWalls.get(i).toArray()));
					Point[] vertices = new Point[4];  
			        rect.points(vertices);
			        
			        verticesLast = vertices;
			        rectLast = rect;
				}
				areaLast = area;
			}
			
		}
		
		if(verticesLast != null && rectLast != null) {
			for(int j = 0; j < 4; j++) {
				Imgproc.line(copy, verticesLast[j], verticesLast[(j+1)%4], new Scalar(0,255,0), 3);
				Imgproc.putText(copy, "Corner", verticesLast[j], 2, 0.5, new Scalar(250,250,250));
			}
			Imgproc.putText(copy, "wall", new Point(rectLast.center.x, rectLast.center.y), 0, 1.5, new Scalar(0, 255, 0));
		}
        
        Image img = HighGui.toBufferedImage(copy);
        imgLabel.setIcon(new ImageIcon(img));
        frame.repaint();
    }
    public static void main(String[] args) {
        // Load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MorphologyDemo1(args);
            }
        });
    }
}