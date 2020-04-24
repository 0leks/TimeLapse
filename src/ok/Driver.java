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
import com.github.sarxos.webcam.ds.dummy.*;

public class Driver {
	
	public static final String TIME_LAPSE_SAVES = "D:/TimeLapses/";
	public static final DimensionOption[] viewSizeOptions = new DimensionOption[] {
			new DimensionOption(WebcamResolution.HD.getSize()),
			new DimensionOption(WebcamResolution.FHD.getSize()),
			new DimensionOption(WebcamResolution.UHD4K.getSize())
	};
	
	JFrame frame;
	JComboBox<Webcam> webcamSelect;
	JComboBox<DimensionOption> viewSizeSelect;
	JTextField delaySelect;
	JToggleButton startButton;
	
	Webcam selectedWebcam;
	
	volatile BufferedImage currentImage;
	String newFolderName;
	volatile int imageCounter;
	Thread thread;
	volatile int delay;
	volatile boolean stopThread;
	
	volatile boolean getShot;
	
	public void updateWebcamList() {
		List<Webcam> tempwebcams = Webcam.getWebcams();
		Webcam[] webcams = new Webcam[tempwebcams.size()];
		int index = 0;
		for(Webcam w : tempwebcams) {
			webcams[index++] = w;
		}
		if(webcamSelect != null) {
			DefaultComboBoxModel<Webcam> model = new DefaultComboBoxModel<>(webcams);
			webcamSelect.setModel(model);
		}
	}
	public void updateViewSizeList() {
		Dimension[] sizes = new Dimension[] { 
				WebcamResolution.HD.getSize(),
				WebcamResolution.FHD.getSize(),
				WebcamResolution.UHD4K.getSize()
				};
		selectedWebcam.setCustomViewSizes(sizes);
		Dimension[] temp = selectedWebcam.getCustomViewSizes();
		DimensionOption[] viewSizes = new DimensionOption[temp.length];
		for(int i = 0; i < temp.length; i++) {
			viewSizes[i] = new DimensionOption(temp[i]);
		}
		if(viewSizeSelect != null) {
			DefaultComboBoxModel<DimensionOption> model = new DefaultComboBoxModel<>(viewSizes);
			viewSizeSelect.setModel(model);
		}
	}
//	public void selectWebcam(int index) {
//		stopThread();
//		if(webcamSelect != null) {
//			webcamSelect.setSelectedIndex(index);
//		}
//		if(selectedWebcam != null) {
//			selectedWebcam.close();
//		}
//		selectedWebcam = webcams[index];
//		selectedWebcam.getLock().unlock();
//		selectedWebcam.open();
//		System.out.println("Selected " + selectedWebcam);
//		updateViewSizeList();
//		startThread();
//	}
//	public void selectViewSize(int index) {
//		stopThread();
//		if(viewSizeSelect != null) {
//			viewSizeSelect.setSelectedIndex(index);
//		}
//		selectedViewSize = viewSizes[index].get();
//		System.out.println("Selected " + selectedViewSize);
//		
//		selectedWebcam.close();
//		selectedWebcam.setViewSize(selectedViewSize);
//		selectedWebcam.getLock().unlock();
//		selectedWebcam.open();
//		startThread();
//	}

	public Driver() {
		if(Webcam.getWebcams().size() == 0) {
			Webcam.setDriver(new WebcamDummyDriver(20));
		}
		
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
		frame.setSize(700, 500);
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
			stopThread();
			startThread();
			//selectWebcam(webcamSelect.getSelectedIndex());
		});
		viewSizeSelect = new JComboBox<>(viewSizeOptions);
		viewSizeSelect.addActionListener(e -> {
			stopThread();
			startThread();
			//selectViewSize(viewSizeSelect.getSelectedIndex());
		});
		delaySelect = new JTextField("1000", 10);
		JButton takeShot = new JButton("Shot");
		takeShot.addActionListener(e -> {
			getShot = true;
		});
		startButton = new JToggleButton("Start");
		startButton.addActionListener(e -> {
			if(startButton.isSelected()) {
				setupFolder();
				imageCounter = 0;
				try {
					delay = Integer.parseInt(delaySelect.getText());
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
		buttonPanel.add(takeShot);

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
		delay = 1000;
		startThread();
		frame.setVisible(true);
	}
	
	public Webcam getSelectedWebcam() {
		Webcam sel = (Webcam) webcamSelect.getSelectedItem();
		Dimension viewSize = ((DimensionOption)viewSizeSelect.getSelectedItem()).get();
		sel.setCustomViewSizes(new Dimension[] {viewSize});
		sel.setViewSize(viewSize);
		return sel;
	}
	
	public void startThread() {
		thread = new Thread(() -> {
			System.err.println("Thread Started");
			try {
				selectedWebcam = getSelectedWebcam();
				System.err.println("Opening " + selectedWebcam);
				selectedWebcam.open();
				long startTime = System.currentTimeMillis();
				while(!stopThread) {
					currentImage = selectedWebcam.getImage();
					frame.repaint();
					if(System.currentTimeMillis() - startTime > delay) {
						startTime += delay;
						if(startButton.isSelected() || getShot) {
							getShot = false;
							String filename = String.format("image%05d.jpg", imageCounter++);
							try {
								ImageIO.write(currentImage, "jpg", new File(newFolderName + filename));
							} catch (IOException ee) {
								ee.printStackTrace();
							}
						}
					}

					Thread.sleep(100);
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			finally {
				System.err.println("Closing " + selectedWebcam);
				selectedWebcam.close();
			}
			System.err.println("Thread Stopping");
			stopThread = false;
		});
		stopThread = false;
		thread.start();
	}
	public void stopThread() {
		stopThread = true;
		try {
			System.err.println("Joining Thread");
			thread.join();
			System.err.println("Joined Thread");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stopTimeLapse() {
		delay = 100;
	}
	public void startTimeLapse(int delay) {
		setupFolder();
		imageCounter = 0;
		this.delay = delay; 
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
