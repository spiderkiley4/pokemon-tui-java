package org.example;

import java.util.ArrayList;
import java.util.List;

public class Pokemon {
    private String name;
    private int hp, maxHp;
    private int attack, defense;
    private int specialAttack, specialDefense;
    private int speed;
    private String type1, type2;
    private List<Move> moves;

    public Pokemon(String name, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, String type1, String type2) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.type1 = type1;
        this.type2 = type2;
        this.moves = new ArrayList<>();
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpecialAttack() { return specialAttack; }
    public int getSpecialDefense() { return specialDefense; }
    public int getSpeed() { return speed; }
    public String getType1() { return type1; }
    public String getType2() { return type2; }
    public List<Move> getMoves() { return moves; }
    
    public void addMove(Move move) {
        if (moves.size() < 4) {
            moves.add(move);
        }
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public boolean isFainted() {
        return hp <= 0;
    }
}
