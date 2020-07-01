package jp.codic.plugins.intellij;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;

import java.text.MessageFormat;
import java.util.ResourceBundle;


public class CodicPlugin {

    private static final String PLUGIN_ID = "jp.codic.plugins.intellij";

    private static ResourceBundle resource;

    public static String getString(String key) {
        if (resource == null) {
            resource = ResourceBundle.getBundle("jp/codic/plugins/intellij/i18n");
        }
        return resource.getString(key);
    }

    public static String getString(String key, Object... args) {
        String message = getString(key);
        MessageFormat format = new MessageFormat(message);
        return format.format(args);
    }

    public static String getVersion() {
        return PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getVersion();
    }
}
