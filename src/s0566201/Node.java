package s0566201;

import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node {

    public float x, y;
    public String id;

    public Node(Vector2f position, String id) {
        this.x = position.x;
        this.y = position.y;
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getId() { return id; }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setId(String id) {
        this.id = id;
    }
}
