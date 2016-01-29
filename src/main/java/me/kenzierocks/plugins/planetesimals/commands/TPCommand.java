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
package me.kenzierocks.plugins.planetesimals.commands;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

public class TPCommand implements Command {

    private static final String NAME = "name";

    private static Supplier<RuntimeException> required(String arg) {
        return () -> new IllegalArgumentException(
                "Argument " + arg + " is required");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        if (src instanceof Entity) {
            String name =
                    args.<String> getOne(NAME).orElseThrow(required(NAME));
            List<String> worldNames = Stream.concat(
                    Sponge.getServer().getWorlds().stream().map(World::getName),
                    Sponge.getServer().getUnloadedWorlds().stream()
                            .map(WorldProperties::getWorldName))
                    .collect(Collectors.toList());
            Optional<World> world =
                    worldNames.stream().filter(Predicate.isEqual(name))
                            .findFirst().flatMap(Sponge.getServer()::loadWorld);
            if (!world.isPresent()) {
                src.sendMessage(Text.of("No such world " + name));
                return CommandResult.empty();
            }
            ((Entity) src).setLocation(world.get().getSpawnLocation());
            if (((Entity) src).transferToWorld(world.get().getUniqueId(),
                    world.get().getSpawnLocation().getPosition())) {
                src.sendMessage(Text.of("Teleport successful."));
            } else {
                src.sendMessage(Text.of("Teleport failed."));
                return CommandResult.empty();
            }
        } else {
            src.sendMessage(Text.of("You can't be teleported."));
            return CommandResult.empty();
        }
        return CommandResult.success();
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments.string(Text.of(NAME));
    }

    @Override
    public String getStringDescription() {
        return "World teleport helper";
    }

}
