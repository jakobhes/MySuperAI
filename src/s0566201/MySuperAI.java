package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;

public class MySuperAI extends AI{

    Graph g;
    AStar<Node> aStar;
    Track track;
    int i;
    Vector2f rayCastMiddle = new Vector2f();
    Vector2f rayLeft = new Vector2f();
    Vector2f rayRight = new Vector2f();
    Vector2f currentCheckpoint;
    Vector2f orientation;
    Vector2f currentPos;
    Vector2f destVector = new Vector2f();
    float distanceToDest;
    float distToPointOnPath;
    float requiredAngularVelocity;
    ArrayList<Node> shortestPath;

    public MySuperAI (Info info) {
        super(info);
        track = info.getTrack();
        enlistForTournament(566201, 566843); //fuer Abgabe
        Node startNode = new Node(new Vector2f(info.getX(), info.getY()));

        g = new Graph(track, startNode);
    }

    @Override
    public String getName() {
        return "JAKOBI";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {

        track = info.getTrack();

        currentCheckpoint = new Vector2f((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());
        Node current = new Node(currentCheckpoint);
        if (!g.coords.contains(current)) {
            i = 1;
            g.coords.add(current);
            g.draw(info.getTrack());
            aStar = new AStar<>(g.graph);
            shortestPath = new ArrayList<>();
            shortestPath.addAll(aStar.astar(g.coords.get(g.coords.size() - 2), g.coords.get(g.coords.size() - 1)));
            shortestPath = increasePathResolution(shortestPath, 2);
        }
        if (wasResetAfterCollision) {
            i = 1;
        }

        Vector2f currentPointOnPath = new Vector2f(shortestPath.get(i).x, shortestPath.get(i).y);
        distToPointOnPath = (float) (Math.sqrt(Math.pow(currentPointOnPath.x - info.getX(), 2) + Math.pow(currentPointOnPath.y - info.getY(), 2)));
        if (distToPointOnPath < 40 && i != shortestPath.size()-1) {
            i++;
        }

        //Vectors init
        orientation = new Vector2f((float)(Math.cos(info.getOrientation())), (float) (Math.sin(info.getOrientation())));
        currentPos = new Vector2f(info.getX(), info.getY());
        Vector2f.sub(currentPointOnPath, currentPos, destVector);

        distanceToDest = (float) (Math.sqrt(Math.pow(currentCheckpoint.x - info.getX(), 2) + Math.pow(currentCheckpoint.y - info.getY(), 2)));
        align();
        avoidObstacle(25);


        float angularVelocity = (requiredAngularVelocity - info.getAngularVelocity()) / 1;
        if (angularVelocity >= info.getMaxAbsoluteAngularAcceleration()) angularVelocity = info.getMaxAbsoluteAngularVelocity();
        else if (angularVelocity <= -info.getMaxAbsoluteAngularAcceleration()) angularVelocity = -info.getMaxAbsoluteAngularVelocity();

        return new DriverAction(acceleration(arrive(10,info.getVelocity().length())), angularVelocity);
    }

    @Override
    public String getTextureResourceName() {
        return "/s0566201/car.png";
    }

    @Override
    public void doDebugStuff() {
        glBegin(GL_LINES);
        glColor3f(1, 0, 0);
        glVertex2f(info.getX(), info.getY());
        glVertex2d(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY());
        glEnd();
        glBegin(GL_LINES);
        glColor3f(1,1,0);
        for (int i = 0; i < shortestPath.size()-1; i++) {
            glVertex2d(shortestPath.get(i).x, shortestPath.get(i).y);
            glVertex2d(shortestPath.get(i+1).x, shortestPath.get(i+1).y);
        }
        glEnd();
        glBegin(GL_POINTS);
        glColor3f(1,1,1);
        for (int i = 0; i < shortestPath.size()-1; i++) {
            glVertex2d(shortestPath.get(i).x, shortestPath.get(i).y);
        }
        glEnd();
        g.visualize();
        glBegin(GL_LINES);
        glColor3f(0,0,1);
        glVertex2f(info.getX(), info.getY());
        glVertex2d(rayCastMiddle.x, rayCastMiddle.y);
        glVertex2f(info.getX(), info.getY());
        glVertex2d(rayCastMiddle.x, rayCastMiddle.y);
        glVertex2f(info.getX(), info.getY());
        glVertex2d(rayLeft.x, rayLeft.y);
        glVertex2f(info.getX(), info.getY());
        glVertex2d(rayRight.x, rayRight.y);
        glEnd();
    }


    public float arrive(float destinationRadius, float baseBreakRadius) {
        if (distanceToDest < destinationRadius) return info.getMaxVelocity();
        if (distanceToDest < baseBreakRadius) return distanceToDest * info.getMaxVelocity() / baseBreakRadius;
        else return info.getMaxVelocity();
    }


    public float acceleration(float speed) {
        return speed - info.getVelocity().length() / 1;
    }


    public void align() {
        float angleBetweenPosAndDest = Vector2f.angle(orientation, destVector);
        float tolerance = 0.01f;
        float dot = orientation.x * -destVector.y + orientation.y * destVector.x;

        if (dot > 0) angleBetweenPosAndDest = -angleBetweenPosAndDest;
        if (Math.abs(angleBetweenPosAndDest) < tolerance) requiredAngularVelocity = 0;
        else if (Math.abs(angleBetweenPosAndDest) < (Math.abs(info.getAngularVelocity())) && distanceToDest > 20) {
            requiredAngularVelocity = angleBetweenPosAndDest * info.getMaxAbsoluteAngularVelocity() / (Math.abs(info.getAngularVelocity())); //TODO: Tweak
        } else requiredAngularVelocity = (angleBetweenPosAndDest > tolerance) ? info.getMaxAbsoluteAngularVelocity() : -info.getMaxAbsoluteAngularVelocity();
    }

    /**
     * Casts 2 rays in a FOV-angle in front of the car, that check for obstacles in the way. If an obstacles is detected steers accordingly
     * @param breakRad: The radius of a zone around checkpoints where the care should start to break.
     **/
    public void avoidObstacle(float breakRad) {
        Track track = info.getTrack();
        float rayCastLength = info.getVelocity().length();

        if (distanceToDest >= 2*breakRad) rayCastLength = 2.5f * info.getVelocity().length();

        Vector2f orientationWithLength = (Vector2f)orientation.scale(rayCastLength);

        //Single Ray (middle)
        Vector2f.add(currentPos, orientationWithLength, rayCastMiddle);

        //turn orientation vector
        float fov = (float)Math.PI/10; //TODO: Tweak
        float ox = orientationWithLength.x;
        float oy = orientationWithLength.y;

        //TODO: evtl vereinfachen (math.pi*2)
        //Ray Left
        Vector2f rayLeftOrientation = new Vector2f((float)(Math.cos(fov) * ox - Math.sin(fov) * oy), (float)(Math.sin(fov) * ox + Math.cos(fov) * oy));
        Vector2f.add(currentPos, rayLeftOrientation, rayLeft);

        //Ray Right
        Vector2f rayRightOrientation = new Vector2f((float)(Math.cos(2*Math.PI-fov) * ox - Math.sin(2*Math.PI-fov) * oy), (float)(Math.sin(2*Math.PI-fov) * ox + Math.cos(2*Math.PI-fov) * oy));
        Vector2f.add(currentPos, rayRightOrientation, rayRight);

        Line2D l = new Line2D.Float(info.getX(), info.getY(), rayLeft.x, rayLeft.y);
        Line2D r = new Line2D.Float(info.getX(), info.getY(), rayRight.x, rayRight.y);
        if (intersects(l, track)) {
            requiredAngularVelocity = -info.getMaxAbsoluteAngularVelocity();
        } else if (intersects(r, track)) {
            requiredAngularVelocity = info.getMaxAbsoluteAngularVelocity();
        }

    }

    //TODO: We also have this method in Graph > refactor
    /**
     * Checks if an edge/line intersects with an obstacle on the track
     * @param edgeToCheck: The edge/line to check
     * @param track: the track containing the obstacles
     * @return: Returns true if the edge intersects with any obstacle
     **/
    public boolean intersects (Line2D edgeToCheck, Track track) {
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
                    x2 = obs.xpoints[j + 1];
                    y2 = obs.ypoints[j + 1];
                }
                Line2D l = new Line2D.Float(x1, y1, x2, y2);
                if (l.intersectsLine(edgeToCheck)) {
                    return true;
                }
            }
        }
        return false;
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
}

