package server;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runnable class to control mole up-and-down action
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMMole.java, Created Apr 11, 2019
 * 
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMMole extends Thread {

	private static float UP_CHANCE = 20; // Chance to go up
	private static float DOWN_CHANCE = 70; // Chance to go down (if up)

	private boolean running = true;
	private boolean up = false;
	private Random random;

	private WAMserver server;
	private int row;
	private int col;


	public WAMMole(WAMserver server, int row, int col) {
		this.server = server;
		this.row = row;
		this.col = col;
		random = new Random();
	}
	
	public void die() {
		server.moleDown(row, col);
		running = false;
	}


	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000)); // Run this routine randomly

				int chance = random.nextInt(100);
				if (up && chance < DOWN_CHANCE) {
					// System.out.println("Down " + row + " " + col);
					server.moleDown(row, col);
					up = false;
				} else if (!up && chance < UP_CHANCE) {
					// System.out.println("Up " + row + " " + col);
					server.moleUp(row, col);
					up = true;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
