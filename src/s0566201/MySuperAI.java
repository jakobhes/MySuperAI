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
    public float steeringThreshold;
    public float steeringStrengthThreshhold;
    public float rotationToComplete;
    float accelaration;

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
        return "Jakobi";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {


        // Car Position as Vec2
        carPosition = new Vec2(info.getX(), info.getY());

        // Orientation of the car (as an Angle)
        carOrientation = info.getOrientation();

        // Position of the Current Checkpoint as Vec2
        checkpointPosition = new Vec2((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());

        // Direction vector from car to current checkpoint
        carToCheckpoint = MathVec.direction(carPosition, checkpointPosition);

        // A vector with an angle of 0
        pointToRight = new Vec2(1,0);

        // Orientation of direction vector from car to checkpoint (as an Angle)
        checkpointOrientation = (carToCheckpoint.b > 0) ? MathVec.angle(pointToRight, carToCheckpoint) : - MathVec.angle(pointToRight, carToCheckpoint);

        // Threshold of orientation alignment
        steeringThreshold = 0.05f;

        // Threshold of steering strength
        steeringStrengthThreshhold = (float)Math.PI/2;

        // current rotation velocity
        rotationVelocity = info.getAngularVelocity();


        rotationToComplete = Math.abs(checkpointOrientation - carOrientation);

        if(rotationToComplete < steeringThreshold) {
            // don't turn
            steeringStrength = 0;
        } else {
            // take a turn
            if (rotationToComplete < steeringStrengthThreshhold) {
                //lower the steering strength
                steeringStrength = (checkpointOrientation - carOrientation) * info.getMaxAbsoluteAngularVelocity() / steeringStrengthThreshhold;
            } else {
                // maximum steering strength
                steeringStrength = (steeringStrength > 0) ? info.getMaxAbsoluteAngularVelocity() : -info.getMaxAbsoluteAngularVelocity();
            }
        }

        float absoluteSteeringStrength = steeringStrength - info.getAngularVelocity();


        if( absoluteSteeringStrength >= 0 && absoluteSteeringStrength > info.getMaxAbsoluteAngularAcceleration()) {
            absoluteSteeringStrength = info.getMaxAbsoluteAngularAcceleration();
        } else if (Math.abs(absoluteSteeringStrength) > info.getMaxAbsoluteAngularAcceleration()) {
            absoluteSteeringStrength = -info.getMaxAbsoluteAngularAcceleration();
        }



        Track track = info.getTrack();
        track.getWidth();
        track.getHeight();
        Polygon[] obstacles = track.getObstacles(); //(Oberflaeche der) Hindernisse
        Polygon obs = obstacles[0];
        track.getObstacles(); // Hindernisse - nächste Übung


        /**
         *
         * BESCHLEUNIGUNG
         *
         */

        float breakRadius = 30;
        float checkpointRadius = 1f;
        float speed;

        if( MathVec.abs(carToCheckpoint) < breakRadius) {
            speed = MathVec.abs(carToCheckpoint) * info.getMaxVelocity() / breakRadius / 4;
            if ( MathVec.abs(carToCheckpoint) < checkpointRadius) {
                Vec2 currentSpeedVector = new Vec2(info.getVelocity().x, info.getVelocity().y);
                float currentSpeed = MathVec.abs(currentSpeedVector);
                accelaration = info.getMaxVelocity() - currentSpeed / 1;
                accelaration = (accelaration > info.getMaxVelocity()) ? info.getMaxVelocity() : accelaration;
                speed = accelaration;
            }
        } else {
            speed = info.getMaxVelocity();
        }
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
