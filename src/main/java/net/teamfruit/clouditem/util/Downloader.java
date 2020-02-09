package net.teamfruit.clouditem.util;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Downloader {
	public static final int timeout = 3000;
	public static final @Nonnull String useragent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36";

	public static final @Nonnull Downloader downloader = new Downloader();

	public @Nonnull CloseableHttpClient client;

	public Downloader() {
		final Builder requestConfig = RequestConfig.custom();

		if (timeout>0) {
			requestConfig.setConnectTimeout(timeout);
			requestConfig.setSocketTimeout(timeout);
		}

		final List<Header> headers = new ArrayList<>();
		headers.add(new BasicHeader("Accept-Charset", "utf-8"));
		headers.add(new BasicHeader("Accept-Language", "ja, en;q=0.8"));
		headers.add(new BasicHeader("User-Agent", useragent));

		this.client = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig.build())
				.setDefaultHeaders(headers)
				.build();
	}
}