package net.teamfruit.clouditem.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NullCommandSender implements ICommandSender {
    public static final ICommandSender INSTANCE = new NullCommandSender();

    private NullCommandSender() {}

    @Override
    public String getName() {
        return "NullCommandSender";
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    @Override
    public World getEntityWorld() {
        return null;
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return null;
    }
}
