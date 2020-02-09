package net.teamfruit.clouditem;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MODID, name = Reference.MODID)
public class ModConfig {

    @Config.RequiresWorldRestart
    @Config.Name("Cloud API Entry Point")
    @Config.Comment({ "The Entry Point of Cloud API" })
    public static String entrypoint = "";

    @Config.Name("Token")
    @Config.Comment({ "Not Implemented!" })
    public static String token = "";

    public static class Messages {

        @Config.Name("Data Available Message")
        @Config.Comment({ "the text that is shown when a player data available on Cloud", "must be formatted in /tellraw nbt format" })
        public static String checkExistsMessage = "{\"text\":\"Your player data is available on Cloud. Type '/cloud load' to continue\"}";

        @Config.Name("Data Unavailable Message")
        @Config.Comment({ "the text that is shown when a player data unavailable on Cloud", "must be formatted in /tellraw nbt format" })
        public static String checkNotExistsMessage = "{\"text\":\"No data is available\"}";

        @Config.Name("Data Check Failed Message")
        @Config.Comment({ "the text that is shown when the data check fails" })
        public static String checkFailedMessage = "Failed to Check";

        @Config.Name("Data Downloading Message")
        @Config.Comment({ "the text that is shown when the data is downloading", "must be formatted in /tellraw nbt format" })
        public static String downloadBeginMessage = "{\"text\":\"Downloading from Cloud...\"}";

        @Config.Name("Data Download Finish Message")
        @Config.Comment({ "the text that is shown when the data is downloaded", "must be formatted in /tellraw nbt format" })
        public static String downloadEndMessage = "{\"text\":\"Finished Data Downloading!\"}";

        @Config.Name("Data Download Failed Message")
        @Config.Comment({ "the text that is shown when the data download fails" })
        public static String downloadFailedMessage = "Failed to Download";

        @Config.Name("Server Data Overwriting Message")
        @Config.Comment({ "the text that is shown when the server data will be overwritten", "must be formatted in /tellraw nbt format" })
        public static String downloadOverwriteMessage = "{\"text\":\"Your Server Data will be overwritten by the Cloud Data. Type '/cloud load force' to continue\"}";

        @Config.Name("Data Uploading Message")
        @Config.Comment({ "the text that is shown when the data is uploading", "must be formatted in /tellraw nbt format" })
        public static String uploadBeginMessage = "{\"text\":\"Uploading to Cloud...\"}";

        @Config.Name("Data Upload Finish Message")
        @Config.Comment({ "the text that is shown when the data is uploaded", "must be formatted in /tellraw nbt format" })
        public static String uploadEndMessage = "{\"text\":\"Finished Data Downloading!\"}";

        @Config.Name("Data Upload Failed Message")
        @Config.Comment({ "the text that is shown when the data upload fails" })
        public static String uploadFailedMessage = "Failed to Upload";

        @Config.Name("Cloud Data Overwriting Message")
        @Config.Comment({ "the text that is shown when the cloud data will be overwritten", "must be formatted in /tellraw nbt format" })
        public static String uploadOverwriteMessage = "{\"text\":\"Your Cloud Data will be overwritten by the Server Data. Type '/cloud save force' to continue\"}";
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class Handler {

        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Reference.MODID))
                ConfigManager.load(Reference.MODID, Config.Type.INSTANCE);
        }
    }
}
