package com.cdm.nearby.modal;

import android.content.Context;
import com.cdm.nearby.common.L;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/15/13
 * Time: 12:21 AM
 */
public class PoiManager extends BaseManger {

    public PoiManager(Context context) {
        super(context);
    }



    public List<Poi> search(String keyword, double longitude, double latitude, int range, int page, int count, String category) throws Exception{
        L.d("Search poi around longitude " + longitude + ", latitude " + latitude);


        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpParams httpParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", sf, 443));

        ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams, registry);

        DefaultHttpClient client = new  DefaultHttpClient(ccm, httpParams);

        List<Poi> pois = new ArrayList<Poi>();

        String url = "https://api.weibo.com/2/location/pois/search/by_geo.json";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("source", "174030432"));
        params.add(new BasicNameValuePair("access_token", "2.00dOFS2ChvFO3Db273eba8caNinXIE"));
        params.add(new BasicNameValuePair("q", keyword));
        params.add(new BasicNameValuePair("coordinate", longitude + "," + latitude));
        params.add(new BasicNameValuePair("range", String.valueOf(range)));
        params.add(new BasicNameValuePair("page",  String.valueOf(page)));
        params.add(new BasicNameValuePair("count", String.valueOf(count)));
        params.add(new BasicNameValuePair("searchtype", category));
        params.add(new BasicNameValuePair("sr", "1"));

        String paramStr = URLEncodedUtils.format(params, "UTF-8");

        String requestUrl;

        if (paramStr == null || "".equals(paramStr.trim())) {
            requestUrl = url;
        } else {
            requestUrl = url + "?" + paramStr;
        }

        L.d("Search poi request url " + requestUrl);

        HttpGet request = new HttpGet(requestUrl);



        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        String jsonStr = EntityUtils.toString(entity);

        L.d("Search poi json result " + jsonStr);

        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONArray poiListJsonArray = jsonObject.optJSONArray("poilist");

        if(poiListJsonArray == null){
           return pois;
        }

        for(int i = 0; i < poiListJsonArray.length(); i++){
            JSONObject poiJsonObject = poiListJsonArray.getJSONObject(i);

            Poi poi = new Poi();
            poi.setName(poiJsonObject.optString("name"));
            poi.setAddress(poiJsonObject.optString("address"));
            poi.setLongitude(poiJsonObject.optDouble("x"));
            poi.setLatitude(poiJsonObject.optDouble("y"));
            poi.setDistance(poiJsonObject.optInt("distance"));
            poi.setPhone(poiJsonObject.optString("tel"));

            pois.add(poi);
        }

        return pois;
    }



    public List<Poi> search(double longitude, double latitude, int range, int page, int count, String category) throws Exception{
        return search(null, longitude, latitude, range, page, count, category);
    }

    public List<Poi> search(String keyword, double longitude, double latitude, int range, int page, int count) throws Exception{
        return search(keyword, longitude, latitude, range, page, count, null);
    }


    public class SSLSocketFactoryEx extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {}

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {}
            };
            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port,autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

}
