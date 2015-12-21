package jp.codic.plugins.intellij.api;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import jp.codic.plugins.intellij.json.JSONArray;
import jp.codic.plugins.intellij.json.JSONException;
import jp.codic.plugins.intellij.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class CodicAPI {

    private static final String HOST = "https://api.codic.jp";
    private static final Logger LOG = Logger.getInstance(CodicAPI.class);

    /**
     * /v1/user_projects.json
     *
     * @throws APIException
     */
    public static UserProject[] getUserProjects(String accessToken) throws APIException {
        String url = HOST + "/v1/user_projects.json";

        Map<String, Object> params = new HashMap<String, Object>();
        try {

            JSONArray json = execHttpGetJsonArray(accessToken, url + "?" + buildQueryString(params));
            UserProject[] entries = new UserProject[json.length()];
            for (int i = 0; i < json.length(); i++) {
                entries[i] = new UserProject();
                entries[i].id = json.getJSONObject(i).getLong("id");
                entries[i].name = json.getJSONObject(i).getString("name");
            }
            return entries;
        } catch (Exception e) {
            throw new APIException(e.getMessage());
        }
    }

    /**
     * @param accessToken The access token.
     * @param projectId The project id.
     * @param query Text to translate.
     * @return Array of Translations
     * @throws APIException
     */
    public static Translation[] translate(String accessToken, Long projectId, String query)
            throws APIException {
        return translate(accessToken, projectId, query, null);
    }

    /**
     * @param accessToken The access token.
     * @param projectId The project id.
     * @param query Text to translate.
     * @return Array of Translations
     * @throws APIException
     */
    public static Translation[] translate(String accessToken, Long projectId, String query, String letterCase)
            throws APIException {
        String url = HOST + "/v1/engine/translate.json";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("project_id", projectId);
        params.put("text", query);
        params.put("casing", letterCase);
        // params.put("count", 20);

        try {
            JSONArray json = execHttpGetJsonArray(accessToken, url + "?" + buildQueryString(params));
            Translation[] entries = new Translation[json.length()];
            for (int i = 0; i < json.length(); i++) {
                entries[i] = new Translation();
                entries[i].translatedText = json.getJSONObject(i).getString("translated_text");
                entries[i].words = mapWord(json.getJSONObject(i).getJSONArray("words"));
                break;
            }
            return entries;
        } catch (Exception e) {
            throw new APIException(e.getMessage());
        }
    }

    public static CedEntry[] lookup(String accessToken, String query)
            throws APIException {
        String url = HOST + "/v1/ced/lookup.json";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("query", query);
        params.put("count", 20);

        try {
            JSONArray json = execHttpGetJsonArray(accessToken, url + "?" + buildQueryString(params));
            CedEntry[] entries = new CedEntry[json.length()];
            for (int i = 0; i < json.length(); i++) {
                entries[i] = new CedEntry();
                entries[i].title = json.getJSONObject(i).getString("title");
                entries[i].digest = json.getJSONObject(i).isNull("digest") ? null :
                        json.getJSONObject(i).optString("digest");
            }
            return entries;
        } catch (Exception e) {
            throw new APIException(e.getMessage());
        }
    }

    private static JSONArray execHttpGetJsonArray(String accessToken, String url)
            throws APIException {
        InputStream is = null;
        URLConnection conn = null;

        initProxy();

        try {
            conn = new URL(url).openConnection();
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("User-Agent", "Codic IntelliJ Plugin/1.0");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(2000);
            is = conn.getInputStream();
            String rawJSON = getResponseString(is);

            return new JSONArray(rawJSON);
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                }
            }
            try {
                //int responseCode = ((HttpURLConnection) conn).getResponseCode();
                is = ((HttpURLConnection) conn).getErrorStream();
                String rawJSON = getResponseString(is);
                JSONObject json = new JSONObject(rawJSON);
                int code = json.getJSONArray("errors").getJSONObject(0).getInt("code");
                String message = json.getJSONArray("errors").getJSONObject(0).getString("message");
                throw new APIException(message, code);
            } catch (IOException e1) {
                throw new APIException(e1.getMessage());
            } catch (JSONException e2) {
                throw new APIException(e2.getMessage());
            }
        } catch (JSONException e2) {
            throw new APIException(e2.getMessage());
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                }
        }
    }

    /**
     * Configure HTTP proxy.
     */
    private static void initProxy()
    {
        HttpConfigurable httpConfigurable = (HttpConfigurable)
                ApplicationManager.getApplication().getComponent("HttpConfigurable");

        if (httpConfigurable == null) {
            httpConfigurable = HttpConfigurable.getInstance();
        }
        if (httpConfigurable != null) {
            if (httpConfigurable.USE_HTTP_PROXY) {
                System.getProperties().put("proxySet", Boolean.valueOf(httpConfigurable.USE_HTTP_PROXY).toString());
                System.getProperties().put("proxyPort", Integer.toString(httpConfigurable.PROXY_PORT));
                System.getProperties().put("proxyHost", httpConfigurable.PROXY_HOST);
                System.getProperties().put("http.proxySet", Boolean.valueOf(httpConfigurable.USE_HTTP_PROXY).toString());
                System.getProperties().put("http.proxyPort", Integer.toString(httpConfigurable.PROXY_PORT));
                System.getProperties().put("http.proxyHost", httpConfigurable.PROXY_HOST);
            }
        }
    }

    /**
     * @param params
     * @return
     */
    private static String buildQueryString(Map<String, Object> params) {
        String query = "";
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                if (!query.isEmpty())
                    query += "&";
                query += encodeUTF8(entry.getKey());
                query += "=";
                query += encodeUTF8(entry.getValue().toString());
            }
        }
        return query;
    }

    /**
     * @param is The input stream.
     * @return
     * @throws IOException
     */
    private static String getResponseString(InputStream is) throws IOException {
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder jsonString = new StringBuilder();

        try {
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            int ret = 0;
            char[] charBuffer = new char[1024];
            while ((ret = br.read(charBuffer)) > 0) {
                jsonString.append(charBuffer, 0, ret);
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            if (is != null)
                is.close();
            if (isr != null)
                isr.close();
            if (br != null)
                br.close();
        }
        return jsonString.toString();
    }

    /**
     * @param value
     * @return
     */
    private static String encodeUTF8(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Suppress exception.
        }
        return null;
    }

    private static Word[] mapWord(JSONArray jsonArray) throws JSONException {
        Word[] words = new Word[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            words[i] = new Word();
            words[i].successful = jsonArray.getJSONObject(i).getBoolean("successful");
            words[i].candidates = mapCandidates(jsonArray.getJSONObject(i).getJSONArray("candidates"));
        }
        return words;
    }

    private static Candidate[] mapCandidates(JSONArray jsonArray) throws JSONException {
        Candidate[] words = new Candidate[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            words[i] = new Candidate();
            words[i].text = jsonArray.getJSONObject(i).getString("text");
        }
        return words;
    }


}
