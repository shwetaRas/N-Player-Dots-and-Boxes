
/* Player side program */
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;

public class BoxGameClient implements Runnable {

	public static final int PORT = 50000;
	public static final String HOST = "localhost";
	private static boolean close = false;
	private static Socket player_socket = null;
	private static PrintStream output_to_socket = null;
	private static DataInputStream read_from_socket = null;

	// public static int state_matrix[5][5];
	// public static Queue<Thread> q;


	public static void main(String[] args) {
		BufferedReader player_input = null;

		/*
		 * create socket to connect with server and create output and input object for
		 * socket.
		 */
		try {
			player_socket = new Socket(HOST, PORT);

			/* this is used to take input from player */
			player_input = new BufferedReader(new InputStreamReader(System.in));

			/* following two are provide output to server and read from server */
			output_to_socket = new PrintStream(player_socket.getOutputStream());
			read_from_socket = new DataInputStream(player_socket.getInputStream());
		} catch (UnknownHostException e) {
			System.out.println("Could not locate the server: " + HOST + " " + e);
		} catch (IOException e) {
			System.out.println("Could not set connection with server " + HOST + " " + e);
		}

		if (player_socket != null && output_to_socket != null && read_from_socket != null) {
			try {

				/*game window should appear*/
				/* Creating a thread for reading from server. */
				new Thread(new BoxGameClient()).start();
				while (!close) {
					output_to_socket.println(player_input.readLine().trim());
				}
				System.out.println("Disconnecting from server...");
				/* closing output, input and socket */
				output_to_socket.close();
				read_from_socket.close();
				player_socket.close();
				System.out.println("Disconnected.");
			} catch (IOException e) {
				System.out.println("Exception in creating thread  " + e);
			}
		}
	}

	public void run() {

		String read_server_reply;

		/* receive server reply and show output to the terminal */
		try {
			while ((read_server_reply = read_from_socket.readLine()) != null) {
				System.out.println(read_server_reply);
			}
			close = true;
		} catch (IOException e) {
			System.out.println("IOException while receiving server reply  " + e);
		}
	}
}