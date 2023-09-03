package rd.trafikrapport;

import android.os.AsyncTask;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

class XmlRetriever extends AsyncTask<String, Void, String> {

    private NetworkResponse callback;
    private Exception exception;

    protected XmlRetriever(NetworkResponse callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urlString) {

        try {
            URL url = new URL(urlString[0]);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                conn.setRequestMethod("GET");
                conn.connect();
                InputStream iStream = conn.getInputStream();
                Scanner s = new Scanner(iStream).useDelimiter("\\A");
                String xml = s.hasNext() ? s.next() : "";
                return xml;
            } else {
                callback.error(new Exception("Failed to connect to server: " + responseCode));
                return null;
            }

        } catch (Exception e) {
            callback.error(e);
            return null;
        } finally {

        }

    }

    @Override
    protected void onPostExecute(String xml) {
        callback.success(xml);
    }

}

