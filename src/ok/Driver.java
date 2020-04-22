package ok;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

import com.github.sarxos.webcam.*;

public class Driver {
	
	public static final String TIME_LAPSE_SAVES = "D:/TimeLapses/";
	
	private class DimensionOption {
		private final Dimension dimension;
		public DimensionOption(Dimension dimension) {
			this.dimension = dimension;
		}
		public Dimension get() {
			return dimension;
		}
		@Override
		public String toString() {
			return "(" + dimension.width + ", " + dimension.height + ")";
		}
	}
	
	JFrame frame;
	JComboBox<Webcam> webcamSelect;
	JComboBox<DimensionOption> viewSizeSelect;
	JTextField delaySelect;
	
	Webcam[] webcams;
	DimensionOption[] viewSizes;
	
	Webcam selectedWebcam;
	Dimension selectedViewSize;
	
	volatile BufferedImage currentImage;
	String newFolderName;
	volatile int imageCounter;
	Thread thread;
	
	public void updateWebcamList() {
		List<Webcam> tempwebcams = Webcam.getWebcams();
		webcams = new Webcam[tempwebcams.size()];
		int index = 0;
		for(Webcam w : tempwebcams) {
			webcams[index++] = w;
		}
		if(webcamSelect != null) {
			DefaultComboBoxModel<Webcam> model = new DefaultComboBoxModel<>(webcams);
			webcamSelect.setModel(model);
		}
		selectWebcam(0);
	}
	public void updateViewSizeList() {
		Dimension[] sizes = new Dimension[] { 
				WebcamResolution.HD.getSize(),
				WebcamResolution.FHD.getSize(),
				WebcamResolution.UHD4K.getSize()
				};
		selectedWebcam.setCustomViewSizes(sizes);
		Dimension[] temp = selectedWebcam.getCustomViewSizes();
		viewSizes = new DimensionOption[temp.length];
		for(int i = 0; i < temp.length; i++) {
			viewSizes[i] = new DimensionOption(temp[i]);
		}
		if(viewSizeSelect != null) {
			DefaultComboBoxModel<DimensionOption> model = new DefaultComboBoxModel<>(viewSizes);
			viewSizeSelect.setModel(model);
		}
		selectViewSize(0);
	}
	public void selectWebcam(int index) {
		if(webcamSelect != null) {
			webcamSelect.setSelectedIndex(index);
		}
		selectedWebcam = webcams[index];
		System.out.println("Selected " + selectedWebcam);
		updateViewSizeList();
	}
	public void selectViewSize(int index) {
		if(viewSizeSelect != null) {
			viewSizeSelect.setSelectedIndex(index);
		}
		selectedViewSize = viewSizes[index].get();
		System.out.println("Selected " + selectedViewSize);
		selectedWebcam.setViewSize(selectedViewSize);
	}

	public Driver() {
		
//		String webcamString = "Available Webcams: ";
//		for (Webcam c : webcams) {
//			webcamString += "\n" + c.getName() + " with View Sizes: ";
//			for (Dimension dim : c.getViewSizes()) {
//				webcamString += "(" + dim.width + ", " + dim.height + ") ";
//			}
//		}
//		System.out.println(webcamString);

//		Webcam cam = Webcam.getDefault();
//		String possSizes = "View Sizes: ";
//		for (Dimension dim : cam.getViewSizes()) {
//			possSizes += "(" + dim.width + ", " + dim.height + ") ";
//		}
//		System.out.println(possSizes);
//		cam.setViewSize(new Dimension(640, 480));
//		cam.open();
//
//		try {
//			ImageIO.write(cam.getImage(), "png", new File("shot.png"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		frame = new JFrame("Time Lapse");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 347);
		frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				if(selectedWebcam != null) {
					selectedWebcam.close();
				}
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		webcamSelect = new JComboBox<Webcam>();
		webcamSelect.addActionListener(e -> {
			selectWebcam(webcamSelect.getSelectedIndex());
		});
		viewSizeSelect = new JComboBox<>();
		viewSizeSelect.addActionListener(e -> {
			selectViewSize(viewSizeSelect.getSelectedIndex());
		});
		delaySelect = new JTextField("1000", 10);
		JToggleButton startButton = new JToggleButton("Start");
		startButton.addActionListener(e -> {
			if(startButton.isSelected()) {
				try {
					int delay = Integer.parseInt(delaySelect.getText());
					startTimeLapse(delay);
				}
				catch(NumberFormatException ee) {
					System.out.println("Invalid delay.");
				}
				webcamSelect.setEnabled(false);
				viewSizeSelect.setEnabled(false);
				delaySelect.setEnabled(false);
				startButton.setText("Stop");
			}
			else {
				stopTimeLapse();
				webcamSelect.setEnabled(true);
				viewSizeSelect.setEnabled(true);
				delaySelect.setEnabled(true);
				startButton.setText("Start");
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(webcamSelect);
		buttonPanel.add(viewSizeSelect);
		buttonPanel.add(delaySelect);
		buttonPanel.add(startButton);

		JPanel imagePanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
			}
		};
		
		frame.add(buttonPanel, BorderLayout.NORTH);
		frame.add(imagePanel, BorderLayout.CENTER);
		updateWebcamList();
		frame.setVisible(true);

	}

	public void stopTimeLapse() {
		if(thread != null) {
			thread.interrupt();
		}
	}
	public void startTimeLapse(int delay) {
		setupFolder();
		imageCounter = 0;
		thread = new Thread(() -> {
			System.out.println("started");
			selectedWebcam.open();
			System.out.println(selectedWebcam);
			System.out.println(selectedWebcam.getViewSize());
			System.out.println(selectedWebcam.isImageNew());
			System.out.println(selectedWebcam.getImage());
			System.out.println(selectedWebcam.getFPS());
			System.out.println(selectedWebcam.getDevice());
			
			try {
				while(!Thread.interrupted()) {
					currentImage = selectedWebcam.getImage();
					String filename = String.format("image%04d.jpg", imageCounter);
					try {
						ImageIO.write(currentImage, "jpg", new File(newFolderName + filename));
					} catch (IOException ee) {
						ee.printStackTrace();
					}
					imageCounter++;
					frame.repaint();
					if(Thread.interrupted()) {
						break;
					}
					Thread.sleep(delay);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				System.out.println("stopped");
			}
			finally {
				selectedWebcam.close();
			}
		});
		thread.start();
	}
	public void setupFolder() {
		long name = System.currentTimeMillis() / 1000 - 1587594000L;
		newFolderName = TIME_LAPSE_SAVES + name + "/";
		File file = new File(newFolderName);
		file.mkdirs();
	}

	public static void main(String[] args) {
		new Driver();
	}
}
