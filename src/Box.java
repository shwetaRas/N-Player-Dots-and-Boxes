//This class represents the actual boxes made by the dots class and connection class.
//It contains references to the four connections which make up the border. The is_boxed
//method returns true when all four of the border connections have true connectionMade
//fields. And boxes are being created by the create_box method.
import java.awt.Color;

public class Box extends Screen {
	Connection[] horizontal_connections;	
	Connection[] vertical_connections;	

	int player;	//tracks the player who closes the box

	public Box() {
		super();

		color=Color.WHITE;	//so that initially the box is the same color as the background

     
        horizontal_connections=new Connection[2];
		vertical_connections=new Connection[2];

		width=Dots.gap;
		height=Dots.gap;

		shape.addPoint(-width/2, -height/2);
        shape.addPoint(-width/2, height/2);
        shape.addPoint(width/2, height/2);
        shape.addPoint(width/2, -height/2);
	}

	public boolean isBoxed() {
		boolean boxed=true;

		for(int i=0; i<2; i++) {
			if(!horizontal_connections[i].connectionMade || !vertical_connections[i].connectionMade) {
				boxed=false;
			}
		}

		return boxed;
	}


	public static Box createBox(int x, int y, Connection[] horizontal_connections, Connection[] vertical_connections) {
		Box box=new Box();

		box.player=0;
		box.x=x;
		box.y=y;

		box.horizontal_connections=horizontal_connections;
		box.vertical_connections=vertical_connections;
		return box;
	}
}

