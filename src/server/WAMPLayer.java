package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import common.WAMProtocol;

/**
 * Manages a single player client
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMPLayer.java, Created Apr 9, 2019
 * 
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMPLayer implements WAMProtocol, Closeable {

	private Socket socket;
	private Scanner networkIn;
	private PrintStream networkOut;

	private boolean running = true;
	private WAMserver server;

	private int score;
	private int player;


	public WAMPLayer(Socket socket, WAMserver server, int player) {
		this.socket = socket;
		this.player = player;
		this.score = 0;
		try {
			networkIn = new Scanner(socket.getInputStream());
			networkOut = new PrintStream(socket.getOutputStream());

			this.server = server;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException(e);
		}

		new Thread(() -> this.run()).start();
	}


	private void run() {
		while (running) {
			try {

				String request = networkIn.next();
				String[] args = networkIn.nextLine().trim().split(" ");

				switch (request) {
				case WHACK:
					whack(Integer.parseInt(args[0]));
					break;
				default:
					break;
				}

			} catch (Exception e) {

			}
		}
	}


	private void whack(int moleNumber) {
		int row = moleNumber / server.getCols();
		int col = moleNumber % server.getCols();

		server.whack(row, col, this);
	}


	/**
	 * Sends the WELCOME message to the client
	 * 
	 * @param rows Number of rows on the board
	 * @param cols Number of columns on the board
	 */
	public void connect(int rows, int cols, int players, int playerNumber) {
		networkOut.println(WELCOME + " " + rows + " " + cols + " " + players + " " + playerNumber);
		networkOut.flush();
	}


	/**
	 * Sends the MOLE_DOWN message to the client
	 * moleNumber is calculated using
	 * moleNumber = col + (cols * row)
	 * 
	 * @param moleNumber The mole number
	 */
	public void moleDown(int moleNumber) {
		networkOut.println(MOLE_DOWN + " " + moleNumber);
		networkOut.flush();
	}


	/**
	 * Sends the MOLE_UP message to the client
	 * moleNumber is calculated using
	 * moleNumber = col + (cols * row)
	 * 
	 * @param moleNumber The mole number
	 */
	public void moleUp(int moleNumber) {
		networkOut.println(MOLE_UP + " " + moleNumber);
		networkOut.flush();
	}
	
	public void sendScores(String scores) {
		networkOut.println(scores);
		networkOut.flush();
	}
	
	public void win() {
		networkOut.println(GAME_WON);
		networkOut.flush();
	}
	
	public void lose() {
		networkOut.println(GAME_LOST);
		networkOut.flush();
	}
	
	public void tie() {
		networkOut.println(GAME_TIED);
		networkOut.flush();
	}
	
	public void score() {
		networkOut.println(SCORE + " " + score);
		networkOut.flush();
	}

	/**
	 * Gets the player's score
	 * 
	 * @return The score
	 */
	public int getScore() {
		return score;
	}


	/**
	 * Adds the specified amount to the score
	 * 
	 * @param amount Amount to add to the score
	 */
	public void addScore(int amount) {
		this.score += amount;
		score();
	}


	/**
	 * Gets the player's number
	 * 
	 * @return The player's number
	 */
	public int getPlayerNum() {
		return player;
	}


	@Override
	public void close() throws IOException {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
