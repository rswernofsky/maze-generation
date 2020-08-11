import java.util.ArrayList;
import java.util.HashMap;

import javalib.impworld.WorldScene;
import javalib.worldimages.AboveImage;
import javalib.worldimages.BesideImage;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;
import tester.Tester;

class ExamplesMaze {

  public static void main(String[] args) {
    MazeWorld mw = new MazeWorld(10, 10);
    mw.bigBang(55 * IConstant.CELL_WIDTH, 55 * IConstant.CELL_WIDTH, .01);
  }

//  void testGeneration(Tester t) {
//    MazeWorld mw = new MazeWorld(10, 10);
//    mw.bigBang(55 * IConstant.CELL_WIDTH, 55 * IConstant.CELL_WIDTH, .01);
//  }

  void testFindRepresentative(Tester t) {
    HashMap<Posn, Posn> reps = new HashMap<Posn,Posn>();
    Maze m = new Maze(4, 4);
    Posn p0 = new Posn(0, 0);
    Posn p1 = new Posn(1, 1);
    Posn p2 = new Posn(2, 2);
    Posn p3 = new Posn(3, 3);
    Posn p4 = new Posn(4, 4);
    reps.put(p0, p0);
    reps.put(p1, p1);
    reps.put(p2, p1);
    reps.put(p3, p2);
    reps.put(p4, p0);
    t.checkExpect(m.findRepresentative(p0, reps), p0);
    t.checkExpect(m.findRepresentative(p1, reps), p1);
    t.checkExpect(m.findRepresentative(p2, reps), p1);
    t.checkExpect(m.findRepresentative(p3, reps), p1);
    t.checkExpect(m.findRepresentative(p4, reps), p0);
  }

//  void testEdgeRemoval(Tester t) {
//    // Four cells connected in a square
//    Cell c00 = new Cell(new Posn(0, 0));
//    Cell c10 = new Cell(new Posn(1, 0));
//    Cell c01 = new Cell(new Posn(0, 1));
//    Cell c11 = new Cell(new Posn(1, 1));
//    Edge e1 = new Edge(c00, c10);
//    Edge e2 = new Edge(c00, c01);
//    Edge e3 = new Edge(c10, c11);
//    Edge e4 = new Edge(c01, c11);
//
//    // Top-left should have neighbor to the right
//    t.checkExpect(c00.hasNeighbor(new Posn(1, 0)), true);
//    // Top-right should have neighbor to the left
//    t.checkExpect(c10.hasNeighbor(new Posn(0, 1)), true);
//    t.checkExpect(c11.hasNeighbor(new Posn(0, -1)), true);
//
//    t.checkExpect(c11.hasNeighbor(new Posn(0, 1)), false);
//    t.checkExpect(c10.hasNeighbor(new Posn(-1, 0)), true);
//    e1.removeSelf();
//    t.checkExpect(c00.hasNeighbor(new Posn(1, 0)), false);
//    t.checkExpect(c10.hasNeighbor(new Posn(-1, 0)), false);
//    e3.removeSelf();
//    t.checkExpect(c10.hasNeighbor(new Posn(0, 1)), false);
//    t.checkExpect(c11.hasNeighbor(new Posn(0, -1)), false);
//  }

  void testGetOther(Tester t) {
    Pair<Integer> ints = new Pair<Integer>(1, 2);
    t.checkExpect(ints.getOther(1), 2);
    t.checkExpect(ints.getOther(2), 1);
  }

