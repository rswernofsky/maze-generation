import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;

// To represent a path-finder that eventually reaches a solution and can operate incrementally
interface IMazeTraverser {
  // To implement visitor pattern
  <R> R accept(IMazeTraverserVisitor<R> f);

  // Draw the status of the path-finder onto the given background,
  // draws everything visited so far or just the most recently visited cell
  void drawOntoScene(WorldScene background, boolean viewVisited);

  // An in-order list of the positions of cells on the path from beginning to end
  ArrayList<Posn> reconstructSolutionPath();

  // Has the path-finder found the target?
  boolean searchComplete();

  // Resets the path-finder to begin at the given start (keeps same end target)
  IMazeTraverser reset(Cell start);
  
  // called only when a final solution has been found
  // returns the total number of new cells visited that are not a part of the solution path
  int wrongMoves();
}

// To represent a manual search that responds to key presses
class ManualSearch implements IMazeTraverser {  

  // Only the positions in order along the path to the target
  private ArrayList<Posn> solution;

  // Position of every cell visited so far
  private ArrayList<Posn> processed;
  private Cell currCell;
  private final Posn targetPosition;

  // Constructor initializes a new manual search that has not visited any cells
  ManualSearch(Cell start, Posn targetPosition) {
    this.solution = new ArrayList<>();
    this.solution.add(start.getPosition());
    this.processed = new ArrayList<>();
    this.currCell = start;
    this.targetPosition = targetPosition;
  }

  // move the player's position according to the given direction if the player is able to move
  // in that direction
  void incrementSearch(String direction) {
    // IDEA for solution: Assume all moves that are not undone by the player
    // are a part of the solution

    if (this.searchComplete()) {
      throw new RuntimeException("The manual search has already been completed. "
          + "No incrementing is necessary.");
    }
    
    
    this.processed.add(this.currCell.getPosition());
    // NOTE: we allow duplicate additions to the processed list because it would be less efficient
    // to check if the list contains this position before adding it every time

    Posn displacement = new Utils().directionToDisplacement(direction);

    // If the move is legal
    if (this.currCell.hasNeighbor(displacement)) {
      // Move to the corresponding neighboring cell
      this.currCell = this.currCell.getNeighbor(displacement);

      // If the move constitutes 'undoing' the previous move, the cell just occupied is not
      // a part of the solution
      if ((this.solution.size() >= 2)
          && currCell.getPosition().equals(this.solution.get(this.solution.size() - 2))) {
        this.solution.remove(this.solution.size() - 1);
      } else {
        this.solution.add(this.currCell.getPosition());

        // add the target cell to the processed list and remove duplicates
        if (this.searchComplete()) {
          this.processed.add(this.currCell.getPosition());
          this.processed = new Utils().withoutDuplicates(this.processed);
        }
      }
    }
  }

  // This has been keeping track of the solution the entire time
  public ArrayList<Posn> reconstructSolutionPath() {
    if (!this.searchComplete()) {
      throw new RuntimeException("Cannot receive solution for incomplete search.");
    }
    return this.solution;
  }

  // The search is complete if the current cell is at the goal position
  public boolean searchComplete() {
    return this.currCell.getPosition().equals(this.targetPosition);
  }

  // Returns a new manual search with the same target but the given starting cell
  public IMazeTraverser reset(Cell start) {
    return new ManualSearch(start, this.targetPosition);
  }

  // To implement the visitor pattern, informs visitor that this is a ManualSearch
  public <R> R accept(IMazeTraverserVisitor<R> f) {
    return f.visitManualSearch(this);
  }

