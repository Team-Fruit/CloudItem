package net.teamfruit.clouditem;

import com.google.common.base.Charsets;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        URI entrypoint = URI.create(ModConfig.api.entrypoint);
        URI playerEntrypoint = URIUtils.resolve(entrypoint, "v1/players/");
        return URIUtils.resolve(playerEntrypoint, playerMP.getUniqueID().toString() + "/");
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
            URI playerDataDate = URIUtils.resolve(playerData, "date/");

            HttpEntity entity = null;

            boolean dataExists;
            try {
                final HttpUriRequest req = new HttpHead(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);
                entity = response.getEntity();

                final int statusCode = response.getStatusLine().getStatusCode();
                dataExists = (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT);
            } finally {
                EntityUtils.consume(entity);
            }

            if (dataExists) {
                String date = "<Unknown>";
                try {
                    final HttpUriRequest req = new HttpGet(playerDataDate);
                    final HttpClientContext context = HttpClientContext.create();
                    final HttpResponse response = Downloader.downloader.client.execute(req, context);
                    entity = response.getEntity();

                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        String dateText = IOUtils.toString(entity.getContent(), Charsets.UTF_8);
                        date = new SimpleDateFormat(ModConfig.messages.checkDataDateMessageFormat).format(new Date(NumberUtils.toLong(dateText)));
                    }
                } finally {
                    EntityUtils.consume(entity);
                }

                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.messages.checkExistsMessage.replace("@@DATE@@", date)), playerMP));
            } else {
                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.messages.checkNotExistsMessage), playerMP));
            }

        } catch (IOException e) {
            throw new CommandException(ModConfig.messages.checkFailedMessage, e);
        }
    }
}