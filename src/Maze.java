import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javalib.impworld.WorldScene;
import javalib.worldimages.AboveImage;
import javalib.worldimages.BesideImage;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;

// represents a randomly generated maze that can be searched to find a solution
interface IConstant {

  // the width of a square in the maze grid
  int CELL_WIDTH = 10; // TODO: make this scalable?

  // the width of a border between squares in the maze grid
  int BORDER_SIZE = CELL_WIDTH / 10;

  // the width of a square in the grid with border width taken into account
  int INTERIOR_WIDTH = CELL_WIDTH - BORDER_SIZE;

  // an image representing the interior
  WorldImage BLANK_CELL_IMG = new Utils().makeInteriorSquare(Color.GRAY);

  // an image representing a cell -> a grey square with a darker grey outline
  // represents a cell with 4 walls
  WorldImage CELL_IMG = new OverlayImage(BLANK_CELL_IMG,
      new RectangleImage(CELL_WIDTH, CELL_WIDTH, OutlineMode.SOLID, Color.DARK_GRAY));

  // an image representing the green starting square of the maze
  WorldImage START_IMG = new Utils().makeInteriorSquare(Color.GREEN);

  // an image representing the green starting square of the maze
  WorldImage END_IMG = new Utils().makeInteriorSquare(Color.MAGENTA);

  // an image representing a square that's been automatically traversed by a BFS
  // or DFS searching algorithm
  WorldImage AUTO_PATH_TRAVERSED = new Utils().makeInteriorSquare(Color.CYAN);

  // an image representing the solution path to the maze 
  WorldImage SEARCH_SOLUTION = new Utils().makeInteriorSquare(Color.BLUE);
  
  // an image representing the square that the player is currently at if they're manually 
  // traversing the maze
  WorldImage PLAYER_IMG = new Utils().makeInteriorSquare(Color.ORANGE);

  WorldImage PLAYER_VISITED_PATH = new Utils().makeInteriorSquare(Color.YELLOW);
  
  // how big the text should be on the endgame screen
  int TEXT_SIZE = CELL_WIDTH * 3 / 2; // TODO: also potentially scalable
}

// represents a randomly generated maze that can be searched to find a solution
class Maze {
  // an image representing the grid of cells, with all walls intact
  private final WorldImage background;

  // the grid of cells
  // each cell is at the corresponding (col, row) of the list according to their
  // grid position
  private final ArrayList<ArrayList<Cell>> grid;

  // a list of all connections between cells in the maze (cells that don't have
  // walls
  // between them)
  private final ArrayList<Edge> edges;

  // the dimensions of this maze, in grid coordinates (not pixels)
  private final Posn dimensions;

  // constructs a Maze with no bias
  Maze(int rows, int columns) {
    this(rows, columns, 0.0);
  }
  
  // constructor initializes this as a random maze with the given dimensions in
  // grid-cells
  // bias, [-1.0, 1.0], represents whether the maze is more likely to feature horizontal or vertical passages,
  // with -1.0 being most horizontal, 0 being without bias, and 1 representing the most vertical
  Maze(int rows, int columns, double bias) {
    // don't allow construction of a maze that's less than 2x2 in dimensions
    if (rows < 2 || columns < 2) {
      throw new IllegalArgumentException("The maze can't be less than 2x2.");
    }
    if (bias < -1 || bias > 1) {
      throw new IllegalArgumentException("The bias must be a number from [-1.0, 1.0]");
    }

    this.dimensions = new Posn(columns, rows);

    // initialize the grid
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.initializeCells();

    // Initialize the edges
    ArrayList<Edge> initialEdges = this.initializeEdges(bias);

    // keep only the edges that form the minimum spanning tree of the cells
    this.edges = this.generateMaze(initialEdges);

    this.background = this.drawBackground();
  }

  // A constructor used for testing to allow non-random edge weights
  Maze(ArrayList<ArrayList<Cell>> grid, ArrayList<Edge> initialEdges) {
    this.dimensions = new Posn(grid.get(0).size(), grid.size());
    this.grid = grid;
    this.edges = this.generateMaze(initialEdges);
    this.background = this.drawBackground();
  }

