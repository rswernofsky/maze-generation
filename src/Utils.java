import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;

// a utility class for methods that don't really belong anywhere else
public class Utils {

  // return a new posn from the summed components of the two given positions
  Posn addPosn(Posn p1, Posn p2) {
    return new Posn(p1.x + p2.x, p1.y + p2.y);
  }
  
  // return a new posn from the subtracted components of the second position subtracted from 
  // the first position
  Posn subtractPosn(Posn p1, Posn p2) {
    return new Posn(p1.x - p2.x, p1.y - p2.y);
  }

  // Returns position corresponding to the displacement of a unit vector in the
  // given direction, using image coordinates
  // Valid directions: "up", "down", "left", "right"
  // Throws exception if invalid direction
  Posn directionToDisplacement(String direction) {
    if (direction.equals("up")) {
      return new Posn(0, -1);
    }
    else if (direction.equals("down")) {
      return new Posn(0, 1);
    }
    else if (direction.equals("left")) {
      return new Posn(-1, 0);
    }
    else if (direction.equals("right")) {
      return new Posn(1, 0);
    }
    else {
      throw new IllegalArgumentException("Direction is not one of: up, down, left, right");
    }
  }
  
  // Places the given image at the center of the cell corresponding to the given cell coordinates
  // EFFECT: Places an image onto the given scene
  void drawImageAtCellCoordinates(WorldScene scene, WorldImage img, Posn cellCoord) {
    scene.placeImageXY(img, (int) ((cellCoord.x + .5) * IConstant.CELL_WIDTH),
        (int) ((cellCoord.y + .5) * IConstant.CELL_WIDTH));
  }

  // Returns a solid square that fits inside one Maze Cell of the given color
  WorldImage makeInteriorSquare(Color c) {
    return new RectangleImage(IConstant.INTERIOR_WIDTH, IConstant.INTERIOR_WIDTH, OutlineMode.SOLID,
        c);
  }
  
  <T> ArrayList<T> withoutDuplicates(ArrayList<T> al) {
    ArrayList<T> result = new ArrayList<>();
    for (T item : al) {
      if (! result.contains(item)) {
        result.add(item);
      }
    }
    return result;
  }
  
  //generate a vertically/horizontally biased weight depending on how the given cells
  // are positioned and what the bias is
  double generateEdgeWeight(Cell cell1, Cell cell2, double bias) {
    // Determine how given cells are related (either horizontally or vertically)
    boolean cellsVertical = cell1.neighborsVerticallyWith(cell2);
    // Begin with a random double [25, 75)
    double unbiasedWeight = (new Random().nextDouble() * 50) + 25;

     // If cells are vertically related, a positive (vertical) bias should reduce the weight (thereby increase the chance
    // that the wall b/w is "knocked down"
    // whereas if cells are horizontally related, a negative (horizontal) bias should reduce the weight
    if (cellsVertical) {
      return unbiasedWeight - 25 * bias;
    } else {
      return unbiasedWeight + 25 * bias;
    }  
  }
}