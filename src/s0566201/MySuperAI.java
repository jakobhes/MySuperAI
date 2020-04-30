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
    public float steeringStrength;
    public float steeringThreshhold;
    public float steeringStrengthThreshhold;

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
        return "Jakob & Tobi";
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

        // Threshhold of orientation alignment
        steeringThreshhold = 0.005f;

        // threshhold of steering strength
        steeringStrengthThreshhold = info.getAngularVelocity() / 2;

        // current rotation velocity
        rotationVelocity = info.getAngularVelocity();

        /**
         *
         * BESCHLEUNIGUNG
         *
         */

        float breakRadius = 50;
        float checkpointRadius = 2f;
        float speed = info.getMaxVelocity();

        if( MathVec.abs(carToCheckpoint) < breakRadius) {
            speed = MathVec.abs(carToCheckpoint) * info.getMaxVelocity() / breakRadius / 2;
            if ( MathVec.abs(carToCheckpoint) < checkpointRadius) {
                speed = info.getMaxVelocity();
//                Vec2 currentSpeed = new Vec2(info.getVelocity().x, info.getVelocity().y);
//                speed -= MathVec.abs(MathVec.direction(currentSpeed, checkpointPosition));
            }
        }


        if(Math.abs(checkpointOrientation - carOrientation) > steeringThreshhold) {
            // take a turn
            if (checkpointOrientation - carOrientation > 0) {
                //turn left (+)
                if (Math.abs(checkpointOrientation - carOrientation) <  steeringStrengthThreshhold) {
                    //lower the steering strength
                    steeringStrength = (checkpointOrientation - carOrientation) * info.getMaxAbsoluteAngularVelocity() / steeringStrengthThreshhold;
                } else {
                    // maximum steering strength
                    steeringStrength = info.getMaxAbsoluteAngularVelocity();
                }
           } else {
                // turn right (-)
                if (Math.abs(checkpointOrientation - carOrientation) <  steeringStrengthThreshhold) {
                    //lower the steering strength
                    steeringStrength = (checkpointOrientation - carOrientation) * info.getMaxAbsoluteAngularVelocity() / steeringStrengthThreshhold;
                } else {
                    // maximum steering strength
                    steeringStrength = - info.getMaxAbsoluteAngularVelocity();
                }
            }
        } else {
            // don't turn
            steeringStrength = 0;
        }

        Track track = info.getTrack();
        track.getWidth();
        track.getHeight();
        Polygon[] obstacles = track.getObstacles(); //(Oberflaeche der) Hindernisse
        Polygon obs = obstacles[0];
        track.getObstacles(); // Hindernisse - nächste Übung

        float absoluteSteeringStrength = steeringStrength - info.getAngularVelocity();

        if( absoluteSteeringStrength >= 0 && absoluteSteeringStrength > info.getMaxAbsoluteAngularAcceleration()) {
            absoluteSteeringStrength = info.getMaxAbsoluteAngularAcceleration();
        } else if (Math.abs(absoluteSteeringStrength) > info.getMaxAbsoluteAngularAcceleration()) {
            absoluteSteeringStrength = -info.getMaxAbsoluteAngularAcceleration();
        }

        System.out.println(speed);
        return new DriverAction(speed, absoluteSteeringStrength);
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
