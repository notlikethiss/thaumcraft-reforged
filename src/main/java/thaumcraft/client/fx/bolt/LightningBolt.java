package thaumcraft.client.fx.bolt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.jspecify.annotations.Nullable;

public final class LightningBolt {
    final BoltVector start;
    final BoltVector end;
    final float length;
    List<Segment> segments = new ArrayList<>();
    private final Map<Integer, Integer> splitParents = new HashMap<>();
    private final Random random;
    float multiplier = 1.0F;
    int numSegments0 = 1;
    int increment = 1;
    int type;
    float width = 0.03F;
    int particleAge;
    int particleMaxAge;
    private int numSplits;
    private boolean finalized;

    public LightningBolt(double x1, double y1, double z1, double x2, double y2, double z2, long seed) {
        start = new BoltVector(x1, y1, z1);
        end = new BoltVector(x2, y2, z2);
        random = new Random(seed);
        length = end.copy().sub(start).length();
        particleMaxAge = 3 + random.nextInt(3) - 1;
        particleAge = -((int) (length * 3.0F));
        segments.add(new Segment(start, end));
    }

    public LightningBolt(double x1, double y1, double z1, double x2, double y2, double z2, long seed, int duration, float multiplier, int speed) {
        this(x1, y1, z1, x2, y2, z2, seed);
        particleMaxAge = duration + random.nextInt(duration) - duration / 2;
        this.multiplier = multiplier;
        increment = speed;
    }

    public LightningBolt setType(int type) {
        this.type = type;
        return this;
    }

    public LightningBolt setWidth(float width) {
        this.width = width;
        return this;
    }

    public LightningBolt setSpeed(int speed) {
        increment = speed;
        return this;
    }

