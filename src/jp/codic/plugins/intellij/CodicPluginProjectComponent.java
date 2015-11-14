package jp.codic.plugins.intellij;

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
        PersistentStateComponent<CodicPluginSettings>, Configurable {

    private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());


    private CodicPluginSettings settings;
    private PreferenceForm gui;


    public CodicPluginProjectComponent(Project project) {
        //Application
    }

    public void initComponent() {
        // Pass
    }

    public void disposeComponent() {
        gui = null;
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
        this.settings = settings;
        //this.settings.updateCaseConventionMap();
        //CodicPluginSettings.applyMaxRecentProjectsToRegistry();
    }


    // Configurable---------------------------------
    @Nls
    @Override
    public String getDisplayName() {
        return "Codic Plugin";
    }

    @Nullable
    public Icon getIcon() {
        return null;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (gui == null) {
            gui = new PreferenceForm(getState());
        }
        return gui.getRoot();
    }

    @Override
    public boolean isModified() {
        return gui.isModified(getState());
    }

    @Override
    public void apply() throws ConfigurationException {
        settings = gui.exportDisplayedSettings();
    }

    @Override
    public void reset() {
        gui.importFrom(settings);
    }

    @Override
    public void disposeUIResources() {
        gui = null;
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

}
