package org.example;

public class Pokemon {
    private String name;
    private int hp, attack;
    private String sprite;

    public Pokemon(String name, int hp, int attack, String sprite) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.sprite = sprite;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public String getSprite() { return sprite; }
}
