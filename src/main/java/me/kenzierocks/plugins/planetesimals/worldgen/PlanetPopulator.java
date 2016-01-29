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
package me.kenzierocks.plugins.planetesimals.worldgen;

import java.util.Optional;
import java.util.Random;

import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;

import com.flowpowered.math.vector.Vector3i;

import me.kenzierocks.plugins.planetesimals.Database;
import me.kenzierocks.plugins.planetesimals.Planetesimals;
import me.kenzierocks.plugins.planetesimals.world.ChunkBoundary;

public class PlanetPopulator implements PopulatorObject {

    static final int MAX_RADIUS = 15;
    static final Vector3i MIN =
            Sponge.getServer().getChunkLayout().getSpaceMin()
                    .mul(Sponge.getServer().getChunkLayout().getChunkSize())
                    .add(MAX_RADIUS, MAX_RADIUS, MAX_RADIUS);
    static final Vector3i MAX =
            Sponge.getServer().getChunkLayout().getSpaceMax()
                    .mul(Sponge.getServer().getChunkLayout().getChunkSize())
                    .sub(MAX_RADIUS, MAX_RADIUS, MAX_RADIUS);

    @Override
    public String getId() {
        return Planetesimals.PIDS;
    }

    @Override
    public String getName() {
        return Planetesimals.NAME;
    }

    @Override
    public boolean canPlaceAt(World world, int x, int y, int z) {
        Optional<ChunkBoundary> chunkB = Sponge.getServer().getChunkLayout()
                .toChunk(x, y, z).map(ChunkBoundary::create);
        if (!chunkB.isPresent()) {
            return false;
        }
        ChunkBoundary chunk = chunkB.get();
        DSLContext db = Planetesimals.getInstance().getDatabase().getDB();
        Vector3i chunkMin = chunk.getChunkMin();
        Result<Record4<Integer, Integer, Integer, Integer>> selectedPlanets = db
                .select(Database.PLANET_X_FIELD, Database.PLANET_Y_FIELD,
                        Database.PLANET_Z_FIELD, Database.PLANET_RADIUS_FIELD)
                .from(Database.PLANETS_TABLE)
                .where(Database.CHUNK_X_FIELD.equal(chunkMin.getX()),
                        Database.CHUNK_Y_FIELD.equal(chunkMin.getY()),
                        Database.CHUNK_Z_FIELD.equal(chunkMin.getZ()))
                .fetch();
        Vector3i pos = new Vector3i(x, y, z);
        if (selectedPlanets.stream().map(this::planetFromRecord)
                .anyMatch(p -> spheresInMaxRadius(pos, p))) {
            Planetesimals.getInstance().getLogger()
                    .info("Hey Hey Hey FALSE " + pos);
            return false;
        }
        Planetesimals.getInstance().getLogger().info("Hyaaaay TRUE " + pos);
        return MIN.compareTo(pos) < 0 && MAX.compareTo(pos) > 0;
    }

    @Override
    public void placeObject(World world, Random random, int x, int y, int z) {
        // expensive debug check
        // checkState(canPlaceAt(world, x, y, z), "you fail, sponge.");
        int radius = random.nextInt(MAX_RADIUS);
        Planet planet = Planet.create(new Vector3i(x, y, z), radius);
        DSLContext db = Planetesimals.getInstance().getDatabase().getDB();
        Vector3i chunkPos =
                Sponge.getServer().getChunkLayout().toChunk(x, y, z)
                        .orElseThrow(() -> new IllegalStateException(
                                "What? " + x + ", " + y + ", " + z
                                        + " isn't in a chunk!"));
        db.update(Database.PLANETS_TABLE)
                .set(Database.CHUNK_X_FIELD, chunkPos.getX())
                .set(Database.CHUNK_Y_FIELD, chunkPos.getY())
                .set(Database.CHUNK_Z_FIELD, chunkPos.getZ())
                .set(Database.PLANET_X_FIELD, planet.getPosition().getX())
                .set(Database.PLANET_Y_FIELD, planet.getPosition().getY())
                .set(Database.PLANET_Z_FIELD, planet.getPosition().getZ())
                .set(Database.PLANET_RADIUS_FIELD, planet.getRadius());
        ShapeHelper.makeSphere(planet.getPosition(),
                PlanetDesign.of(BlockTypes.STONE), planet.getRadius(), true,
                (pos, state) -> {
                    if (world.containsBlock(pos)) {
                        world.setBlock(pos, state, false);
                    }
                });
    }

    private Planet planetFromRecord(
            Record4<Integer, Integer, Integer, Integer> record) {
        return Planet.create(
                new Vector3i(record.value1(), record.value2(), record.value3()),
                record.value4());
    }

    // private boolean spheresTouch(Planet x, Planet z) {
    // int d2 = x.getPosition().distanceSquared(z.getPosition());
    // int targetRad = (x.getRadius() + z.getRadius())
    // * (x.getRadius() + z.getRadius());
    // if (d2 <= targetRad) {
    // return true;
    // }
    // return false;
    // }

    private boolean spheresInMaxRadius(Vector3i pos, Planet z) {
        int d2 = z.getPosition().distanceSquared(pos);
        int subTargetRad = MAX_RADIUS + z.getRadius();
        int targetRad = subTargetRad * subTargetRad;
        if (d2 <= targetRad) {
            return true;
        }
        return false;
    }

}
