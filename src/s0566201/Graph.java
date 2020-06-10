package s0566201;

import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class Graph {

    public GraphAStar<Node> graph;
    public ArrayList<Node> coords = new ArrayList<>();
    public Map<Node, Map<Node, Double>> heuristic = new HashMap<>();

    public Graph() {}

    //TODO: This should be the Constructor
    public void createGraph(Track track) {
        addHeuristic();
        graph = new GraphAStar<>(heuristic);
        for (Node coord : coords) {
            graph.addNode(coord);
        }
        createEdges(track);
    }

    /**
     * adds nodes to the field coords that are used for the shortest way path, considering obstacles to drive around
     * @param track: a track containing obstacles
     **/
    public void checkCoordAndAdd(Track track) {
        float x2, x3, y2, y3;
        int offset = 20;
        Polygon[] obstacles = track.getObstacles();
        for (Polygon obs : obstacles) {
            for (int j = 0; j < obs.npoints; j++) {
                float x1 = obs.xpoints[j];
                float y1 = obs.ypoints[j];
                if (j == obs.npoints - 1) {
                    x2 = obs.xpoints[0];
                    y2 = obs.ypoints[0];
                    x3 = obs.xpoints[1];
                    y3 = obs.ypoints[1];
                } else if (j == obs.npoints - 2) {
                    x2 = obs.xpoints[j + 1];
                    y2 = obs.ypoints[j + 1];
                    x3 = obs.xpoints[0];
                    y3 = obs.ypoints[0];
                } else {
                    x2 = obs.xpoints[j + 1];
                    y2 = obs.ypoints[j + 1];
                    x3 = obs.xpoints[j + 2];
                    y3 = obs.ypoints[j + 2];
                }

                Vector2f v1 = new Vector2f(x2 - x1, y2 - y1);
                Vector2f v2 = new Vector2f(x3 - x2, y3 - y2);

                float crossProduct = (v1.x * v2.y) - (v2.x * v1.y);
                // if crossproduct positive: outside angle > 180
                // if crossproduct negative: outside angle < 180

                if (crossProduct > 0 && x1 != 0 && y1 != 0 && x1 != track.getWidth() && y1 != track.getHeight() &&
                        x2 != 0 && y2 != 0 && x2 != track.getWidth() && y2 != track.getHeight() &&
                        x3 != 0 && y3 != 0 && x3 != track.getWidth() && y3 != track.getHeight()) {
                    Vector2f v3 = new Vector2f(x1-x2, y1-y2);
                    double angle = (2*Math.PI - Vector2f.angle(v3,v2))/2;
                    Vector2f v4 = new Vector2f(x2, y2);
                    v3.normalise(v3);
                    v3.scale(offset);
                    Vector2f.add(v4, v3, v3);
                    double rotX = Math.cos(angle)*(v3.x-x2)-Math.sin(angle)*(v3.y-y2)+x2;
                    double rotY = Math.sin(angle)*(v3.x-x2)+Math.cos(angle)*(v3.y-y2)+y2;
                    x2 = (float)rotX;
                    y2 = (float)rotY;

                    Node n = new Node(new Vector2f(x2, y2));
                    coords.add(n);
                }
            }
        }
    }

    /**
     * calculates the heuristics of all coordinates to each other and puts them into the field heuristics
     **/
    public void addHeuristic() {
        Map<Node, Double> heurMap = new HashMap<>();
        for (int i = 0; i < coords.size(); i++) {
            for (int j = 1; j < coords.size(); j++) {
                heurMap.put(coords.get(i), calcDistanceBetween(coords.get(i), coords.get(j)));
            }
            heuristic.put(coords.get(i), heurMap);
        }
    }

    /**
     * adds edges to the field graph if they don't intersect with an obstacle
     * @param track: a track containing obstacles
     **/
    public void createEdges(Track track) {
        for (int i = 0; i < coords.size(); i++) {
            Map<Node, Double> edgeMap = new HashMap<>();
            for (int j = 1; j < coords.size(); j++) {
                edgeMap.put(coords.get(j), calcDistanceBetween(coords.get(i), coords.get(j)));
                Line2D edgeToCheck = new Line2D.Float(coords.get(i).x, coords.get(i).y, coords.get(j).x, coords.get(j).y);

                if (!intersects(edgeToCheck, track))
                    graph.addEdge(coords.get(i), coords.get(j), edgeMap.get(coords.get(j)));
            }
        }
    }


    //TODO: We also have this method in MySuperAI > refactor
    /**
     * Checks if an edge/line intersects with an obstacle on the track
     * @param edgeToCheck: The edge/line to check
     * @param track: the track containing the obstacles
     * @return: Returns true if the edge intersects with any obstacle
     **/
    public boolean intersects (Line2D edgeToCheck, Track track){
        float x1, x2, y1, y2;
        Polygon[] obstacles = track.getObstacles();
        for (Polygon obs : obstacles) {
            for (int j = 0; j < obs.npoints; j++) {
                x1 = obs.xpoints[j];
                y1 = obs.ypoints[j];
                if (j == obs.npoints - 1) {
                    x2 = obs.xpoints[0];
                    y2 = obs.ypoints[0];
                } else {
                    x2 = obs.xpoints[j+1];
                    y2 = obs.ypoints[j+1];
                }
                Line2D l = new Line2D.Float(x1, y1, x2, y2);
                if (l.intersectsLine(edgeToCheck)) {
                    return true;

                } else if (edgeToCheck.ptSegDist(x1, y1) < 7){ //TODO: maybe tweak value, also make it a param
                    return  true;
                }
            }
        }
        return  false;
    }

    /**
     * calculates the distances between 2 nodes
     * @param a: the first node
     * @param b: the second node
     * @return: the distance of 2 nodes as a double
     **/
    public double calcDistanceBetween(Node a, Node b) {
        return (Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2)));
    }


    /*** DEBUG ***/

    public void visualize () {
        glBegin(GL_POINTS);
        glColor3f(0,1,0);
        for (Node reflexCorner : coords) {
            glPointSize(0.1f);
            glVertex2d(reflexCorner.x, reflexCorner.y);
        }
        glEnd();
    }

}