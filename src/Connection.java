//Connection class is a subclass of Screen class. There are two types of connections:
//vertical and horizontal. create_connection method is used to create the connections
//at proper coordinates and building its shape.
import java.awt.Color;

public class Connection extends Screen {
	public static final int horizontal_conn=1;
    public static final int vertical_conn=2;

    boolean connectionMade;	//checks whether the user has clicked to connect the dota

    public Connection() {
    	
        super();

        connectionMade=false;
        color=Color.WHITE;
    }

    public static Connection create_connection(int type, int x, int y) {
    	Connection connect=new Connection();

        if(type==Connection.horizontal_conn) {
        	connect.width=Dots.gap;
        	connect.height=Dots.size;
        } else if(type==Connection.vertical_conn) {
        	connect.width=Dots.size;
        	connect.height=Dots.gap;
        } else {
        	return null;
        }

        connect.x=x;
        connect.y=y;

        connect.shape.addPoint(-connect.width/2, -connect.height/2);
        connect.shape.addPoint(-connect.width/2, connect.height/2);
        connect.shape.addPoint(connect.width/2, connect.height/2);
        connect.shape.addPoint(connect.width/2, -connect.height/2);

        return connect;
    }
}
