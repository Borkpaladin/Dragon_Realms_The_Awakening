package com.keypuncher.awakening;

import javax.swing.*;

public class AwakeningApplet extends JApplet {
	
	/**
	 *  Copyright<c> Owen Lennon 2013
	 */
	private static final long serialVersionUID = 6770890113936512201L;

	private Awakening awakening;
	
	public void start() {
		setSize(Awakening.WIDTH, Awakening.HEIGHT);
		awakening = new Awakening(true);
		add(awakening);
		awakening.start();
	}
	
}
