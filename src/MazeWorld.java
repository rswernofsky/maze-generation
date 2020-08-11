import java.awt.Color;

import javalib.impworld.*;
import javalib.worldimages.Posn;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldEnd;

// a world class that contains a maze that can be searched
// and the current thing that is searching the maze
class MazeWorld extends World {

  // the maze that this world traverses 
  private Maze maze;

  private IMazeTraverser traverser;

  private boolean viewVisited;
  
  private double bias;

  // convenience constructor which has no bias
  MazeWorld(int rows, int cols) {
    this(rows, cols, 0.0);
  }
  
  // constructor is given dimensions and a bias and begins with a manual search by 
  // default (since easy to change)
  MazeWorld(int rows, int cols, double bias) {
    this.maze = new Maze(rows, cols);
    this.traverser = this.maze.initializeManualSearch();
    this.viewVisited = true;
    this.bias = bias;
  }

  // draw this current state of this maze's traversal
  public WorldScene makeScene() {
    WorldScene scene = this.maze.makeScene();
    this.traverser.drawOntoScene(scene, this.viewVisited);
    if (this.traverser.searchComplete()) { // TODO: make this a variable for efficiency sake?
      Posn dimensions = this.maze.getDimensions();
      scene.placeImageXY(new TextImage("Search Complete", IConstant.TEXT_SIZE, Color.BLACK), 
          dimensions.x * IConstant.CELL_WIDTH / 2, dimensions.y * IConstant.CELL_WIDTH / 4);
      scene.placeImageXY(new TextImage("Number of wrong moves: " + 
          Integer.toString(this.traverser.wrongMoves()), IConstant.TEXT_SIZE, Color.BLACK), 
          dimensions.x * IConstant.CELL_WIDTH / 2, dimensions.y * IConstant.CELL_WIDTH / 2);
    }
    
    return scene;
  }

  // update the player's position in the direction of the arrow key press if the maze 
  // is in manual traversal mode
  public void onKeyEvent(String key) {
    if (this.traverser.searchComplete()) {
      if (key.equals("n")) {
        this.maze = this.maze.randomize(this.bias);
        this.traverser = this.maze.initializeManualSearch();
//        this.traverser = this.traverser.reset(this.maze.getStart());
      }
    } else {
      
      // Arrow key movement for player
      if (key.equals("up") || key.equals("down") || key.equals("left") || key.equals("right")) {
        new OnKeyTraverser(key).apply(this.traverser);

      }
      // 'm' for Manual Search (player)
      else if (key.equals("m")) {
        this.traverser = this.maze.initializeManualSearch();
      }
      // 'n' for new maze with the same bias
      else if (key.equals("n")) {
        this.maze = this.maze.randomize(this.bias);
        this.traverser = this.traverser.reset(this.maze.getStart());
      }
      // 'd' for Depth First Search
      else if (key.equals("d")) {
        this.traverser = this.maze.initializeAutomaticSearch(false);
      }
      // 'b' for Breadth First Search
      else if (key.equals("b")) {
        this.traverser = this.maze.initializeAutomaticSearch(true);
      }
      // 'v' to toggle whether the visited path is shown
      else if (key.equals("v")) {
        this.viewVisited = !this.viewVisited;
      }
    }
  }

//  // ends the world if a path has been found to the end of the maze
//  public WorldEnd worldEnds() {
//    if (this.traverser.searchComplete()) {
//      return new WorldEnd(true, this.makeScene());
//    } else {
//      return new WorldEnd(false, this.makeScene());
//    }
//  }

  // increment the maze search if the maze's traversal mode is currently in BFS or DFS mode
  public void onTick() {
    new OnTickTraverser().apply(this.traverser);
  }

  // getter specifically for testing whether the viewVisited state changes upon the
  // correct key presses
  public boolean getViewVisited() {
    return this.viewVisited;
  }

  // getter specifically for testing whether the maze is randomly regenerated upon
  // pressing the "n" key 
  public Maze getMaze() {
    return this.maze;
  }

  // getter specifically for testing whether the maze traverser is reset upon pressing
  // the "n" key
  public IMazeTraverser getTraverser() {
    return this.traverser;
  }
}