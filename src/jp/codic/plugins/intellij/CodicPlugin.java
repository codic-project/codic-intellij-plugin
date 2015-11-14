package jp.codic.plugins.intellij;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;


public class CodicPlugin {

    private static ResourceBundle resource;

    public static String getString(String key) {
        if (resource == null) {
            resource = ResourceBundle.getBundle("jp/codic/plugins/intellij/i18n");
        }
        return resource.getString(key);
    }

}