  void testCellEdges(Tester t) {
    // GOAL: Ensure bijectivity between edges that connect all the cells in the grid
    // and the list of edges that the maze contains
    int testSize = 10;
    // All maze initialization occurs in constructor
    Maze mz = new Maze(testSize, testSize);

    // Every edge that is kept by 'mz' represents edges that are a part of the spanning tree
    // Each one of these edges should also appear in the cells they connect
    for (Edge e : mz.getMazeEdges()) {
      Pair<Cell> connectedCells = e.getConnectedCells();
      t.checkExpect(connectedCells.first.getConnections().contains(e), true);
      t.checkExpect(connectedCells.second.getConnections().contains(e), true);
    }

    // Ensure that the grid has proper dimensions
    ArrayList<ArrayList<Cell>> cells = mz.getGrid();
    t.checkExpect(cells.size(), testSize);
    t.checkExpect(cells.get(0).size(), testSize);

    // For every edge stored among the cells in the grid remaining after initialization,
    // that edge must appear in the Maze's list of edges that represent the spanning tree
    for (int yCoord = 0; yCoord < testSize; yCoord += 1) {
      for (int xCoord = 0; xCoord < testSize; xCoord += 1) {
        Cell c = cells.get(yCoord).get(xCoord);
        for (Edge e : c.getConnections()) {
          t.checkExpect(mz.getMazeEdges().contains(e), true);
        }
      }
    }
  }

  void testSpanningTree(Tester t) {
    //GOAL: Demonstrate that the maze is spanning with no cycles
    int testSize = 10;

    // Square maze initialized
    Maze mz = new Maze(testSize, testSize);

    // A spanning tree of 'n' nodes must have 'n - 1' edges (n = width * height)
    t.checkExpect(mz.getMazeEdges().size(), testSize * testSize - 1);

    // The maze is spanning if every cell can be reached from at least one cell, since 
    // edges are undirected
    // this implies that any cell can reach any other cell

    // Iterate over all possible target coordinates
    for (int targetY = 0; targetY < testSize; targetY += 1) {
      for (int targetX = 0; targetX < testSize; targetX += 1) {
        // Begin a new depth-first search (could be bfs) at the top-left, with corresponding
        // target
        AutomaticSearch as = new AutomaticSearch(mz.getStart(), new Stack<Cell>(),
            new Posn(targetX, targetY));
        // Allow the search to run maximum 100 times
        for (int numSteps = 0; numSteps < testSize * testSize; numSteps += 1) {
          if (! as.searchComplete()) {
            as.incrementSearch();
          }
        }
        // The target must have been found in this time if the maze is spanning
        t.checkExpect(as.searchComplete(), true);
      }
    } 
    // The combination of these two properties (edges are spanning AND there are 'n-1' edges)
    // implies that no cycles have been created, since spanning requires at MINIMUM n-1
    // nodes, and any extra edges would create a cycle
  }

