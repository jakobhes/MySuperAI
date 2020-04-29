package s0566201;


public class MathVec {

    public MathVec() {
    }

    // Gets the dot product of two vectors
    public static float dot(Vec2 vector1, Vec2 vector2) {
        return (vector1.a * vector2.a) + (vector1.b * vector2.b);
    }

    // Gets the absolute value of a vector
    public static float abs(Vec2 vector) {
        return (float) Math.sqrt(Math.pow(vector.a, 2) + Math.pow(vector.b, 2));
    }

    // Gets the angle between two vectors
    public static float angle(Vec2 vector1, Vec2 vector2) {
        return (float) Math.toDegrees(Math.acos(dot(vector1, vector2) / (abs(vector1) * abs(vector2))));
    }

    // Gets the direction vector
    public static Vec2 direction(Vec2 vector1, Vec2 vector2) {
        return new Vec2(vector1.a - vector2.a, vector1.b - vector2.b);
    }
}
