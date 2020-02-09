package net.teamfruit.clouditem;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;

import java.io.IOException;
import java.net.URI;

public class ModCommand extends CommandTreeBase {
    public static class Level
    {
        public static final Level ALL = new Level(0, (server, sender, command) -> true);
        public static final Level OP_OR_SP = new Level(2, (server, sender, command) -> server.isSinglePlayer() || sender.canUseCommand(2, command.getName()));
        public static final Level OP = new Level(2, (server, sender, command) -> sender.canUseCommand(2, command.getName()));
        public static final Level STRONG_OP_OR_SP = new Level(4, (server, sender, command) -> server.isSinglePlayer() || sender.canUseCommand(4, command.getName()));
        public static final Level STRONG_OP = new Level(4, (server, sender, command) -> sender.canUseCommand(4, command.getName()));
        public static final Level SERVER = new Level(4, (server, sender, command) -> sender instanceof MinecraftServer);

        public interface PermissionChecker
        {
            boolean checkPermission(MinecraftServer server, ICommandSender sender, ICommand command);
        }

        public final int requiredPermissionLevel;
        public final PermissionChecker permissionChecker;

        public Level(int l, PermissionChecker p)
        {
            requiredPermissionLevel = l;
            permissionChecker = p;
        }
    }

    public ModCommand() {
        addSubcommand(new ModCommandLoad());
        addSubcommand(new ModCommandSave());
        addSubcommand(new CommandTreeHelp(this));
    }

    public static URI getPlayerURI(EntityPlayerMP playerMP) {
        URI entrypoint = URI.create(ModConfig.entrypoint);
        URI playerEntrypoint = URIUtils.resolve(entrypoint, "v1/players/");
        URI playerData = URIUtils.resolve(playerEntrypoint, playerMP.getUniqueID().toString());
        return playerData;
    }

    @Override
    public String getName() {
        return "cloud";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cloud [load|save]";
    }

    public final Level level = Level.ALL;

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
        EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);

        if (args.length != 0) {
            super.execute(server, sender, args);
            return;
        }

        try {
            URI playerData = ModCommand.getPlayerURI(playerMP);

            boolean dataExists = false;
            {
                final HttpUriRequest req = new HttpHead(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK)
                    dataExists = true;
            }

            if (dataExists) {
                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.Messages.checkExistsMessage), playerMP));
            } else {
                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.Messages.checkNotExistsMessage), playerMP));
            }

        } catch (IOException e) {
            throw new CommandException(ModConfig.Messages.checkFailedMessage);
        }
    }
}