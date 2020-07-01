package jp.codic.plugins.intellij;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;


@State(name = "CodicPluginSettings", storages = {
        @Storage("CodicPluginSettings.xml")
})
public class CodicPluginProjectComponent implements
        PersistentStateComponent<CodicPluginSettings> {

    private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());


    private CodicPluginSettings settings;


    public CodicPluginProjectComponent(Project project) {
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

    public static CodicPluginProjectComponent getInstance(Project project) {
        return project.getComponent(CodicPluginProjectComponent.class);
    }
}
