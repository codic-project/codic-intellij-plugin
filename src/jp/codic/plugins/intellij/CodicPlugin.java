package jp.codic.plugins.intellij;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ResourceBundle;


public class CodicPlugin {

    private static final String VERSION = "1.0.8";

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
        return VERSION;
    }
}
