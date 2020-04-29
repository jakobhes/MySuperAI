package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class MySuperAI extends AI{

    public Vector2f carPosition;
    public Vector2f pointsToRight;
    public Vector2f currentCheckpoint;
    public Vector2f directionToCP;
    public float angleOfCPDirection;
    public float rotationVelocity;
    public float carOrientation;

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
        carPosition = new Vector2f(info.getX(), info.getY());

        // direction vector that points to right
        pointsToRight = new Vector2f(1,0);

        // CurrentCheckpoint as Vector
        currentCheckpoint = new Vector2f((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());

        // Direction vector from car to current checkpoint
        directionToCP = new Vector2f();
        Vector2f.sub(currentCheckpoint, carPosition, directionToCP);

        // initial angle of car direction and check point direction
        angleOfCPDirection = Vector2f.angle(pointsToRight, directionToCP);


        // orientation of car
        carOrientation = info.getOrientation();

        // current rotation velocity
        rotationVelocity = info.getAngularVelocity();


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
        double currentX = info.getCurrentCheckpoint().getX();
        double currentY = info.getCurrentCheckpoint().getY();

        float distanceToDest = (float) (Math.sqrt(Math.pow(currentCheckpoint.x - carPosition.x, 2) + Math.pow(currentCheckpoint.y - carPosition.y, 2)));
        float angleToDest = (float) Math.acos((currentCheckpoint.x - carPosition.x) / distanceToDest);

        float wunschdrehgeschw = 0f;

        float tolerance = 0.005f;

        if (Math.abs(angleToDest - carOrientation) < Math.abs(rotationVelocity)) {
            wunschdrehgeschw = (angleToDest - carOrientation) * info.getMaxAbsoluteAngularVelocity() / 2*rotationVelocity;
        } else if (angleToDest - carOrientation >= tolerance){
            wunschdrehgeschw = info.getMaxAbsoluteAngularVelocity();
        } else  if (angleToDest - carOrientation <= -tolerance)
            wunschdrehgeschw = -info.getMaxAbsoluteAngularVelocity();

        if (Math.abs(angleOfCPDirection - carOrientation) > Math.PI) wunschdrehgeschw = -wunschdrehgeschw;

        float drehbeschleunigungVonAlign = (wunschdrehgeschw - info.getAngularVelocity()) / 1;

        return new DriverAction(1, drehbeschleunigungVonAlign);
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
