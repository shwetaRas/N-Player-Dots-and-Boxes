import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import java.awt.Color;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.FileNotFoundException;
import java.util.Queue;
import java.util.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


interface ValueSubmittedListener 
{
    public void onSubmitted(String value);
}


interface ValueSubmittedListener2 
{
    public void onSubmitted2(String value, String value2);
}


class BoxGameClient implements Runnable, ValueSubmittedListener {

	public static final int PORT = 50000;
	public static final String HOST = "localhost";
	private static boolean close = false;
	private static Socket player_socket = null;
	private static PrintStream output_to_socket = null;
	private static DataInputStream read_from_socket = null;
	private String player_name = null;

	private List<ValueSubmittedListener2> listeners = new ArrayList<ValueSubmittedListener2>();


	public void client_call(BoxGameClient client) {
    
		BufferedReader player_input = null;

		/*
		 * create socket to connect with server and create output and input object for
		 * socket.
		 */
        String host ="";
        try {
            host = new Scanner(new File("serverconfig")).useDelimiter("\\A").next();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
		try {
			player_socket = new Socket(host, PORT);

			/* this is used to take input from player */
			player_input = new BufferedReader(new InputStreamReader(System.in));

			/* following two are provide output to server and read from server */
			output_to_socket = new PrintStream(player_socket.getOutputStream());
			read_from_socket = new DataInputStream(player_socket.getInputStream());
		} catch (UnknownHostException e) {
			System.out.println("Could not locate the server: " + host + " " + e);
		} catch (IOException e) {
			System.out.println("Could not set connection with server " + host + " " + e);
		}

		if (player_socket != null && output_to_socket != null && read_from_socket != null) {
			try {
		
                Dots dots = new Dots();
                dots.addListener(client);
                client.addListener(dots);

				new Thread(client).start();

                player_name = JOptionPane.showInputDialog(null,"Enter the player name: ");
				
				output_to_socket.println(player_name);



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
				if(read_server_reply.length() > 0) 
				{

                    System.out.println("server reply :" + read_server_reply.charAt(0));
					if((read_server_reply.charAt(0) == '%') || (read_server_reply.charAt(0) == '#') || (read_server_reply.charAt(0) == '$') || (read_server_reply.charAt(0) == '!'))
					{                        
						notifyListeners(read_server_reply, player_name);
					}
                    else if(read_server_reply.charAt(0) == '@')
                    {
                        output_to_socket.println(JOptionPane.showInputDialog(null,"Enter the matrix size : "));
                    }
                    else if(read_server_reply.charAt(0) == '^')
                    {
                        break;
                    }
				}

			}
			close = true;
		} catch (IOException e) {
			System.out.println("IOException while receiving server reply  " + e);
		}
	}


	public void onSubmitted(String value) {

		output_to_socket.println(value);
        
    }

	public void addListener(ValueSubmittedListener2 listener) {
    	System.out.println("Adding Listeners22222");
        listeners.add(listener);
    }

    private void notifyListeners(String val, String player_name) {
        for (ValueSubmittedListener2 listener : listeners) {
            System.out.println("Inside notifyListeners");
            listener.onSubmitted2(val, player_name);
        }
    }

}

class Dots extends JFrame implements MouseMotionListener, MouseListener, WindowListener, ValueSubmittedListener2 {


    //public int num = 5;
    public int num;
    public static final int gap=24;		//gap between dots
    public static final int size=4;		//dot size

    public static final int PLAYER_ONE=1;
    public static final int PLAYER_TWO=2;


    public static final Color PLAYER_ONE_COLOR=Color.RED; //color of player 1	
    public static final Color PLAYER_TWO_COLOR=Color.GREEN;	//color of player 2	
 


  private Connection[] horizontal_connections;	//array for all the connections which horizontally connect dots
    private Connection[] vertical_connections;	//array for all the connections which vertically connect dots
    private Box[] boxes; //array for all the boxes
    private Screen[] dots; //array for all the dots

