package org.example;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.nio.charset.Charset;

public class LanternaConsoleExample {
    public static void main(String[] args) {
        try {
            // Initialize the terminal for console (real console, not Swing)
            Terminal terminal = new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF-8")).createTerminal();
            
            // Now you can use the terminal
            terminal.enterPrivateMode();  // Entering private mode (optional)
            terminal.putCharacter('H');
            terminal.flush();
            terminal.readInput();  // Wait for key press

            // Exit private mode and stop terminal
            terminal.exitPrivateMode();
            terminal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
