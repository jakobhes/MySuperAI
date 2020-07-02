package s0566201;

import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;

public class EdgeOperations {

    public EdgeOperations() {
    }

    /**
     * Increases the amount of nodes in a path by adding new nodes between existing ones
     * @param graph: a list of nodes that represent a path
     * @param resolution: the amount of times a section on the path is subdivided into new sections
     * @return the new graph
     **/
    // TODO: this should actually be in class Graph
    public ArrayList<Vector2f> increaseGraphResolution(ArrayList<Vector2f> graph, int resolution){
        while (resolution != 0) {
            ArrayList<Vector2f> highResPath = new ArrayList<>();
            int j = 0;
            for (int i = 0; i < graph.size()-1; i ++) {
                Vector2f firstCoord = new Vector2f(graph.get(i).x, graph.get(i).y);
                Vector2f secondCoord = new Vector2f(graph.get(i+1).x, graph.get(i+1).y);
                Vector2f newCoord = new Vector2f((firstCoord.x+secondCoord.x)/2, (firstCoord.y + secondCoord.y)/2);
                highResPath.add(j, firstCoord);
                highResPath.add(j+1, secondCoord);
                highResPath.add(j+2, newCoord);
                j = j + 3;
            }
            resolution--;
            return increaseGraphResolution(highResPath, resolution);
        }
        return graph;
    }
}
