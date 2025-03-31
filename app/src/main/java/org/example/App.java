package org.example;

import org.example.Point3D;
import java.util.Arrays;

public class App {
  static final int WIDTH = 80;
  static final int LENGTH = 40;
  static final double DISTANCE = 5.0;

  static Point3D[] cube = {
    new Point3D(-1,-1,-1),
    new Point3D( 1,-1,-1),
    new Point3D( 1, 1,-1),
    new Point3D(-1, 1,-1),
    new Point3D(-1,-1, 1),
    new Point3D( 1,-1, 1),
    new Point3D(-1, 1, 1),
    new Point3D( 1, 1, 1),
  }

  public static void main(String[] args) throws InteruptedException {
    while(true) {
      char[] screen = new char[HEIGHT][WIDTH];
      for(char[] row : screen) {
        Arrays.fill(row, "");
      }
      for(Point3D p : cube) {
        double rotatedX = p.x * Math.cos(System.currentTimeMilis);
        double rotatedY = p.y * Math.cos(System.currentTimeMilis);
      }
    }
  }
}
