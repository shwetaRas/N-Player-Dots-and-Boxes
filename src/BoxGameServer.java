
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.lang.String;

public class BoxGameServer {

	private static final int max_no_player = 8;
	private static final playerThread[] threads = new playerThread[max_no_player];
	private static Socket player_socket = null;
	private static ServerSocket server_socket = null;
	public static final int PORT = 50000;

	public static int[][] state_matrix = new int[5][5];
	
	public static void main(String args[]) {

		/* Open a server socket */
		try {
			server_socket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Execetion in creating server socket " + e);
		}

		System.out.println("Server is up and running...");

		/*initialize state matrix to 0*/
		for (int i = 0; i< 5; i++)
		{
			for (int j = 0; j< 5; j++)
			{
				state_matrix[i][j] = 0;
			}
		}

		/* create a player socket for each player and pass it to thread */
		while (true) {
			try {
				player_socket = server_socket.accept();
				int i = 0;
				while (i < max_no_player) {
					// if(i == 0)
					// 	activeThread = threads[0];
					if (threads[i] == null) {

						(threads[i] = new playerThread(player_socket, threads, i, state_matrix)).start();
						break;
					}
					i++;
				}
			} catch (IOException e) {
				System.out.println("Execetion in creating player socket " + e);
			}
		}
	}
}

/* player thread class */
class playerThread extends Thread {

	private Socket player_socket = null;
	private String player_name = null;
	private int player_id = -1;
	private int max_no_player;
	private PrintWriter printWriter = null;
	private final playerThread[] threads;

	public static int[][] state_matrix = new int[5][5];
	public static Queue<playerThread> q = new LinkedList<>();
	

	/* initialize the class member */
	public playerThread(Socket playerSocket, playerThread[] threads, int id, int[][] state_matrix1) {
		max_no_player = threads.length;
		this.player_id = id;
		this.player_socket = playerSocket;
		this.threads = threads;
		state_matrix = state_matrix1;
		q.add(this);
		//activeThread = q.peek();
	} 

