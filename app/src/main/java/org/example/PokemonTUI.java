package org.example;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PokemonTUI {
  private static List<Pokemon> pokemon = new ArrayList<>();
  private static boolean running = true;

  public static void main(String[] args) throws IOException {
    loadCSVData();

    // Set up the terminal to use the real console
    Terminal terminal = new DefaultTerminalFactory(System.out, System.in, StandardCharsets.UTF_8).createTerminal();
    Screen screen = new DefaultTerminalFactory().setTerminal(terminal).createScreen();
    screen.startScreen();
    TextGraphics tg = screen.newTextGraphics();

    while (running) {
      screen.clear();
      tg.putString(5, 2, "Pokémon TUI Battle");
      tg.putString(5, 4, "[1] Start Battle");
      tg.putString(5, 5, "[2] Exit");
      screen.refresh();

      KeyStroke key = screen.readInput();
      if (key.getKeyType() == KeyType.Character) {
        char choice = key.getCharacter();
        if (choice == '1') {
          startBattle(screen, tg);
        } else if (choice == '2') {
          running = false;
        }
      }
    }

    screen.stopScreen();
  }

  private static void startBattle(Screen screen, TextGraphics tg) throws IOException {
    Random rand = new Random();
    Pokemon playerPokemon = pokemon.get(rand.nextInt(pokemon.size()));
    Pokemon opponentPokemon = pokemon.get(rand.nextInt(pokemon.size()));

    screen.clear();

    // Load sprite as ANSI string
    String playerSpriteAnsi = loadSprite(playerPokemon.getName());
    String opponentSpriteAnsi = loadSprite(opponentPokemon.getName());

    // Draw parsed ANSI sprites
    drawSprite(tg, playerSpriteAnsi, 5, 5);
    drawSprite(tg, opponentSpriteAnsi, 40, 5);

    // Display names
    tg.setForegroundColor(TextColor.ANSI.WHITE);
    tg.putString(5, 4, "Player's Pokémon: " + playerPokemon.getName());
    tg.putString(40, 4, "Opponent's Pokémon: " + opponentPokemon.getName());

    tg.putString(5, 20, "[Press any key to return to menu]");

    // Refresh before waiting for input!
    screen.refresh();

    // Wait for keypress to return
    screen.readInput();
  }

  private static void loadCSVData() throws IOException {
    try (InputStream inputStream = PokemonTUI.class.getClassLoader().getResourceAsStream("pokemon.csv")) {
      if (inputStream == null) {
        throw new FileNotFoundException("pokemon.csv not found in resources.");
      }

      List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().toList();

      for (String line : lines.subList(1, lines.size())) { // Skip header
        if (line.trim().isEmpty()) continue;

        String[] values = line.split(",", 3); // Only 3 columns now: name, hp, attack
        if (values.length < 3) {
          System.err.println("Skipping invalid line: " + line);
          continue;
        }

        try {
          String name = values[0].trim();
          int hp = Integer.parseInt(values[1].trim());
          int attack = Integer.parseInt(values[2].trim());

          pokemon.add(new Pokemon(name, hp, attack));
        } catch (NumberFormatException e) {
          System.err.println("Error parsing line (invalid number format): " + line);
        }
      }
    }
  }

  private static String loadSprite(String name) throws IOException {
    try (InputStream input = PokemonTUI.class.getResourceAsStream("/colorscripts/small/regular/" + name.toLowerCase())) {
      if (input == null) throw new FileNotFoundException("Missing sprite: " + name);
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static void drawSprite(TextGraphics graphics, String ansiText, int x, int y) {
    String[] lines = ansiText.split("\n");
    for (int row = 0; row < lines.length; row++) {
      int col = 0;
      for (ANSIParser.ParsedCharacter pc : ANSIParser.parse(lines[row])) {
        graphics.setForegroundColor(pc.color);
        graphics.putString(x + col, y + row, String.valueOf(pc.character));
        col++;
      }
    }
  }
}
