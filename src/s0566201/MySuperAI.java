package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;

import java.awt.*;


public class MySuperAI extends AI{

    public MySuperAI (Info info) {
        super(info);
        //enlistForTournament(566201); //fuer Abgabe
        enlistForInternalDevelopmentPurposesOnlyAndDoNOTConsiderThisAsPartOfTheHandedInSolution(); //zum testen
        //hier irgendwas
    }

    @Override
    public String getName() {
        return "JAKOB";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
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


        float drehbeschleunigungVonAlign = 0.1f;
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
