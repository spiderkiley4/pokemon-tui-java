package org.example;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.example.Pokemon;
import org.example.Move;

public class PokemonTUI {
  private static List<Pokemon> pokemon = new ArrayList<>();
  private static List<Move> moves = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    loadCSVData();
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen = terminalFactory.createScreen();
    screen.startScreen();

    MultiWindowTextGUI gui = new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
    BasicWindow window = new BasicWindow("Pokémon TUI Battle");
    Panel panel = new Panel();
    panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

    Button battleButton = new Button("Start Battle", PokemonTUI::startBattle);
    Button exitButton = new Button("Exit", () -> System.exit(0));

    panel.addComponent(battleButton);
    panel.addComponent(exitButton);
    window.setComponent(panel);
    gui.addWindowAndWait(window);
  }

  private static void startBattle() {
    Random rand = new Random();
    Pokemon playerPokemon = pokemon.get(rand.nextInt(pokemon.size()));
    Pokemon opponentPokemon = pokemon.get(rand.nextInt(pokemon.size()));

    System.out.println("Battle Start!");
    System.out.println("Player's Pokémon: " + playerPokemon.getName());
    System.out.println("Opponent's Pokémon: " + opponentPokemon.getName());
  }

  private static void loadCSVData() throws IOException {
    List<String> pokemonData = Files.readAllLines(Paths.get("pokemon.csv"));
    for (String line : pokemonData.subList(1, pokemonData.size())) {
      String[] values = line.split(",");
      pokemon.add(new Pokemon(values[0], Integer.parseInt(values[1]), Integer.parseInt(values[2])));
    }

    List<String> moveData = Files.readAllLines(Paths.get("moves.csv"));
    for (String line : moveData.subList(1, moveData.size())) {
      String[] values = line.split(",");
      moves.add(new Move(values[0], Integer.parseInt(values[1])));
    }
  }
}
