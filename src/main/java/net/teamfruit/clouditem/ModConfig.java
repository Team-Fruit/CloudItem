package net.teamfruit.clouditem;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MODID, name = Reference.MODID)
public class ModConfig {

    @Config.Name("API")
    @Config.Comment({ "Settings of Cloud API" })
    public static Api api = new Api();

    @Config.Name("Messages")
    @Config.Comment({ "Settings of Messages" })
    public static Messages messages = new Messages();

    public static class Api {

        @Config.Name("Cloud API Entry Point")
        @Config.Comment({ "The Entry Point of Cloud API" })
        public String entrypoint = "";

        @Config.Name("Token")
        @Config.Comment({ "Not Implemented!" })
        public String token = "";
    }

    public static class Messages {

        @Config.Name("Login Load Message")
        @Config.Comment({ "the text that is shown when a player logged in and items are loaded", "must be formatted in /tellraw nbt format" })
        public String loginLoadedMessage = "{\"text\":\"Cloud Inventory is successfully loaded.\"}";

        @Config.Name("Data Available Message")
        @Config.Comment({ "the text that is shown when a player data available on Cloud", "must be formatted in /tellraw nbt format" })
        public String checkExistsMessage = "{\"text\":\"Your player data is available on Cloud. [@@DATE@@] Type '/cloud load' to use it!\"}";

        @Config.Name("Data Unavailable Message")
        @Config.Comment({ "the text that is shown when a player data unavailable on Cloud", "must be formatted in /tellraw nbt format" })
        public String checkNotExistsMessage = "{\"text\":\"Nothing to download\"}";

        @Config.Name("Local Data Unavailable Message")
        @Config.Comment({ "the text that is shown when a player data unavailable on Local", "must be formatted in /tellraw nbt format" })
        public String checkLocalNotExistsMessage = "{\"text\":\"Nothing to upload\"}";

        @Config.Name("Data Check Failed Message")
        @Config.Comment({ "the text that is shown when the data check fails", "must be formatted in /tellraw nbt format" })
        public String checkFailedMessage = "{\"text\":\"Failed to Check\"}";

        @Config.Name("Data Date Message Format")
        @Config.Comment({ "the text that is shown when the data check fails" })
        public String checkDataDateMessageFormat = "yyyy/MM/dd (E) H:mm";

        @Config.Name("Data Downloading Message")
        @Config.Comment({ "the text that is shown when the data is downloading", "must be formatted in /tellraw nbt format" })
        public String downloadBeginMessage = "{\"text\":\"Downloading from Cloud...\"}";

        @Config.Name("Data Download Finish Message")
        @Config.Comment({ "the text that is shown when the data is downloaded", "must be formatted in /tellraw nbt format" })
        public String downloadEndMessage = "{\"text\":\"Finished Data Downloading!\"}";

        @Config.Name("Data Download Failed Message")
        @Config.Comment({ "the text that is shown when the data download fails", "must be formatted in /tellraw nbt format" })
        public String downloadFailedMessage = "{\"text\":\"Failed to Download\"}";

        @Config.Name("Your Data Overwriting Message")
        @Config.Comment({ "the text that is shown when the local data will be overwritten", "must be formatted in /tellraw nbt format" })
        public String downloadOverwriteMessage = "{\"text\":\"Your Local Data will be overwritten by the Cloud Data. Type '/cloud load force' to continue\"}";

        @Config.Name("Data Uploading Message")
        @Config.Comment({ "the text that is shown when the data is uploading", "must be formatted in /tellraw nbt format" })
        public String uploadBeginMessage = "{\"text\":\"Uploading to Cloud...\"}";

        @Config.Name("Data Upload Finish Message")
        @Config.Comment({ "the text that is shown when the data is uploaded", "must be formatted in /tellraw nbt format" })
        public String uploadEndMessage = "{\"text\":\"Finished Data Uploading!\"}";

        @Config.Name("Data Upload Failed Message")
        @Config.Comment({ "the text that is shown when the data upload fails", "must be formatted in /tellraw nbt format" })
        public String uploadFailedMessage = "{\"text\":\"Failed to Upload\"}";

        @Config.Name("Cloud Data Overwriting Message")
        @Config.Comment({ "the text that is shown when the cloud data will be overwritten", "must be formatted in /tellraw nbt format" })
        public String uploadOverwriteMessage = "{\"text\":\"Your Cloud Data will be overwritten by the Local Data. Type '/cloud save force' to continue\"}";
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
