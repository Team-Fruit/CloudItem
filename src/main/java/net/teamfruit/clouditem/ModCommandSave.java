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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;

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

        if (!playerMP.inventory.isEmpty()) {
            if (!(args.length >= 1 && StringUtils.equals(args[0], "force"))) {
                playerMP.sendMessage(TextComponentUtils.processComponent(server,
                        ITextComponent.Serializer.jsonToComponent(ModConfig.messages.downloadOverwriteMessage), playerMP));
                return;
            }
        }

        try {
            URI playerData = ModCommand.getPlayerURI(playerMP);

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadBeginMessage), playerMP));

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
                if (!(args.length >= 1 && StringUtils.equals(args[0], "force"))) {
                    playerMP.sendMessage(TextComponentUtils.processComponent(server,
                            ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadOverwriteMessage), playerMP));
                    return;
                }
            }

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadBeginMessage), playerMP));

            if (playerMP.inventory.isEmpty()) {
                final HttpUriRequest req = new HttpDelete(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK)
                    throw new HttpResponseException(statusCode, "Failed to delete");
            } else {
                NBTTagCompound tags = new NBTTagCompound();
                playerMP.inventory.writeToNBT(tags.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                CompressedStreamTools.writeCompressed(tags, output);

                {
                    final HttpPost req = new HttpPost(playerData);
                    req.setEntity(new ByteArrayEntity(output.toByteArray()));
                    final HttpClientContext context = HttpClientContext.create();
                    final HttpResponse response = Downloader.downloader.client.execute(req, context);

                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK)
                        throw new HttpResponseException(statusCode, "Failed to upload");
                }

                playerMP.inventory.clear();
                playerMP.inventory.markDirty();
            }

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.uploadEndMessage), playerMP));

        } catch (IOException e) {
            throw new CommandException(ModConfig.messages.uploadFailedMessage);
        }
    }
}
