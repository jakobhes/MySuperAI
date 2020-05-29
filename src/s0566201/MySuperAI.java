package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class MySuperAI extends AI{

    Vector2f rayCastMiddle = new Vector2f();
    Vector2f rayLeft = new Vector2f();
    Vector2f rayRight = new Vector2f();
    Vector2f currentCheckpoint;
    Vector2f orientation;
    Vector2f currentPos;
    Vector2f destVector = new Vector2f();
    float distanceToDest;
    float requiredAngularVelocity;
    ArrayList<Node> reflexCorners = new ArrayList<>();


    public MySuperAI (Info info) {
        super(info);
        enlistForTournament(566201, 566843); //fuer Abgabe
        addReflexCorners();
//        enlistForInternalDevelopmentPurposesOnlyAndDoNOTConsiderThisAsPartOfTheHandedInSolution();//zum testen
    }

    @Override
    public String getName() {
        return "JAKOBI";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {

        Track track = info.getTrack();

        //Vectors init
        currentCheckpoint = new Vector2f((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());
        orientation = new Vector2f((float)(Math.cos(info.getOrientation())), (float) (Math.sin(info.getOrientation())));
        currentPos = new Vector2f(info.getX(), info.getY());
        Vector2f.sub(currentCheckpoint, currentPos, destVector);

        distanceToDest = (float) (Math.sqrt(Math.pow(currentCheckpoint.x - info.getX(), 2) + Math.pow(currentCheckpoint.y - info.getY(), 2)));

        align();
        avoidObstacle(50);

        float angularVelocity = (requiredAngularVelocity - info.getAngularVelocity()) / 1;

        return new DriverAction(acceleration(arrive(3f,50f)), angularVelocity);
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
        glBegin(GL_POINTS);
        glColor3f(0,1,0);
        for (int i = 0; i < reflexCorners.size(); i++) {
            glVertex2d(reflexCorners.get(i).x, reflexCorners.get(i).y);
        }
        glEnd();
//        glBegin(GL_LINES);
//        glColor3f(0,0,1);
//        glVertex2f(info.getX(), info.getY());
//        glVertex2d(rayCastMiddle.x, rayCastMiddle.y);
//        glVertex2f(info.getX(), info.getY());
//        glVertex2d(rayCastMiddle.x, rayCastMiddle.y);
//        glVertex2f(info.getX(), info.getY());
//        glVertex2d(rayLeft.x, rayLeft.y);
//        glVertex2f(info.getX(), info.getY());
//        glVertex2d(rayRight.x, rayRight.y);
//        glEnd();
    }

    public float arrive(float destinationRadius, float baseBreakRadius) {
        if (distanceToDest >= info.getVelocity().length()/baseBreakRadius) return info.getMaxVelocity();
        else {
            //if (distanceToDest < destinationRadius) return info.getMaxVelocity();
            return distanceToDest * info.getMaxVelocity() / baseBreakRadius;
        }
    }

    public float acceleration(float speed) {
        return speed - info.getVelocity().length() / 1;
    }

    public void align() {
        float angleBetweenPosAndDest = Vector2f.angle(orientation, destVector);
        float tolerance = 0.000001f;
        float dot = orientation.x * -destVector.y + orientation.y * destVector.x;
        if (dot > 0) angleBetweenPosAndDest = -angleBetweenPosAndDest;
        if (Math.abs(angleBetweenPosAndDest) < Math.abs(info.getAngularVelocity())/2) {
            requiredAngularVelocity = (angleBetweenPosAndDest * info.getMaxAbsoluteAngularVelocity() / 2*Math.abs(info.getAngularVelocity())); //TODO: Tweak
        } else requiredAngularVelocity = (angleBetweenPosAndDest > tolerance) ? info.getMaxAbsoluteAngularVelocity() : -info.getMaxAbsoluteAngularVelocity();
    }

    public void avoidObstacle(float breakRad) {
        Track track = info.getTrack();
        Polygon[] obstacles = track.getObstacles(); //(Oberflaeche der) Hindernisse
        float rayCastLength = info.getVelocity().length();
        if (distanceToDest >= 4*breakRad) {
            rayCastLength = 4 * info.getVelocity().length();
        }
        Vector2f orientationWithLength = (Vector2f)orientation.scale(rayCastLength);

        //Single Ray (middle)
        Vector2f.add(currentPos, orientationWithLength, rayCastMiddle);

        //turn orientation vector
        float fov = (float)Math.PI/8; //TODO: Tweak
        float ox = orientationWithLength.x;
        float oy = orientationWithLength.y;

        //TODO: evtl vereinfachen (math.pi*2)
        //Ray Left
        Vector2f rayLeftOrientation = new Vector2f((float)(Math.cos(fov) * ox - Math.sin(fov) * oy), (float)(Math.sin(fov) * ox + Math.cos(fov) * oy));
        Vector2f.add(currentPos, rayLeftOrientation, rayLeft);

        //Ray Right
        Vector2f rayRightOrientation = new Vector2f((float)(Math.cos(2*Math.PI-fov) * ox - Math.sin(2*Math.PI-fov) * oy), (float)(Math.sin(2*Math.PI-fov) * ox + Math.cos(2*Math.PI-fov) * oy));
        Vector2f.add(currentPos, rayRightOrientation, rayRight);

        for (int i = 0; i < obstacles.length; i++) {
            if (obstacles[i].contains(rayLeft.x, rayLeft.y))
                requiredAngularVelocity = -info.getMaxAbsoluteAngularVelocity();
            else if (obstacles[i].contains(rayRight.x, rayRight.y))
                requiredAngularVelocity = info.getMaxAbsoluteAngularVelocity();
        }
    }

//    public List<Node> aStarSearch(Vector2f start, Vector2f goal)
//    {
//
//        Node startNode = new Node (start);
//        Node endNode = new Node (goal);
//
//        // setup for A*
//        HashMap<Node,Node> parentMap = new HashMap<Node,Node>();
//        HashSet<Node> visited = new HashSet<Node>();
//        Map<Node, Double> distances = initializeAllToInfinity();
//
//        Queue<Node> priorityQueue = initQueue();
//
//        //  enque StartNode, with distance 0
//        startNode.setDistanceToStart(new Double(0));
//        distances.put(startNode, new Double(0));
//        priorityQueue.add(startNode);
//        Node current = null;
//
//        while (!priorityQueue.isEmpty()) {
//            current = priorityQueue.remove();
//
//            if (!visited.contains(current) ){
//                visited.add(current);
//                // if last element in PQ reached
//                if (current.equals(endNode)) return reconstructPath(parentMap, startNode, endNode, 0);
//
//                Set<Node> neighbors = getNeighbors(current);
//                for (Node neighbor : neighbors) {
//                    if (!visited.contains(neighbor) ){
//
//                        // calculate predicted distance to the end node
//                        double predictedDistance = neighbor.getLocation().distance(endNode.getLocation());
//
//                        // 1. calculate distance to neighbor. 2. calculate dist from start node
//                        double neighborDistance = current.calculateDistance(neighbor);
//                        double totalDistance = current.getDistanceToStart() + neighborDistance + predictedDistance;
//
//                        // check if distance smaller
//                        if(totalDistance < distances.get(neighbor) ){
//                            // update n's distance
//                            distances.put(neighbor, totalDistance);
//                            // used for PriorityQueue
//                            neighbor.setDistanceToStart(totalDistance);
//                            neighbor.setPredictedDistance(predictedDistance);
//                            // set parent
//                            parentMap.put(neighbor, current);
//                            // enqueue
//                            priorityQueue.add(neighbor);
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

    public void addReflexCorners() {
        int xPoint2, xPoint3, yPoint2, yPoint3;
        Track track = info.getTrack();
        Polygon[] obstacles = track.getObstacles();
        for (int i = 0; i < obstacles.length; i++) {
            Polygon obs = obstacles[i];
            for (int j = 0; j < obs.npoints; j++) {
                int xPoint1 = obs.xpoints[j];
                int yPoint1 = obs.ypoints[j];
                if (j == obs.npoints - 1) {
                    xPoint2 = obs.xpoints[0];
                    yPoint2 = obs.ypoints[0];
                    xPoint3 = obs.xpoints[1];
                    yPoint3 = obs.ypoints[1];
                } else if (j == obs.npoints - 2) {
                    xPoint2 = obs.xpoints[j+1];
                    yPoint2 = obs.ypoints[j+1];
                    xPoint3 = obs.xpoints[0];
                    yPoint3 = obs.ypoints[0];
                }
                else {
                    xPoint2 = obs.xpoints[j+1];
                    yPoint2 = obs.ypoints[j+1];
                    xPoint3 = obs.xpoints[j+2];
                    yPoint3 = obs.ypoints[j+2];
                }

                Vector2f v1 = new Vector2f(xPoint2-xPoint1, yPoint2-yPoint1);
                Vector2f v2 = new Vector2f(xPoint3-xPoint2, yPoint3-yPoint2);

                float crossProduct = (v1.x*v2.y)-(v2.x*v1.y);
                // if crossproduct positive: outside angle > 180
                // if crossproduct negative: outside angle < 180

                if (crossProduct > 0 && xPoint1 != 0 && yPoint1 != 0 && xPoint1 != track.getWidth() && yPoint1 != track.getHeight() &&
                                        xPoint2 != 0 && yPoint2 != 0 && xPoint2 != track.getWidth() && yPoint2 != track.getHeight() &&
                                        xPoint3 != 0 && yPoint3 != 0 && xPoint3 != track.getWidth() && yPoint3 != track.getHeight()) {
                    Vector2f v = new Vector2f(xPoint2, yPoint2);
                    Node n = new Node(v);
                    reflexCorners.add(n);
                }
            }
        }
    }

}

