package org.example;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PokemonTUI {
    private static List<Pokemon> pokemon = new ArrayList<>();
    private static List<Move> moves = new ArrayList<>();
    private static boolean running = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        loadMoves();
        loadCSVData();

        // Set up the terminal in private mode
        UnixTerminal terminal = new UnixTerminal();
        terminal.enterPrivateMode();

        while (running) {
            terminal.clearScreen();
            terminal.setCursorPosition(0, 0);

            writeString(terminal, 0, 0, "Pokémon TUI Battle");
            writeString(terminal, 0, 1, "[1] Start Battle");
            writeString(terminal, 0, 2, "[2] Exit");
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

    private static void loadMoves() throws IOException {
        try (InputStream inputStream = PokemonTUI.class.getClassLoader().getResourceAsStream("metadata_pokemon_moves.csv")) {
            if (inputStream == null) {
                throw new FileNotFoundException("metadata_pokemon_moves.csv not found in resources.");
            }

            List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().toList();

            for (String line : lines.subList(1, lines.size())) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length < 9) {
                    System.err.println("Skipping invalid move: " + line);
                    continue;
                }

                String name = values[0].trim();
                double accuracy = values[2].isEmpty() ? 100.0 : Double.parseDouble(values[2].trim());
                int pp = Integer.parseInt(values[3].trim());
                double power = values[4].isEmpty() ? 0.0 : Double.parseDouble(values[4].trim());
                int priority = Integer.parseInt(values[5].trim());
                String type = values[6].trim();
                String description = values[8].trim();
                String damageClass = values[9].trim();

                moves.add(new Move(name, power, accuracy, pp, type, damageClass, priority, description));
            }
        }
    }

    private static void startBattle(Terminal terminal) throws IOException, InterruptedException {
        Random rand = new Random();
        Pokemon playerPokemon = pokemon.get(rand.nextInt(pokemon.size()));
        Pokemon opponentPokemon = pokemon.get(rand.nextInt(pokemon.size()));

        // Assign random moves to both Pokemon
        assignRandomMoves(playerPokemon);
        assignRandomMoves(opponentPokemon);

        boolean battleRunning = true;
        while (battleRunning && !playerPokemon.isFainted() && !opponentPokemon.isFainted()) {
            terminal.clearScreen();
            terminal.setCursorPosition(0, 0);

            // Draw sprites and status
            writeString(terminal, 0, 0, String.format("Player's %s (HP: %d/%d)", 
                playerPokemon.getName(), playerPokemon.getHp(), playerPokemon.getMaxHp()));
            writeString(terminal, 0, 1, String.format("Type: %s%s", 
                playerPokemon.getType1(), 
                playerPokemon.getType2() != null ? "/" + playerPokemon.getType2() : ""));
            writeANSIString(terminal, 0, 2, loadSprite(playerPokemon.getName()));

            writeString(terminal, 0, 15, String.format("Opponent's %s (HP: %d/%d)", 
                opponentPokemon.getName(), opponentPokemon.getHp(), opponentPokemon.getMaxHp()));
            writeString(terminal, 0, 16, String.format("Type: %s%s", 
                opponentPokemon.getType1(), 
                opponentPokemon.getType2() != null ? "/" + opponentPokemon.getType2() : ""));
            writeANSIString(terminal, 0, 17, loadSprite(opponentPokemon.getName()));

            // Show moves with more details
            writeString(terminal, 0, 30, "Choose your move:");
            List<Move> playerMoves = playerPokemon.getMoves();
            for (int i = 0; i < playerMoves.size(); i++) {
                Move move = playerMoves.get(i);
                writeString(terminal, 0, 31 + i, String.format("[%d] %s (%s | Power: %.0f | Accuracy: %.0f%%)", 
                    i + 1, move.getName(), move.getType(), move.getPower(), move.getAccuracy()));
            }
            writeString(terminal, 0, 35, "[x] Retreat");
            terminal.flush();

            // Handle player input
            KeyStroke key = terminal.readInput();
            if (key.getKeyType() == KeyType.Character) {
                char choice = key.getCharacter();
                if (choice == 'x') {
                    battleRunning = false;
                    continue;
                }
                
                int moveIndex = Character.getNumericValue(choice) - 1;
                if (moveIndex >= 0 && moveIndex < playerMoves.size()) {
                    // Determine turn order based on speed and move priority
                    boolean playerFirst = determineOrder(playerPokemon, opponentPokemon, 
                                                       playerMoves.get(moveIndex), 
                                                       opponentPokemon.getMoves().get(rand.nextInt(opponentPokemon.getMoves().size())));
                    
                    if (playerFirst) {
                        executeMove(terminal, playerPokemon, opponentPokemon, playerMoves.get(moveIndex), true);
                        if (!opponentPokemon.isFainted()) {
                            executeMove(terminal, opponentPokemon, playerPokemon, 
                                      opponentPokemon.getMoves().get(rand.nextInt(opponentPokemon.getMoves().size())), false);
                        }
                    } else {
                        executeMove(terminal, opponentPokemon, playerPokemon, 
                                  opponentPokemon.getMoves().get(rand.nextInt(opponentPokemon.getMoves().size())), false);
                        if (!playerPokemon.isFainted()) {
                            executeMove(terminal, playerPokemon, opponentPokemon, playerMoves.get(moveIndex), true);
                        }
                    }
                }
            }
        }

        // Show battle result
        terminal.clearScreen();
        terminal.setCursorPosition(0, 0);
        if (playerPokemon.isFainted()) {
            writeString(terminal, 0, 0, "You lost the battle!");
        } else if (opponentPokemon.isFainted()) {
            writeString(terminal, 0, 0, "You won the battle!");
        } else {
            writeString(terminal, 0, 0, "Battle ended in retreat!");
        }
        writeString(terminal, 0, 2, "[Press any key to return to menu]");
        terminal.flush();
        terminal.readInput();
    }

    private static void assignRandomMoves(Pokemon pokemon) {
        Random rand = new Random();
        // Assign 2-4 random moves
        int numMoves = rand.nextInt(3) + 2;
        List<Move> availableMoves = new ArrayList<>(moves);
        Collections.shuffle(availableMoves);
        
        for (int i = 0; i < numMoves && i < availableMoves.size(); i++) {
            pokemon.addMove(availableMoves.get(i));
        }
    }

    private static int calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        if (move.isStatus()) return 0;

        // Base damage calculation
        double level = 50; // Using a fixed level for simplicity
        int attackStat = move.isSpecial() ? attacker.getSpecialAttack() : attacker.getAttack();
        int defenseStat = move.isSpecial() ? defender.getSpecialDefense() : defender.getDefense();
        
        double baseDamage = ((2 * level + 10) / 250.0) * 
                           (attackStat / (double)defenseStat) * 
                           move.getPower() + 2;

        // Random factor (85-100%)
        double random = 0.85 + Math.random() * 0.15;
        
        // Type effectiveness
        double effectiveness = calculateTypeEffectiveness(move.getType(), defender.getType1(), defender.getType2());
        
        // STAB (Same Type Attack Bonus)
        double stab = (move.getType().equals(attacker.getType1()) || 
                      (attacker.getType2() != null && move.getType().equals(attacker.getType2()))) ? 1.5 : 1.0;
        
        return (int) Math.max(1, baseDamage * random * effectiveness * stab);
    }

    private static double calculateTypeEffectiveness(String moveType, String defenderType1, String defenderType2) {
        double effectiveness = getTypeEffectiveness(moveType, defenderType1);
        if (defenderType2 != null) {
            effectiveness *= getTypeEffectiveness(moveType, defenderType2);
        }
        return effectiveness;
    }

    private static double getTypeEffectiveness(String attackType, String defenseType) {
        // Type effectiveness matrix (simplified version)
        if (attackType.equals("Water")) {
            if (defenseType.equals("Fire") || defenseType.equals("Ground") || defenseType.equals("Rock")) return 2.0;
            if (defenseType.equals("Water") || defenseType.equals("Grass") || defenseType.equals("Dragon")) return 0.5;
        }
        else if (attackType.equals("Fire")) {
            if (defenseType.equals("Grass") || defenseType.equals("Ice") || defenseType.equals("Bug") || defenseType.equals("Steel")) return 2.0;
            if (defenseType.equals("Fire") || defenseType.equals("Water") || defenseType.equals("Rock") || defenseType.equals("Dragon")) return 0.5;
        }
        else if (attackType.equals("Grass")) {
            if (defenseType.equals("Water") || defenseType.equals("Ground") || defenseType.equals("Rock")) return 2.0;
            if (defenseType.equals("Fire") || defenseType.equals("Grass") || defenseType.equals("Poison") || defenseType.equals("Flying") ||
                defenseType.equals("Bug") || defenseType.equals("Dragon") || defenseType.equals("Steel")) return 0.5;
        }
        else if (attackType.equals("Electric")) {
            if (defenseType.equals("Water") || defenseType.equals("Flying")) return 2.0;
            if (defenseType.equals("Grass") || defenseType.equals("Electric") || defenseType.equals("Dragon")) return 0.5;
            if (defenseType.equals("Ground")) return 0.0;
        }
        // Add more type effectiveness rules...
        
        return 1.0; // Neutral effectiveness
    }

    private static boolean determineOrder(Pokemon pokemon1, Pokemon pokemon2, Move move1, Move move2) {
        if (move1.getPriority() != move2.getPriority()) {
            return move1.getPriority() > move2.getPriority();
        }
        return pokemon1.getSpeed() >= pokemon2.getSpeed();
    }

    private static void executeMove(Terminal terminal, Pokemon attacker, Pokemon defender, Move move, boolean isPlayer) 
            throws IOException, InterruptedException {
        int damage = calculateDamage(attacker, defender, move);
        defender.takeDamage(damage);
        
        String effectiveness = "";
        double typeEffect = calculateTypeEffectiveness(move.getType(), defender.getType1(), defender.getType2());
        if (typeEffect > 1.9) effectiveness = "It's super effective!";
        else if (typeEffect < 0.1) effectiveness = "It has no effect...";
        else if (typeEffect < 0.9) effectiveness = "It's not very effective...";
        
        String attackerName = isPlayer ? attacker.getName() : "Opponent's " + attacker.getName();
        writeString(terminal, 0, 36 + (isPlayer ? 0 : 1), 
            String.format("%s used %s!", attackerName, move.getName()));
        writeString(terminal, 0, 37 + (isPlayer ? 0 : 1), 
            String.format("Dealt %d damage! %s", damage, effectiveness));
        terminal.flush();
        Thread.sleep(1000);
    }

    private static void writeANSIString(Terminal terminal, int x, int y, String ansiText) throws IOException {
        String[] lines = ansiText.split("\n");
        int currentY = y;
        
        for (String line : lines) {
            terminal.setCursorPosition(x, currentY);
            List<ANSIParser.ANSIStyle> styles = ANSIParser.parse(line);
            int currentX = x;
            
            for (ANSIParser.ANSIStyle style : styles) {
                terminal.setCursorPosition(currentX, currentY);
                
                if (style.foreground != null) {
                    terminal.setForegroundColor(style.foreground);
                }
                if (style.background != null) {
                    terminal.setBackgroundColor(style.background);
                }
                
                // Write the text
                for (char c : style.text.toCharArray()) {
                    terminal.putCharacter(c);
                    currentX++;
                }
            }
            
            // Reset colors after each line
            terminal.setForegroundColor(TextColor.ANSI.DEFAULT);
            terminal.setBackgroundColor(TextColor.ANSI.DEFAULT);
            currentY++;
        }
    }

    private static TextColor convertColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "black" -> TextColor.ANSI.BLACK;
            case "red" -> TextColor.ANSI.RED;
            case "green" -> TextColor.ANSI.GREEN;
            case "yellow" -> TextColor.ANSI.YELLOW;
            case "blue" -> TextColor.ANSI.BLUE;
            case "magenta" -> TextColor.ANSI.MAGENTA;
            case "cyan" -> TextColor.ANSI.CYAN;
            case "white" -> TextColor.ANSI.WHITE;
            case "bright_black" -> TextColor.ANSI.BLACK_BRIGHT;
            case "bright_red" -> TextColor.ANSI.RED_BRIGHT;
            case "bright_green" -> TextColor.ANSI.GREEN_BRIGHT;
            case "bright_yellow" -> TextColor.ANSI.YELLOW_BRIGHT;
            case "bright_blue" -> TextColor.ANSI.BLUE_BRIGHT;
            case "bright_magenta" -> TextColor.ANSI.MAGENTA_BRIGHT;
            case "bright_cyan" -> TextColor.ANSI.CYAN_BRIGHT;
            case "bright_white" -> TextColor.ANSI.WHITE_BRIGHT;
            default -> TextColor.ANSI.DEFAULT;
        };
    }

    private static void writeString(Terminal terminal, int x, int y, String text) throws IOException {
        terminal.setCursorPosition(x, y);
        for (char c : text.toCharArray()) {
            terminal.putCharacter(c);
        }
        terminal.flush();
    }

    private static void loadCSVData() throws IOException {
        try (InputStream inputStream = PokemonTUI.class.getClassLoader().getResourceAsStream("metadata_pokemon.csv")) {
            if (inputStream == null) {
                throw new FileNotFoundException("metadata_pokemon.csv not found in resources.");
            }

            List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().toList();

            for (String line : lines.subList(1, lines.size())) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length < 11) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }

                try {
                    String name = values[0].trim();
                    int hp = Integer.parseInt(values[2].trim());
                    int attack = Integer.parseInt(values[3].trim());
                    int defense = Integer.parseInt(values[4].trim());
                    int specialAttack = Integer.parseInt(values[5].trim());
                    int specialDefense = Integer.parseInt(values[6].trim());
                    int speed = Integer.parseInt(values[7].trim());
                    String type1 = values[10].trim();
                    String type2 = values.length > 11 && !values[11].trim().isEmpty() ? values[11].trim() : null;

                    pokemon.add(new Pokemon(name, hp, attack, defense, specialAttack, specialDefense, speed, type1, type2));
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
}
