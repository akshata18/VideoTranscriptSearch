package org.transcriptSearch;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.Verticle;

public class Client extends Verticle{
	public void start()
	{
		HttpClient client = vertx.createHttpClient();
		client.setPort(8080);
		client.setHost("localhost");
		HttpClientRequest request = client.get("", new Handler<HttpClientResponse>() {
		    public void handle(HttpClientResponse resp) {
		        container.logger().info("Got a response: " + resp.statusCode());
		    }
		});
		request.write("efficiency of algorithms");
		request.end();
	}
}
