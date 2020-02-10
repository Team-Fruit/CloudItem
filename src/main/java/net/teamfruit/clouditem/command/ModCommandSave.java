package net.teamfruit.clouditem.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.teamfruit.clouditem.Log;
import net.teamfruit.clouditem.ModConfig;
import net.teamfruit.clouditem.util.Downloader;
import net.teamfruit.clouditem.util.ServerThreadExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class ModCommandSave extends CommandBase {
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cloud save";
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
        boolean force = (args.length >= 1 && StringUtils.equals(args[0], "force"));
        execute(sender, playerMP, force);
    }

    public static void execute(ICommandSender sender, EntityPlayer playerMP, boolean force) {
        URI playerData;
        try {
            playerData = ModCommand.getPlayerURI(playerMP);

            if (playerMP.inventory.isEmpty()) {
                ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(ModConfig.messages.checkLocalNotExistsMessage));
                return;
            }
        } catch (Exception e) {
            Log.log.warn("Failed to upload", e);
            ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
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
                    EntityUtils.consumeQuietly(entity);
                }

                return dataExists;
            } catch (Exception e) {
                Log.log.warn("Failed to upload", e);
                ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.uploadFailedMessage));
                throw new CancellationException();
            }

        }).thenApplyAsync(dataExists -> {
            try {
                if (dataExists) {
                    if (!force) {
                        ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
                                ModConfig.messages.uploadOverwriteMessage));
                        throw new CancellationException();
                    }
                }

                NBTTagCompound tags = new NBTTagCompound();
                NBTTagList tagList = new NBTTagList();
                playerMP.inventory.writeToNBT(tagList);
                tags.setTag("inventory", tagList);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                CompressedStreamTools.writeCompressed(tags, output);

                playerMP.inventory.clear();
                playerMP.inventory.markDirty();

                return Pair.of(output.toByteArray(), tags);
            } catch (CancellationException e) {
                throw e;
            } catch (Exception e) {
                Log.log.warn("Failed to upload", e);
                ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.uploadFailedMessage));
                throw new CancellationException();
            }

        }, ServerThreadExecutor.INSTANCE).thenApplyAsync(out -> {
            try {
                HttpEntity entity = null;

                ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.uploadBeginMessage));

                try {
                    final HttpPut req = new HttpPut(playerData);
                    MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
                    multipart.addBinaryBody("nbt", out.getLeft(), ContentType.APPLICATION_OCTET_STREAM, "nbt.dat");
                    req.setEntity(multipart.build());
                    final HttpClientContext context = HttpClientContext.create();
                    final HttpResponse response = Downloader.downloader.client.execute(req, context);
                    entity = response.getEntity();

                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT))
                        throw new HttpResponseException(statusCode, "Failed to upload");
                } finally {
                    EntityUtils.consumeQuietly(entity);
                }

                ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.uploadEndMessage));

                return Optional.<NBTTagCompound>empty();

            } catch (Exception e) {
                Log.log.warn("Failed to upload", e);
                ModCommand.sendMessage(sender, ITextComponent.Serializer.jsonToComponent(
                        ModConfig.messages.uploadFailedMessage));
                return Optional.ofNullable(out.getRight());
            }

        }).thenAcceptAsync(revert -> {
            revert.ifPresent(tags -> {
                playerMP.inventory.readFromNBT(tags.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
                playerMP.inventory.markDirty();
            });

        }, ServerThreadExecutor.INSTANCE);
    }
}