    public LightningBolt setMultiplier(float multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public void fractal(int splits, float amount, float splitChance, float splitLength, float splitAngle) {
        if (finalized) {
            return;
        }
        List<Segment> oldSegments = segments;
        segments = new ArrayList<>();
        Segment prev;
        for (Segment segment : oldSegments) {
            prev = segment.prev;
            BoltVector subSegment = segment.diff.copy().scale(1.0F / splits);
            BoltPoint[] newPoints = new BoltPoint[splits + 1];
            BoltVector startPoint = segment.startPoint.point;
            newPoints[0] = segment.startPoint;
            newPoints[splits] = segment.endPoint;
            for (int i = 1; i < splits; i++) {
                BoltVector randOff = BoltVector.getPerpendicular(segment.diff).rotate(random.nextFloat() * 360.0F, segment.diff);
                randOff.scale((random.nextFloat() - 0.5F) * amount);
                BoltVector basePoint = startPoint.copy().add(subSegment.copy().scale(i));
                newPoints[i] = new BoltPoint(basePoint, randOff);
            }
            for (int i = 0; i < splits; i++) {
                Segment next = new Segment(newPoints[i], newPoints[i + 1], segment.light, segment.segmentNo * splits + i, segment.splitNo);
                next.prev = prev;
                if (prev != null) {
                    prev.next = next;
                }
                if (i != 0 && random.nextFloat() < splitChance) {
                    BoltVector splitRot = BoltVector.xCrossProduct(next.diff).rotate(random.nextFloat() * 360.0F, next.diff);
                    BoltVector diff = next.diff.copy().rotate((random.nextFloat() * 0.66F + 0.33F) * splitAngle, splitRot).scale(splitLength);
                    numSplits++;
                    splitParents.put(numSplits, next.splitNo);
                    Segment split = new Segment(
                        newPoints[i],
                        new BoltPoint(newPoints[i + 1].basePoint, newPoints[i + 1].offsetVec.copy().add(diff)),
                        segment.light / 2.0F,
                        next.segmentNo,
                        numSplits
                    );
                    split.prev = prev;
                    segments.add(split);
                }
                prev = next;
                segments.add(next);
            }
            if (segment.next != null) {
                segment.next.prev = prev;
            }
        }
        numSegments0 *= splits;
    }

    public LightningBolt defaultFractal() {
        fractal(2, length * multiplier / 8.0F, 0.7F, 0.1F, 45.0F);
        fractal(2, length * multiplier / 12.0F, 0.5F, 0.1F, 50.0F);
        fractal(2, length * multiplier / 17.0F, 0.5F, 0.1F, 55.0F);
        fractal(2, length * multiplier / 23.0F, 0.5F, 0.1F, 60.0F);
        fractal(2, length * multiplier / 30.0F, 0.0F, 0.0F, 0.0F);
        fractal(2, length * multiplier / 34.0F, 0.0F, 0.0F, 0.0F);
        fractal(2, length * multiplier / 40.0F, 0.0F, 0.0F, 0.0F);
        return this;
    }

    private void calculateCollisionAndDiffs() {
        Map<Integer, Integer> lastActiveSegment = new HashMap<>();
        segments.sort(Comparator.<Segment>comparingInt(segment -> segment.splitNo).thenComparingInt(segment -> segment.segmentNo));
        int lastSplitCalc = 0;
        int lastActiveSeg = 0;
        for (Segment segment : segments) {
            if (segment.splitNo > lastSplitCalc) {
                lastActiveSegment.put(lastSplitCalc, lastActiveSeg);
                lastSplitCalc = segment.splitNo;
                lastActiveSeg = lastActiveSegment.getOrDefault(splitParents.get(segment.splitNo), 0);
            }
            lastActiveSeg = segment.segmentNo;
        }
        lastActiveSegment.put(lastSplitCalc, lastActiveSeg);
        lastSplitCalc = 0;
        lastActiveSeg = lastActiveSegment.getOrDefault(0, 0);
        Iterator<Segment> iterator = segments.iterator();
        while (iterator.hasNext()) {
            Segment segment = iterator.next();
            if (lastSplitCalc != segment.splitNo) {
                lastSplitCalc = segment.splitNo;
                lastActiveSeg = lastActiveSegment.getOrDefault(segment.splitNo, 0);
            }
            if (segment.segmentNo > lastActiveSeg) {
                iterator.remove();
            }
            segment.calcEndDiffs();
        }
    }

    public LightningBolt finalizeBolt() {
        if (!finalized) {
            finalized = true;
            calculateCollisionAndDiffs();
            segments.sort((first, second) -> Float.compare(second.light, first.light));
        }
        return this;
    }

    void tick() {
        particleAge += increment;
        if (particleAge > particleMaxAge) {
            particleAge = particleMaxAge;
        }
    }

    boolean isDead() {
        return particleAge >= particleMaxAge;
    }

    static final class BoltPoint {
        final BoltVector point;
        final BoltVector basePoint;
        final BoltVector offsetVec;

        BoltPoint(BoltVector basePoint, BoltVector offsetVec) {
            this.point = basePoint.copy().add(offsetVec);
            this.basePoint = basePoint;
            this.offsetVec = offsetVec;
        }
    }

    static final class Segment {
        final BoltPoint startPoint;
        final BoltPoint endPoint;
        BoltVector diff;
        @Nullable Segment prev;
        @Nullable Segment next;
        BoltVector nextDiff;
        BoltVector prevDiff;
        float sinPrev;
        float sinNext;
        final float light;
        final int segmentNo;
        final int splitNo;

        Segment(BoltPoint start, BoltPoint end, float light, int segmentNo, int splitNo) {
            this.startPoint = start;
            this.endPoint = end;
            this.light = light;
            this.segmentNo = segmentNo;
            this.splitNo = splitNo;
            this.diff = end.point.copy().sub(start.point);
            this.prevDiff = diff;
            this.nextDiff = diff;
        }

        Segment(BoltVector start, BoltVector end) {
            this(new BoltPoint(start, new BoltVector(0.0, 0.0, 0.0)), new BoltPoint(end, new BoltVector(0.0, 0.0, 0.0)), 1.0F, 0, 0);
        }

        void calcEndDiffs() {
            if (prev != null) {
                BoltVector prevDiffNorm = prev.diff.copy().normalize();
                BoltVector thisDiffNorm = diff.copy().normalize();
                prevDiff = thisDiffNorm.add(prevDiffNorm).normalize();
                sinPrev = (float) Math.sin(BoltVector.anglePreNorm(thisDiffNorm, prevDiffNorm.scale(-1.0F)) / 2.0F);
            } else {
                prevDiff = diff.copy().normalize();
                sinPrev = 1.0F;
            }
            if (next != null) {
                BoltVector nextDiffNorm = next.diff.copy().normalize();
                BoltVector thisDiffNorm = diff.copy().normalize();
                nextDiff = thisDiffNorm.add(nextDiffNorm).normalize();
                sinNext = (float) Math.sin(BoltVector.anglePreNorm(thisDiffNorm, nextDiffNorm.scale(-1.0F)) / 2.0F);
            } else {
                nextDiff = diff.copy().normalize();
                sinNext = 1.0F;
            }
        }
    }
}
