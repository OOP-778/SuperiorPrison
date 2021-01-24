package com.bgsoftware.superiorprison.plugin;


public class Updater {

    private static SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();
    private static String latestVersion, versionDescription;

    static {
        setLatestVersion();
    }

    // Just so no one would be able to call the constructor
    private Updater() {
    }

    public static boolean isOutdated() {
        return !getPlugin().getDescription().getVersion().equals(latestVersion);
    }

    public static String getLatestVersion() {
        return latestVersion;
    }

    public static String getVersionDescription() {
        return versionDescription;
    }

    @SuppressWarnings("unchecked")
    private static void setLatestVersion() {
//        try {
//            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://bg-software.com/versions.json").openConnection();
//
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
//            connection.setDoInput(true);
//
//            try (InputStream reader = connection.getInputStream()) {
//                BufferedReader jsonReader = new BufferedReader(new InputStreamReader(reader));
//                JsonObject allVersions = new Gson().fromJson(jsonReader, JsonObject.class);
//                JsonObject superiorprison = allVersions.getAsJsonObject("superiorprison");
//
//                if (superiorprison == null) {
//                    latestVersion = getPlugin().getDescription().getVersion();
//                    return;
//                }
//
//                String version = superiorprison.getAsJsonPrimitive("version").getAsString();
//                String description = superiorprison.getAsJsonPrimitive("description").getAsString();
//
//                latestVersion = version;
//                versionDescription = description;
//            }
//        } catch (Exception ex) {
//            //Something went wrong...
//            if (getPlugin() == null)
//                latestVersion = "Broken...";
//            else
//                latestVersion = getPlugin().getDescription().getVersion();
//        }
        latestVersion = getPlugin().getDescription().getVersion();
    }

    public static SuperiorPrisonPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(SuperiorPrisonPlugin plugin) {
        Updater.plugin = plugin;
    }
}
