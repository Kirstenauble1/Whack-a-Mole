package server;

/**
 * Timer runnable for the game timer
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMTimer.java, Created Apr 24, 2019
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMTimer implements Runnable {

	private WAMserver server;
	private int seconds;
	
	public WAMTimer(WAMserver server, int seconds) {
		this.server = server;
		this.seconds = seconds;
	}
	
	@Override
	public void run() {
		while (seconds > -1) {
			seconds--;
			server.updateTime(seconds);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
