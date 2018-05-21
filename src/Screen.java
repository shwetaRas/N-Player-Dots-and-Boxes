//This class represents the objects (dots) that are being drawn on the screen. Screen 
//is a method to check whether a point is within the drawn object or not. This class
//also takes care of drawing things on the screen.
import java.awt.Polygon;
import java.awt.Color;
import java.awt.Graphics;

public class Screen {
	Polygon shape;	//shape to be drawn
    Color color;	//color of the shape
    int width;		
    int height;		
    int x;			//horizontal coordinate of the center of the screen
    int y;			//vertical coordinate of the center of the screen

    public Screen() {
    	
        shape=new Polygon();
        width=0;
        height=0;
        x=0;
        y=0;
        color=Color.BLACK;
    }

    public void render(Graphics g) 
    {
    	
//This method takes care of positioning the screen at proper location
        g.setColor(color);

        Polygon rendered_shape=new Polygon();
        for(int i=0; i<shape.npoints; i++) 
        {
            int rendered_x=shape.xpoints[i] + x + width / 2;
            int rendered_y=shape.ypoints[i] + y + height / 2;
            rendered_shape.addPoint(rendered_x, rendered_y);
        }
        g.fillPolygon(rendered_shape);
    }

    public boolean contain_point(int x, int y) 
    {  	
    	return shape.contains(x - this.x - width /2, y - this.y - height /2);
    }
}

