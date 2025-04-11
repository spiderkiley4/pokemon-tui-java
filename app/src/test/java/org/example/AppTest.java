package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.logging.Logger;

class AppTest {
    private static final Logger logger = Logger.getLogger(AppTest.class.getName());

    @Test
    void testPokemonCreation() {
        logger.info("Starting testPokemonCreation");
        Pokemon pokemon = new Pokemon("Charizard", 78, 84, 78, 109, 85, 100, "Fire", "Flying");
        
        logger.info("Asserting Pokemon properties");
        assertEquals("Charizard", pokemon.getName(), "Pokemon name mismatch");
        assertEquals(78, pokemon.getHp(), "Pokemon HP mismatch");
        assertEquals(78, pokemon.getMaxHp(), "Pokemon Max HP mismatch");
        assertEquals(84, pokemon.getAttack(), "Pokemon Attack mismatch");
        assertEquals(78, pokemon.getDefense(), "Pokemon Defense mismatch");
        assertEquals(109, pokemon.getSpecialAttack(), "Pokemon Special Attack mismatch");
        assertEquals(85, pokemon.getSpecialDefense(), "Pokemon Special Defense mismatch");
        assertEquals(100, pokemon.getSpeed(), "Pokemon Speed mismatch");
        assertEquals("Fire", pokemon.getType1(), "Pokemon Type1 mismatch");
        assertEquals("Flying", pokemon.getType2(), "Pokemon Type2 mismatch");
        logger.info("testPokemonCreation completed successfully");
    }

    @Test
    void testMoveCreation() {
        logger.info("Starting testMoveCreation");
        Move move = new Move("Thunderbolt", 90, 100.0, 15, "Electric", "Special", 0, "Has a $effect_chance% chance to paralyze the target.");
        
        logger.info("Asserting Move properties");
        assertEquals("Thunderbolt", move.getName(), "Move name mismatch");
        assertEquals(90, move.getPower(), "Move power mismatch");
        assertEquals(100.0, move.getAccuracy(), "Move accuracy mismatch");
        assertEquals(15, move.getPp(), "Move PP mismatch");
        assertEquals("Electric", move.getType(), "Move type mismatch");
        assertEquals("Special", move.getDamageClass(), "Move damage class mismatch");
        assertTrue(move.isSpecial(), "Move should be special");
        assertFalse(move.isPhysical(), "Move should not be physical");
        logger.info("testMoveCreation completed successfully");
    }

    @Test
    void testDamageCalculation() {
        Pokemon attacker = new Pokemon("Charizard", 78, 84, 78, 109, 85, 100, "Fire", "Flying");
        Pokemon defender = new Pokemon("Venusaur", 80, 82, 83, 100, 100, 80, "Grass", "Poison");
        Move move = new Move("Flamethrower", 90, 100.0, 15, "Fire", "Special", 0, "Has a $effect_chance% chance to burn the target.");
        
        attacker.addMove(move);
        
        // Fire is super effective against Grass (2x) and neutral against Poison (1x)
        // STAB bonus should apply (1.5x) because Charizard is Fire type
        // Base power is 90, attacker's Special Attack is 109, defender's Special Defense is 100
        
        // Test that damage is non-zero and within expected range
        int damage = calculateDamage(attacker, defender, move);
        assertTrue(damage > 0);
        assertTrue(damage < attacker.getSpecialAttack() * 3); // Damage shouldn't exceed reasonable bounds
    }

    @Test
    void testTypeEffectiveness() {
        Pokemon waterPokemon = new Pokemon("Blastoise", 79, 83, 100, 85, 105, 78, "Water", null);
        Pokemon grassPokemon = new Pokemon("Venusaur", 80, 82, 83, 100, 100, 80, "Grass", "Poison");
        Move waterMove = new Move("Hydro Pump", 110, 80.0, 5, "Water", "Special", 0, "Inflicts regular damage with no additional effect.");
        
        // Water is not very effective against Grass
        double effectiveness = calculateTypeEffectiveness("Water", "Grass", null);
        assertEquals(0.5, effectiveness);
        
        // Water is neutral against Poison
        effectiveness = calculateTypeEffectiveness("Water", "Poison", null);
        assertEquals(1.0, effectiveness);
    }

    @Test
    void testFaintingMechanic() {
        Pokemon pokemon = new Pokemon("Pikachu", 35, 55, 40, 50, 50, 90, "Electric", null);
        assertFalse(pokemon.isFainted());
        
        pokemon.takeDamage(pokemon.getHp());
        assertTrue(pokemon.isFainted());
    }

    @Test
    void testMoveManagement() {
        Pokemon pokemon = new Pokemon("Charizard", 78, 84, 78, 109, 85, 100, "Fire", "Flying");
        Move move1 = new Move("Flamethrower", 90, 100.0, 15, "Fire", "Special", 0, "Has a $effect_chance% chance to burn the target.");
        Move move2 = new Move("Air Slash", 75, 95.0, 15, "Flying", "Special", 0, "Has a $effect_chance% chance to make the target flinch.");
        
        pokemon.addMove(move1);
        pokemon.addMove(move2);
        
        assertEquals(2, pokemon.getMoves().size());
        assertEquals("Flamethrower", pokemon.getMoves().get(0).getName());
        assertEquals("Air Slash", pokemon.getMoves().get(1).getName());
    }

    @Test
    void testMoveLimitation() {
        Pokemon pokemon = new Pokemon("Charizard", 78, 84, 78, 109, 85, 100, "Fire", "Flying");
        
        // Try to add more than 4 moves
        for (int i = 0; i < 5; i++) {
            pokemon.addMove(new Move("Move" + i, 50, 100.0, 15, "Normal", "Physical", 0, "Test move"));
        }
        
        // Should still only have 4 moves
        assertEquals(4, pokemon.getMoves().size());
    }

    private double calculateTypeEffectiveness(String moveType, String defenderType1, String defenderType2) {
        if (moveType.equals("Water")) {
            if (defenderType1.equals("Fire") || defenderType1.equals("Ground") || defenderType1.equals("Rock")) return 2.0;
            if (defenderType1.equals("Water") || defenderType1.equals("Grass") || defenderType1.equals("Dragon")) return 0.5;
            if (defenderType2 != null) {
                if (defenderType2.equals("Fire") || defenderType2.equals("Ground") || defenderType2.equals("Rock")) return 2.0;
                if (defenderType2.equals("Water") || defenderType2.equals("Grass") || defenderType2.equals("Dragon")) return 0.5;
            }
        }
        return 1.0;
    }

    private int calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        if (move.isStatus()) return 0;

        double level = 50;
        int attackStat = move.isSpecial() ? attacker.getSpecialAttack() : attacker.getAttack();
        int defenseStat = move.isSpecial() ? defender.getSpecialDefense() : defender.getDefense();
        
        double baseDamage = ((2 * level + 10) / 250.0) * 
                           (attackStat / (double)defenseStat) * 
                           move.getPower() + 2;

        double random = 0.85 + Math.random() * 0.15;
        double effectiveness = calculateTypeEffectiveness(move.getType(), defender.getType1(), defender.getType2());
        double stab = (move.getType().equals(attacker.getType1()) || 
                      (attacker.getType2() != null && move.getType().equals(attacker.getType2()))) ? 1.5 : 1.0;
        
        return (int) Math.max(1, baseDamage * random * effectiveness * stab);
    }
}
