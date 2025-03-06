package ngo.nabarun.tools.util;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T, R> R sendPost(String url, T requestBody, Map<String, String> headers, Class<R> responseType) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            
            if (requestBody != null) {
                String jsonBody = objectMapper.writeValueAsString(requestBody);
                httpPost.setEntity(new StringEntity(jsonBody));
            }
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, responseType);
            }
        }
    }
}
