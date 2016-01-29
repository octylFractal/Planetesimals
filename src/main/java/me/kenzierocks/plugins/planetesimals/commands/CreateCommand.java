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

import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;

import me.kenzierocks.plugins.planetesimals.Planetesimals;
import me.kenzierocks.plugins.planetesimals.worldgen.PlanetGenerator;

public class CreateCommand implements Command {

    private static final String NAME = "name";
    private static final Text CREATING_MESSAGE =
            Text.of("Creating your world right now...");
    private static final Text NO_PERMISSION_MESSAGE =
            Text.of(TextColors.RED, "You don't have permission to do that!");

    private static Text doneMessage(String worldName) {
        return Text.of("Created a new Planetesimals world named " + worldName);
    }

    private Text failMessage(String worldName, String reason) {
        return Text.of(TextColors.RED,
                String.format(
                        "Couldn't create a new Planetesimals world named %s: %s",
                        worldName, reason));
    }

    private static Supplier<RuntimeException> required(String arg) {
        return () -> new IllegalArgumentException(
                "Argument " + arg + " is required");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        if (src.hasPermission(Planetesimals.ID + ".world.create")) {
            src.sendMessage(CREATING_MESSAGE);
            String name =
                    args.<String> getOne(NAME).orElseThrow(required(NAME));
            Planetesimals.getInstance().getExecutor()
                    .execute(() -> createWorld(src, name));
        } else {
            src.sendMessage(NO_PERMISSION_MESSAGE);
            return CommandResult.empty();
        }
        return CommandResult.success();
    }

    private void createWorld(CommandSource init, String name) {
        Optional<WorldProperties> props =
                Sponge.getServer().createWorldProperties(
                        WorldCreationSettings.builder().loadsOnStartup(true)
                                .dimension(DimensionTypes.OVERWORLD)
                                .generatorModifiers(PlanetGenerator.INSTANCE)
                                // .generatorSettings(GEN_SETTINGS)
                                .name(name).build());
        if (!props.isPresent()) {
            init.sendMessage(failMessage(name, "Properties invalid"));
            return;
        }
        Optional<World> world = Sponge.getServer().loadWorld(props.get());
        if (!world.isPresent()) {
            init.sendMessage(failMessage(name, "Couldn't load world"));
            return;
        }
        init.sendMessage(doneMessage(name));
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments.string(Text.of(NAME));
    }

    @Override
    public String getStringDescription() {
        return "World creation helper";
    }

}
