package net.teamfruit.clouditem.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

public class ModCommandAdminExec extends CommandBase {
    @Override
    public String getName() {
        return "exec";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cloud admin exec <player> [load|save] [force]";
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

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        String playerName = args[0];
        EntityPlayerMP playerMP = getPlayer(server, sender, playerName);

        if (args.length == 1)
            ModCommand.execute(sender, playerMP, false);
        else {
            boolean force = (args.length >= 3 && StringUtils.equals(args[2], "force"));
            if (StringUtils.equals(args[1], "load"))
                ModCommandLoad.execute(sender, playerMP, force);
            else if (StringUtils.equals(args[1], "save"))
                ModCommandSave.execute(sender, playerMP, force);
            else
                throw new WrongUsageException(getUsage(sender));
        }
    }
}
