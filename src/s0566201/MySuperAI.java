package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;

import java.awt.*;


public class MySuperAI extends AI{

    public Vec2 carPosition;
    public Vec2 currentCheckpoint;

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

        // CurrentCheckpoint as Vec2
        currentCheckpoint = new Vec2((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());


        // align
        info.getX(); // meine Position
        info.getY();
        info.getCurrentCheckpoint(); // Zielposition
        info.getOrientation(); // Blickrichtung zwischen -PI und +PI
        info.getAngularVelocity(); // aktuelle Drehgeschwindigkeit


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

        float distanceToDest = (float) (Math.sqrt(Math.pow(currentX - info.getX(), 2) + Math.pow(currentY - info.getY(), 2)));
        float angleToDest = (float) Math.acos((currentX - info.getX()) / distanceToDest);

        float wunschdrehgeschw = 0f;


        if (currentY < info.getY()) {
            angleToDest = -angleToDest;
        }

        float tolerance = 0.005f;

        if (Math.abs(angleToDest - info.getOrientation()) < Math.abs(info.getAngularVelocity())) {
            wunschdrehgeschw = (angleToDest - info.getOrientation()) * info.getMaxAbsoluteAngularVelocity() / 2*info.getAngularVelocity();
        } else if (angleToDest - info.getOrientation() >= tolerance){
            wunschdrehgeschw = info.getMaxAbsoluteAngularVelocity();
        } else  if (angleToDest - info.getOrientation() <= -tolerance)
            wunschdrehgeschw = -info.getMaxAbsoluteAngularVelocity();

        float drehbeschleunigungVonAlign = (wunschdrehgeschw - info.getAngularVelocity()) / 1;
//        if (wunschdrehgeschw > info.getMaxAbsoluteAngularAcceleration()) wunschdrehgeschw = info.getMaxAbsoluteAngularAcceleration();
//        if (wunschdrehgeschw < -info.getMaxAbsoluteAngularAcceleration()) wunschdrehgeschw = -info.getMaxAbsoluteAngularAcceleration();

//        System.out.println("Orientierung: " + info.getOrientation() + " Winkel: " + angleToDest + " " + " Drehbeschleunigung: " + drehbeschleunigungVonAlign);
//        System.out.println((Math.abs(info.getOrientation() - angleToDest) < Math.abs(info.getAngularVelocity())) + " " + info.getAngularVelocity());
//        System.out.println("Checkpoint Y | X " + currentY + " " + currentX);
//        System.out.println("Meine Pos " + info.getX() + " " + info.getY());


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
