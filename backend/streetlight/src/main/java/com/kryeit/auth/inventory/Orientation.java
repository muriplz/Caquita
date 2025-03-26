package com.kryeit.auth.inventory;

public enum Orientation {
    UP, DOWN, LEFT, RIGHT
    ;

    public Orientation opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public Orientation rotate(boolean clockwise) {
        return switch (this) {
            case UP -> clockwise ? RIGHT : LEFT;
            case DOWN -> clockwise ? LEFT : RIGHT;
            case LEFT -> clockwise ? UP : DOWN;
            case RIGHT -> clockwise ? DOWN : UP;
        };
    }
}
