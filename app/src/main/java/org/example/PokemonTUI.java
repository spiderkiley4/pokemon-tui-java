package org.example;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PokemonTUI {
  private static List<Pokemon> pokemon = new ArrayList<>();
  private static boolean running = true;

  public static void main(String[] args) throws IOException {
    loadCSVData();

    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen = terminalFactory.createScreen();
    screen.startScreen();

    while (running) {
      drawMainMenu(screen);
      handleUserInput(screen);
    }

    screen.stopScreen();
  }

  // Draw the main menu
  private static void drawMainMenu(Screen screen) throws IOException {
    TextGraphics tg = screen.newTextGraphics();
    screen.clear();

    tg.putString(5, 2, "POKÉMON TUI BATTLE", com.googlecode.lanterna.SGR.BOLD);
    tg.putString(5, 4, "[S] Start Battle");
    tg.putString(5, 5, "[Q] Quit");

    screen.refresh();
  }

  // Handle user input
  private static void handleUserInput(Screen screen) throws IOException {
    KeyStroke keyStroke = screen.pollInput();
    if (keyStroke == null) return;

    KeyType keyType = keyStroke.getKeyType();
    Character c = keyStroke.getCharacter();

    if (keyType == KeyType.Character && c != null) {
      if (c == 's' || c == 'S') {
        startBattle(screen);
      } else if (c == 'q' || c == 'Q') {
        running = false;
      }
    }
  }

  // Start battle and display Pokémon sprites
  private static void startBattle(Screen screen) throws IOException {
    Random rand = new Random();
    Pokemon playerPokemon = pokemon.get(rand.nextInt(pokemon.size()));
    Pokemon opponentPokemon = pokemon.get(rand.nextInt(pokemon.size()));

    screen.clear();
    TextGraphics tg = screen.newTextGraphics();

    tg.putString(5, 2, "BATTLE START!", com.googlecode.lanterna.SGR.BOLD);
    tg.putString(5, 4, "Player's Pokémon: " + playerPokemon.getName());
    drawPokemonSprite(tg, playerPokemon.getSprite(), 5, 6);

    tg.putString(40, 4, "Opponent's Pokémon: " + opponentPokemon.getName());
    drawPokemonSprite(tg, opponentPokemon.getSprite(), 40, 6);

    tg.putString(5, 15, "[B] Back to Main Menu");

    screen.refresh();

    while (true) {
      KeyStroke keyStroke = screen.readInput();
      if (keyStroke != null) {
        if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 'b') {
          break;
        }
      }
    }
  }

  // Draw a Pokémon sprite manually using TextGraphics
  private static void drawPokemonSprite(TextGraphics tg, String sprite, int x, int y) {
    String[] lines = sprite.split("\n");
    for (int i = 0; i < lines.length; i++) {
      tg.putString(x, y + i, lines[i]);
    }
  }

  // Load Pokémon data from CSV
  private static void loadCSVData() throws IOException {
    try (InputStream inputStream = PokemonTUI.class.getClassLoader().getResourceAsStream("pokemon.csv")) {
      if (inputStream == null) {
        throw new FileNotFoundException("pokemon.csv not found in resources.");
      }

      List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().toList();

      for (String line : lines.subList(1, lines.size())) { // Skip header
        if (line.trim().isEmpty()) continue;

        String[] values = line.split(",", 4);
        if (values.length < 4) {
          System.err.println("Skipping invalid line: " + line);
          continue;
        }

        try {
          String name = values[0].trim();
          int hp = Integer.parseInt(values[1].trim());
          int attack = Integer.parseInt(values[2].trim());
          String sprite = values[3].trim().replace("\\n", "\n");

          pokemon.add(new Pokemon(name, hp, attack, sprite));
        } catch (NumberFormatException e) {
          System.err.println("Error parsing line (invalid number format): " + line);
        }
      }
    }
  }

  private static List<String> loadSprite(String name, boolean shiny) throws IOException {
    String basePath = "colorscripts/" + (shiny ? "shiny" : "regular") + "/small/";
    Path spritePath = Paths.get(basePath + name);

    if (!Files.exists(spritePath)) {
      throw new FileNotFoundException("Sprite not found: " + spritePath);
    }

    return Files.readAllLines(spritePath, StandardCharsets.UTF_8);
  }

}