  // return a new random maze with the same dimensions as this maze
  // and the given bias for passage type
  Maze randomize(double bias) {
    return new Maze(this.dimensions.y, this.dimensions.x, bias);
  }

  // add all the cells to the grid (with an empty list of connections to adjacent
  // cells)
  void initializeCells() {
    for (int ycoord = 0; ycoord < this.dimensions.y; ycoord += 1) {
      ArrayList<Cell> cellsInRow = new ArrayList<>();
      for (int xcoord = 0; xcoord < this.dimensions.x; xcoord += 1) {
        cellsInRow.add(new Cell(new Posn(xcoord, ycoord)));
      }
      this.grid.add(cellsInRow);
    }
  }

  // connects every cell in the initial grid with its four neighbors
  // EFFECT: modifies each cell's list of immediate edges to include connections
  // made, and adds
  // created edges to this' list of global edges and sorts them by increasing
  // weight
  ArrayList<Edge> initializeEdges(double bias) {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    // initialize the top row's horizontal edges
    ArrayList<Cell> currRow = this.grid.get(0);
    for (int xcoord = 1; xcoord < this.dimensions.x; xcoord += 1) {
      edges.add(new Edge(bias, currRow.get(xcoord - 1), currRow.get(xcoord))); // left
    }

    // initialize all the horizontal and vertical edges for the other rows
    for (int ycoord = 1; ycoord < this.dimensions.y; ycoord += 1) {
      ArrayList<Cell> prevRow = this.grid.get(ycoord - 1);
      currRow = this.grid.get(ycoord);

      // add vertical edges to all the left column cells
      edges.add(new Edge(bias, prevRow.get(0), currRow.get(0)));

      // for every cell not in the very top row or very leftmost column, create a
      // vertical edge between it and the cell above, and a horizontal edge 
      // between it and the cell to the left
      for (int xcoord = 1; xcoord < this.dimensions.x; xcoord += 1) {
        edges.add(new Edge(bias, currRow.get(xcoord - 1), currRow.get(xcoord))); // left
        edges.add(new Edge(bias, prevRow.get(xcoord), currRow.get(xcoord))); // up
      }
    }

    return edges;
  }

  // set edges to a minimum spanning tree of edges connecting the cells in the
  // grid
  ArrayList<Edge> generateMaze(ArrayList<Edge> initialEdges) {

    // sort the edges based on weight from smallest to largest
    initialEdges.sort(new EdgeWeightComparator());

    HashMap<Posn, Posn> reps = this.initialRepresentatives();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    ArrayList<Edge> worklist = initialEdges;// all edges in graph, sorted by edge weights;

    // keep looping until there are enough connections to form a spanning tree
    // If the edges do not create any cycles, then exactly 'n - 1' edges are needed,
    // where 'n' is total number of cells (width * height)

    // TERMINATION: As long as there are fewer than 'n - 1' edges in 'edgesInTree'
    // there is at least one cell not a part of the spanning tree, so eventually
    // the 'next' edge from the work-list will connect to that cell not connected
    // and
    // add it to 'edgesInTree'. The tree will be spanning before the worklist runs
    // out of edges
    while (edgesInTree.size() < (this.dimensions.x * this.dimensions.y - 1)) {
      Edge next = worklist.remove(0);
      Pair<Posn> cellPosns = next.connectedCellPositions();
      // Determine if the edge connects two cells already connected or not
      if (!(this.findRepresentative(cellPosns.first, reps)
          .equals(this.findRepresentative(cellPosns.second, reps)))) {
        edgesInTree.add(next);
        reps.replace(this.findRepresentative(cellPosns.first, reps), cellPosns.second);
      }
      else {
        next.removeSelf();
      }
    }

    // remove the remaining edges that are not a part of the spanning tree
    for (Edge toRemove : worklist) {
      toRemove.removeSelf();
    }

    // replace edges (previously all the possible connections between cells in the
    // maze grid)
    // with just the edges that form a minimum spanning tree between the cells of
    // the grid
    return edgesInTree;
  }

  // find the representative of a given position in the given hashmap of
  // representatives
  Posn findRepresentative(Posn cellPosn, HashMap<Posn, Posn> reps) {
    while (!reps.get(cellPosn).equals(cellPosn)) {
      cellPosn = reps.get(cellPosn);
    }
    return cellPosn;
  }

