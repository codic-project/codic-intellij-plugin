package jp.codic.plugins.intellij;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;


public class CodicPluginApplicationComponent implements ApplicationComponent {

	public static CodicPluginApplicationComponent getInstance() {
		return ServiceManager.getService(CodicPluginApplicationComponent.class);
	}

	public CodicPluginApplicationComponent() {
	}

	public void initComponent() {
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "CodicPluginApplicationComponent";
	}



}
