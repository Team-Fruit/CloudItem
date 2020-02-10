package net.teamfruit.clouditem.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class ModCommandAdmin extends CommandTreeBase {
    public ModCommandAdmin() {
        addSubcommand(new ModCommandAdminExec());
        addSubcommand(new ModCommandAdminReload());
        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cloud admin [exec|reload]";
    }

    public final ModCommand.Level level = ModCommand.Level.OP.or(ModCommand.Level.SERVER);

    @Override
    public int getRequiredPermissionLevel() {
        return level.requiredPermissionLevel;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return level.permissionChecker.checkPermission(server, sender, this);
    }
}
