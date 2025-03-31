package org.example;

import org.example.Point3D;
import java.util.Arrays;
import java.lang.InterruptedException;

public class App {
  static final int WIDTH = 80;
  static final int HEIGHT = 40;
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
  };

  public static void main(String[] args) throws InterruptedException {
    while(true) {
      char[][] screen = new char[HEIGHT][WIDTH];
      for(char[] row : screen) {
        Arrays.fill(row, ' ');
      }
      for (Point3D p : cube) {
        // Apply simple rotation
        double rotatedX = p.x * Math.cos(System.currentTimeMillis() * 0.001) - p.z * Math.sin(System.currentTimeMillis() * 0.001);
        double rotatedZ = p.x * Math.sin(System.currentTimeMillis() * 0.001) + p.z * Math.cos(System.currentTimeMillis() * 0.001);
        double rotatedY = p.y;

        // Perspective projection
        double projX = (rotatedX / (rotatedZ + DISTANCE)) * 10 + WIDTH / 2;
        double projY = (rotatedY / (rotatedZ + DISTANCE)) * 10 + HEIGHT / 2;

        int px = (int) projX;
        int py = (int) projY;

        if (px >= 0 && px < WIDTH && py >= 0 && py < HEIGHT) {
          screen[py][px] = '#';
        }
      }
      // Clear screen
      System.out.print("\033[H\033[2J");
      System.out.flush();

      // Print screen
      for (char[] row : screen) {
        System.out.println(row);
      }

      Thread.sleep(50);
    }
  }
}
