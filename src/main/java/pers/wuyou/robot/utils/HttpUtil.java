package pers.wuyou.robot.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import pers.wuyou.robot.common.GlobalVariable;
import pers.wuyou.robot.entity.RequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网络请求Util, 有的话可以用别的代替
 *
 * @author wuyou
 */
@SuppressWarnings("unused")
public class HttpUtil {
    private static final CookieStore STORE;
    private static final CloseableHttpClient CLOSEABLE_HTTP_CLIENT;

    static {
        // 缓存Cookie
        STORE = new BasicCookieStore();
        CLOSEABLE_HTTP_CLIENT = HttpClients.custom().setDefaultCookieStore(STORE).build();
        /*.setProxy(new HttpHost("172.24.58.136",5553))*/
    }

    /**
     * get请求
     *
     * @param url 请求的URL
     */
    public static RequestEntity get(String url) {
        Map<String, String> cookies = new HashMap<>(0);
        Map<String, String> params = new HashMap<>(0);
        return get(url, params, cookies);
    }

    /**
     * get请求
     *
     * @param url     请求的URL
     * @param params  请求的参数
     * @param cookies 请求携带的cookie
     */
    public static RequestEntity get(String url, Map<String, String> params, Map<String, String> cookies) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null) {
                params.forEach(uriBuilder::addParameter);
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            setCookies(httpGet, cookies);
            return request(httpGet);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new RequestEntity();
    }

    /**
     * post请求
     *
     * @param url 请求的URL
     */
    public static RequestEntity post(String url) {
        Map<String, String> cookies = new HashMap<>(0);
        Map<String, String> params = new HashMap<>(0);
        return post(url, params, cookies);
    }

    /**
     * post请求
     *
     * @param url     请求的URL
     * @param params  请求的参数
     * @param cookies 请求携带的cookie
     */
    public static RequestEntity post(String url, Map<String, String> params, Map<String, String> cookies) {
        try {
            HttpPost httpPost = new HttpPost(url);
            if (params != null) {
                List<NameValuePair> paramsList = new ArrayList<>();
                params.forEach((key, value) -> paramsList.add(new BasicNameValuePair(key, value)));
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsList, "utf-8");
                formEntity.setContentType("Content-Type:application/json");
                httpPost.setEntity(formEntity);
            }
            setCookies(httpPost, cookies);
            return request(httpPost);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new RequestEntity();
    }

    /**
     * 设置cookie
     */
    protected static void setCookies(HttpRequestBase httpRequestBase, Map<String, String> cookies) {
        if (cookies != null) {
            StringBuilder cookie = new StringBuilder();
            cookies.forEach((key, value) -> cookie.append(key).append("=").append(value).append(";"));
            httpRequestBase.setHeader("Cookie", cookie.toString());
            httpRequestBase.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");

        }
    }

    /**
     * 网络请求具体实现
     */
    private static RequestEntity request(HttpRequestBase httpRequestBase) {
        try {
            return GlobalVariable.THREAD_POOL.submit(() -> {
                RequestEntity requestEntity = new RequestEntity();
                httpRequestBase.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0");
                try (CloseableHttpResponse closeableHttpResponse = CLOSEABLE_HTTP_CLIENT.execute(httpRequestBase)) {
                    requestEntity.setResponse(EntityUtils.toString(closeableHttpResponse.getEntity(), "UTF-8"));
                    requestEntity.setCookies(STORE.getCookies());
                    httpRequestBase.setHeader("Cookie", "");
                    STORE.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return requestEntity;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RequestEntity();
    }

    /**
     * 解析返回结果的一个方法
     */
    public static JSONObject getJson(String body, String separator) {
        body = body.replace(" ", "");
        String jsonStr = body.substring(body.indexOf(separator) + separator.length() + 1);
        jsonStr = jsonStr.substring(0, jsonStr.indexOf("</script>"));
        return JSONObject.parseObject(jsonStr);
    }
}
