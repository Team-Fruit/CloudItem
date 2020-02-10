package net.teamfruit.clouditem.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.server.command.CommandTreeBase;
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

public class ModCommandAdmin extends CommandTreeBase {
    public ModCommandAdmin() {
        addSubcommand(new ModCommandAdminReload());
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cloud admin [reload]";
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
