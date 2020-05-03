package s0566201;

import static org.lwjgl.opengl.GL11.*;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

public class MySuperAI extends AI{

    Vector2f rayCastMiddle = new Vector2f();
    Vector2f rayLeft = new Vector2f();
    Vector2f rayRight = new Vector2f();

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

        float destRad = 3;
        float breakRad = info.getVelocity().length()/1.5f;
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

        float tolerance = 0.000001f;

        if (Math.abs(angleBetweenPosAndDest) < Math.abs(info.getAngularVelocity())/2) {
            wunschdrehgeschw = (angleBetweenPosAndDest * info.getMaxAbsoluteAngularVelocity() / 2*Math.abs(info.getAngularVelocity()));
        } else if (angleBetweenPosAndDest > tolerance){
            wunschdrehgeschw = info.getMaxAbsoluteAngularVelocity();
        } else {
            wunschdrehgeschw = -info.getMaxAbsoluteAngularVelocity();
        }


        //--------------------------COLLISION / OBSTACLE AVOIDANCE-------------------------------

        //Single Ray (middle)
        float rayCastLength;
        if (distanceToDest <= 4*breakRad) {
            rayCastLength = info.getVelocity().length();
        } else {
            rayCastLength = 4*info.getVelocity().length();
        }
        Vector2f orientationWithLength = (Vector2f)orientation.scale(rayCastLength);
        Vector2f.add(currentPos, orientationWithLength, rayCastMiddle);

        //Ray Left
        //orientation vektor drehen
        float radian = (float)Math.PI/8;
        float ox = orientationWithLength.x;
        float oy = orientationWithLength.y;
        Vector2f rayLeftOrientation = new Vector2f((float)(Math.cos(radian) * ox - Math.sin(radian) * oy), (float)(Math.sin(radian) * ox + Math.cos(radian) * oy));
        Vector2f.add(currentPos, rayLeftOrientation, rayLeft);

        //Ray Right
        Vector2f rayRightOrientation = new Vector2f((float)(Math.cos(2*Math.PI-radian) * ox - Math.sin(2*Math.PI-radian) * oy), (float)(Math.sin(2*Math.PI-radian) * ox + Math.cos(2*Math.PI-radian) * oy));
        Vector2f.add(currentPos, rayRightOrientation, rayRight);

        for (int i = 2; i < obstacles.length; i++) {
            if (obstacles[i].contains(rayLeft.x, rayLeft.y))
                wunschdrehgeschw = -info.getMaxAbsoluteAngularVelocity();
            else if (obstacles[i].contains(rayRight.x, rayRight.y))
                wunschdrehgeschw = info.getMaxAbsoluteAngularVelocity();
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
}