  // Draws the status of the manual traversal, with an option to view
  // all visited cells so far or just the current cell, draws solution if search is complete
  public void drawOntoScene(WorldScene background, boolean viewVisited) {
    Utils u = new Utils();
    // Draw all visited cells if applicable
    if (viewVisited) {
      for (Posn visitedPosn : this.processed) {
        u.drawImageAtCellCoordinates(background, IConstant.PLAYER_VISITED_PATH, visitedPosn);
      }
    }
    // Draw the solution
    if (this.searchComplete()) {
      for (Posn solutionPosn : this.solution) {
        u.drawImageAtCellCoordinates(background, IConstant.SEARCH_SOLUTION, solutionPosn);
      }
    }

    // Draw the player
    Posn currPosition = this.currCell.getPosition();
    u.drawImageAtCellCoordinates(background, IConstant.PLAYER_IMG, currPosition);
  }
  
  // called only when a final solution has been found
  // returns the total number of new cells visited that are not a part of the solution path
  public int wrongMoves() {
    if (! this.searchComplete()) {
      throw new RuntimeException("The search hasn't been completed yet.");
    } else {
      return this.processed.size() - this.solution.size();
    }
  }
}

//a searcher that looks for a solution to the maze using either BFS or DFS
//depending on what kind of work-list it has
class AutomaticSearch implements IMazeTraverser {

  // a map of all the positions encountered so far, and which edge led to them
  private HashMap<Posn, Edge> cameFromEdges;

  // a worklist of cells left to search over
  private IWorkList<Cell> worklist;

  // a list of cells that have already been processed
  private ArrayList<Cell> processed;

  // the goal position of this search
  private final Posn targetPosition;

  // has the search has been completed?
  private boolean searchComplete; // Recorded as a field for efficiency purposes

  // the starting cell of the search
  private final Cell start;

  // constructor
  // initializes the cameFromEdges map and the processed list as empty
  // initializes the worklist as the given one and adds the topLeft cell
  AutomaticSearch(Cell start, IWorkList<Cell> worklist, Posn targetPosition) {
    this.cameFromEdges = new HashMap<Posn, Edge>();
    this.worklist = worklist;
    this.worklist.add(start);
    this.processed = new ArrayList<Cell>();
    this.targetPosition = targetPosition;
    this.start = start;
    this.searchComplete = this.start.getPosition().equals(targetPosition);
  }

  // draws the search onto the given scene based on whether viewing the previously visited 
  // positions is toggled
  public void drawOntoScene(WorldScene background, boolean viewVisited) {
    Utils u = new Utils();
    // Show path for every cell visited
    if (viewVisited) {
      for (Cell cellCovered : this.processed) {
        Posn cellPos = cellCovered.getPosition();
        u.drawImageAtCellCoordinates(background, IConstant.AUTO_PATH_TRAVERSED, cellPos);
      }
    } else {
      // Only show on the most recently visited cell
      if (this.processed.size() > 0) {
        Posn cellPos = this.processed.get(this.processed.size() - 1).getPosition();
        u.drawImageAtCellCoordinates(background, IConstant.AUTO_PATH_TRAVERSED, cellPos);
      }
    }

    // if the search has been completed, draw the solution path
    if (this.searchComplete) {
      for (Posn cellInSolution : this.reconstructSolutionPath()) {
        u.drawImageAtCellCoordinates(background, IConstant.SEARCH_SOLUTION, cellInSolution);
      }
    }
  }

  // creates a list of all the positions in the solution path based on which edge each cell along
  // the path came from in the cameFromEdges hashmap
  public ArrayList<Posn> reconstructSolutionPath() {
    // throw an error if an attempt is made to reconstruct the solution path when a solution
    // hasn't been found yet
    if (!this.searchComplete) {
      throw new RuntimeException("Solution has not yet been found.");
    } else {
      ArrayList<Posn> path = new ArrayList<Posn>(); // backwards from target
      Posn currPosn = this.targetPosition;

      // while the current position on the path isn't at the beginning of the path yet, add it to
      // the list of positions on the path and then get the next position on the solution path
      // based on which edge the current position came from in cameFromEdges
      while (! currPosn.equals(this.start.getPosition())) {
        path.add(currPosn);
        currPosn = this.cameFromEdges.get(currPosn).connectedCellPositions().getOther(currPosn);
      }
      path.add(this.start.getPosition()); // add the starting position, since it's not in 
      // the hashmap
      return path;
    }
  }

