/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.httpclient.impl.client;

import org.apache.httpclient.client.ClientProtocolException;
import org.apache.httpclient.client.config.RequestConfig;
import org.apache.httpclient.client.methods.CloseableHttpResponse;
import org.apache.httpclient.client.methods.Configurable;
import org.apache.httpclient.client.methods.HttpExecutionAware;
import org.apache.httpclient.client.methods.HttpRequestWrapper;
import org.apache.httpclient.client.protocol.HttpClientContext;
import org.apache.httpclient.conn.ClientConnectionManager;
import org.apache.httpclient.conn.ClientConnectionRequest;
import org.apache.httpclient.conn.HttpClientConnectionManager;
import org.apache.httpclient.conn.ManagedClientConnection;
import org.apache.httpclient.conn.routing.HttpRoute;
import org.apache.httpclient.conn.scheme.SchemeRegistry;
import org.apache.httpclient.impl.execchain.MinimalClientExec;
import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpHost;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.annotation.Contract;
import org.apache.httpcore.annotation.ThreadingBehavior;
import org.apache.httpcore.impl.DefaultConnectionReuseStrategy;
import org.apache.httpcore.params.BasicHttpParams;
import org.apache.httpcore.params.HttpParams;
import org.apache.httpcore.protocol.BasicHttpContext;
import org.apache.httpcore.protocol.HttpContext;
import org.apache.httpcore.protocol.HttpRequestExecutor;
import org.apache.httpcore.util.Args;

import java.io.IOException;
import java.util.concurrent.TimeUnit;



/**
 * Internal class.
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
@SuppressWarnings("deprecation")
class MinimalHttpClient extends CloseableHttpClient {

    private final HttpClientConnectionManager connManager;
    private final MinimalClientExec requestExecutor;
    private final HttpParams params;

    public MinimalHttpClient(
            final HttpClientConnectionManager connManager) {
        super();
        this.connManager = Args.notNull(connManager, "HTTP connection manager");
        this.requestExecutor = new MinimalClientExec(
                new HttpRequestExecutor(),
                connManager,
                DefaultConnectionReuseStrategy.INSTANCE,
                DefaultConnectionKeepAliveStrategy.INSTANCE);
        this.params = new BasicHttpParams();
    }

    @Override
    protected CloseableHttpResponse doExecute(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context) throws IOException, ClientProtocolException {
        Args.notNull(target, "Target host");
        Args.notNull(request, "HTTP request");
        HttpExecutionAware execAware = null;
        if (request instanceof HttpExecutionAware) {
            execAware = (HttpExecutionAware) request;
        }
        try {
            final HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(request);
            final HttpClientContext localcontext = HttpClientContext.adapt(
                context != null ? context : new BasicHttpContext());
            final HttpRoute route = new HttpRoute(target);
            RequestConfig config = null;
            if (request instanceof Configurable) {
                config = ((Configurable) request).getConfig();
            }
            if (config != null) {
                localcontext.setRequestConfig(config);
            }
            return this.requestExecutor.execute(route, wrapper, localcontext, execAware);
        } catch (final HttpException httpException) {
            throw new ClientProtocolException(httpException);
        }
    }

    @Override
    public HttpParams getParams() {
        return this.params;
    }

    @Override
    public void close() {
        this.connManager.shutdown();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {

        return new ClientConnectionManager() {

            @Override
            public void shutdown() {
                connManager.shutdown();
            }

            @Override
            public ClientConnectionRequest requestConnection(
                    final HttpRoute route, final Object state) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void releaseConnection(
                    final ManagedClientConnection conn,
                    final long validDuration, final TimeUnit timeUnit) {
                throw new UnsupportedOperationException();
            }

            @Override
            public SchemeRegistry getSchemeRegistry() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void closeIdleConnections(final long idletime, final TimeUnit timeUnit) {
                connManager.closeIdleConnections(idletime, timeUnit);
            }

            @Override
            public void closeExpiredConnections() {
                connManager.closeExpiredConnections();
            }

        };

    }

}
