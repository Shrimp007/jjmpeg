/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpegdemos.
 * 
 * jjmpegdemos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpegdemos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jjmpegdemos.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.notzed.jjmpeg.videoscanner;

import au.notzed.jjmpeg.exception.AVDecodingError;
import au.notzed.jjmpeg.exception.AVIOException;
import au.notzed.jjmpeg.exception.AVInvalidCodecException;
import au.notzed.jjmpeg.exception.AVInvalidStreamException;
import au.notzed.jjmpeg.util.VideoFileChooser;
import au.notzed.jjmpeg.io.JJVideoScanner;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Demo of JJVideoScanner class
 * @author notzed
 */
public class Main implements ActionListener {

	/**
	 * Use a separate thread or run on the event loop with a timeout
	 */
	static final boolean useThread = true;
	String name;
	JFrame frame;
	BufferedImage image;
	JLabel label;
	JJVideoScanner vs;
	Timer timer;

	public Main(String name) {
		this.name = name;

		frame = new JFrame("Video Frames");
		label = new JLabel();
		//label.setPreferredSize(new Dimension(640, 480));
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	void start() {
		try {
			vs = new JJVideoScanner(name);

			image = vs.createImage();
			label.setIcon(new ImageIcon(image));
			frame.pack();

			if (!useThread) {
				timer = new Timer(20, this);
				timer.start();
			} else {
				new Thread(run).start();
			}

		} catch (AVInvalidCodecException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVInvalidStreamException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVIOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	static File chooseFile() {
		VideoFileChooser fc = new VideoFileChooser();
		if (fc.showOpenDialog(null) == fc.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				String name;

				if (args.length == 0) {
					File f = chooseFile();
					if (f == null) {
						return;
					}

					name = f.getPath();
				} else {
					name = args[0];
				}

				new Main(name).start();
			}
		});
	}
	// Calling from a thread
	Runnable run = new Runnable() {

		public void run() {
			long pts;

			try {
				do {
					//ByteBuffer raw = vs.readFrame();
					//pts = vs.getPTS();
					pts = vs.readFrame(image);
					label.repaint();

					//System.out.println("pts = " + pts);
					if (pts == -1) {
						//if (raw == null) {
						System.out.println("end of file, restart " + pts);
						vs.dispose();
						vs = null;
						vs = new JJVideoScanner(name);
						pts = 0;
					}

					Thread.sleep(20);
				} while (pts != -1);
			} catch (AVInvalidStreamException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InterruptedException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (AVDecodingError ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (AVIOException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (Exception ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	};

	// calling from the event loop
	public void actionPerformed(ActionEvent e) {
		try {
			long pts = vs.readFrame(image);

			if (pts == -1) {
				System.out.println("eof?? " + pts);
				System.out.println("end of file, restart " + pts);
				vs.dispose();
				vs = null;
				vs = new JJVideoScanner(name);
			}
			label.repaint();
		} catch (AVInvalidCodecException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVInvalidStreamException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVDecodingError ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVIOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}