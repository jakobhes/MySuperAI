package s0566201;

import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node {

    //unser stuff
    public float x, y;

    private String name;

    private List<Node> shortestPath = new LinkedList<>();

    private float distanceToStart = Float.MAX_VALUE;
    private float distanceToDestination;
    private float cost = Float.MAX_VALUE;


    Map<Node, Integer> adjacentNodes = new HashMap<>();


    //unser stuff
    public Node(Vector2f position) {
        this.x = position.x;
        this.y = position.y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setDistanceToStart(float distanceToStart) {
        this.distanceToStart = distanceToStart;
    }

    public void setDistanceToDestination(float distanceToDestination) {
        this.distanceToDestination = distanceToDestination;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }
}
