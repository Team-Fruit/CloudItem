package net.teamfruit.clouditem;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
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

import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

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

        URI playerData;
        try {
            playerData = ModCommand.getPlayerURI(playerMP);
        } catch (Exception e) {
            Log.log.warn("Failed to upload", e);
            ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                    ModConfig.messages.uploadFailedMessage));
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
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
                    ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                            ModConfig.messages.checkNotExistsMessage));
                    throw new CancellationException();
                }

                if (!playerMP.inventory.isEmpty()) {
                    if (!(args.length >= 1 && StringUtils.equals(args[0], "force"))) {
                        ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                                ModConfig.messages.downloadOverwriteMessage));
                        throw new CancellationException();
                    }
                }

                ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.downloadBeginMessage));

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

                ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.downloadEndMessage));

                return tags;

            } catch (Exception e) {
                Log.log.warn("Failed to download", e);
                ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.downloadFailedMessage));
                throw new CancellationException();
            }

        }).thenAcceptAsync(tags -> {
            try {
                playerMP.inventory.readFromNBT(tags.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
                playerMP.inventory.markDirty();
            } catch (Exception e) {
                Log.log.warn("Failed to download", e);
                ModCommand.sendMessage(playerMP, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.downloadFailedMessage));
                throw new CancellationException();
            }

        }, ServerThreadExecutor.INSTANCE);
    }
}
