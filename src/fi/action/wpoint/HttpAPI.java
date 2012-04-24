package fi.action.wpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class HttpAPI {
	
	HttpClient client;
	
	public HttpAPI() {
		client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
	}

	public String get(String url) throws ClientProtocolException, IOException {
		HttpPost get = new HttpPost(url);
		String response = responseToString(client.execute(get));
		return response;
	}
	
	public String post(String url, JSONObject json) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(url);
		
		StringEntity entity = new StringEntity(json.toString());
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));
		post.setEntity(entity);
		
		String response = responseToString(client.execute(post));
		return response;
	}
	
	private String responseToString(HttpResponse response) {
		if(response == null)
			return null;
		
		InputStream input = null;		
		BufferedReader reader;
		StringBuilder builder = new StringBuilder();;
		try {
			input = response.getEntity().getContent();
			reader = new BufferedReader(new InputStreamReader(input));
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(input != null)
					input.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
		return builder.toString();
	}
}