	public int[][] updateMatrix(int[][] state_matrix, int row, int col, int pos, int thread_id)
	{
		String new_value1, new_value2;
		char[] charArray, charArray2;
		int max_num = 5;
		int len2;
		int numCh = 7;
		int score = 0;

		
		String t_bin = Integer.toBinaryString(thread_id);
		int t_len = t_bin.length();

		

		int len = Integer.toBinaryString(state_matrix[row][col]).length();
		charArray = ("00000000" + Integer.toBinaryString(state_matrix[row][col])).substring(len).toCharArray();

		if(pos == 0)
		{			  
			charArray[numCh-0] = '1';


			if(row != 0)
			{
				len2 = Integer.toBinaryString(state_matrix[row-1][col]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(state_matrix[row-1][col])).substring(len2).toCharArray();
				  
				charArray2[numCh-2] = '1';		

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				state_matrix[row-1][col] = Integer.parseInt(String.valueOf(charArray2), 2);		
			}			
		
		}
		else if(pos == 1)
		{
				  
			charArray[numCh-1] = '1';
			

			if(col != max_num -1)
			{				

				len2 = Integer.toBinaryString(state_matrix[row][col+1]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(state_matrix[row][col+1])).substring(len2).toCharArray();
				  
				charArray2[numCh-3] = '1';	

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				state_matrix[row][col+1] = Integer.parseInt(String.valueOf(charArray2), 2);		
			}
			
		}
		else if(pos == 2)
		{
				  
			charArray[numCh-2] = '1';

			if(row != max_num-1)
			{

				len2 = Integer.toBinaryString(state_matrix[row+1][col]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(state_matrix[row+1][col])).substring(len2).toCharArray();
				  
				charArray2[numCh-0] = '1';	

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				state_matrix[row+1][col] = Integer.parseInt(String.valueOf(charArray2), 2);	
			}

		}
		else if(pos == 3)
		{

				  
			charArray[numCh-3] = '1';
			

			if(col != 0)
			{
				
				len2 = Integer.toBinaryString(state_matrix[row][col-1]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(state_matrix[row][col-1])).substring(len2).toCharArray();
				  
				charArray2[numCh-1] = '1';		

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				state_matrix[row][col-1] = Integer.parseInt(String.valueOf(charArray2), 2);			
				
			}
			
		}

		if(charArray[numCh-0] == '1' && charArray[numCh-1] == '1' && charArray[numCh-2] == '1' && charArray[numCh-3] == '1')
				{
					charArray = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				

		state_matrix[row][col] = Integer.parseInt(String.valueOf(charArray), 2);

		// if (score == 0)
		// {
		// 	q.add(q.poll());
		// 	this.printWriter.println("After Update" +q.peek());
		// 	// activeThread = q.peek();
		// }

		

		return state_matrix;
	}


	public void run() {
		String name_of_player, input_msg;
		playerThread[] threads = this.threads;
		int total_player = this.max_no_player;
		int max_num = 5;

		try {
			/* read input from client via socket */
			BufferedReader bufferReaderSocket = new BufferedReader(
					new InputStreamReader(player_socket.getInputStream()));
			printWriter = new PrintWriter(player_socket.getOutputStream(), true);

			/* prompt to player to enter name */
			printWriter.println("Enter your name.");
			name_of_player = bufferReaderSocket.readLine();

			/* Welcome message for new player */
			printWriter.println("Welcome " + name_of_player + "\nTo quit enter Quit\n");
			printWriter.println("This is your state matrix : \n");

			for (int i = 0; i< 5; i++)
			{
				for (int j = 0; j< 5; j++)
				{
					printWriter.print(state_matrix[i][j]);
				}
				printWriter.println();
			}


			/* synchronize the threads */
			synchronized (this) {
				int i;
				i = 0;
				/* initialize the current thread with name */
				while (i < total_player) {
					if (threads[i] != null && threads[i] == this) {
						player_name = name_of_player;
						break;
					}
					i++;
				} // while

				i = 0;
				/* let know other players about the new player */
				while (i < total_player) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].printWriter.println(player_name + " entered to play the game.");
					}
					i++;
				} // while
			}


			/* listen from socket */
			while (true) {

				/* read from socket */
				input_msg = bufferReaderSocket.readLine();

				/* Quit the connection */
				if (input_msg.equals("Quit")) {
					break;
				}

				String[] parts = input_msg.split(" ");
				int[] int_arr = new int[parts.length];

				for(int n = 0; n < parts.length; n++) 
					{
			   			int_arr[n] = Integer.parseInt(parts[n]);
					}

				state_matrix = updateMatrix(state_matrix, int_arr[0], int_arr[1], int_arr[2], this.player_id);

				/* This part send one player message to all other players */
				synchronized (this) {
					int i = 0;
					/* send message to all other players */
					while (i < total_player) {
						if (threads[i] != null && threads[i].player_name != null) {
							threads[i].printWriter.println(player_name + ": " + input_msg);

							for (int s = 0; s < max_num; s++)
							{
								for (int r = 0; r < max_num; r++)
								{
									threads[i].printWriter.print(state_matrix[s][r]+" ");
								}
								threads[i].printWriter.println();
							}
			
						}
						i++;
					}
			
				}

				// }
			} // end of while


			/* after exit from listening, let other know player is leaving */
			synchronized (this) {
				int i = 0;
				/* let know other players, this player is leaving */
				while (i < total_player) {
					if (threads[i] != null && threads[i] != this && threads[i].player_name != null) {
						threads[i].printWriter.println(player_name + " is leaving the game.");
					}
					i++;
				}
			}

			/* initialized the current thread with null, so that other player can join */
			synchronized (this) {
				int i = 0;
				/* initialize current thread as empty */
				while (i < total_player) {
					if (threads[i] == this) {
						threads[i] = null;
					}
					i++;
				}
			}

			/* here closing input, output and socket conneciton */
			bufferReaderSocket.close();
			printWriter.close();
			player_socket.close();
		} catch (IOException e) {
			System.out.println("Execetion in creating player thread: " + e);
		}
	}
}