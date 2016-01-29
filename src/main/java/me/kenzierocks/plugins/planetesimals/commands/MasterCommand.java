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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.google.common.collect.ImmutableList;

public final class MasterCommand implements Command {

    private static final Text LEFTOVERS_KEY = Text.of("other");
    private static final Map<List<String>, CommandCallable> CHILDREN =
            new HashMap<>();
    private static final Map<String, CommandCallable> HIDDEN_CHILDREN =
            new HashMap<>();

    private static void addChild(Command command, String... aliases) {
        CHILDREN.put(ImmutableList.copyOf(aliases), command.getSpec());
    }

    private static void addHiddenChild(Command command, String... aliases) {
        CommandCallable spec = command.getSpec();
        Stream.of(aliases).forEach(x -> HIDDEN_CHILDREN.put(x, spec));
    }

    static {
        addChild(new CreateCommand(), "create", "c");
        addChild(new TPCommand(), "tp");
        addHiddenChild(new EmptyDBCommand(), "empty");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        Optional<String> subcmd = args.<String> getOne(LEFTOVERS_KEY.toPlain());
        if (subcmd.isPresent()) {
            String[] parts = subcmd.get().split(" ", 2);
            Optional<CommandCallable> callable =
                    Optional.ofNullable(HIDDEN_CHILDREN.get(parts[0]));
            if (callable.isPresent()) {
                return callable.get().process(src,
                        parts.length > 1 ? parts[1] : "");
            }
        }
        src.sendMessage(Text
                .of("Not a command: " + subcmd.orElse("literally nothing")));
        return CommandResult.success();
    }

    @Override
    public CommandSpec.Builder
            doExtraConfiguration(CommandSpec.Builder builder) {
        return builder.children(CHILDREN);
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments.remainingJoinedStrings(LEFTOVERS_KEY);
    }

    @Override
    public String getStringDescription() {
        return "Planetesimals master command";
    }

}
