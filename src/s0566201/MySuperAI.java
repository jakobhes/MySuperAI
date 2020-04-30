package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;

import java.awt.*;


public class MySuperAI extends AI{

    public Vec2 carPosition;
    public float carOrientation;
    public Vec2 checkpointPosition;
    public Vec2 carToCheckpoint;
    public Vec2 pointToRight;
    public float checkpointOrientation;
    public float tolerance;
    public float rotationVelocity;

    public MySuperAI (Info info) {
        super(info);
        //enlistForTournament(566201); //fuer Abgabe
        enlistForInternalDevelopmentPurposesOnlyAndDoNOTConsiderThisAsPartOfTheHandedInSolution(); //zum testen
        //hier irgendwas
        //ja
    }

    @Override
    public String getName() {
        return "JAKOB";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {

        // Car Position as Vec2
        carPosition = new Vec2(info.getX(), info.getY());

        // Orientation of the car (as an Angle)
        carOrientation = (info.getOrientation() > 0) ? info.getOrientation() : info.getOrientation() + (float)Math.PI*2;

        // Position of the Current Checkpoint as Vec2
        checkpointPosition = new Vec2((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());

        // Direction vector from car to current checkpoint
        carToCheckpoint = MathVec.direction(carPosition, checkpointPosition);

        // A vector with an angle of 0
        pointToRight = new Vec2(1,0);

        // Orientation of direction vector from car to checkpoint (as an Angle)
        checkpointOrientation = (carToCheckpoint.b > 0) ? MathVec.angle(pointToRight, carToCheckpoint) : (float)Math.PI*2 - MathVec.angle(pointToRight, carToCheckpoint);

        // tolerance of orientation alignment
        tolerance = 2f;

        // current rotation velocity
        rotationVelocity = info.getAngularVelocity();

        if(Math.abs(checkpointOrientation - carOrientation) > tolerance) {
            if (checkpointOrientation - carOrientation > 0) {
                //turn left (+)
                rotationVelocity = 0.1f;
           } else {
                // turn right (-)
                rotationVelocity = -0.1f;
            }
        } else {
            // don't turn
            rotationVelocity = 0;
        }



        Track track = info.getTrack();
        track.getWidth();
        track.getHeight();
        Polygon[] obstacles = track.getObstacles(); //(Oberflaeche der) Hindernisse
        Polygon obs = obstacles[0];

        obs.contains(info.getX(), info.getY()); //ist der Punkt im Hinderniss?

        int numberofObstacles = obs.npoints; //Anzahl der Punkte des Hindernisses
        //A = obs.xpoints[0], obs.ypoints[0];
        //B = obs.xpoints[1], obs.ypoints[1]; //flee to get away from these points, collison avoidance viel besser
        //Erstelle Streck von A und B
        //Erstelle Richtungsvektor
        info.getVelocity();
        //Berechne Schnittpunkt der beiden obigen, pruefe Abstand


        track.getObstacles(); // Hindernisse - nächste Übung


        //Current Checkpoint Coordinates
//        double currentX = info.getCurrentCheckpoint().getX();
//        double currentY = info.getCurrentCheckpoint().getY();
//
//        float distanceToDest = (float) (Math.sqrt(Math.pow(currentX - info.getX(), 2) + Math.pow(currentY - info.getY(), 2)));
//        float angleToDest = (float) Math.acos((currentX - info.getX()) / distanceToDest);
//
//        float wunschdrehgeschw = 0f;
//
//
//        if (currentY < info.getY()) {
//            angleToDest = -angleToDest;
//        }
//
//        float tolerance = 0.005f;
//
//        if (Math.abs(angleToDest - info.getOrientation()) < Math.abs(info.getAngularVelocity())) {
//            wunschdrehgeschw = (angleToDest - info.getOrientation()) * info.getMaxAbsoluteAngularVelocity() / 2*info.getAngularVelocity();
//        } else if (angleToDest - info.getOrientation() >= tolerance){
//            wunschdrehgeschw = info.getMaxAbsoluteAngularVelocity();
//        } else  if (angleToDest - info.getOrientation() <= -tolerance)
//            wunschdrehgeschw = -info.getMaxAbsoluteAngularVelocity();
//
//        float drehbeschleunigungVonAlign = (wunschdrehgeschw - info.getAngularVelocity()) / 1;

            return new DriverAction(1, -1);
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
    }
}
