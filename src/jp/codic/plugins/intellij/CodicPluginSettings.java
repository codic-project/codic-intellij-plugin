package jp.codic.plugins.intellij;

import com.intellij.openapi.diagnostic.Logger;

import java.util.HashMap;
import java.util.Map;

public class CodicPluginSettings {
	private final Logger LOG = Logger.getInstance(CodicPluginSettings.class );

    private String accessToken = "";
    private Long projectId;
    private String letterCaseConvention = "";
    private Map<String, String> letterCaseConventionIndex = null;

    public CodicPluginSettings() {

    }

    public String getLetterCaseConvention() {
        return letterCaseConvention;
    }

    public void setLetterCaseConvention(String letterCaseConvention) {
        this.letterCaseConvention = letterCaseConvention;
        updateCaseConventionIndex();
    }

    public void addLetterCaseConvention(String name, String id) {
        updateCaseConventionIndex();
        letterCaseConventionIndex.put(name, id);
        updateCaseConventionRaw();
    }

    public String findLetterCaseConvention(String name) {
        updateCaseConventionIndex();
        return letterCaseConventionIndex.get(name);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    private void updateCaseConventionRaw() {
        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<String, String> entry : letterCaseConventionIndex.entrySet()) {
            buffer.append(entry.getKey() + ":" + entry.getValue()).append(",");
        }
        this.letterCaseConvention = buffer.toString();
    }

    private void updateCaseConventionIndex() {
        if (letterCaseConventionIndex == null) {
            letterCaseConventionIndex = new HashMap<String, String>();
            for (String text : letterCaseConvention.split(",")) {
                if (text.isEmpty() || text.indexOf(":") == -1)
                    continue;
                String fields[] = text.split(":");
                letterCaseConventionIndex.put(fields[0], fields[1]);
            }
        }
    }

}
