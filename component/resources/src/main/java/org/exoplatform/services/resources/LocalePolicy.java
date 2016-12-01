/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.resources;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;


/**
 * This interface represents a pluggable mechanism for different locale determining algorithms
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public interface LocalePolicy {
    /**
     * Determine the Locale to be used for current request
     *
     * @param localeContext locale context info available to implementations as inputs to use when determining appropriate
     *        Locale
     * @return Locale to be used for current user's request
     */
    Locale determineLocale(LocaleContextInfo localeContext);
    
    /**
     *  Automate setters invocation for {@link LocaleContextInfo} object
     * @param username
     * @param portalLocale
     * @param sessionLocale
     * @param requestLocales
     * @param cookieLocales
     * @return LocaleContextInfo
     */
    LocaleContextInfo buildLocaleContextInfo(String username, Locale portalLocale, Locale sessionLocale, Enumeration<Locale> requestLocales, List<Locale> cookieLocales);
}
