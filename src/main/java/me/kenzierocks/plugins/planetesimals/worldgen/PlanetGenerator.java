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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.common.world.gen.populators.RandomObjectPopulator;

import me.kenzierocks.plugins.planetesimals.Planetesimals;

public enum PlanetGenerator implements WorldGeneratorModifier {

    INSTANCE;

    @Override
    public String getId() {
        return Planetesimals.PIDS;
    }

    @Override
    public String getName() {
        return Planetesimals.NAME;
    }

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world,
            DataContainer settings, WorldGenerator worldGenerator) {
        // Set planet maker.
        worldGenerator.setBaseGenerationPopulator(
                new NoNonsenseGenerationPopulator());
        // No per-chunk stuff.
        worldGenerator.getGenerationPopulators().clear();
        // No other populators.
        worldGenerator.getPopulators().clear();
        worldGenerator
                .getPopulators().add(
                        new RandomObjectPopulator(new PlanetPopulator(),
                                VariableAmount.fixed(10),
                                VariableAmount.baseWithRandomAddition(
                                        PlanetPopulator.MIN.getY(),
                                        PlanetPopulator.MAX.getY()
                                                - PlanetPopulator.MIN.getY()),
                0.2));
        // worldGenerator.setBiomeGenerator(new NothingBiomeGenerator());
        Sponge.getRegistry().getAllOf(BiomeType.class).stream()
                .map(worldGenerator::getBiomeSettings).forEach(gen -> {
                    gen.getPopulators().clear();
                    gen.getGenerationPopulators().clear();
                    gen.getGroundCoverLayers().clear();
                });
    }

}
