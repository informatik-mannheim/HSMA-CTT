/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.controller;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class Utilities {
    @Value("${server.port}")
    private String port;

    @Value("${hostname}")
    private String host;

    /**
     * Optional override for the URL the service is reachable at. This URL is used to construct absolute links.
     * Useful for reverse proxy setups/CDNs.
     */
    @Value("${url_override:#{null}}")
    private String urlOverride;

    public Date uhrzeitAufDatumSetzen(Date datum, String zeit) {
        if (zeit != null && zeit.length() == 5) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(datum);
            cal.set(Calendar.HOUR, Integer.parseInt(zeit.substring(0, 2)));
            cal.set(Calendar.MINUTE, Integer.parseInt(zeit.substring(3, 5)));

            datum = cal.getTime();
        }

        return datum;
    }

    /**
     * Converts a relative local path to an absolute URI
     *
     * @param localPath Local path of the resource
     * @param request   The request is used to differentiate between http/https. Should be moved to setting!
     * @return An absolute URI to the given resource
     */
    public UriComponents getUriToLocalPath(String scheme, String localPath) {
        return createUriBuilder(scheme, localPath).build();
    }

    public UriComponents getUriToLocalPath(String scheme, String localPath, String query) {
        return createUriBuilder(scheme, localPath)
                .query(query)
                .build();
    }

    private UriComponentsBuilder createUriBuilder(String scheme, String path){
        if (urlOverride == null) {
            return UriComponentsBuilder.newInstance()
                    .scheme(scheme) // TODO: Optimally http/https should be configured somewhere
                    .host(host)
                    .port(port)
                    .path(path);
        } else {
            return UriComponentsBuilder.newInstance()
                    .uri(URI.create(urlOverride))
                    .path(path);
        }
    }
}
