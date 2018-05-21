// This is the main class
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import java.awt.Color;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Dots extends JFrame implements MouseMotionListener, MouseListener {

   // public  int num=Integer.parseInt(JOptionPane.showInputDialog(null,"Enter the number of dots 'n' to form a nxn grid:"));	
    public int num = 5;
    public static final int gap=24;		//gap between dots
    public static final int size=4;		//dot size

    public static final int PLAYER_ONE=1;
    public static final int PLAYER_TWO=2;
   // public static final int PLAYER_THREE=3;

    public static final Color PLAYER_ONE_COLOR=Color.RED; //color of player 1	
    public static final Color PLAYER_TWO_COLOR=Color.GREEN;	//color of player 2	
   // public static final Color PLAYER_THREE_COLOR=Color.RED;


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

    public Dots() {
        super("Connect the Dots");

        setSize(400, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        load_properties();
        load_dots();

        new_game();

        setVisible(true);
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
    	active_player=PLAYER_ONE;
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

    private void communicated_conn(int box_row, int box_col, int part)
    {
        int box_number = box_row * (num-1) + box_col;
        if(part == 0)
            make_connection(boxes[box_number].horizontal_connections[0]);
        else if(part == 1)
            make_connection(boxes[box_number].vertical_connections[1]);
        else if (part == 2) 
            make_connection(boxes[box_number].horizontal_connections[1]);
        else if(part == 3)
            make_connection(boxes[box_number].vertical_connections[0]);                 
    }

    private boolean[] box_status() {
    	boolean[] stat=new boolean[boxes.length];
    	for(int i=0; i<stat.length; i++) {
    		stat[i]=boxes[i].isBoxed();
    	}

    	return stat;
    }

    private int[] score_calculator() {
    	int[] score={0, 0};

    	for(int i=0; i<boxes.length; i++) {
    		if(boxes[i].isBoxed() && boxes[i].player!=0) {
    			score[boxes[i].player - 1]++;
    		}
    	}

    	return score;
    }

    private boolean make_connection(Connection connection) {
    	boolean new_box=false;

    	boolean[] boxStatusBeforeConnection=box_status();	

    	connection.connectionMade=true;


    	boolean[] boxStatusAfterConnection=box_status();

    	for(int i=0; i<boxes.length; i++) {
    		if(boxStatusAfterConnection[i]!=boxStatusBeforeConnection[i]) {
    			new_box=true;
    			boxes[i].player=active_player;
    		}
    	}

    	if(!new_box) {	
    		if(active_player==PLAYER_ONE)
    			active_player=PLAYER_TWO;
    		else
    			active_player=PLAYER_ONE;
    	}

    	gameover_check();

    	return new_box;
    }

    private void gameover_check() {
    	int[] score=score_calculator();
    	if((score[0] + score[1])==((num - 1) * (num - 1))) {
    		JOptionPane.showMessageDialog(this, "Player1: " + score[0] + "\nPlayer2: " + score[1], "Game Over", JOptionPane.PLAIN_MESSAGE);
    		new_game();
    		repaint();
    	}
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


        //changes here

        send_str = box_number_row+" "+ box_number_col+" "+part;
        return send_str.toCharArray();



    }


    private void clickhandler() {
    	Connection connection=get_connection(click_x, click_y);
    	if(connection==null)
    		return;

    	if(!connection.connectionMade) {

            send_connection(connection);

                    //changes here
                    //make_connection(connection);

    	}

    	repaint();
    }

    public void mouseMoved(MouseEvent event) {
    	mouse_x=event.getX();
    	mouse_y=event.getY();
    	repaint();
    }

    public void mouseDragged(MouseEvent event) {
    	mouseMoved(event);
    }

    public void mouseClicked(MouseEvent event) {
    	click_x=event.getX();
    	click_y=event.getY();

        System.out.println(click_x);
        System.out.println(click_y);

    	clickhandler();
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

    		if(!horizontal_connections[i].connectionMade && active_player==PLAYER_ONE) {
    			if(horizontal_connections[i].contain_point(mouse_x, mouse_y)) {
    				horizontal_connections[i].color=Color.RED;
                                
                                
    			} else {
    				horizontal_connections[i].color=Color.WHITE;
    			}
                        
    		} else if(!horizontal_connections[i].connectionMade && active_player==PLAYER_TWO) {
    			if(horizontal_connections[i].contain_point(mouse_x, mouse_y)) {
    				horizontal_connections[i].color=Color.GREEN;
    			} else {
    				horizontal_connections[i].color=Color.WHITE;
    			}
                }
    		horizontal_connections[i].render(g);
    	}

    	for(int i=0; i<vertical_connections.length; i++) {

    		if(!vertical_connections[i].connectionMade && active_player==PLAYER_ONE) {
    			if(vertical_connections[i].contain_point(mouse_x, mouse_y)) {
                            if(active_player==PLAYER_ONE){
    				vertical_connections[i].color=Color.RED;
                            }
                            
    			} else {
    				vertical_connections[i].color=Color.WHITE;
    			}
    		} else if(!vertical_connections[i].connectionMade && active_player==PLAYER_TWO) {
    			if(vertical_connections[i].contain_point(mouse_x, mouse_y)) {
    				vertical_connections[i].color=Color.GREEN;
    			} else {
    				vertical_connections[i].color=Color.WHITE;
    			}
                }
    		vertical_connections[i].render(g);
        
        }}

    public void paint_boxes(Graphics g) {
    	for(int i=0; i<boxes.length; i++) {
    		if(boxes[i].isBoxed()) {
    			if(boxes[i].player==PLAYER_ONE) {
    				boxes[i].color=PLAYER_ONE_COLOR;
    			} else if(boxes[i].player==PLAYER_TWO) {
    				boxes[i].color=PLAYER_TWO_COLOR;
    			}
    		} else {
    			boxes[i].color=Color.WHITE;
    		}

    		boxes[i].render(g);
    	}
    }

    public void paint_status(Graphics g) {
    	int[] scores=score_calculator();
    	String status1="It is player" + active_player + "'s turn";
    	String status2="Player 1: " + scores[0];
    	String status3="Player 2: " + scores[1];

    	g.setColor(Color.BLACK);
    	g.drawString(status1, 10, dim.height-50);

    	g.setColor(PLAYER_ONE_COLOR);
    	g.drawString(status2, 10, dim.height-35);

    	g.setColor(PLAYER_TWO_COLOR);
    	g.drawString(status3, 10, dim.height-20);
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

    public static void main(String[] args) {


    	new Dots();
    }
}