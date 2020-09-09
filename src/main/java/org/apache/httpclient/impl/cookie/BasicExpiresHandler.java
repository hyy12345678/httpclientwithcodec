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
package org.apache.httpclient.impl.cookie;

import org.apache.httpclient.client.utils.DateUtils;
import org.apache.httpclient.cookie.ClientCookie;
import org.apache.httpclient.cookie.CommonCookieAttributeHandler;
import org.apache.httpclient.cookie.MalformedCookieException;
import org.apache.httpclient.cookie.SetCookie;
import org.apache.httpcore.annotation.Contract;
import org.apache.httpcore.annotation.ThreadingBehavior;
import org.apache.httpcore.util.Args;

import java.util.Date;



/**
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicExpiresHandler extends AbstractCookieAttributeHandler implements CommonCookieAttributeHandler {

    /** Valid date patterns */
    private final String[] datepatterns;

    public BasicExpiresHandler(final String[] datepatterns) {
        Args.notNull(datepatterns, "Array of date patterns");
        this.datepatterns = datepatterns;
    }

    @Override
    public void parse(final SetCookie cookie, final String value)
            throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (value == null) {
            throw new MalformedCookieException("Missing value for 'expires' attribute");
        }
        final Date expiry = DateUtils.parseDate(value, this.datepatterns);
        if (expiry == null) {
            throw new MalformedCookieException("Invalid 'expires' attribute: "
                    + value);
        }
        cookie.setExpiryDate(expiry);
    }

    @Override
    public String getAttributeName() {
        return ClientCookie.EXPIRES_ATTR;
    }

}
