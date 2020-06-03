package s0566201;

import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;


public class Graph {

    ArrayList<Node> nodes = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();


    // public Graph(Polygon[] obstacles) { }

    public Graph() {}

    // adds reflex corners and moves them away from obstacle
    public void addReflexCorners(Track track) {
        float x2, x3, y2, y3;
        int moveDistance = 20;
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
                    v3.scale(moveDistance);
                    Vector2f.add(v4, v3, v3);
                    double rotX = Math.cos(angle)*(v3.x-x2)-Math.sin(angle)*(v3.y-y2)+x2;
                    double rotY = Math.sin(angle)*(v3.x-x2)+Math.cos(angle)*(v3.y-y2)+y2;
                    x2 = (float)rotX;
                    y2 = (float)rotY;
                    Vector2f v = new Vector2f(x2, y2);
                    Node n = new Node(v);
                    nodes.add(n);
                }
            }
        }
    }

    public void createEdges (Track track) {
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 1; j < nodes.size(); j++) {
                Line2D edgeToCheck = new Line2D.Float(nodes.get(i).x, nodes.get(i).y, nodes.get(j).x, nodes.get(j).y);
                if (!intersects(edgeToCheck, track)) {
                    Edge e = new Edge(nodes.get(i), nodes.get(j));
                    edges.add(e);
                }
            }
        }
    }

    //internet stuff
    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }


    public void addNode(Track track, Node n) {
        for (int i = 0; i < nodes.size(); i++) {
            Line2D edgeToCheck = new Line2D.Float(nodes.get(i).x, nodes.get(i).y, n.x, n.y);
            if (!intersects(edgeToCheck, track)) {
                Edge e = new Edge(nodes.get(i), n);
                    edges.add(e);
            }
        }
        nodes.add(n);
    }


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

                } else if (edgeToCheck.ptSegDist(x1, y1) < 7){ //TODO: maybe tweak value
                    return  true;
                }
            }
        }
        return  false;
    }

    public void visualize () {
        glBegin(GL_POINTS);
        glColor3f(0,1,0);
        for (Node reflexCorner : nodes) {
            glPointSize(0.1f);
            glVertex2d(reflexCorner.x, reflexCorner.y);
        }
        glEnd();
        glBegin(GL_LINES);
        for (Edge edge : edges) {
            glVertex2d(edge.a.x, edge.a.y);
            glVertex2d(edge.b.x, edge.b.y);
        }
        glEnd();
    }

    public void draw (Track track) {
        addReflexCorners(track);
        createEdges(track);
    }


    public float calcDistanceBetween(Node a, Node b) {
        return (float) (Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2)));
    }

    public ArrayList<Node> findShortesPath(Node start, Node destination, Track track) {
        start.setCost(0);
        start.setDistanceToStart(0);
        start.setDistanceToDestination(calcDistanceBetween(start, destination));
        addNode(track, start);

        destination.setDistanceToDestination(0);
        addNode(track, destination);


        return null;
    }
}