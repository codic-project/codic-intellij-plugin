package jp.codic.plugins.intellij;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;


public class CodicPluginApplicationComponent implements ApplicationComponent {

	public static CodicPluginApplicationComponent getInstance() {
		return ServiceManager.getService(CodicPluginApplicationComponent.class);
	}

	public CodicPluginApplicationComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "CodicPluginApplicationComponent";
	}

}
