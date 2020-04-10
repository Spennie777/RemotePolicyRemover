package Main;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class Connection {

    private String baseURL;
    private URL url;
    private HttpsURLConnection connection;
    private String encoding;
    private int responseCode;

    public Connection(String baseURL) throws MalformedURLException {
        this.baseURL = baseURL;
        this.url = new URL(this.baseURL);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean connect(String username, String password) throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        try {
            String credentials = username + ":" + password;
            encoding = Base64.getEncoder().encodeToString((credentials).getBytes(StandardCharsets.UTF_8));
            connection = (HttpsURLConnection) url.openConnection();

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public InputStream getXMLResponse(String username, String password) throws IOException {
        url = new URL(baseURL);
        try {
            if (connect(username, password)) {
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoding);
                connection.setRequestProperty("Accept", "application/xml");
                responseCode = connection.getResponseCode();

                System.out.println("Response Code: " + responseCode);
                if (responseCode == 200) {
                    return connection.getInputStream();
                }
            }
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int deletePolicy(int id, String username, String password) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        url = new URL(baseURL + "/id/" + id);
        responseCode = 0;
        if (connect(username, password)) {
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            //connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestMethod("DELETE");
            responseCode = connection.getResponseCode();
            System.out.println("Deleting Policy: ID: " + id);
            System.out.println("Response Code: " + responseCode);
        }
        return responseCode;
    }
}
