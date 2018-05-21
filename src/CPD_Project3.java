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


class stateMatrix
{
	int m_size;
	int[][] matrix;

	void initMatrix(int size)
	{
	m_size = size;
	matrix = new int[m_size][m_size];

	for(int i = 0; i< m_size; i++)
	{
		for (int j = 0; j< m_size; j++)
		{
			matrix[i][j] = 0;
		}
	}
	}

	public synchronized boolean updateMatrix(int row, int col, int pos, int thread_id)
	{


		char[] charArray, charArray2;
		// int max_num = 5;
		int len2;
		int numCh = 7;
		int score = 0;

		
		String t_bin = Integer.toBinaryString(thread_id);
		int t_len = t_bin.length();
		

		

		int len = Integer.toBinaryString(matrix[row][col]).length();
		charArray = ("00000000" + Integer.toBinaryString(matrix[row][col])).substring(len).toCharArray();

		if(pos == 0)
		{			  
			charArray[numCh-0] = '1';


			if(row != 0)
			{
				len2 = Integer.toBinaryString(matrix[row-1][col]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(matrix[row-1][col])).substring(len2).toCharArray();
				  
				charArray2[numCh-2] = '1';		

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				matrix[row-1][col] = Integer.parseInt(String.valueOf(charArray2), 2);		
			}			
		
		}
		else if(pos == 1)
		{
				  
			charArray[numCh-1] = '1';
			

			if(col != m_size -1)
			{				

				len2 = Integer.toBinaryString(matrix[row][col+1]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(matrix[row][col+1])).substring(len2).toCharArray();
				  
				charArray2[numCh-3] = '1';	

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				matrix[row][col+1] = Integer.parseInt(String.valueOf(charArray2), 2);		
			}
			
		}
		else if(pos == 2)
		{
				  
			charArray[numCh-2] = '1';

			if(row != m_size-1)
			{

				len2 = Integer.toBinaryString(matrix[row+1][col]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(matrix[row+1][col])).substring(len2).toCharArray();
				  
				charArray2[numCh-0] = '1';	

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				matrix[row+1][col] = Integer.parseInt(String.valueOf(charArray2), 2);	
			}

		}
		else if(pos == 3)
		{

				  
			charArray[numCh-3] = '1';
			

			if(col != 0)
			{
				
				len2 = Integer.toBinaryString(matrix[row][col-1]).length();

				charArray2 = ("00000000" + Integer.toBinaryString(matrix[row][col-1])).substring(len2).toCharArray();
				  
				charArray2[numCh-1] = '1';		

				if(charArray2[numCh-0] == '1' && charArray2[numCh-1] == '1' && charArray2[numCh-2] == '1' && charArray2[numCh-3] == '1')
				{
					charArray2 = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				matrix[row][col-1] = Integer.parseInt(String.valueOf(charArray2), 2);			
				
			}
			
		}

		if(charArray[numCh-0] == '1' && charArray[numCh-1] == '1' && charArray[numCh-2] == '1' && charArray[numCh-3] == '1')
				{
					charArray = ("00000000"+t_bin+ "1111").substring(t_len+4).toCharArray();
					score = 1;
				}

				

		matrix[row][col] = Integer.parseInt(String.valueOf(charArray), 2);

		if (score == 0)
		{
			return false;
		}

		return true;

	}

	public void show()
	{
		for(int i = 0; i< m_size; i++)
		{
			for (int j = 0; j< m_size; j++)
			{
				System.out.print(matrix[i][j]+ " ");
			}
			System.out.println();
		}
	}

}


class playerThread extends Thread 
{

	private Socket player_socket = null;
	private final playerThread[] threads;
	private int max_no_player;
	private String player_name = null;
	private int player_id = -1;
	private PrintWriter printWriter = null;
	public static int matrix_size;
	public static boolean wait_var;

	public  stateMatrix s_m;

	public Queue<Integer> queue;

	private boolean activeFlag;
	public static final Object LOCK = new Object();
	public static final Object LOCK123 = new Object();



	public playerThread(Socket playerSocket, playerThread[] threads, int id, stateMatrix SM, Queue<Integer> queue1)
	{
		this.player_socket = playerSocket;
		this.threads = threads;
		this.player_id = id;
		this.s_m = SM;
		this.queue = queue1;
		activeFlag = false;

		queue.add(player_id);
		max_no_player = threads.length;
	}