  // is the search complete?
  // used to end the world in MazeWorld
  public boolean searchComplete() {
    return this.searchComplete;
  }

  // A new automatic search of the same type (bfs or dfs) with the same target
  // at the given cell position
  public IMazeTraverser reset(Cell start) {
    
    this.worklist.clear();
    return new AutomaticSearch(start, this.worklist, this.targetPosition);
  }

  // To implement the visitor pattern on automatic searches
  public <R> R accept(IMazeTraverserVisitor<R> f) {
    return f.visitAutomaticSearch(this);
  }

  // increment the automatic search by one cell
  // EFFECT: Removes next element from worklist
  // EFFECT: Adds neighbors of cell processed to worklist
  // EFFECT: Adds neighbors and edges to the hashmap
  void incrementSearch() {
    // World should have ended rather than continually incrementing search
    if (this.searchComplete) {
      throw new RuntimeException("The automatic search has already been completed. "
          + "No incrementing is necessary.");
    }
    if (this.worklist.isEmpty()) {
      throw new NoSuchElementException("No more elements in worklist.");
    }
    Cell next = this.worklist.next();
    // Toggle search is complete since next is at the target position
    if (next.getPosition().equals(this.targetPosition)) {
      this.processed.add(next);
      this.processed = new Utils().withoutDuplicates(this.processed);
      this.searchComplete = true;
    } else {
      this.processed.add(next);
      // For every cell that connects from this, add it to the worklist
      // And add edge to HashMap
      for (Edge e : next.getConnections()) {
        Cell neighbor = e.findOther(next);
        if (! this.processed.contains(neighbor)) {
          this.worklist.add(neighbor);
          this.cameFromEdges.put(neighbor.getPosition(), e);
        }
      }
    }    
  }

  // a getter method specifically to test that key presses correctly change the maze traverser
  IWorkList<Cell> getWorkList() {
    return this.worklist;
  }
  
  // called only when a final solution has been found
  // returns the total number of new cells visited that are not a part of the solution path
  public int wrongMoves() {
    if (! this.searchComplete()) {
      throw new RuntimeException("The search hasn't been completed yet.");
    } else {
      return this.processed.size() - this.reconstructSolutionPath().size();
    }
  }
}

// A function with a single argument
interface IFunc<A, R> {
  R apply(A arg);
}

// A visitor on IMazeTraverser is a function that takes a single argument
// and must handle manual and automatic searches
interface IMazeTraverserVisitor<R> extends IFunc<IMazeTraverser, R> {
  
  R visitManualSearch(ManualSearch ms);
  R visitAutomaticSearch(AutomaticSearch as);
}

// To appropriately increment the traverser IF it is an automatic search
class OnTickTraverser implements IMazeTraverserVisitor<Void> {

  // Apply this to the given IMazeTraverser
  public Void apply(IMazeTraverser arg) {
    return arg.accept(this);
  }

  // Manual search should not be affected by a tick
  public Void visitManualSearch(ManualSearch ms) {
    // do nothing
    return null;
  }

  // Automatic search should be incremented on a tick
  public Void visitAutomaticSearch(AutomaticSearch as) {
    if (! as.searchComplete()) {
      as.incrementSearch();
    }
    return null;
  }
}

// To appropriately tick the traverser when a key was pressed IF it is a manual search
class OnKeyTraverser implements IMazeTraverserVisitor<Void> {

  String key;

  // Constructor parametrizes this with the key
  OnKeyTraverser(String key) {
    this.key = key;
  }

  // Apply this to the given IMazeTraverser
  public Void apply(IMazeTraverser arg) {
    return arg.accept(this);
  }

  // Manual Search should be updated according to the key pressed
  public Void visitManualSearch(ManualSearch ms) {
    if (! ms.searchComplete()) {
      ms.incrementSearch(key);
    }
    return null;
  }

  // Automatic search should not be affected
  public Void visitAutomaticSearch(AutomaticSearch as) {
    // do nothing
    return null;
  }
}