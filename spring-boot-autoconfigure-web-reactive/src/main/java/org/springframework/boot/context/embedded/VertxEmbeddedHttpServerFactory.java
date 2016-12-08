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
import org.springframework.http.server.reactive.HttpHandler;

/**
 * @author Dave Syer
 */
public class VertxEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	private final Vertx vertx;

	public VertxEmbeddedHttpServerFactory(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public VertxEmbeddedReactiveHttpServer getReactiveHttpServer(HttpHandler httpHandler,
			EmbeddedReactiveHttpServerCustomizer... customizers) {
		VertxEmbeddedReactiveHttpServer server = new VertxEmbeddedReactiveHttpServer(vertx);
		server.setHandler(httpHandler);
		for (EmbeddedReactiveHttpServerCustomizer customizer : customizers) {
			customizer.customize(server);
		}
		try {
			server.afterPropertiesSet();
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		return server;
	}
}