  void testStandardDrawing(Tester t) {
    // GOAL: Demonstrate functionality of basic drawing of maze w/o walls
    //int testSize = 2;

    // Square maze initialized
    Maze mz = new Maze(2, 2);

    t.checkInexact(mz.makeScene().width * 1.0, 2 * IConstant.CELL_WIDTH * 1.0, .1);
    t.checkInexact(mz.makeScene().height * 1.0, 2 * IConstant.CELL_WIDTH * 1.0, .1);

    WorldImage backgroundImg = new EmptyImage();
    WorldImage row = new BesideImage(new BesideImage(backgroundImg, IConstant.CELL_IMG), 
        IConstant.CELL_IMG);
    WorldImage grid = new AboveImage(new AboveImage(backgroundImg, row), row);
    t.checkExpect(mz.drawBackground(), grid);

    // Four cells connected in a square
    Cell c00 = new Cell(new Posn(0, 0));
    Cell c10 = new Cell(new Posn(1, 0));
    Cell c01 = new Cell(new Posn(0, 1));
    Cell c11 = new Cell(new Posn(1, 1));
    ArrayList<Edge> initialEdges = new ArrayList<Edge>();
    Edge e1 = new Edge(c00, c10, 50);
    Edge e2 = new Edge(c00, c01, 40);
    Edge e3 = new Edge(c10, c11, 30);
    Edge e4 = new Edge(c01, c11, 100);
    initialEdges.add(e1);
    initialEdges.add(e2);
    initialEdges.add(e3);
    initialEdges.add(e4);

    ArrayList<Cell> topRow = new ArrayList<Cell>();
    topRow.add(c00);
    topRow.add(c10);
    ArrayList<Cell> bottomRow = new ArrayList<Cell>();
    bottomRow.add(c01);
    bottomRow.add(c11);
    ArrayList<ArrayList<Cell>> gridCells = new ArrayList<>();
    gridCells.add(topRow);
    gridCells.add(bottomRow);

    Maze small = new Maze(gridCells, initialEdges);
    // Maze should look like this:
    //  __ __
    // |     |
    // |__|__| 

    // Ensuring that the only edge removed corresponds to text image above
    t.checkExpect(c01.hasNeighbor(new Posn(1, 0)), false);
    t.checkExpect(c01.hasNeighbor(new Posn(0, -1)), true);

    WorldScene smallScene = new WorldScene(2 * IConstant.CELL_WIDTH, 2 * IConstant.CELL_WIDTH);
    // Initialize background
    smallScene.placeImageXY(small.drawBackground(), IConstant.CELL_WIDTH, IConstant.CELL_WIDTH);
    // Manually 'knocking down' the walls
    smallScene.placeImageXY(IConstant.BLANK_CELL_IMG, IConstant.CELL_WIDTH / 2, IConstant.CELL_WIDTH);
    smallScene.placeImageXY(IConstant.BLANK_CELL_IMG, IConstant.CELL_WIDTH, IConstant.CELL_WIDTH / 2);
    smallScene.placeImageXY(IConstant.BLANK_CELL_IMG, 3 * (IConstant.CELL_WIDTH / 2), IConstant.CELL_WIDTH);
    t.checkExpect(small.initializeBackground(), smallScene);

    // Adding squares for start and end positions
    smallScene.placeImageXY(IConstant.START_IMG, IConstant.CELL_WIDTH / 2, IConstant.CELL_WIDTH / 2);
    smallScene.placeImageXY(IConstant.END_IMG, 3 * (IConstant.CELL_WIDTH / 2), 3 * (IConstant.CELL_WIDTH / 2));
    t.checkExpect(small.makeScene(), smallScene);
  }

