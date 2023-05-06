package com.worldbuilder.mapgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double distance(Position other) {
        int dx = other.x - this.x;
        int dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static List<Position> sortByDistance(List<Position> positions, Position referencePosition) {
        List<Position> sortedPositions = new ArrayList<>(positions);

        Collections.sort(sortedPositions, new Comparator<Position>() {
            @Override
            public int compare(Position p1, Position p2) {
                double distance1 = p1.distance(referencePosition);
                double distance2 = p2.distance(referencePosition);
                return Double.compare(distance1, distance2);
            }
        });

        return sortedPositions;
    }
}