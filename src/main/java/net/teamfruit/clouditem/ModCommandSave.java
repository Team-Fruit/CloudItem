package net.teamfruit.clouditem;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.IOException;
import java.net.URI;

public class ModCommandSave extends CommandBase {
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/vote save";
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

            if (playerMP.inventory.isEmpty()) {
                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.messages.checkLocalNotExistsMessage), playerMP));
                return;
            }

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
                if (!(args.length >= 1 && StringUtils.equals(args[0], "force"))) {
                    playerMP.sendMessage(TextComponentUtils.processComponent(server,
                            ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadOverwriteMessage), playerMP));
                    return;
                }
            }

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadBeginMessage), playerMP));

            NBTTagCompound tags = new NBTTagCompound();
            NBTTagList tagList = new NBTTagList();
            playerMP.inventory.writeToNBT(tagList);
            tags.setTag("inventory", tagList);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tags, output);

            try {
                final HttpPut req = new HttpPut(playerData);
                MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
                multipart.addBinaryBody("player.dat", output.toByteArray(), ContentType.APPLICATION_OCTET_STREAM, "nbt");
                req.setEntity(multipart.build());
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);
                entity = response.getEntity();

                final int statusCode = response.getStatusLine().getStatusCode();
                if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT))
                    throw new HttpResponseException(statusCode, "Failed to upload");
            } finally {
                EntityUtils.consume(entity);
            }

            playerMP.inventory.clear();
            playerMP.inventory.markDirty();

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadEndMessage), playerMP));

        } catch (IOException e) {
            throw new CommandException(ModConfig.messages.uploadFailedMessage, e);
        }
    }
}