  void testBFSandDrawing(Tester t) {
    //GOAL: Test BFS drawing (same principle for DFS)
    // Implicitly tests the workings of BFS
    // Four cells connected in a square
    Cell c00 = new Cell(new Posn(0, 0));
    Cell c10 = new Cell(new Posn(1, 0));
    Cell c01 = new Cell(new Posn(0, 1));
    Cell c11 = new Cell(new Posn(1, 1));
    ArrayList<Edge> initialEdges = new ArrayList<Edge>();
    Edge e1 = new Edge(c00, c10, 50);
    Edge e2 = new Edge(c00, c01, 40);
    Edge e3 = new Edge(c10, c11, 30);
    Edge e4 = new Edge(c01, c11, 100);
    initialEdges.add(e1);
    initialEdges.add(e2);
    initialEdges.add(e3);
    initialEdges.add(e4);

    ArrayList<Cell> topRow = new ArrayList<Cell>();
    topRow.add(c00);
    topRow.add(c10);
    ArrayList<Cell> bottomRow = new ArrayList<Cell>();
    bottomRow.add(c01);
    bottomRow.add(c11);
    ArrayList<ArrayList<Cell>> gridCells = new ArrayList<>();
    gridCells.add(topRow);
    gridCells.add(bottomRow);

    Maze small = new Maze(gridCells, initialEdges);

    // Maze should look like this:
    //  __ __
    // |     |
    // |__|__|

    WorldScene smallSceneActual = small.makeScene();
    WorldScene smallSceneExpected = small.makeScene();
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Initial search should not display anything
    IMazeTraverser imt = small.initializeAutomaticSearch(true);
    imt.drawOntoScene(smallSceneActual, true);

    t.checkExpect(smallSceneActual, smallSceneExpected);
    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now, top-left should have a blue square
    smallSceneExpected.placeImageXY(IConstant.AUTO_PATH_TRAVERSED, 
        IConstant.CELL_WIDTH / 2, IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now, top-right should also have a blue square
    smallSceneExpected.placeImageXY(IConstant.AUTO_PATH_TRAVERSED, 
        (3 * IConstant.CELL_WIDTH) / 2, IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now, bottom-left should also have a blue square
    smallSceneExpected.placeImageXY(IConstant.AUTO_PATH_TRAVERSED, 
        IConstant.CELL_WIDTH / 2, (3 * IConstant.CELL_WIDTH) / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Ensure that if 'viewVisited' is false, only the most recently visited is shown
    // rather than the entire path
    WorldScene noViewVisitedActual = small.makeScene();
    imt.drawOntoScene(noViewVisitedActual, false);
    WorldScene noViewVisitedExpected = small.makeScene();
    noViewVisitedExpected.placeImageXY(IConstant.AUTO_PATH_TRAVERSED, 
        IConstant.CELL_WIDTH / 2, (3 * IConstant.CELL_WIDTH) / 2);
    t.checkExpect(noViewVisitedActual, noViewVisitedExpected);

    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Solution found, path should receive different color
    smallSceneExpected.placeImageXY(IConstant.SEARCH_SOLUTION, 
        IConstant.CELL_WIDTH / 2, IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(IConstant.SEARCH_SOLUTION, 
        (3 * IConstant.CELL_WIDTH) / 2, IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(IConstant.SEARCH_SOLUTION, 
        (3 * IConstant.CELL_WIDTH) / 2, (3 * IConstant.CELL_WIDTH) / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);
  }

  void testDFS(Tester t) {
    //GOAL: Test details of DFS workings for a tiny maze
    // Four cells connected in a square
    Cell c00 = new Cell(new Posn(0, 0));
    Cell c10 = new Cell(new Posn(1, 0));
    Cell c01 = new Cell(new Posn(0, 1));
    Cell c11 = new Cell(new Posn(1, 1));
    ArrayList<Edge> initialEdges = new ArrayList<Edge>();
    Edge e1 = new Edge(c00, c10, 50);
    Edge e2 = new Edge(c00, c01, 40);
    Edge e3 = new Edge(c10, c11, 100);
    Edge e4 = new Edge(c01, c11, 30);
    initialEdges.add(e1);
    initialEdges.add(e2);
    initialEdges.add(e3);
    initialEdges.add(e4);

    ArrayList<Cell> topRow = new ArrayList<Cell>();
    topRow.add(c00);
    topRow.add(c10);
    ArrayList<Cell> bottomRow = new ArrayList<Cell>();
    bottomRow.add(c01);
    bottomRow.add(c11);
    ArrayList<ArrayList<Cell>> gridCells = new ArrayList<>();
    gridCells.add(topRow);
    gridCells.add(bottomRow);

    Maze small = new Maze(gridCells, initialEdges);

    // Maze should look like this:
    //  __ __
    // |   __|
    // |__ __|

    WorldScene smallSceneActual = small.makeScene();
    WorldScene smallSceneExpected = small.makeScene();
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Initial search should not display anything
    IMazeTraverser imt = small.initializeAutomaticSearch(false);
    imt.drawOntoScene(smallSceneActual, true);

    t.checkExpect(smallSceneActual, smallSceneExpected);
    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now, top-left should have a blue square
    smallSceneExpected.placeImageXY(IConstant.AUTO_PATH_TRAVERSED, 
        IConstant.CELL_WIDTH / 2, IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now, bottom-left should also have a blue square
    smallSceneExpected.placeImageXY(IConstant.AUTO_PATH_TRAVERSED, 
        IConstant.CELL_WIDTH / 2, (3 * IConstant.CELL_WIDTH) / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Tick the search
    new OnTickTraverser().apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Solution found, path should receive different color
    smallSceneExpected.placeImageXY(IConstant.SEARCH_SOLUTION, 
        IConstant.CELL_WIDTH / 2, IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(IConstant.SEARCH_SOLUTION, 
        IConstant.CELL_WIDTH / 2, (3 * IConstant.CELL_WIDTH) / 2);
    smallSceneExpected.placeImageXY(IConstant.SEARCH_SOLUTION, 
        (3 * IConstant.CELL_WIDTH) / 2, (3 * IConstant.CELL_WIDTH) / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // This shows a difference from BFS since the top-right cell was never visited,
    // whereas all cells were visited in BFS
  }

  void testSearches(Tester t) {
    //GOAL: Demonstrate that all possible BFS and DFS searches terminate in expected
    // number of iterations and that BFS and DFS produce
    // same solution
    int testSize = 10;

    // Square maze initialized
    Maze mz = new Maze(testSize, testSize);

    // Iterate over all possible target coordinates
    for (int targetY = 0; targetY < testSize; targetY += 1) {
      for (int targetX = 0; targetX < testSize; targetX += 1) {
        // Begin a new depth-first search (could be bfs) at the top-left, with corresponding 
        // target
        AutomaticSearch bfs = new AutomaticSearch(mz.getStart(), new Stack<Cell>(), 
            new Posn(targetX, targetY));
        AutomaticSearch dfs = new AutomaticSearch(mz.getStart(), new Queue<Cell>(), 
            new Posn(targetX, targetY));
        // Allow the search to run maximum 100 times
        for (int numSteps = 0; numSteps < testSize * testSize; numSteps += 1) {
          if (! bfs.searchComplete()) {
            bfs.incrementSearch();
          }
          if (! dfs.searchComplete()) {
            dfs.incrementSearch();
          }
        }
        // The target must have been found in this time if the maze is spanning
        t.checkExpect(bfs.searchComplete(), true);
        t.checkExpect(dfs.searchComplete(), true);
        // Solution should be exactly the same
        t.checkExpect(bfs.reconstructSolutionPath(), dfs.reconstructSolutionPath());
        // To show that the solution found is feasible, it must have a size at least the
        // Manhattan distance between start to end
        t.checkExpect(bfs.reconstructSolutionPath().size() >= targetX + targetY, true);
      }
    } 
    // The combination of these two properties (edges are spanning AND there are 'n-1' edges)
    // implies that no cycles have been created, since spanning requires at
    // MINIMUM n-1 nodes, and any
    // extra edges would create a cycle
  }

  void testManualSearch(Tester t) {
    // GOAL: Test that player makes only valid moves
    // and that path is drawn correctly
    // Four cells connected in a square
    Cell c00 = new Cell(new Posn(0, 0));
    Cell c10 = new Cell(new Posn(1, 0));
    Cell c01 = new Cell(new Posn(0, 1));
    Cell c11 = new Cell(new Posn(1, 1));
    ArrayList<Edge> initialEdges = new ArrayList<Edge>();
    Edge e1 = new Edge(c00, c10, 50);
    Edge e2 = new Edge(c00, c01, 40);
    Edge e3 = new Edge(c10, c11, 30);
    Edge e4 = new Edge(c01, c11, 100);
    initialEdges.add(e1);
    initialEdges.add(e2);
    initialEdges.add(e3);
    initialEdges.add(e4);

    ArrayList<Cell> topRow = new ArrayList<Cell>();
    topRow.add(c00);
    topRow.add(c10);
    ArrayList<Cell> bottomRow = new ArrayList<Cell>();
    bottomRow.add(c01);
    bottomRow.add(c11);
    ArrayList<ArrayList<Cell>> gridCells = new ArrayList<>();
    gridCells.add(topRow);
    gridCells.add(bottomRow);

    Maze small = new Maze(gridCells, initialEdges);

    // Maze should look like this:
    //  __ __
    // |     |
    // |__|__|

    IMazeTraverser imt = small.initializeManualSearch();

    WorldScene smallSceneActual = small.makeScene();
    WorldScene smallSceneExpected = small.makeScene();
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Initial search should not display anything
    imt.drawOntoScene(smallSceneActual, true);
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_IMG, IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Attempt illegal move upward
    new OnKeyTraverser("up").apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // No change should occur
    t.checkExpect(smallSceneActual, smallSceneExpected);

    new OnKeyTraverser("down").apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Player in bottom-left, path visited shows in top-left as well
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_IMG, IConstant.CELL_WIDTH / 2, 
        3 * IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_VISITED_PATH, IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Attempt illegal move to the right
    new OnKeyTraverser("right").apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // No change should occur
    t.checkExpect(smallSceneActual, smallSceneExpected);

    // Tick the search
    new OnKeyTraverser("up").apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now player back at top with bottom-left visited
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_VISITED_PATH, IConstant.CELL_WIDTH / 2, 
        3 * IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_IMG, IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    new OnKeyTraverser("right").apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now player back at top-right, entire left side visited
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_IMG, 3 * IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_VISITED_PATH, IConstant.CELL_WIDTH / 2, 
        3 * IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_VISITED_PATH, IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);

    new OnKeyTraverser("down").apply(imt);
    imt.drawOntoScene(smallSceneActual, true);
    // Now player has reached end, show player image at end with solution path colored (excludes bottom-left)
    smallSceneExpected.placeImageXY(
        IConstant.PLAYER_IMG, 3 * IConstant.CELL_WIDTH / 2, 
        3 * IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(
        IConstant.SEARCH_SOLUTION, 3 * IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    smallSceneExpected.placeImageXY(
        IConstant.SEARCH_SOLUTION, IConstant.CELL_WIDTH / 2, 
        IConstant.CELL_WIDTH / 2);
    t.checkExpect(smallSceneActual, smallSceneExpected);
  }

  // test that keys that the user presses do the correct things to the maze world
  void testKeyPresses(Tester t) {
    // GOAL: Basic

    MazeWorld mw = new MazeWorld(2, 3);

    // test that the ability to view visited paths is toggleable 
    t.checkExpect(mw.getViewVisited(), true);
    mw.onKeyEvent("v");
    t.checkExpect(mw.getViewVisited(), false);
    mw.onKeyEvent("v");
    t.checkExpect(mw.getViewVisited(), true);

    // test that random mazes are able to be generated through key presses
    Maze maze = mw.getMaze();
    IMazeTraverser traverser = mw.getTraverser();

    t.checkExpect(mw.getMaze().equals(maze), true);
    t.checkExpect(mw.getTraverser().equals(traverser), true);
    t.checkExpect(mw.getTraverser() instanceof ManualSearch, true);

    mw.onKeyEvent("n");
    t.checkExpect(mw.getMaze().getGrid().size() == maze.getGrid().size(), true);
    t.checkExpect(mw.getMaze().getGrid().get(0).size() == maze.getGrid().get(0).size(), true);
    t.checkExpect(mw.getTraverser() instanceof ManualSearch, true);

    // test that pressing "d", "b", and "m" make the traverser a new dfs, bfs, and manual
    // traverser respectively

    t.checkExpect(mw.getTraverser() instanceof ManualSearch, true);
    mw.onKeyEvent("b");
    t.checkExpect(((AutomaticSearch) mw.getTraverser()).getWorkList() instanceof Queue, true);
    mw.onKeyEvent("m");
    t.checkExpect(mw.getTraverser() instanceof ManualSearch, true);
    mw.onKeyEvent("d");
    t.checkExpect(((AutomaticSearch) mw.getTraverser()).getWorkList() instanceof Stack, true);

  }


}
