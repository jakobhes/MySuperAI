package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;

import java.awt.*;


public class MySuperAI extends AI {

    // Car Position as Vec2
    public Vec2 carPosition;

    // Orientation of the car (as an Angle)
    public float carOrientation;

    // Position of the Current Checkpoint as Vec2
    public Vec2 checkpointPosition;

    // Direction vector from car to current checkpoint
    public Vec2 carToCheckpoint;

    // Orientation of direction vector from car to checkpoint (as an Angle)
    public float checkpointOrientation;

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
        carPosition = new Vec2(info.getX(), info.getY());
        carOrientation = info.getOrientation();
        checkpointPosition = new Vec2((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());
        carToCheckpoint = MathVec.direction(carPosition, checkpointPosition);
        checkpointOrientation = (carToCheckpoint.b > 0) ? MathVec.angle(new Vec2(1,0), carToCheckpoint) : - MathVec.angle(new Vec2(1,0), carToCheckpoint);

        Track track = info.getTrack();
        track.getWidth();
        track.getHeight();
        Polygon[] obstacles = track.getObstacles(); //(Oberflaeche der) Hindernisse
        Polygon obs = obstacles[0];
        track.getObstacles(); // Hindernisse - nächste Übung

        return new DriverAction(accelarate(50,1), steering(0.01f, (float)Math.PI/3));
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

    // controling car speed
    public float accelarate(float breakRadius, float checkpointRadius) {
        float speed;

        if( MathVec.abs(carToCheckpoint) < breakRadius) {
            speed = MathVec.abs(carToCheckpoint) * info.getMaxVelocity() / breakRadius / 4;
            if ( MathVec.abs(carToCheckpoint) < checkpointRadius) {
                Vec2 currentSpeedVector = new Vec2(info.getVelocity().x, info.getVelocity().y);
                float currentSpeed = MathVec.abs(currentSpeedVector);
                float accelaration = info.getMaxVelocity() - currentSpeed / 1;

                //clipping
                accelaration = (accelaration > info.getMaxVelocity()) ? info.getMaxVelocity() : accelaration;
                speed = accelaration;
            }
        } else {
            speed = info.getMaxVelocity();
        }
        return speed;
    }

    // steering behaviour
    public float steering(float steeringThreshold, float steeringStrengthThreshold) {
        float rotationToComplete = Math.abs(checkpointOrientation - carOrientation);
        float steeringStrength = 0;

        if(rotationToComplete > steeringThreshold) {
            // take a turn
            if (rotationToComplete < steeringStrengthThreshold) {
                //lower the steering strength
                steeringStrength = (checkpointOrientation - carOrientation) * info.getMaxAbsoluteAngularVelocity() / steeringStrengthThreshold;
            } else {
                // maximum steering strength
                steeringStrength = (steeringStrength > 0) ? info.getMaxAbsoluteAngularVelocity() : -info.getMaxAbsoluteAngularVelocity();
            }
        }

        float absoluteSteeringStrength = steeringStrength - info.getAngularVelocity();

        //clipping
        if( absoluteSteeringStrength >= 0 && absoluteSteeringStrength > info.getMaxAbsoluteAngularAcceleration()) {
            absoluteSteeringStrength = info.getMaxAbsoluteAngularAcceleration();
        } else if (Math.abs(absoluteSteeringStrength) > info.getMaxAbsoluteAngularAcceleration()) {
            absoluteSteeringStrength = -info.getMaxAbsoluteAngularAcceleration();
        }
        return absoluteSteeringStrength;
    }
}
