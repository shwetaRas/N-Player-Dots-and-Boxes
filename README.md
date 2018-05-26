<b>MULTI-PLAYER DOTS AND BOXES GAME</b><br>

This project implements a multi-player dots and boxes game using the client server model in java.<br>

<p> The Dots and Boxes application has two components: a game server and a game client. A game server is an application that maintains the game state of each player in the game, the connections between different players and a queue for all the active players. The game client on the other hand handles different clients who want to connect to the server. It maintains a record of all the active players, dead players (that is, players who have left in the middle of the game), all the horizontal and vertical connections made, and all the boxes belonging to different players. </p>

<p>
The server program consists of two classes: the state_matrix class and the server_connection class. <br>
* The state_matrix class maintains the game state of each player in the game. <br>
* The server_connection class creates a socket and listens to any connections coming in from various client. <br></p>

<p>
The client application consists of two classes: the dots_and_boxes class and the client_connection class. <br>
* The dots_and_boxes class manages the GUI of the application. <br>
* The client_connection class handles connection with the server.<br></p>

<p> Our application can handle a maximum of 8 players, who can join in and leave at any point of time in the game. </p>

