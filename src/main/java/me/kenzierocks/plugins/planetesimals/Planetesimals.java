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
package me.kenzierocks.plugins.planetesimals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import com.google.inject.Inject;

import me.kenzierocks.plugins.planetesimals.commands.CommandManager;
import me.kenzierocks.plugins.planetesimals.worldgen.PlanetGenerator;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = Planetesimals.ID, name = Planetesimals.NAME,
        version = Planetesimals.VERSION)
public final class Planetesimals {

    // TODO make an AP to replace at runtime
    // public static final String ID = "@ID@";
    // public static final String NAME = "@NAME@";
    // public static final String VERSION = "@VERSION@";
    public static final String ID = "planetesimals";
    public static final String PIDS = ID + ":" + ID;
    public static final String NAME = "Planetesimals";
    public static final String VERSION = "0.0.1-SNAPSHOT";
    private static Planetesimals INSTANCE;

    public static Planetesimals getInstance() {
        return INSTANCE;
    }

    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> config;

    {
        INSTANCE = this;
    }

    private SpongeExecutorService executor;
    private Database database = new Database();

    public Logger getLogger() {
        return this.logger;
    }

    public SpongeExecutorService getExecutor() {
        if (this.executor == null) {
            this.executor = Sponge.getScheduler().createSyncExecutor(this);
        }
        return this.executor;
    }

    public Database getDatabase() {
        return this.database;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.logger.info("Loading " + NAME + " v" + VERSION);
        try {
            Files.createDirectories(this.configDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot use the plugin with no configs!",
                    e);
        }
        this.database.init();
        CommandManager.addCommands(this);
        Sponge.getRegistry().register(WorldGeneratorModifier.class,
                PlanetGenerator.INSTANCE);
        this.logger.info("Loaded " + NAME + " v" + VERSION);
    }

    public Path getConfigDir() {
        return this.configDir;
    }

    public String getJDBCUrl() {
        CommentedConfigurationNode root;
        try {
            root = this.config.load();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load configuration.", e);
        }
        CommentedConfigurationNode jdbc = root.getNode("jdbc");
        jdbc.setComment("The JDBC url. Leave as H2 for now.");
        String url = jdbc.getString("jdbc:h2:"
                + this.configDir.resolve("chunks.h2").toAbsolutePath());
        jdbc.setValue(url);
        try {
            this.config.save(root);
        } catch (IOException e) {
            // NOT FATAL. WE CAN CONTINUE.
            this.logger.warn("Didn't save config",
                    new IllegalStateException("Cannot save configuration.", e));
        }
        return url;
    }

}
