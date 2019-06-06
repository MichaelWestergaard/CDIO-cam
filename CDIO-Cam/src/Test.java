import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Test{
	
	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		VideoCapture camera = new VideoCapture(0);
		
		if(!camera.isOpened()){
	        System.out.println("Camera Error");
	    }
	    else{
	        System.out.println("Camera OK?");
	    }

	    Mat frame = new Mat();
	    camera.read(frame);
	    
	    if(!frame.empty()) {
	    	Mat blurredImage = new Mat();
			Mat hsvImage = new Mat();
			Mat mask = new Mat();
			Mat morphOutput = new Mat();
			
			// remove some noise
			Imgproc.blur(frame, blurredImage, new Size(7, 7));
			
			// convert the frame to HSV
			Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
			
			// get thresholding values from the UI
			// remember: H ranges 0-180, S and V range 0-255
			Scalar minValues = new Scalar(0, 0, 0);
			Scalar maxValues = new Scalar(100, 100, 100);
			
			// threshold HSV image to select tennis balls
			Core.inRange(hsvImage, minValues, maxValues, mask);
			// show the partial output

			// morphological operators
			// dilate with large element, erode with small ones
			Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
			Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
			
			Imgproc.erode(mask, morphOutput, erodeElement);
			Imgproc.erode(morphOutput, morphOutput, erodeElement);
			
			Imgproc.dilate(morphOutput, morphOutput, dilateElement);
			Imgproc.dilate(morphOutput, morphOutput, dilateElement);
			
			// show the partial output
			
			// find the tennis ball(s) contours and show them
			frame = findAndDrawBalls(morphOutput, frame);
			
			Image bi = toBufferedImage(morphOutput);
	        File outputfile = new File("c:/new/saved-test.png");
	        ImageIO.write((RenderedImage) bi, "png", outputfile);
	        
	        Image bi2 = toBufferedImage(frame);
	        File outputfile2 = new File("c:/new/saved-real.png");
	        ImageIO.write((RenderedImage) bi2, "png", outputfile2);
	    }
	    
	    /*
	    for (int i = 0; i < 10; i++) {
	    	try {
		    	camera.read(frame);
		    	
		        Image bi = toBufferedImage(frame);
		        File outputfile = new File("c:/new/saved"+i+".png");
		        ImageIO.write((RenderedImage) bi, "png", outputfile);
		    } catch (IOException e) {
		    	System.out.println(e.getMessage());
		    }
		}
	    
	    Mat srcH = new Mat();
	    frame.convertTo(srcH, -1, 0.2, 0);
	    Image bi = toBufferedImage(srcH);
        File outputfile = new File("c:/new/contrast.jpg");
        ImageIO.write((RenderedImage) bi, "png", outputfile);
        */

	}
	
	private static Mat findAndDrawBalls(Mat maskedImage, Mat frame)
	{
		// init
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		
		// find contours
		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
		{
			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
			{
				Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
			}
		}
		
		return frame;
}

    public static Image toBufferedImage(Mat m){
          int type = BufferedImage.TYPE_BYTE_GRAY;
          if ( m.channels() > 1 ) {
              type = BufferedImage.TYPE_3BYTE_BGR;
          }
          int bufferSize = m.channels()*m.cols()*m.rows();
          byte [] b = new byte[bufferSize];
          m.get(0,0,b); // get all the pixels
          BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
          final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          System.arraycopy(b, 0, targetPixels, 0, b.length);  
          return image;

      }


}