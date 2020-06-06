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

    Graph g = new Graph();
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
    ArrayList<Node>shortPathPoints;



    public MySuperAI (Info info) {
        super(info);
        track = info.getTrack();
        enlistForTournament(566201, 566843); //fuer Abgabe
        Node startNode = new Node(new Vector2f(info.getX(), info.getY()));
        g.checkCoordAndAdd(track);
        g.coords.add(startNode);
        g.createGraph(info.getTrack());
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
            g.createGraph(info.getTrack());
            aStar = new AStar<>(g.graph);
            shortPathPoints = new ArrayList<>();
            shortPathPoints.addAll(aStar.astar(g.coords.get(g.coords.size() - 2), g.coords.get(g.coords.size() - 1)));
        }
        if (wasResetAfterCollision) {
            i = 1;
        }

        Vector2f currentPointOnPath = new Vector2f(shortPathPoints.get(i).x, shortPathPoints.get(i).y);
        distToPointOnPath = (float) (Math.sqrt(Math.pow(currentPointOnPath.x - info.getX(), 2) + Math.pow(currentPointOnPath.y - info.getY(), 2)));
        if (distToPointOnPath < 40 && i != shortPathPoints.size()-1) {
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

//        return new DriverAction(acceleration(arrive(3f,40f)), angularVelocity);
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
        for (int i = 0; i < shortPathPoints.size(); i++) {
            glVertex2d(shortPathPoints.get(i).x, shortPathPoints.get(i).y);
            if (i < shortPathPoints.size() -1)
                glVertex2d(shortPathPoints.get(i+1).x, shortPathPoints.get(i+1).y);
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

//        //OLD STUFF
//        if (distanceToDest >= info.getVelocity().length()/baseBreakRadius) return info.getMaxVelocity();
//        else {
//            //if (distanceToDest < destinationRadius) return info.getMaxVelocity();
//            return distanceToDest * info.getMaxVelocity() / baseBreakRadius;
//        }
        if (distanceToDest < destinationRadius) {
            return info.getMaxVelocity();
        } else if (distanceToDest < baseBreakRadius){
            return distanceToDest * info.getMaxVelocity() / baseBreakRadius;
        } else {
            return info.getMaxVelocity();
        }

    }

    public float acceleration(float speed) {
        return speed - info.getVelocity().length() / 1;
    }

    public void align() {

        float angleBetweenPosAndDest = Vector2f.angle(orientation, destVector);
        float tolerance = 0.01f;
        float dot = orientation.x * -destVector.y + orientation.y * destVector.x;
        if (dot > 0) {
            angleBetweenPosAndDest = -angleBetweenPosAndDest;
        }
        if (Math.abs(angleBetweenPosAndDest) < tolerance) {
            requiredAngularVelocity = 0;
        }
        else if (Math.abs(angleBetweenPosAndDest) < (Math.abs(info.getAngularVelocity())) && distanceToDest > 30) {
            requiredAngularVelocity = angleBetweenPosAndDest * info.getMaxAbsoluteAngularVelocity() / (Math.abs(info.getAngularVelocity()));//TODO: Tweak
        } else requiredAngularVelocity = (angleBetweenPosAndDest > tolerance) ? info.getMaxAbsoluteAngularVelocity() : -info.getMaxAbsoluteAngularVelocity();
    }




    public void avoidObstacle(float breakRad) {
        Track track = info.getTrack();
        Polygon[] obstacles = track.getObstacles(); //(Oberflaeche der) Hindernisse
        float rayCastLength = info.getVelocity().length();
        //OLD STUFF
        if (distanceToDest >= 2*breakRad) {
            rayCastLength = 1.6f * info.getVelocity().length();
        }
//        if (distToPointOnPath >= 4*breakRad) {
//            rayCastLength = 4 * info.getVelocity().length();
//        }
        Vector2f orientationWithLength = (Vector2f)orientation.scale(rayCastLength);

        //Single Ray (middle)
        Vector2f.add(currentPos, orientationWithLength, rayCastMiddle);

        //turn orientation vector
        float fov = (float)Math.PI/6; //TODO: Tweak
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
//        for (Polygon obstacle : obstacles) {
//            if (obstacle.contains(rayLeft.x, rayLeft.y))
//                requiredAngularVelocity = -info.getMaxAbsoluteAngularVelocity();
//            else if (obstacle.contains(rayRight.x, rayRight.y))
//                requiredAngularVelocity = info.getMaxAbsoluteAngularVelocity();
//        }
    }
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
}

