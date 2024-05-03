//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.incendo.cloud.sponge;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.parser.standard.LiteralParser;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.type.tuple.Pair;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import static net.kyori.adventure.text.Component.text;

final class CloudSpongeCommand<C> implements Command.Raw {

    private final SpongeCommandManager<C> commandManager;
    private final String label;

    CloudSpongeCommand(
        final @NonNull String label,
        final @NonNull SpongeCommandManager<C> commandManager
    ) {
        this.label = label;
        this.commandManager = commandManager;
    }

    @Override
    public CommandResult process(final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable arguments) {
        final C cloudSender = this.commandManager.senderMapper().map(cause);
        final String input = this.formatCommandForParsing(arguments.input());
        this.commandManager.commandExecutor().executeCommand(cloudSender, input);
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(
        final @NonNull CommandCause cause,
        final ArgumentReader.@NonNull Mutable arguments
    ) {
        return this.commandManager.suggestionFactory()
            .suggestImmediately(this.commandManager.senderMapper().map(cause),
                this.formatCommandForSuggestions(arguments.input()))
            .list()
            .stream()
            .map(s -> CommandCompletion.of(s.suggestion(), s.tooltip()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(final @NonNull CommandCause cause) {
        return this.checkAccess(
            cause,
            this.namedNode().nodeMeta()
                .getOrDefault(CommandNode.META_KEY_ACCESS, Collections.emptyMap())
        );
    }

    @Override
    public Optional<Component> shortDescription(final CommandCause cause) {
        return Optional.of(this.usage(cause));
    }

    @Override
    public Optional<Component> extendedDescription(final CommandCause cause) {
        return Optional.of(this.usage(cause));
    }

    @Override
    public Optional<Component> help(final @NonNull CommandCause cause) {
        return Optional.of(this.usage(cause));
    }

    @Override
    public Component usage(final CommandCause cause) {
        return text(this.commandManager.commandSyntaxFormatter()
            .apply(this.commandManager.senderMapper().map(cause), Collections.emptyList(), this.namedNode()));
    }

    private CommandNode<C> namedNode() {
        return this.commandManager.commandTree().getNamedNode(this.label);
    }

    @Override
    public CommandTreeNode.Root commandTree() {
        final CommandTreeNode<CommandTreeNode.Root> root = CommandTreeNode.root();

        final CommandNode<C> cloud = this.namedNode();

        if (canExecute(cloud)) {
            root.executable();
        }

        this.addRequirement(cloud, root);

        this.addChildren(root, cloud);
        return (CommandTreeNode.Root) root;
    }

    private void addChildren(final CommandTreeNode<?> node, final CommandNode<C> cloud) {
        for (final CommandNode<C> child : cloud.children()) {
            final CommandComponent<C> value = child.component();
            final CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>> treeNode;
            if (value.parser() instanceof LiteralParser) {
                treeNode = (CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>) CommandTreeNode.literal();
            } else if (value.parser() instanceof AggregateParser<C, ?> aggregate) {
                this.handleAggregate(node, child, aggregate);
                continue;
            } else {
                treeNode = this.commandManager.parserMapper().mapComponent(value);
            }
            this.addRequirement(child, treeNode);
            if (canExecute(child)) {
                treeNode.executable();
            }
            this.addChildren(treeNode, child);
            node.child(value.name(), treeNode);
        }
    }

    private void handleAggregate(
        final CommandTreeNode<?> node,
        final CommandNode<C> child,
        final AggregateParser<C, ?> compound
    ) {
        final CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>> treeNode;
        final ArrayDeque<Pair<String, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>>> nodes = new ArrayDeque<>();
        for (final CommandComponent<C> component : compound.components()) {
            final String name = component.name();
            nodes.add(Pair.of(name, this.commandManager.parserMapper().mapParser(component.parser())));
        }
        Pair<String, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>> argument = null;
        while (!nodes.isEmpty()) {
            final Pair<String, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>> prev = argument;
            argument = nodes.removeLast();
            if (prev != null) {
                argument.second().child(prev.first(), prev.second());
            } else {
                // last node
                if (canExecute(child)) {
                    argument.second().executable();
                }
            }
            this.addRequirement(child, argument.second());
        }
        treeNode = argument.second();
        this.addChildren(treeNode, child);
        node.child(compound.components().get(0).toString(), treeNode);
    }

    private static <C> boolean canExecute(final @NonNull CommandNode<C> node) {
        return node.isLeaf()
            || !node.component().required()
            || node.command() != null
            || node.children().stream().noneMatch(c -> c.component().required());
    }

    private void addRequirement(
        final @NonNull CommandNode<C> cloud,
        final @NonNull CommandTreeNode<? extends CommandTreeNode<?>> node
    ) {
        final Map<Type, Permission> accessMap =
            cloud.nodeMeta().getOrDefault(CommandNode.META_KEY_ACCESS, Collections.emptyMap());
        node.requires(cause -> this.checkAccess(cause, accessMap));
    }

    private boolean checkAccess(final CommandCause cause, final Map<Type, Permission> accessMap) {
        final C cloudSender = this.commandManager.senderMapper().map(cause);
        for (final Map.Entry<Type, Permission> entry : accessMap.entrySet()) {
            if (GenericTypeReflector.isSuperType(entry.getKey(), cloudSender.getClass())) {
                if (this.commandManager.testPermission(cloudSender, entry.getValue()).allowed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String formatCommandForParsing(final @NonNull String arguments) {
        if (arguments.isEmpty()) {
            return this.label;
        }
        return this.label + " " + arguments;
    }

    private String formatCommandForSuggestions(final @NonNull String arguments) {
        return this.label + " " + arguments;
    }

}
