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

public class Interface extends JFrame implements MouseMotionListener, MouseListener 
{

	public int num = 5;
    public static final int gap=24;		//gap between dots
    public static final int size=4;		//dot size

	private int side; //length of the sides of the board/grid
    private int space;	//length of 1 dot plus 1 connection

    private int center_x; //x coordinate of center of the board/grid
    private int center_y; //y coordinate of center of the board/grid
	
	private int click_x; //x coordinate of mouse click	
    private int click_y; //y coordinate of mouse click	

    private int mouse_x; //x coordinate of mouse location
    private int mouse_y; //y coordinate of mouse location	

    private Dimension dim; //dimension of the window screen
    
    private Screen[] dots; //array for all the dots
    
	public Interface() 
	{
        super("Connect the Dots");

        setSize(400, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMouseListener(this);
        addMouseMotionListener(this);

        load_properties();
        load_dots();



        setVisible(true);

    }

    private void load_properties() 
    {   	

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

    private void load_dots() 
    {
        int temp = 0;
        dots=new Screen[num * num];
        for(int row=0; row<num; row++) 
        {
            for(int col=0; col<num; col++) 
            {
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

    	//clickhandler();
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

    public void update_screen(Graphics g) {
    	paint(g);
    }

    public void paint(Graphics g) 
    {   	

    	Image bufferImage=createImage(dim.width, dim.height);
    	Graphics bufferGraphics=bufferImage.getGraphics();

    	background_paint(bufferGraphics);
    	paint_dots(bufferGraphics);


    	g.drawImage(bufferImage, 0, 0, null);
    }

    public static void main(String[] args) 
    {
    	new Interface();
    }
}