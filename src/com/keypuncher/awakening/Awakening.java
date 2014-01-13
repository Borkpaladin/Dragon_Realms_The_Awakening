package com.keypuncher.awakening;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;

import javax.swing.*;

public class Awakening extends Canvas implements Runnable {

	/**
	 * Copyright<c> Owen Lennon 2013
	 */
	private static final long serialVersionUID = 1650614265154853998L;
	private JFrame frame;
	public static final String title = "Dragon Realms : The Awakening";
	public static final String version = "Alpha v0.1";

	private BufferStrategy bs;
	private BufferedImage buffer;
	private boolean buffered = false;

	public static final int WIDTH = 640, HEIGHT = 336;
	private boolean hasFocus = true;
	private boolean running = false;

	public static Font font;

	private boolean[] keys = new boolean[KeyEvent.KEY_LAST];
	private Controller controls;
	private Game game;

	public Awakening(boolean applet) {
		if (applet) {
			init();
		} else {
			init();
			frame = new JFrame(title);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(this);
			frame.setResizable(false);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			new Thread(this).start();
		}
	}

	private void init() {
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, getClass()
					.getResourceAsStream("font.ttf"));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		game = new Game();
		controls = new Controller();

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
		addFocusListener(controls);
		addKeyListener(controls);
		addMouseListener(controls);
		addMouseMotionListener(controls);
	}

	/** Required for applet version **/
	public void start() {
		new Thread(this).start();
	}

	public void run() {
		running = true;
		Graphics2D g;
		while (running) {
			if (!buffered) {
				buffered = true;
				createBufferStrategy(2);
				bs = getBufferStrategy();
			}

			// Clear screen
			g = (Graphics2D) buffer.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, WIDTH, HEIGHT);

			// Update & Render
			game.render(g);

			if (hasFocus) {
				game.update();

			} else {
				g.setColor(new Color(0f, 0f, 0f, .8f));
				g.fillRect(0, 0, WIDTH, HEIGHT);
				g.setColor(Color.white);
				g.setFont(font.deriveFont(16f));
				g.drawString("-- Click to Focus! --", ((Game.CHARSPERWIDTH)
						* Game.CHARWIDTH / 2)
						- "-- Click to Focus! --".length()
						* (Game.CHARWIDTH / 2), Game.CHARHEIGHT * 12);
			}

			// Blit to screen
			bs.getDrawGraphics().drawImage(buffer, 0, 0, null);
			bs.show();

			// Sleep for 16ms to aim for 60fps
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Controller implements KeyListener, FocusListener,
			MouseListener, MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void focusGained(FocusEvent e) {
			hasFocus = true;
		}

		@Override
		public void focusLost(FocusEvent e) {
			hasFocus = false;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			keys[e.getKeyCode()] = true;
			game.input(e);
			game.input(keys);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keys[e.getKeyCode()] = false;
			game.input(keys);
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

	}

	public static void main(String[] args) {
		new Awakening(false);
	}

}