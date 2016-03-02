package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.connection;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.StartService;

/**
 * Created by Martin on 24.11.2015.
 */
public class ServerConnection {

    static JSONObject jObj = null;

    // constructor
    public ServerConnection() {
    }

    public static DefaultHttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), StartService.API_PORT));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param url POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    public static void post(String url, List<NameValuePair> params) throws IOException {
        DefaultHttpClient httpClient = getNewHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        httpClient.execute(httpPost);
        return;
    }

    /**
     * Issue a GET request to the server.
     *
     * @param url POST address.
     * @param params request parameters.
     * @return the reply of the server
     *
     * @throws IOException propagated from POST.
     */
    public static JSONObject get(String url, List<NameValuePair> params) throws IOException, JSONException {

        DefaultHttpClient httpClient = getNewHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        StringBuilder stringBuilder = new StringBuilder();
        HttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } else {
            throw new IOException();
        }
        return new JSONObject(stringBuilder.toString());
    }
}
