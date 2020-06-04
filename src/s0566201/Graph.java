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

    public GraphAStar<Vector2f> graph;
    public ArrayList<Vector2f> coords;
    public Map<Vector2f, Map<Vector2f, Double>> heuristic = new HashMap<>();
    // public Graph(Polygon[] obstacles) { }

    public Graph() {}

    // adds reflex corners and moves them away from obstacle
    public void checkNodeAndAdd(Track track) {
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
                    Vector2f v = new Vector2f(x2, y2);
                    coords.add(v);
                }
            }
        }
    }

    public void createGraph(Track track) {
        createEdges(track);
        graph = new GraphAStar<>(heuristic);
        for (Vector2f coord : coords) {
            graph.addNode(coord);
        }
    }

    public void createEdges(Track track) {
        for (int i = 0; i < coords.size(); i++) {
            Map<Vector2f, Double> edgeMap = new HashMap<>();
            for (int j = 1; j < coords.size(); j++) {
                Line2D edgeToCheck = new Line2D.Float(coords.get(i).x, coords.get(i).y, coords.get(j).x, coords.get(j).y);
                if (!intersects(edgeToCheck, track)) {
                    edgeMap.put(coords.get(j), calcDistanceBetween(coords.get(i), coords.get(j)));
                    graph.addEdge(coords.get(i), coords.get(j), edgeMap.get(i));
                }
            }
            heuristic.put(coords.get(i), edgeMap);
        }
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


    public void draw(Track track) {
        checkNodeAndAdd(track);
        createEdges(track);
    }

    public double calcDistanceBetween(Vector2f a, Vector2f b) {
        return (Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2)));
    }


    /***

     DEBUG

     */


    public void visualize () {
        glBegin(GL_POINTS);
        glColor3f(0,1,0);
        for (Vector2f reflexCorner : coords) {
            glPointSize(0.1f);
            glVertex2d(reflexCorner.x, reflexCorner.y);
        }
        glEnd();
//        glBegin(GL_LINES);
//        for (Edge edge : edges) {
//            glVertex2d(edge.a.x, edge.a.y);
//            glVertex2d(edge.b.x, edge.b.y);
//        }
//        glEnd();
    }

}