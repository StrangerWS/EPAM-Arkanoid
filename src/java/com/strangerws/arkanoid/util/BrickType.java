package com.strangerws.arkanoid.util;

import javafx.scene.paint.Color;

public enum BrickType {

    Common(1, Color.AQUAMARINE, false, 10),
    Uncommon(2, Color.LIGHTBLUE, false, 15),
    Rare(3, Color.BLUE, false, 25),
    Epic(5, Color.DARKVIOLET, false, 50),
    Legendary(10, Color.GOLDENROD, false, 100),
    Unbreakable(1, Color.GRAY, true, 0);

    public int brickHealth;
    public Color color;
    public boolean isIndestructible;
    public int points;

    BrickType(int brickHealth, Color color, boolean isIndestructible, int points) {
        this.brickHealth = brickHealth;
        this.color = color;
        this.isIndestructible = isIndestructible;
        this.points = points;
    }
}
