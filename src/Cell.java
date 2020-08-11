import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;

import javalib.worldimages.Posn;

// represents a single square of a maze grid
class Cell {

  // the coordinates of the cell, in grid coordinates (not pixel)
  private final Posn position;

  // the edges connecting this cell to adjacent cells with no wall in between
  private final ArrayList<Edge> connections;

  // constructor
  // initializes the cell with the given position and no connections to adjacent
  // cells
  Cell(Posn position) {
    this.position = position;
    this.connections = new ArrayList<>();
  }

  // add an edge to this cell's list of edges to adjacent cells
  // throws an error if the given edge is already in the list
  void addNeighbor(Edge e) {
    if (this.connections.contains(e)) {
      throw new IllegalArgumentException("The given edge already exists in this cell.");
    }
    else {
      this.connections.add(e);
    }
  }

  // remove an edge form this cell's list of edges to adjacent cells
  // throws an error if the given edge is not in the list
  void removeEdge(Edge e) {
    if (!this.connections.contains(e)) {
      throw new NoSuchElementException("This cell does not have the given edge.");
    }
    this.connections.remove(e);
  }

  // determines if this cell is connected to a neighbor at the given displacement
  boolean hasNeighbor(Posn displacement) {
    Posn target = new Utils().addPosn(this.position, displacement);
    for (Edge e : this.connections) {
      if (e.connectedCellPositions().contains(target)) {
        return true;
      }
    }
    return false;
  }

  // returns the adjacent cell neighbor at the given displacement if there is one
  // throws an exception if there is not a neighbor in that direction
  // used for moving the player cell during manual traversal of the maze
  Cell getNeighbor(Posn displacement) {
    Posn target = new Utils().addPosn(this.position, displacement);
    for (Edge e : this.connections) {
      if (e.connectedCellPositions().contains(target)) {
        return e.findOther(this);
      }
    }
    throw new IllegalArgumentException(
        "The cell doesn't have a neighbor at the given displacement.");
  }

  // Two Cells are equal if they are at the same position and have neighbors in the same direction
  // Does not verify that the neighbors themselves correspond
  public boolean equals(Object o) {
    if (! (o instanceof Cell)) {
      return false;
    } else {
      Utils u = new Utils();
      Cell other = (Cell) o;

      return other.getPosition().equals(this.getPosition())
          && ((this.hasNeighbor(u.directionToDisplacement("up"))) 
              == other.hasNeighbor(u.directionToDisplacement("up")))
          && ((this.hasNeighbor(u.directionToDisplacement("down"))) 
              == other.hasNeighbor(u.directionToDisplacement("down")))
          && ((this.hasNeighbor(u.directionToDisplacement("left"))) 
              == other.hasNeighbor(u.directionToDisplacement("left")))
          && ((this.hasNeighbor(u.directionToDisplacement("right"))) 
              == other.hasNeighbor(u.directionToDisplacement("right")));
    }
  }

  // The hash code of the cell is just the hash code of its position
  public int hashCode() {
    return this.position.hashCode();
  }

  // returns this cell's list of edge connections to adjacent cells
  // used inside MazeSearch's search iteration algorithm when updating MazeSearch
  // fields
  ArrayList<Edge> getConnections() {
    return this.connections;
  }

  //returns this' position
  // used as keys for the union/find representatives to avoid gratuitous dispatch
  Posn getPosition() {
    return this.position;
  }
  
  // is the given neighboring cell directly above or directly below this cell?
  boolean neighborsVerticallyWith(Cell other) {
    Utils u = new Utils();
    Posn displacementToOther = u.subtractPosn(this.position, other.getPosition());
    // horizontally related (to the left or to the right)
    if (displacementToOther.equals(u.directionToDisplacement("left"))
        || displacementToOther.equals(u.directionToDisplacement("right"))) {
      return false;
    } else if (displacementToOther.equals(u.directionToDisplacement("up"))
        || displacementToOther.equals(u.directionToDisplacement("down"))) {
      return true;
    } else {
      throw new IllegalArgumentException("The given cell is not a neighbor of this cell.");
    }
    
  }
}

// represents a connection between two squares in the maze grid
// (represents a lack of a wall)
class Edge implements Comparable<Edge> {

  // the two cells that this edge connects
  private final Pair<Cell> cells;

  // the difficulty of traversing this edge, used for calculating the minimum
  // spanning tree of the grid to produce a maze
  // TODO: make private
  final double weight;

