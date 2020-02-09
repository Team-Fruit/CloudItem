package net.teamfruit.clouditem;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;

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
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.downloadBeginMessage), playerMP));

            NBTTagCompound tags;
            {
                final HttpUriRequest req = new HttpGet(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK)
                    throw new HttpResponseException(statusCode, "Failed to get");

                final HttpEntity entity = response.getEntity();
                tags = CompressedStreamTools.readCompressed(entity.getContent());
            }

            {
                final HttpUriRequest req = new HttpDelete(playerData);
                final HttpClientContext context = HttpClientContext.create();
                final HttpResponse response = Downloader.downloader.client.execute(req, context);

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK)
                    throw new HttpResponseException(statusCode, "Failed to delete");
            }

            playerMP.inventory.readFromNBT(tags.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
            playerMP.inventory.markDirty();

            playerMP.sendMessage(TextComponentUtils.processComponent(server,
                    ITextComponent.Serializer.jsonToComponent(ModConfig.messages.downloadEndMessage), playerMP));

        } catch (IOException e) {
            throw new CommandException(ModConfig.messages.downloadFailedMessage);
        }
    }
}
