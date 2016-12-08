/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.context.embedded;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.http.server.reactive.VertxHttpHandlerAdapter;
import org.springframework.util.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Dave Syer
 */
public class VertxEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer
		implements EmbeddedReactiveHttpServer {

	private VertxHttpHandlerAdapter vertxHandler;

	private final Vertx vertx;
	private io.vertx.core.http.HttpServer vertxServer;
	private CountDownLatch closeLatch = new CountDownLatch(1);

	private boolean running;

	public VertxEmbeddedReactiveHttpServer(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getHttpHandler());
		this.vertxHandler = new VertxHttpHandlerAdapter(getHttpHandler());
		HttpServerOptions options = new HttpServerOptions();
		if(getAddress() != null) {
			options.setHost(getAddress().getHostAddress());
			options.setPort(getPort());
		} else {
			options.setPort(getPort());
		}
		this.vertxServer = vertx.createHttpServer(options);
		vertxServer.requestHandler(request -> vertxHandler.apply(request).subscribe());
	}

	@Override
	public void start() {
		if (!this.running) {
			this.running = true;
			this.vertxServer.listen();
			startDaemonAwaitThread();
		}
	}

	@Override
	public void stop() {
		if (this.running) {
			this.running = false;
			this.vertx.close(ar -> {
				closeLatch.countDown();
			});
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	private void startDaemonAwaitThread() {
		Thread awaitThread = new Thread("container-1") {
			@Override
			public void run() {
				try {
					VertxEmbeddedReactiveHttpServer.this.closeLatch.await(20, TimeUnit.SECONDS);
				} catch (InterruptedException ignore) {
				}
			}

		};
		awaitThread.setContextClassLoader(getClass().getClassLoader());
		awaitThread.setDaemon(false);
		awaitThread.start();
	}
}
