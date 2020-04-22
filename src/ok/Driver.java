package ok;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.github.sarxos.webcam.Webcam;

public class Driver {

  JFrame frame;
  BufferedImage currentImage;
  Driver() {
    System.out.println("asdf");
    
    Webcam cam = Webcam.getDefault();
    String possSizes = "View Sizes: ";
    for(Dimension dim : cam.getViewSizes()) {
      possSizes += "(" + dim.width + ", " + dim.height + ") ";
    }
    System.out.println(possSizes);
    cam.setViewSize(new Dimension(640, 480));
    cam.open();
    
    try {
      ImageIO.write(cam.getImage(), "png", new File("shot.png"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    frame = new JFrame("Time Lapse");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500, 500);
    
    JPanel imagePanel = new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
      }
    };
    
    Timer tim = new Timer(1000, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        
        currentImage = cam.getImage();
        try {
          ImageIO.write(currentImage, "png", new File("shot.png"));
        } catch (IOException ee) {
          // TODO Auto-generated catch block
          ee.printStackTrace();
        }
      }
      
    });
    
    
  }
  public static void main(String[] args) {
    new Driver();
  }
}
