package org.example;

class Pokemon {
    private String name;
    private int hp;
    private int attack;

    public Pokemon(String name, int hp, int attack) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
    }

    public String getName() { return name; }
}