	public void run()
	{
		String name_of_player, input_msg;
		boolean turn;
		try {
				/* read input from client via socket */
				BufferedReader bufferReaderSocket = new BufferedReader(
						new InputStreamReader(player_socket.getInputStream()));
				printWriter = new PrintWriter(player_socket.getOutputStream(), true);

				/* prompt to player to enter name */
					name_of_player = bufferReaderSocket.readLine();				

				if(this.player_id == 0)
				{

				printWriter.println("@ ");
				matrix_size = Integer.parseInt(bufferReaderSocket.readLine());
				s_m.initMatrix(matrix_size);

				wait_var = true;
				}

				System.out.println("matrix_size :"+matrix_size);
				System.out.println("wait_var :"+wait_var);
				synchronized(LOCK123)					
				{

					while(!wait_var) {
				         try {

				            LOCK123.wait();

				         } catch (InterruptedException e) {
				            e.printStackTrace();
				         }
			      	}	

					printWriter.println("% "+matrix_size);
					LOCK123.notifyAll();
				}
				
				
				
				

				/* Welcome message for new player */
				printWriter.println("Welcome " + name_of_player + "\nTo quit enter Quit\n");

				/* synchronize the threads */
				synchronized (this) {
					int i;
					i = 0;
					/* initialize the current thread with name */
					while (i < max_no_player) {
						if (threads[i] != null && threads[i] == this) {
							player_name = name_of_player;
							break;
						}
						i++;
					} // while

					i = 0;
					/* let know other players about the new player */
					while (i < max_no_player) {
						if (threads[i] != null && threads[i] != this) {
							threads[i].printWriter.println(player_name + " entered to play the game.");
						}
						i++;
					} // while
				}

				System.out.println("Player id : " + this.player_id );
				System.out.println("Queue Peek : " + queue.peek() );

				if (this.player_id == queue.peek())
					this.activeFlag = true;

				synchronized (this) {
						int i = 0;
						/* send message to all other players */
						while (i < max_no_player) {
							if (threads[i] != null && threads[i].player_name != null) {
								threads[i].printWriter.println("$ "+threads[queue.peek()].player_id+" "+threads[queue.peek()].player_name);

								}
							i++;
						}
				
					}


				while (true) {

					synchronized(LOCK)
					{	
					while(!this.activeFlag) {
				         try {

				            LOCK.wait();

				         } catch (InterruptedException e) {
				            e.printStackTrace();
				         }
			      	}					

					/* read from socket */
					input_msg = bufferReaderSocket.readLine();

					System.out.println(input_msg);

					/* Quit the connection */
					if (input_msg.equals("Quit")) {

						this.activeFlag = false;
						queue.poll();
						
						if (queue.size() > 0)
							threads[queue.peek()].activeFlag = true;
						int i = 0;
						/* send message to all other players */
						while (i < max_no_player) {
						if (threads[i] != null && threads[i] != this && threads[i].player_name != null) {
							threads[i].printWriter.println("! "+player_id);
							threads[i].printWriter.println("$ "+threads[queue.peek()].player_id+" "+threads[queue.peek()].player_name);

							}
						if (threads[i] == this)
						{
							threads[i].printWriter.println("^");
						}
						i++;
						}
						

						LOCK.notifyAll();

						break;
					}

					String[] parts = input_msg.split(" ");
					int[] int_arr = new int[parts.length];

					for(int n = 0; n < parts.length; n++) 
						{
				   			int_arr[n] = Integer.parseInt(parts[n]);
						}

					turn = s_m.updateMatrix(int_arr[0], int_arr[1], int_arr[2], player_id);
					s_m.show();

					if(turn == false)
					{
						this.activeFlag = false;
						queue.add(queue.poll());
						threads[queue.peek()].activeFlag = true;
						LOCK.notifyAll();	
					}				

				}

				synchronized (this) {
					int i = 0;
					/* send message to all other players */
					while (i < max_no_player) {
						if (threads[i] != null && threads[i].player_name != null) {
							threads[i].printWriter.println("# "+input_msg);
							threads[i].printWriter.println("$ "+threads[queue.peek()].player_id+" "+threads[queue.peek()].player_name);

							}
						i++;
					}
			
				}

				} // end of while


				/* after exit from listening, let other know player is leaving */
				synchronized (this) {
					int i = 0;
					/* let know other players, this player is leaving */
					while (i < max_no_player) {
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
					while (i < max_no_player) {
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
				System.out.println("Exception in creating player thread: " + e);
			}
	}

}


//main class is public
public class CPD_Project3
{

	private static final int max_no_player = 8;
	private static final playerThread[] threads = new playerThread[max_no_player];
	private static Socket player_socket = null;
	private static ServerSocket server_socket = null;
	public static final int PORT = 50000;
	public static final Queue<Integer> queue = new LinkedList<>();


	public static void main(String args[])
	{
		/*initialize a state matrix*/

		stateMatrix SM = new stateMatrix();

		/* Open a server socket */
		try {
			server_socket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Exception in creating server socket " + e);
		}

		System.out.println("Server is up and running...");

		/* create a player socket for each player and pass it to thread */
		while (true) {
			try {
				player_socket = server_socket.accept();
				int i = 0;
				while (i < max_no_player) {
					if (threads[i] == null) {

						(threads[i] = new playerThread(player_socket, threads, i, SM, queue)).start();
						break;
					}
					i++;
				}
			} catch (IOException e) {
				System.out.println("Exception in creating player socket " + e);
			}
		}	
	}
}