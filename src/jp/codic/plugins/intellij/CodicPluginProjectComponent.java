package jp.codic.plugins.intellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


@State(name = "CodicPluginSettings", storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/CodicPluginSettings.xml",
                scheme = StorageScheme.DIRECTORY_BASED)
})
public class CodicPluginProjectComponent implements ProjectComponent,
        PersistentStateComponent<CodicPluginSettings> {

    private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());


    private CodicPluginSettings settings;


    public CodicPluginProjectComponent(Project project) {
    }

    @Override
    public void initComponent() {
        // Pass
    }

    @Override
    public void disposeComponent() {
        //gui = null;
    }

    @NotNull
    public String getComponentName() {
        return "CodicPluginProjectComponent";
    }

    @NotNull
    @Override
    public CodicPluginSettings getState() {
        if (settings == null) {
            settings = new CodicPluginSettings();
        }
        return settings;
    }

    @Override
    public void loadState(CodicPluginSettings settings) {
        XmlSerializerUtil.copyBean(settings, this.getState());
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    public static CodicPluginProjectComponent getInstance(Project project) {
        return project.getComponent(CodicPluginProjectComponent.class);
    }
}
