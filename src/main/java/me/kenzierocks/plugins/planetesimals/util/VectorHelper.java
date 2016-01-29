/*
 * This file is part of Planetesimals, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <https://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins.planetesimals.util;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.spongepowered.api.util.GuavaCollectors;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

public class VectorHelper {

    public static void loopVolume(Vector3i min, Vector3i max, Filter filter,
            Consumer<Vector3i> callback) {
        range(min.getX(), max.getX()).filter(filter.getXFilter()).forEach(x -> {
            range(min.getY(), max.getY()).filter(filter.getYFilter())
                    .forEach(y -> {
                range(min.getZ(), max.getZ()).filter(filter.getZFilter())
                        .forEach(z -> {
                    callback.accept(new Vector3i(x, y, z));
                });
            });
        });
    }

    public static <T> Stream<T> streamVolume(Vector3i min, Vector3i max,
            Filter filter, Function<Vector3i, T> callback) {
        return range(min.getX(), max.getX()).filter(filter.getXFilter())
                .mapToObj(x -> {
                    return range(min.getY(), max.getY())
                            .filter(filter.getYFilter()).mapToObj(y -> {
                        return range(min.getZ(), max.getZ())
                                .filter(filter.getZFilter()).mapToObj(z -> {
                            return callback.apply(new Vector3i(x, y, z));
                        });
                    }).flatMap(Function.identity());
                }).flatMap(Function.identity());
    }

    private static IntStream range(int x1, int x2) {
        return IntStream.rangeClosed(x1, x2);
    }

    private static final Collection<Vector3i> NEIGHBOR_OFFSETS;

    static {
        ImmutableSet.Builder<Vector3i> offsetVectors = ImmutableSet.builder();
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    if (x + y + z == 0) {
                        continue;
                    }
                    offsetVectors.add(new Vector3i(x, y, z));
                }
            }
        }
        NEIGHBOR_OFFSETS = offsetVectors.build();
    }

    /**
     * Neighbors includes corners as well.
     */
    public static Set<Vector3i> getNeighbors(Vector3i vec) {
        return NEIGHBOR_OFFSETS.stream().map(x -> vec.add(x))
                .collect(GuavaCollectors.toImmutableSet());
    }

    public static Set<Vector3i> generateRandomVectors(Random random, int count,
            Range<Integer> xRange, Range<Integer> yRange,
            Range<Integer> zRange) {
        ImmutableSet.Builder<Vector3i> vecs = ImmutableSet.builder();
        for (int i = 0; i < count; i++) {
            vecs.add(generateRandomVector(random, xRange, yRange, zRange));
        }
        return vecs.build();
    }

    public static Vector3i generateRandomVector(Random random,
            Range<Integer> xRange, Range<Integer> yRange,
            Range<Integer> zRange) {
        return new Vector3i(
                getIntInRange(random,
                        xRange.upperEndpoint() - xRange.lowerEndpoint(),
                        xRange.lowerEndpoint()),
                getIntInRange(random,
                        yRange.upperEndpoint() - yRange.lowerEndpoint(),
                        yRange.lowerEndpoint()),
                getIntInRange(random,
                        zRange.upperEndpoint() - zRange.lowerEndpoint(),
                        zRange.lowerEndpoint()));
    }

    private static int getIntInRange(Random random, int reducedUpper,
            int diff) {
        return random.nextInt(reducedUpper) + diff;
    }

}
