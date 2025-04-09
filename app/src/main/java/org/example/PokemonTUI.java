package org.example;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
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

    // Set up the terminal in private mode
    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    terminal.enterPrivateMode();

    while (running) {
      terminal.clearScreen();
      terminal.setCursorPosition(0, 0);
      terminal.writer().println("Pokémon TUI Battle");
      terminal.writer().println("[1] Start Battle");
      terminal.writer().println("[2] Exit");
      terminal.flush();

      KeyStroke key = terminal.readInput();
      if (key.getKeyType() == KeyType.Character) {
        char choice = key.getCharacter();
        if (choice == '1') {
          startBattle(terminal);
        } else if (choice == '2') {
          running = false;
        }
      }
    }

    terminal.exitPrivateMode();
    terminal.close();
  }

  private static void startBattle(Terminal terminal) throws IOException {
    Random rand = new Random();
    Pokemon playerPokemon = pokemon.get(rand.nextInt(pokemon.size()));
    Pokemon opponentPokemon = pokemon.get(rand.nextInt(pokemon.size()));

    terminal.clearScreen();
    terminal.setCursorPosition(0, 0);

    // Load sprite as ANSI string
    String playerSpriteAnsi = loadSprite(playerPokemon.getName());
    String opponentSpriteAnsi = loadSprite(opponentPokemon.getName());

    // Draw parsed ANSI sprites
    terminal.writer().println("Player's Pokémon: " + playerPokemon.getName());
    terminal.writer().println(playerSpriteAnsi);
    terminal.writer().println("Opponent's Pokémon: " + opponentPokemon.getName());
    terminal.writer().println(opponentSpriteAnsi);

    terminal.writer().println("[Press any key to return to menu]");
    terminal.flush();

    // Wait for keypress to return
    terminal.readInput();
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