  // initialize a list of representatives for each position in the grid by letting
  // each position
  // represent itself
  HashMap<Posn, Posn> initialRepresentatives() {
    HashMap<Posn, Posn> representatives = new HashMap<Posn, Posn>();
    for (int ycoord = 0; ycoord < this.dimensions.y; ycoord += 1) {
      for (int xcoord = 0; xcoord < this.dimensions.x; xcoord += 1) {
        representatives.put(new Posn(xcoord, ycoord), new Posn(xcoord, ycoord));
      }
    }
    return representatives;
  }

  // initialize the background of the maze by covering up connections that are
  // part of the minimum
  // spanning tree of the maze
  WorldScene initializeBackground() {
    WorldScene backgroundScene = new WorldScene(this.dimensions.x * IConstant.CELL_WIDTH,
        this.dimensions.y * IConstant.CELL_WIDTH);
    backgroundScene.placeImageXY(this.background, this.dimensions.x * IConstant.CELL_WIDTH / 2, 
        this.dimensions.y * IConstant.CELL_WIDTH / 2);
    this.removeWalls(backgroundScene);
    return backgroundScene;
  }

  // visually remove all the walls in the maze that shouldn't be there by placing
  // a background
  // square over every connection in the list of edges that make up the maze's
  // minimum spanning
  // tree
  void removeWalls(WorldScene backgroundScene) {
    for (Edge edge : edges) {
      Pair<Posn> cellPosns = edge.connectedCellPositions();
      int wallX = (IConstant.CELL_WIDTH * (cellPosns.first.x + cellPosns.second.x + 1)) / 2;
      int wallY = (IConstant.CELL_WIDTH * (cellPosns.first.y + cellPosns.second.y + 1)) / 2;
      backgroundScene.placeImageXY(IConstant.BLANK_CELL_IMG, wallX, wallY);
    }
  }

  // creates a background image of all the cells with all their walls still up
  WorldImage drawBackground() {
    WorldImage background = new EmptyImage();
    for (ArrayList<Cell> row : this.grid) {
      WorldImage rowImg = new EmptyImage();
      for (Cell cell : row) {
        rowImg = new BesideImage(rowImg, IConstant.CELL_IMG);
      }
      background = new AboveImage(background, rowImg);
    }
    return background;
  }

  // creates the scene representing the current state of this maze
  WorldScene makeScene() {
    Utils u = new Utils();
    // make the maze by visually covering all the edges that make up the minimum
    // spanning tree
    WorldScene backgroundScene = this.initializeBackground();

    // draw the start and end position with separate colors to visually distinguish
    // them
    u.drawImageAtCellCoordinates(backgroundScene, IConstant.START_IMG, new Posn(0, 0));

    u.drawImageAtCellCoordinates(backgroundScene, IConstant.END_IMG,
        u.addPosn(this.dimensions, new Posn(-1, -1)));

    return backgroundScene;
  }

  // create a new automatic searcher for this maze based on whether it's BFS or DFS
  IMazeTraverser initializeAutomaticSearch(boolean breadthFirst) {
    Utils u = new Utils();

    IWorkList<Cell> forSearch;
    if (breadthFirst) {
      forSearch = new Queue<Cell>();
    }
    else {
      forSearch = new Stack<Cell>();
    }
    return new AutomaticSearch(this.getStart(), forSearch,
        u.addPosn(this.dimensions, new Posn(-1, -1)));
  }

  // create a new manual search for this maze
  IMazeTraverser initializeManualSearch() {
    Utils u = new Utils();

    return new ManualSearch(this.getStart(), u.addPosn(this.dimensions, new Posn(-1, -1)));
  }

  // return cell at the starting position of this maze
  // used to initialize traversers
  Cell getStart() {
    return this.grid.get(0).get(0);
  }

  // For testing purposes only to ensure that maze generation is functional
  ArrayList<ArrayList<Cell>> getGrid() {
    return this.grid;
  }

  // For testing purposes only to ensure that maze generation is functional
  ArrayList<Edge> getMazeEdges() {
    return this.edges;
  }
  
  // gets the maze's dimensions
  Posn getDimensions() {
    return this.dimensions;
  }
}
