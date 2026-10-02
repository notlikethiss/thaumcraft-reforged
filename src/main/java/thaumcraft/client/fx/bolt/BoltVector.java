package thaumcraft.client.fx.bolt;

public final class BoltVector {
    public float x;
    public float y;
    public float z;

    public BoltVector(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    public BoltVector add(BoltVector vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public BoltVector sub(BoltVector vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public BoltVector scale(float scale) {
        x *= scale;
        y *= scale;
        z *= scale;
        return this;
    }

    public BoltVector normalize() {
        float length = length();
        x /= length;
        y /= length;
        z /= length;
        return this;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public BoltVector copy() {
        return new BoltVector(x, y, z);
    }

    public BoltVector rotate(float angle, BoltVector axis) {
        BoltVector normal = axis.copy().normalize();
        float ax = normal.x;
        float ay = normal.y;
        float az = normal.z;
        double radians = angle * 0.0174532925;
        float cos = (float) Math.cos(radians);
        float ocos = 1.0F - cos;
        float sin = (float) Math.sin(radians);
        float nx = x * (ax * ax * ocos + cos) + y * (ay * ax * ocos + az * sin) + z * (ax * az * ocos - ay * sin);
        float ny = x * (ax * ay * ocos - az * sin) + y * (ay * ay * ocos + cos) + z * (ay * az * ocos + ax * sin);
        float nz = x * (ax * az * ocos + ay * sin) + y * (ay * az * ocos - ax * sin) + z * (az * az * ocos + cos);
        x = nx;
        y = ny;
        z = nz;
        return this;
    }

    public static BoltVector crossProduct(BoltVector vec1, BoltVector vec2) {
        return new BoltVector(vec1.y * vec2.z - vec1.z * vec2.y, vec1.z * vec2.x - vec1.x * vec2.z, vec1.x * vec2.y - vec1.y * vec2.x);
    }

    public static BoltVector xCrossProduct(BoltVector vec) {
        return new BoltVector(0.0, vec.z, -vec.y);
    }

    public static BoltVector zCrossProduct(BoltVector vec) {
        return new BoltVector(-vec.y, vec.x, 0.0);
    }

    public static float dotProduct(BoltVector vec1, BoltVector vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
    }

    public static float anglePreNorm(BoltVector vec1, BoltVector vec2) {
        return (float) Math.acos(dotProduct(vec1, vec2));
    }

    public static BoltVector getPerpendicular(BoltVector vec) {
        return vec.z == 0.0F ? zCrossProduct(vec) : xCrossProduct(vec);
    }
}
