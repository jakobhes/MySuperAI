package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class MySuperAI extends AI{

    public MySuperAI (Info info) {
        super(info);
        //enlistForTournament(566201); //fuer Abgabe
        enlistForInternalDevelopmentPurposesOnlyAndDoNOTConsiderThisAsPartOfTheHandedInSolution();//zum testen
        //hier irgendwas
        //ja
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

        //Current Checkpoint Coordinates
        double currentX = info.getCurrentCheckpoint().getX();
        double currentY = info.getCurrentCheckpoint().getY();

        float distanceToDest = (float) (Math.sqrt(Math.pow(currentX - info.getX(), 2) + Math.pow(currentY - info.getY(), 2)));
        Vector2f orientation = new Vector2f((float)(Math.cos(info.getOrientation())), (float) (Math.sin(info.getOrientation())));
        Vector2f currentCheckpoint = new Vector2f((float)info.getCurrentCheckpoint().getX(), (float)info.getCurrentCheckpoint().getY());
        Vector2f currentPos = new Vector2f(info.getX(), info.getY());
        Vector2f destVektor = new Vector2f();
        Vector2f.sub(currentCheckpoint, currentPos, destVektor);
        float dot = orientation.x * -destVektor.y + orientation.y * destVektor.x;
        float angleBetweenPosAndDest = Vector2f.angle(orientation, destVektor);
        float wunschdrehgeschw;

        //---------------------------------------ARRIVE----------------------------------------

        float destRad = 5;
        float breakRad = info.getVelocity().length();
        float speed = info.getMaxVelocity();
        if (distanceToDest < breakRad) {
            speed = (distanceToDest * info.getMaxVelocity() / breakRad);
            if (distanceToDest < destRad) {
                speed = info.getMaxVelocity();
            }
        }
        float acceleration = speed - info.getVelocity().length() / 1;


        //----------------------------------------ALIGN----------------------------------------
        if (dot > 0) {
            angleBetweenPosAndDest = -angleBetweenPosAndDest;
        }

        float tolerance = 0.0000000001f;
        if (Math.abs(angleBetweenPosAndDest) < Math.abs(info.getAngularVelocity())) {
            wunschdrehgeschw = (angleBetweenPosAndDest * info.getMaxAbsoluteAngularVelocity() / Math.abs(info.getAngularVelocity()));
        } else if (angleBetweenPosAndDest >= tolerance){
            wunschdrehgeschw = info.getMaxAbsoluteAngularVelocity();
        } else  {
            wunschdrehgeschw = -info.getMaxAbsoluteAngularVelocity();
        }

        float drehbeschleunigungVonAlign = (wunschdrehgeschw - info.getAngularVelocity()) / 1;

        return new DriverAction(acceleration, drehbeschleunigungVonAlign);
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