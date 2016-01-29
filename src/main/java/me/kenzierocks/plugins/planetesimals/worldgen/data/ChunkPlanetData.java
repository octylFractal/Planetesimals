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
package me.kenzierocks.plugins.planetesimals.worldgen.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import com.flowpowered.math.vector.Vector3i;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.kenzierocks.plugins.planetesimals.worldgen.Planet;

@AutoValue
public abstract class ChunkPlanetData {

    private static final int PLANET_ID = 0;

    public static ChunkPlanetData readFromFile(InputStream in)
            throws IOException {
        ImmutableSet.Builder<Planet> planets = ImmutableSet.builder();
        try (
                DataInputStream stream = new DataInputStream(in)) {
            while (true) {
                int id = stream.readInt();
                int length = stream.readInt();
                byte[] data = new byte[length];
                stream.readFully(data);
                if (id == PLANET_ID) {
                    planets.add(readPlanet(ByteStreams.newDataInput(data)));
                } else {
                    throw new UnsupportedOperationException(
                            "Don't know how to process " + id);
                }
            }
        } catch (EOFException eof) {
            // Expected, marks end of data.
        }
        return fromPlanets(planets.build());
    }

    private static Planet readPlanet(ByteArrayDataInput stream) {
        int x = stream.readInt();
        int y = stream.readInt();
        int z = stream.readInt();
        int radius = stream.readInt();
        return Planet.create(new Vector3i(x, y, z), radius);
    }

    private static void writePlanet(Planet planet, ByteArrayDataOutput stream) {
        stream.writeInt(planet.getPosition().getX());
        stream.writeInt(planet.getPosition().getY());
        stream.writeInt(planet.getPosition().getZ());
        stream.writeInt(planet.getRadius());
    }

    public static ChunkPlanetData
            fromPlanets(Collection<? extends Planet> planets) {
        return new AutoValue_ChunkPlanetData(ImmutableSet.copyOf(planets));
    }

    ChunkPlanetData() {
    }

    public abstract ImmutableSet<Planet> getPlanets();

    public void writeToFile(OutputStream out) throws IOException {
        try (
                DataOutputStream stream = new DataOutputStream(out)) {
            for (Planet planet : getPlanets()) {
                ByteArrayDataOutput planetData = ByteStreams.newDataOutput();
                writePlanet(planet, planetData);
                byte[] data = planetData.toByteArray();
                stream.writeInt(PLANET_ID);
                stream.writeInt(data.length);
                stream.write(data);
            }
        }
    }

}
