package s0566201;

import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class Node {

    public float x, y;
//    public String id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Float.compare(node.x, x) == 0 &&
                Float.compare(node.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

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

//    public String getId() { return id; }


    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

//    public void setId(String id) {
//        this.id = id;
//    }
}
