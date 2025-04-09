package org.example;

public class Move {
    private String name;
    private double power;
    private double accuracy;
    private int pp;
    private String type;
    private String damageClass;
    private int priority;
    private String description;

    public Move(String name, double power, double accuracy, int pp, String type, String damageClass, int priority, String description) {
        this.name = name;
        this.power = power;
        this.accuracy = accuracy;
        this.pp = pp;
        this.type = type;
        this.damageClass = damageClass;
        this.priority = priority;
        this.description = description;
    }

    public String getName() { return name; }
    public double getPower() { return power; }
    public double getAccuracy() { return accuracy; }
    public int getPp() { return pp; }
    public String getType() { return type; }
    public String getDamageClass() { return damageClass; }
    public int getPriority() { return priority; }
    public String getDescription() { return description; }
    
    public boolean isSpecial() {
        return "Special".equals(damageClass);
    }
    
    public boolean isPhysical() {
        return "Physical".equals(damageClass);
    }
    
    public boolean isStatus() {
        return "Status".equals(damageClass);
    }
}