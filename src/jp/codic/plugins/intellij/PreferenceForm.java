package jp.codic.plugins.intellij;

import com.intellij.ui.ListCellRendererWrapper;
import jp.codic.plugins.intellij.api.APIException;
import jp.codic.plugins.intellij.api.CodicAPI;
import jp.codic.plugins.intellij.api.UserProject;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PreferenceForm {

	private JPanel root;
    private DefaultComboBoxModel projectFieldModel;
    private JComboBox projectField;
    private JTextField accessTokenField;
    private JLabel accessTokenErrorLabel;
    private CodicPluginSettings settings;

	public PreferenceForm(CodicPluginSettings settings) {
		this.settings = settings;
		initModel(settings);
	}

	private void initModel(CodicPluginSettings settings) {

        projectFieldModel = new DefaultComboBoxModel();
        projectField.setModel(projectFieldModel);
        projectField.setRenderer(new ListCellRendererWrapper() {
            @Override
            public void customize(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof UserProject)
                    setText(((UserProject) value).name);
            }
        });

        accessTokenField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                // Pass
            }
            @Override
            public void focusLost(FocusEvent focusEvent) {
                updateUserProjects();
            }
        });
	}

    private void updateUserProjects() {
        updateUserProjects(null);
    }

    private void updateUserProjects(Long selected) {

        // Clear message.
        accessTokenErrorLabel.setText(null);
        projectField.setEnabled(false);
        projectFieldModel.removeAllElements();

        try {

            // Update project options.
            String accessToken = accessTokenField.getText();

            UserProject[] userProjects = new UserProject[0];
            if (accessToken != null && !accessToken.equals("")) {
                userProjects = CodicAPI.getUserProjects(accessToken);
            }

            projectFieldModel.removeAllElements();

            if (userProjects.length > 0) {
                UserProject selectedItem = userProjects[0];
                for (UserProject userProject : userProjects) {
                    projectFieldModel.addElement(userProject);
                    if (userProject.id.equals(selected)) {
                        selectedItem = userProject;
                    }
                }
                projectFieldModel.setSelectedItem(selectedItem);
                projectField.setEnabled(true);
            }

        } catch (APIException e) {
            accessTokenErrorLabel.setText(e.getMessage());
        }
    }

	public JPanel getRoot() {
		return root;
	}

	public void importFrom(CodicPluginSettings data) {
		initModel(data);
        accessTokenField.setText(settings.getAccessToken());
        updateUserProjects(data.getProjectId());
	}

	public CodicPluginSettings exportDisplayedSettings() {
        settings.setAccessToken(accessTokenField.getText());
        UserProject userProject = (UserProject)projectFieldModel.getSelectedItem();
        settings.setProjectId(userProject != null ? userProject.id : null);

		return settings;
	}

	public boolean isModified(CodicPluginSettings data) {
		if (isModifiedCustom(data)) {
			return true;
		}
		return false;
	}

	private boolean isModifiedCustom(CodicPluginSettings data) {
		if (projectFieldModel.getSelectedItem() != data.getProjectId()) {
			return true;
		}
        if (!this.accessTokenField.getText().equals(data.getAccessToken())) {
            return true;
        }

        return true;
	}


    private void createUIComponents() {

    }
}
