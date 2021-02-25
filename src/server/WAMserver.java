package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.WAMBoard;

/**
 * Main class for the Whack-A-Mole Server
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMserver.java, Created Apr 9, 2019
 * 
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMserver {

	private ServerSocket server;
	private int rows;
	private int cols;
	private int numPlayers;
	private int time;
	private List<WAMPLayer> players;
	private List<WAMMole> moleThreads;

	private WAMBoard board;

	private Thread timerThread;


	public WAMserver(String[] args) {
		players = new ArrayList<>();
		moleThreads = new ArrayList<>();
		int port = Integer.parseInt(args[0]);
		rows = Integer.parseInt(args[1]);
		cols = Integer.parseInt(args[2]);
		numPlayers = Integer.parseInt(args[3]);
		time = Integer.parseInt(args[4]);
		board = new WAMBoard(rows, cols);

		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Failed to initialize server: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}


	public void run() {
		try {
			while (players.size() < numPlayers) {
				System.out.println("Waiting for player " + (players.size() + 1) + " of " + numPlayers + "...");
				Socket socket = server.accept();
				WAMPLayer pclient = new WAMPLayer(socket, this, players.size() + 1);
				pclient.connect(rows, cols, numPlayers, players.size());
				players.add(pclient);
				System.out.println("Player " + players.size() + " connected.");
			}
		} catch (IOException e) {
			// squash
		}

		play();
	}


	private void play() {
		// Start a thread for each mole spot
		// This is honestly a really dumb way to implement this, but why not
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				WAMMole mole = new WAMMole(this, r, c);
				moleThreads.add(mole);
				mole.start();
			}
		}
		// Start the timer
		timerThread = new Thread(new WAMTimer(this, time));
		timerThread.start();
	}


	public void moleDown(int row, int col) {
		int moleNumber = col + (cols * row);
		for (WAMPLayer p : players) {
			p.moleDown(moleNumber);
		}
		board.updateSpot(row, col, false);
	}


	public void moleUp(int row, int col) {
		int moleNumber = col + (cols * row);
		for (WAMPLayer p : players) {
			p.moleUp(moleNumber);
		}
		board.updateSpot(row, col, true);
	}


	public void updateTime(int secondsRemaining) {
		if (secondsRemaining < 0) {
			endGame();
		}
	}


	private void endGame() {
		try {
			for (WAMMole mole : moleThreads) {
				mole.die();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<WAMPLayer> winners = new ArrayList<>();

		for (WAMPLayer player : players) {
			if (winners.isEmpty())
				winners.add(player);
			else {
				if (winners.get(0).getScore() < player.getScore()) {
					winners.clear();
					winners.add(player);
				} else if (winners.get(0).getScore() == player.getScore()) {
					winners.add(player);
				}
			}
		}

		// Winner / Tie
		if (winners.size() > 1) {
			for (WAMPLayer winner : winners) {
				winner.tie();
				players.remove(winner);
			}
		} else {
			winners.get(0).win();
			players.remove(winners.get(0));
		}

		// Losers
		for (WAMPLayer loser : players) {
			loser.lose();
		}

		// close the server
		System.out.println("Shutting down server...");
		System.exit(0);

	}


	public void whack(int row, int col, WAMPLayer player) {
		boolean up = board.getSpot(row, col);
		if (up) {
			this.moleDown(row, col);
			player.addScore(1);
		} else {
			player.addScore(-1);
		}
	}


	public int getRows() {
		return rows;
	}


	public int getCols() {
		return cols;
	}


	public static void main(String[] args) {
		if (args.length != 5 || Integer.parseInt(args[3]) < 1) {
			System.err.print("Usage: Wrong arguments.");
		}
		WAMserver server = new WAMserver(args);
		server.run();
	}
}