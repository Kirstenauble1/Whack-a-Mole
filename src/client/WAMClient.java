package client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import common.WAMBoard;
import common.WAMProtocol;

/**
 * Client side network interface for the Whack-A-Mole game
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMClient.java, Created Apr 9, 2019
 * 
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMClient implements WAMProtocol {

	private Socket clientSocket;
	private Scanner networkIn;
	private PrintStream networkOut;
	private WAMBoard board;
	private boolean running;

	private int rows;
	private int cols;
	
	private int moles;


	public WAMClient(String server, int port) {
		try {
			this.clientSocket = new Socket(server, port);
			this.networkIn = new Scanner(clientSocket.getInputStream());
			this.networkOut = new PrintStream(clientSocket.getOutputStream());
			this.running = true;

			// Wait for connect message
			String request = this.networkIn.next();
			String args[] = this.networkIn.nextLine().trim().split(" ");

			if (!request.equals(WAMProtocol.WELCOME)) {
				throw new RuntimeException("Expected WELCOME from server");
			}

			// Expected arguments: ROWS COLS PLAYERS PLAYER

			// Create the board
			rows = Integer.parseInt(args[0]);
			cols = Integer.parseInt(args[1]);
			board = new WAMBoard(rows, cols);
			startListener();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Called from the GUI when it is ready to start receiving messages
	 * from the server.
	 */
	public void startListener() {
		new Thread(() -> this.run()).start();
	}


	private void run() {
		board.setStatus("RUN");
		while (running) {
			try {
				String request = networkIn.next();
				String[] args = networkIn.nextLine().trim().split(" ");

				// System.out.println(request);
				switch (request) {
				case MOLE_DOWN:
					moleDown(Integer.parseInt(args[0]));
					break;
				case MOLE_UP:
					moleUp(Integer.parseInt(args[0]));
					break;
				case SCORE:
					board.updateScore(Integer.parseInt(args[0]));
					break;
				case GAME_WON:
					board.setStatus("You Won!");
					this.stop();
					break;
				case GAME_LOST:
					board.setStatus("You Lost!");
					this.stop();
					break;
				case GAME_TIED:
					board.setStatus("You Tied!");
					this.stop();
					break;
				case WHACK:
					board.setStatus(WHACK + args[0]);
				default:
					break;
				}
			}

			catch (NoSuchElementException nse) {
				// Looks like the connection shut down.
				this.stop();
			} catch (Exception e) {
				this.stop();
			}
		}
		this.close();
	}
	
	
	/**
	 * Sends a message to the server that the client whacked a position on the board.
	 * @param row  Row on the board
	 * @param col  Column on the board
	 */
	public void whack(int row, int col) {
		int moleNumber = col + (cols * row);
		networkOut.println(WHACK + " " + moleNumber);
		networkOut.flush();
	}

	public void updateTimeLeft(int time){
		board.updateTimeLeft(time);
	}

	/**
	 * Updates the GUI that a mole has gone down
	 * MoleNumber is calculated to:
	 * row = num // COLS
	 * col = num % COLS
	 * 
	 * @param moleNumber The mole number
	 */
	private void moleDown(int moleNumber) {
		int row = moleNumber / cols;
		int col = moleNumber % cols;
		board.updateSpot(row, col, false);
	}


	/**
	 *
	 * Updates the GUI that a mole has come up
	 * MoleNumber is calculated to:
	 * row = num // COLS
	 * col = num % COLS
	 * 
	 * @param moleNumber The mole number
	 * 
	 */
	private void moleUp(int moleNumber) {
		moles++;
		int row = moleNumber / cols;
		int col = moleNumber % cols;
		board.updateSpot(row, col, true);
	}

	/**
	 * @return Are we still running
	 */
	private synchronized boolean stillRunning() {
		return running;
	}


	/**
	 * Sets Running to false
	 */
	private synchronized void stop() {
		running = false;
	}


	/**
	 * Close the connection to the server
	 */
	public void close() {
		try {
			this.clientSocket.close();
		} catch (IOException ioe) {
			// squash
		}
	}
	
	
	/**
	 * Gets the number of moles that have popped up
	 * @return  Number of moles
	 */
	public int getNumMoles() {
		return moles;
	}


	public WAMBoard getBoard() {
		return board;
	}

}
