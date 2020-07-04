package s0566201;

import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class Graph {

    public GraphAStar<Node> graph;
    public ArrayList<Node> coords = new ArrayList<>();
    public Map<Node, Map<Node, Double>> heuristic = new HashMap<>();
    public EdgeOperations eOp = new EdgeOperations();

    public Graph() {}

    public Graph(Track track, Node startNode) {
        checkCoordAndAdd(track, track.getObstacles(), 20, false);
        checkCoordAndAdd(track, track.getFastZones(), 5, false);
        checkCoordAndAdd(track, track.getSlowZones(), 5, false);
        coords.add(startNode);
        draw(track);
    }


//    public ArrayList<Vector2f> convertToArrayList(Polygon[] areas) {
//        ArrayList vectorAL = new ArrayList();
//        for (Polygon area : areas) {
//            for (int j = 0; j < area.npoints; j++) {
//                Vector2f x = new Vector2f(area.xpoints[j],area.ypoints[j]);
//                vectorAL.add(x);
//            }
//        }
//        return vectorAL;
//    }

    /**
     * draws the graph
     * @param track: a track
     **/
    public void draw(Track track) {
        addHeuristic();
        graph = new GraphAStar<>(heuristic);
        for (Node coord : coords) {
            graph.addNode(coord);
        }
        createEdges(track);
    }

    /**
     * adds nodes to the field coords that are used for the shortest way path, considering areas to drive around
     * @param track: a track containing areas
     * @param areas: polygon arrays (obstacles, slow zones or fast zones)
     * @param offset: distance between node of the graph and node of the polygon
     **/
    public void checkCoordAndAdd(Track track, Polygon[] areas, int offset, boolean isObstacle) {
        ArrayList<Node> highRes = new ArrayList<>();
        float x2, x3, y2, y3;
        for (Polygon area : areas) {
            for (int j = 0; j < area.npoints; j++) {
                float x1 = area.xpoints[j];
                float y1 = area.ypoints[j];
                if (j == area.npoints - 1) {
                    x2 = area.xpoints[0];
                    y2 = area.ypoints[0];
                    x3 = area.xpoints[1];
                    y3 = area.ypoints[1];
                } else if (j == area.npoints - 2) {
                    x2 = area.xpoints[j + 1];
                    y2 = area.ypoints[j + 1];
                    x3 = area.xpoints[0];
                    y3 = area.ypoints[0];
                } else {
                    x2 = area.xpoints[j + 1];
                    y2 = area.ypoints[j + 1];
                    x3 = area.xpoints[j + 2];
                    y3 = area.ypoints[j + 2];
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

                    if (isObstacle) {
                        coords.add(n);
                    } else {
                        highRes.add(n);
                    }


                }
            }
        }
        if (!isObstacle) coords.addAll(increasePathResolution(highRes, 1));
    }

    /**
     * Increases the amount of nodes in a path by adding new nodes between existing ones
     * @param path: a list of nodes that represent a path
     * @param resolution: the amount of times a section on the path is subdivided into new sections
     * @return the new graph
     **/
    // TODO: this should actually be in class Graph
    public ArrayList<Node> increasePathResolution(ArrayList<Node> path, int resolution){
        while (resolution != 0) {
            ArrayList<Node> highResPath = new ArrayList<>();
            int j = 0;
            for (int i = 0; i < path.size()-1; i ++) {
                Vector2f a = new Vector2f(path.get(i).x, path.get(i).y);
                Node xn = new Node(a);
                Vector2f b = new Vector2f(path.get(i+1).x, path.get(i+1).y);
                Node yn = new Node(b);
                Vector2f n = new Vector2f((a.x+b.x)/2, (a.y + b.y)/2);
                Node nn = new Node(n);
                highResPath.add(j, xn);
                highResPath.add(j+1, nn);
                highResPath.add(j+2, yn);
                j = j + 3;
            }
            resolution--;
            return increasePathResolution(highResPath, resolution);
        }
        return path;
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
        double slowZoneWeight = 4;
        double fastZoneWeight = 0.75;

        for (int i = 0; i < coords.size(); i++) {
            Map<Node, Double> edgeMap = new HashMap<>();
            for (int j = 1; j < coords.size(); j++) {
                Line2D edgeToCheck = new Line2D.Float(coords.get(i).x, coords.get(i).y, coords.get(j).x, coords.get(j).y);

                if (!intersects(edgeToCheck, track.getObstacles())) {
                    if (intersects(edgeToCheck, track.getSlowZones())) {
                        // Node n = findIntersectionPoint(edgeToCheck, );
                        edgeMap.put(coords.get(j), slowZoneWeight * calcDistanceBetween(coords.get(i), coords.get(j)));
                        graph.addEdge(coords.get(i), coords.get(j), edgeMap.get(coords.get(j)));
                    } else if (intersects(edgeToCheck, track.getFastZones())) {
                        edgeMap.put(coords.get(j), fastZoneWeight * calcDistanceBetween(coords.get(i), coords.get(j)));
                        graph.addEdge(coords.get(i), coords.get(j), edgeMap.get(coords.get(j)));
                    } else {
                        edgeMap.put(coords.get(j), calcDistanceBetween(coords.get(i), coords.get(j)));
                        graph.addEdge(coords.get(i), coords.get(j), edgeMap.get(coords.get(j)));
                    }
                }
            }
        }
    }

    //TODO: We also have this method in MySuperAI > refactor
    /**
     * Checks if an edge/line intersects with an obstacle on the track
     * @param edgeToCheck: The edge/line to check
     *
     * @return: Returns true if the edge intersects with any obstacle
     **/
    public boolean intersects (Line2D edgeToCheck, Polygon[] obstacles){
        float x1, x2, y1, y2;
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
                } else if (edgeToCheck.ptSegDist(x1, y1) < 10){ //TODO: maybe tweak value, also make it a param
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

    private static Node findIntersectionPoint(Line2D l1, Line2D l2) {
        double a1 = l1.getY2() - l1.getY1();
        //double a1 = l1.e.y - l1.s.y;
        double b1 = l1.getX1() - l1.getX2();
        //double b1 = l1.s.x - l1.e.x;
        double c1 = a1 * l1.getX1() + b1 * l1.getY1();
        //double c1 = a1 * l1.s.x + b1 * l1.s.y;

        double a2 = l2.getY2() - l2.getY1();
        //double a2 = l2.e.y - l2.s.y;
        double b2 = l2.getX1() - l2.getX2();
        //double b2 = l2.s.x - l2.e.x;

        double c2 = a2 * l2.getX1() + b2 * l2.getY1();
        //double c2 = a2 * l2.s.x + b2 * l2.s.y;

        double delta = a1 * b2 - a2 * b1;
        return new Node (new Vector2f((float)((b2 * c1 - b1 * c2) / delta), (float)((a1 * c2 - a2 * c1) / delta)));
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

        glBegin(GL_LINES);
        glColor3f(1,1,1);
        for (Node node : graph) {
            glVertex2d(node.getX(), node.getY());

        }
        glEnd();
    }

}