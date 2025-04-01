package org.example;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PokemonTUI {
  private static List<Pokemon> pokemon = new ArrayList<>();
  private static List<Move> moves = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    loadCSVData();
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen = terminalFactory.createScreen();
    screen.startScreen();

    MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
    BasicWindow window = new BasicWindow("Pokémon TUI Battle");
    Panel panel = new Panel();
    panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

    Button battleButton = new Button("Start Battle", PokemonTUI::startBattle);
    Button exitButton = new Button("Exit", () -> System.exit(0));

    panel.addComponent(battleButton);
    panel.addComponent(exitButton);
    window.setComponent(panel);
    gui.addWindowAndWait(window);
    //gui.updateScreen();
  }

  private static void startBattle() {
    Random rand = new Random();
    Pokemon playerPokemon = pokemon.get(rand.nextInt(pokemon.size()));
    Pokemon opponentPokemon = pokemon.get(rand.nextInt(pokemon.size()));

    BasicWindow battleWindow = new BasicWindow("Battle!");
    Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

    panel.addComponent(new Label("Player's Pokémon: " + playerPokemon.getName()));
    panel.addComponent(new Label(playerPokemon.getSprite()));

    panel.addComponent(new Label("Opponent's Pokémon: " + opponentPokemon.getName()));
    panel.addComponent(new Label(opponentPokemon.getSprite()));

    Button closeButton = new Button("Close", battleWindow::close);
    panel.addComponent(closeButton);

    battleWindow.setComponent(panel);
    MultiWindowTextGUI gui = new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), new DefaultTerminalFactory().createScreen());
    gui.addWindowAndWait(battleWindow);
  }


  private static void loadCSVData() throws IOException {
    try (InputStream inputStream = PokemonTUI.class.getClassLoader().getResourceAsStream("pokemon.csv")) {
      if (inputStream == null) {
        throw new FileNotFoundException("pokemon.csv not found in resources.");
      }
      List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines().toList();
      for (String line : lines.subList(1, lines.size())) {
        String[] values = line.split(",");
        String name = values[0];
        int hp = Integer.parseInt(values[1]);
        int attack = Integer.parseInt(values[2]);
        String sprite = values[3].replace("\\n", "\n"); // Ensure newlines work properly
        pokemon.add(new Pokemon(name, hp, attack, sprite));
      }
    }
  }


}
