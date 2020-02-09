package net.teamfruit.clouditem;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

public class ModCommandLoad extends CommandBase {
    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cloud load";
    }

    public final ModCommand.Level level = ModCommand.Level.ALL;

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

        try {
            URI playerData = ModCommand.getPlayerURI(playerMP);

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

            if (!dataExists) {
                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.messages.checkNotExistsMessage), playerMP));
                return;
            }

            if (!playerMP.inventory.isEmpty()) {
                if (!(args.length >= 1 && StringUtils.equals(args[0], "force"))) {
                    playerMP.sendMessage(TextComponentUtils.processComponent(server,
                            ITextComponent.Serializer.jsonToComponent(ModConfig.messages.downloadOverwriteMessage), playerMP));
                    return;
                }
            }

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.downloadBeginMessage), playerMP));

            NBTTagCompound tags;
            try {
                final HttpUriRequest req = new HttpGet(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);
                entity = response.getEntity();

                final int statusCode = response.getStatusLine().getStatusCode();
                if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT))
                    throw new HttpResponseException(statusCode, "Failed to get");

                tags = CompressedStreamTools.readCompressed(entity.getContent());
            } finally {
                EntityUtils.consume(entity);
            }

            try {
                final HttpUriRequest req = new HttpDelete(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);
                entity = response.getEntity();

                final int statusCode = response.getStatusLine().getStatusCode();
                if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT))
                    throw new HttpResponseException(statusCode, "Failed to delete");
            } finally {
                EntityUtils.consume(entity);
            }

            playerMP.inventory.readFromNBT(tags.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
            playerMP.inventory.markDirty();

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.downloadEndMessage), playerMP));

        } catch (IOException e) {
            throw new CommandException(ModConfig.messages.downloadFailedMessage, e);
        }
    }
}