  // convenience constructor for testing edge weights 
  // creates an edge with a specified weight between the two given cells
  // EFFECT: adds this edge to the given cells
  Edge(Cell cell1, Cell cell2, double weight) {
    this.cells = new Pair<Cell>(cell1, cell2);
    this.weight = weight;

    cell1.addNeighbor(this);
    cell2.addNeighbor(this);
  }

  // constructor creates an edge with a random weight
  // that takes the bias into consideration upon generation
  // Note: double for 'bias' is placed at front to differentiate from constructor that uses 'weight'
  // EFFECT: adds this edge to the given cells
  Edge(double bias, Cell cell1, Cell cell2) {
    this(cell1, cell2, new Utils().generateEdgeWeight(cell1, cell2, bias));
//    this.cells = new Pair<Cell>(cell1, cell2);
//    cell1.addNeighbor(this);
//    cell2.addNeighbor(this);
//    this.weight = this.generateWeight(cell1, cell2, verticallyBiasedOverall);
  }

  // compare this edge to another edge based on weight
  // return a positive number if this edge's weight is larger, a 0 if they're
  // equal, and a
  // negative number if this edge's weight is smaller
  public int compareTo(Edge other) {
    double difference = other.weightDifference(this.weight); // this.weight - other.weight
    if (difference > 0) {
      return 1; 
    }
    else if (difference == 0) {
      return 0;
    }
    else {
      return -1;
    }
  }

  // calculates the difference in weight between some other weight and this edge's
  // weight
  // used to compare two edges based on weight
  double weightDifference(double weightOther) {
    return weightOther - this.weight;
  }

  // returns the positions of the cells connected by this edge
  // used to reconstruct the solution path from BFS and DFS, generate the minimum
  // spanning tree
  // of the maze, and visually knock down walls
  Pair<Posn> connectedCellPositions() {
    // Pair is like Posn, allowing field of field
    return new Pair<Posn>(this.cells.first.getPosition(), this.cells.second.getPosition());
  }

  // EFFECT: removes this edge from the maze grid by removing it from both of the
  // cells it connects
  void removeSelf() {
    // Pair is like Posn, allowing field of field
    this.cells.first.removeEdge(this);
    this.cells.second.removeEdge(this);
  }

  // given a cell, finds the other cell that this edge connects
  // throws an error if the given cell isn't connected by this edge
  Cell findOther(Cell c) {
    return this.cells.getOther(c);
  }

  // For testing only, to ensure that the cells the edge says it connects are actually connected
  Pair<Cell> getConnectedCells() {
    return this.cells;
  }

  // Two edges are equal if they have the same weight and if they have the same cells in 
  // either order in the pair
  public boolean equals(Object o) {
    if (! (o instanceof Edge)) {
      return false;
    } else {
      Edge other = (Edge) o;
      return other.weightDifference(this.weight) == 0
          && other.getConnectedCells().contains(this.getConnectedCells().first)
          && other.getConnectedCells().contains(this.getConnectedCells().second);
    }
  }

  // Hash code must be commutative such that it does not matter which cell is in 
  // which position of the pair
  public int hashCode() {
    return this.connectedCellPositions().first.hashCode() 
        + this.connectedCellPositions().second.hashCode();
  }
}
  

// To represent two elements of the same type 
class Pair<T> {

  // the first element that this pair contains
  // not private, since a pair is like a posn: it's just a convenient wrapper
  T first;

  // the second element that this pair contains
  // not private, since a pair is like a posn: it's just a convenient wrapper
  T second;

  // default constructor initializes fields
  Pair(T first, T second) {
    this.first = first;
    this.second = second;
  }

  // does this pair contain the given element?
  boolean contains(T target) {
    return this.first.equals(target) || this.second.equals(target);
  }

  // return the element in this pair that isn't the given element
  // if the given element isn't contained in this pair, throw an error
  T getOther(T item) {
    if (this.first.equals(item)) {
      return this.second;
    }
    else if (this.second.equals(item)) {
      return this.first;
    }
    else {
      throw new IllegalArgumentException(
          "The given element must be one of the element this pair contains.");
    }
  }
}

// a function object that compares the weight of two edges
class EdgeWeightComparator implements Comparator<Edge> {
  public int compare(Edge left, Edge right) {
    return left.compareTo(right);
  }
}
