package org.aksw.mlqa.analyzer.querytype;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

public abstract class ASpotter {

	public abstract Map<String, List<Entity>> getEntities(String question) throws MalformedURLException, ProtocolException, IOException, ParseException;

	private boolean useCache = true;
	private static PersistentCache cache = new PersistentCache();

	protected String requestPOST(String input, String requestURL) throws MalformedURLException, ProtocolException, IOException {
			if (useCache) {
				if (cache.containsKey(input)) {
					return cache.get(input);
				}
			}

			String output = POST(input, requestURL);
			cache.put(input, output);
			if (useCache) {
				cache.writeCache();
			}

			return output;
	}

	private String POST(String urlParameters, String requestURL) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(requestURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.length()));

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();

		InputStream inputStream = connection.getInputStream();
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(in);

		StringBuilder sb = new StringBuilder();
		while (reader.ready()) {
			sb.append(reader.readLine());
		}

		wr.close();
		reader.close();
		connection.disconnect();

		return sb.toString();
	}

	@Override
	public String toString() {
		String[] name = getClass().getName().split("\\.");
		return name[name.length - 1].substring(0, 3);
	}
}