    private Dimension dim; //dimension of the window screen

    private int click_x; //x coordinate of mouse click	
    private int click_y; //y coordinate of mouse click	

    private int mouse_x; //x coordinate of mouse location
    private int mouse_y; //y coordinate of mouse location	

    private int center_x; //x coordinate of center of the board/grid
    private int center_y; //y coordinate of center of the board/grid

    private int side; //length of the sides of the board/grid
    private int space;	//length of 1 dot plus 1 connection

    private int active_player; //current player

    private Color active_color;

    private List<ValueSubmittedListener> listeners = new ArrayList<ValueSubmittedListener>();

    private List<Color> colors = new ArrayList<Color>();

    private List<Integer> scores = new ArrayList<Integer>();

    private List<Integer> been_active = new ArrayList<Integer>();

    private List<Integer> dead_player = new ArrayList<Integer>();


    private String magic_str;

    private boolean mouse_enabled = true;



    public Dots()
    {
         super("Connect the Dots");
    }
           

    public void initDots(String value) {

        System.out.println("Inside init first");
        String[] splited = value.split("\\s+");
        num = Integer.parseInt(splited[1]);

        setSize(400, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMouseListener(this);
        addMouseMotionListener(this);
        addWindowListener(this);
        init_score();
        addColors();
        
        load_properties();
        load_dots();

        new_game();

        setVisible(true);
        System.out.println("Inside init last");

        
    }

    public void addColors()
    {
    	colors.add(Color.RED);
    	colors.add(Color.GREEN);
    	colors.add(Color.BLUE);
    	colors.add(Color.YELLOW);
    	colors.add(Color.ORANGE);
    	colors.add(Color.MAGENTA);
    	colors.add(Color.GRAY);
    	colors.add(Color.PINK);
    }

    public void init_score()
    {
    	scores.add(0);
    	scores.add(0);
    	scores.add(0);
    	scores.add(0);
    	scores.add(0);
    	scores.add(0);
    	scores.add(0);
    	scores.add(0);
    }

    public void addListener(ValueSubmittedListener listener) {
    	System.out.println("Adding Listeners");
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (ValueSubmittedListener listener : listeners) {
            listener.onSubmitted(magic_str);
        }
    }

    public void onSubmitted2(String value, String player_name) {

        System.out.println("Inside submitted22222222222 "+value.charAt(0));
        if(value.charAt(0) == '%')
            initDots(value);
    	else if(value.charAt(0) == '#')
			communicated_conn(value);
        else if(value.charAt(0) == '!')
            deadPlayer(value);
		else
			mouse_func(value, player_name);
        
    }

    public void deadPlayer(String value)
    {
        String[] splited = value.split("\\s+");
        dead_player.add(Integer.parseInt(splited[1]));
        repaint();
    }

    public void mouse_func(String value, String player_name) {

    	String[] splited = value.split("\\s+");


    	active_player = Integer.parseInt(splited[1]);
    	active_color = colors.get(active_player);

    	repaint();

    	if (!been_active.contains(active_player))
    	{
    		been_active.add(active_player);
    	}

    	System.out.println("Value : "+splited[2].trim());
    	System.out.println("player_name : "+player_name);

		if(splited[2].trim().equals(player_name.trim()))
		{
			mouse_enabled = true;
			System.out.println("player_name : "+player_name);
			System.out.println("mouse_enabled : "+mouse_enabled);
		}
		else
		{
			mouse_enabled = false;
			System.out.println("player_name : "+player_name);
			System.out.println("mouse_enabled : "+mouse_enabled);
		}
        
    }

    private void load_properties() {
    	

        click_x=0;
        click_y=0;
        mouse_x=0;
        mouse_y=0;

        dim=getSize();
        center_x=dim.width/2;
        center_y=(dim.height - 100) /2;

        side=num * size + (num - 1) * gap;	
    	space=size + gap;
    }

    private void load_connections() {

        horizontal_connections=new Connection[(num-1) * num];
        vertical_connections=new Connection[(num-1) * num];

        for(int i=0; i<horizontal_connections.length; i++) {
        	int col_x=i % (num-1);
        	int row_x=i / (num-1);
        	int hor_x=center_x - side / 2 + size + col_x * space;
        	int hor_y=center_y - side / 2 + row_x * space;
        	horizontal_connections[i]=Connection.create_connection(Connection.horizontal_conn, hor_x, hor_y);

            
            System.out.println("hor "+hor_x + " "+hor_y);
            

        	int col_y=i % num;
        	int row_y=i / num;
        	int vert_x=center_x - side / 2 + col_y * space;
        	int vert_y=center_y - side / 2 + size + row_y * space;
        	vertical_connections[i]=Connection.create_connection(Connection.vertical_conn, vert_x, vert_y);

            System.out.println("ver "+vert_x + " "+vert_y);
        }
    }

    private void load_boxes() {

    	boxes=new Box[(num-1) * (num-1)];

    	for(int i=0; i<boxes.length; i++) {
    		int col=i % (num-1);
    		int row=i / (num-1);

    		int box_x=center_x - side / 2 + size + col * space;
    		int box_y=center_y - side / 2 + size + row * space;

    		Connection[] hor_conn=new Connection[2];
    		hor_conn[0]=horizontal_connections[i];
    		hor_conn[1]=horizontal_connections[i + (num - 1)];

    		Connection[] ver_conn=new Connection[2];		
    		ver_conn[0]=vertical_connections[i + row];
    		ver_conn[1]=vertical_connections[i + row + 1];

    		boxes[i]=Box.createBox(box_x, box_y, hor_conn, ver_conn);
    	}
    }

    private void load_dots() 
    {

        int temp = 0;
        dots=new Screen[num * num];
        for(int row=0; row<num; row++) {
            for(int col=0; col<num; col++) {
                Screen dot=new Screen();
                dot.width=size;
                dot.height=size;
                dot.x=center_x - side/2 + col * space;
                dot.y=center_y - side/2 + row * space;
                dot.shape.addPoint(-size/2, -size/2);
                dot.shape.addPoint(-size/2, size/2);
                dot.shape.addPoint(size/2, size/2);
                dot.shape.addPoint(size/2, -size/2);
                int index=row * num + col;
                dots[index]=dot;
                temp = index;
            }
        }

        for (int i = 0; i< temp; i++)
        {
            if(i%num == 0)
                System.out.println();
            System.out.print(dots[i].x + " "+ dots[i].y+" : ");
        }
    }

    private void new_game() {
    
    	active_player = 0;
    	active_color = colors.get(active_player);
    	load_connections();
        load_boxes();
    }

    private Connection get_connection(int x, int y) {

    	

    	for(int i=0; i<horizontal_connections.length; i++) {
    		if(horizontal_connections[i].contain_point(x, y)) {
    			return horizontal_connections[i];
    		}
    	}

    	for(int i=0; i<vertical_connections.length; i++) {
    		if(vertical_connections[i].contain_point(x, y)) {
    			return vertical_connections[i];
    		}
    	}

    	return null;
    }

    private void communicated_conn(String val)
    {

    	String[] splited = val.split("\\s+");

    	
    	//0th value is pound
    	int box_row = Integer.parseInt(splited[1]);
    	int box_col = Integer.parseInt(splited[2]);
    	int part = Integer.parseInt(splited[3]);

        int box_number = box_row * (num-1) + box_col;
        if(part == 0)
            make_connection(boxes[box_number].horizontal_connections[0]);
        else if(part == 1)
            make_connection(boxes[box_number].vertical_connections[1]);
        else if (part == 2) 
            make_connection(boxes[box_number].horizontal_connections[1]);
        else if(part == 3)
            make_connection(boxes[box_number].vertical_connections[0]);   

        repaint();  
                 
    }

    private boolean[] box_status() {
    	boolean[] stat=new boolean[boxes.length];
    	for(int i=0; i<stat.length; i++) {
    		stat[i]=boxes[i].isBoxed();
    	}

    	return stat;
    }


    private boolean make_connection(Connection connection) {
    	Graphics g;

    	boolean new_box=false;

    	boolean[] boxStatusBeforeConnection=box_status();	

    	connection.connectionMade=true;


    	boolean[] boxStatusAfterConnection=box_status();

    	for(int i=0; i<boxes.length; i++) {
    		if(boxStatusAfterConnection[i]!=boxStatusBeforeConnection[i]) {
    			new_box=true;
    			boxes[i].player=active_player;
    			scores.set(boxes[i].player, scores.get(boxes[i].player) + 1);

    		}
    	}


    	for(int i=0; i<horizontal_connections.length; i++) {
    		if(horizontal_connections[i] == connection)
    		{
    			horizontal_connections[i].color = active_color;
    			break;
    		}

    	}

    	for(int i=0; i<vertical_connections.length; i++) {
    		if(vertical_connections[i] == connection)
    		{
    			vertical_connections[i].color = active_color;
    			break;
    		}

    	}

    	repaint();

    	gameover_check();

    	return new_box;
    }


    private void gameover_check() {

    	for(int i=0; i<boxes.length; i++) {
    		if(!boxes[i].isBoxed()) {
    			return;
    		}
    	}

    	String status = "";
    	int max_score = 0;
    	int winner = -1;

    	for (int i = 0; i< been_active.size(); i++)
    	{
    		status = status + "Player "+been_active.get(i)+" : "+scores.get(been_active.get(i))+"\n";
    		if(scores.get(been_active.get(i)) > max_score)
    		{
    			max_score = scores.get(been_active.get(i));
    			winner = been_active.get(i);
    		}
    	}

    	status = status + "Player "+winner+" wins!!!\n";

    	JOptionPane.showMessageDialog(this, status, "Game Over", JOptionPane.PLAIN_MESSAGE);

     }

    private char[] send_connection(Connection connection)
    {

        String send_str = null;
        int box_number_row = -1;
        int box_number_col = -1;
        int part  = -1;

        for (int i = 0; i < boxes.length; i++)
        {
            for (int j = 0; j< 2; j++)
            {
                if (connection == boxes[i].horizontal_connections[j])
                {
                    box_number_row = i/(num-1);
                    box_number_col = i%(num-1);
                    part = (j==0) ? 0 : 2;
                    break;
                }
                else if (connection == boxes[i].vertical_connections[j])
                {
                    box_number_row = i/(num-1);
                    box_number_col = i%(num-1);
                    part = (j==0) ? 3 : 1;
                    break;
                }
            }
        }


        send_str = box_number_row+" "+ box_number_col+" "+part;
        magic_str = send_str;
        return send_str.toCharArray();
    }


    private void clickhandler() {
    	Connection connection=get_connection(click_x, click_y);
    	if(connection==null)
    		return;

    	if(!connection.connectionMade) {

            send_connection(connection);
            notifyListeners();
    	}

    	repaint();
    }


    public void mouseMoved(MouseEvent event) {
    	if (!mouse_enabled) 
    	{
    		return;
  		}
    	mouse_x=event.getX();
    	mouse_y=event.getY();
    	repaint();
    }


    public void mouseDragged(MouseEvent event) {
    	if (!mouse_enabled) 
    	{
    		return;
  		}
    	mouseMoved(event);
    }


    public void mouseClicked(MouseEvent event) {
    	if (!mouse_enabled) 
    	{
    		return;
  		}
    	click_x=event.getX();
    	click_y=event.getY();

        System.out.println(click_x);
        System.out.println(click_y);

    	clickhandler();
    }


    public void windowClosed(WindowEvent e) {

    }

    public void windowClosing(WindowEvent e) {
        //This will only be seen on standard output.
        magic_str = "Quit";
        notifyListeners();

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // try {
        //     Thread.sleep(2000);
        // } catch (InterruptedException e1) {
        //     e1.printStackTrace();
        // }
   
    }



    public void windowOpened(WindowEvent e) {
        
    }

    public void windowIconified(WindowEvent e) {
        
    }

    public void windowDeiconified(WindowEvent e) {
      
    }

    public void windowActivated(WindowEvent e) {
       
    }

    public void windowDeactivated(WindowEvent e) {
        
    }

    public void windowGainedFocus(WindowEvent e) {
        
    }

    public void windowLostFocus(WindowEvent e) {
      
    }

    public void windowStateChanged(WindowEvent e) {

    }

    public void mouseEntered(MouseEvent event) {
    }


    public void mouseExited(MouseEvent event) {
    }


    public void mousePressed(MouseEvent event) {
    }


    public void mouseReleased(MouseEvent event) {
    }


    private void background_paint(Graphics g) {
    	g.setColor(Color.WHITE);
    	g.fillRect(0, 0, dim.width, dim.height);
    }


    private void paint_dots(Graphics g) {
    	for(int i=0; i<dots.length; i++) {
    		dots[i].render(g);
    	}
    }

    
    private void paint_connections(Graphics g) {
    	for(int i=0; i<horizontal_connections.length; i++) {

    		if(!horizontal_connections[i].connectionMade && active_player > -1) {
    			if(horizontal_connections[i].contain_point(mouse_x, mouse_y)) {
    				horizontal_connections[i].color=active_color;
                                
                                
    			} else {

    				horizontal_connections[i].color=Color.WHITE;
    			}
    		}
                        
    	
    		horizontal_connections[i].render(g);
    	}

    	for(int i=0; i<vertical_connections.length; i++) {

    		if(!vertical_connections[i].connectionMade && active_player > -1 ) {
    			if(vertical_connections[i].contain_point(mouse_x, mouse_y)) {                          
    				vertical_connections[i].color=active_color;
                            
                            
    			} else {
    				vertical_connections[i].color=Color.WHITE;
    			}
    		}
    		
    		vertical_connections[i].render(g);
        
        }
    }


    public void paint_boxes(Graphics g) {
    	for(int i=0; i<boxes.length; i++) {
    		if(boxes[i].isBoxed()) {
    			boxes[i].color = colors.get(boxes[i].player);    			
    		} else {
    			boxes[i].color=Color.WHITE;
    		}

    		boxes[i].render(g);
    	}
    }


    public void paint_status(Graphics g) {

    	String turn = "Its "+active_player+"'s turn";
    	g.setColor(Color.BLACK);
    	g.drawString(turn, 10, dim.height-65);


    	for (int i = 0; i< been_active.size(); i++)
    	{     
            int flag = 0;
            for (int j = 0; j< dead_player.size(); j++)
            {
                if (been_active.get(i) == dead_player.get(j))
                {
                    flag = 1;
                }
            }

            String status1 = null;
            if(flag == 0)
    		{ 
                status1 = "Player "+been_active.get(i)+" : "+scores.get(been_active.get(i));
            }
            else
            {
                status1 = "Player "+been_active.get(i)+" : "+scores.get(been_active.get(i))+"(Left)";
            }
    		g.setColor(colors.get(been_active.get(i)));
    		g.drawString(status1, 10, dim.height-50+(i*15));
    	}
    }


    public void update_screen(Graphics g) {
    	paint(g);
    }

    public void paint(Graphics g) {
    	

    	Image bufferImage=createImage(dim.width, dim.height);
    	Graphics bufferGraphics=bufferImage.getGraphics();

    	background_paint(bufferGraphics);
    	paint_dots(bufferGraphics);
    	paint_connections(bufferGraphics);
    	paint_boxes(bufferGraphics);
    	paint_status(bufferGraphics);

    	g.drawImage(bufferImage, 0, 0, null);
    }    
}


public class Test
{
	public static void main(String args[])
	{
		BoxGameClient client = new BoxGameClient();

		client.client_call(client);
	}
}