package jp.codic.plugins.intellij;

import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;



public class CodicPluginConfigurable extends BaseConfigurable {

    private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

    private Project project;
    private PreferenceForm gui;


    public CodicPluginConfigurable(Project project) {
        this.project =project;
    }

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
            gui = new PreferenceForm(getSettings());
        }
        return gui.getRoot();
    }

    @Override
    public boolean isModified() {
        return gui.isModified(getSettings());
    }

    @Override
    public void apply() throws ConfigurationException {
        CodicPluginSettings settings = getSettings();
        CodicPluginSettings newSettings = gui.exportDisplayedSettings();
        // Copy properties.
        settings.setQuickLookHeight(newSettings.getQuickLookHeight());
        settings.setQuickLookWidth(newSettings.getQuickLookWidth());
        settings.setAccessToken(newSettings.getAccessToken());
        settings.setProjectId(newSettings.getProjectId());
        settings.setLetterCaseConvention(newSettings.getLetterCaseConvention());

    }

    @Override
    public void reset() {
        gui.importFrom(getSettings());
    }

    @Override
    public void disposeUIResources() {
        gui = null;
    }

    private CodicPluginSettings getSettings() {
        return CodicPluginProjectComponent.getInstance(project).getState();
    }

}
