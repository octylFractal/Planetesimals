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
package me.kenzierocks.plugins.planetesimals.world;

import java.util.Collection;
import java.util.Set;

import org.spongepowered.api.util.GuavaCollectors;

import com.flowpowered.math.vector.Vector3i;
import com.google.auto.value.AutoValue;

import me.kenzierocks.plugins.planetesimals.util.VectorHelper;

@AutoValue
public abstract class ChunkBoundary {

    private static final Vector3i OFFSET_TO_MAX_IN_CHUNKS = Vector3i.ONE;
    private static final Vector3i BLOCK_CONVERSION = Vector3i.ONE.mul(16);

    public static ChunkBoundary create(int chunkX, int chunkY, int chunkZ) {
        return create(new Vector3i(chunkX, chunkY, chunkZ));
    }

    public static ChunkBoundary create(Vector3i chunkPos) {
        Vector3i chunkMin = chunkPos;
        Vector3i chunkMax = chunkMin.add(OFFSET_TO_MAX_IN_CHUNKS);
        Vector3i blockMin = chunkMin.mul(BLOCK_CONVERSION);
        Vector3i blockMax = chunkMax.mul(BLOCK_CONVERSION);
        return new AutoValue_ChunkBoundary(chunkMin, chunkMax, blockMin,
                blockMax);
    }

    public static Set<ChunkBoundary> getNeighbors(ChunkBoundary chunk) {
        Vector3i chunkPos = chunk.getChunkMin();
        Set<Vector3i> neighbors = VectorHelper.getNeighbors(chunkPos);
        return neighbors.stream().map(ChunkBoundary::create)
                .collect(GuavaCollectors.toImmutableSet());
    }

    public static Set<ChunkBoundary>
            getNeighbors(Collection<ChunkBoundary> chunks) {
        return chunks.stream()
                .flatMap(bndry -> VectorHelper.getNeighbors(bndry.getChunkMin())
                        .stream())
                .map(ChunkBoundary::create).distinct()
                .collect(GuavaCollectors.toImmutableSet());
    }

    ChunkBoundary() {
    }

    public abstract Vector3i getChunkMin();

    public abstract Vector3i getChunkMax();

    public abstract Vector3i getBlockMin();

    public abstract Vector3i getBlockMax();

}
