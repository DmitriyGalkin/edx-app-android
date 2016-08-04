package org.edx.mobile.http;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.event.NewVersionAvailableEvent;
import org.edx.mobile.third_party.versioning.ArtifactVersion;
import org.edx.mobile.third_party.versioning.DefaultArtifactVersion;
import org.edx.mobile.util.DateUtil;

import java.io.IOException;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * An OkHttp interceptor that checks for information about app
 * updates in the response headers, and broadcasts them on the event
 * bus if found.
 */
public class NewVersionBroadcastInterceptor implements Interceptor {
    /**
     * Header field name for the latest version number of the app
     * that is available in the app stores.
     */
    private static final String HEADER_APP_LATEST_VERSION =
            "EDX-APP-LATEST-VERSION";
    /**
     * Header field name for the last date up to which the API used
     * by the current version of the app will be supported and
     * maintained.
     */
    private static final String HEADER_APP_VERSION_LAST_SUPPORTED_DATE =
            "EDX-APP-VERSION-LAST-SUPPORTED-DATE";

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Response response = chain.proceed(chain.request());

        final ArtifactVersion appLatestVersion; {
            final String appLatestVersionString = response.header(HEADER_APP_LATEST_VERSION);
            appLatestVersion = appLatestVersionString == null ?
                    null : new DefaultArtifactVersion(appLatestVersionString);
        }

        final Date lastSupportedDate = DateUtil.convertToDate(
                response.header(HEADER_APP_VERSION_LAST_SUPPORTED_DATE));

        final boolean isUnsupported = response.code() == HttpStatus.UPGRADE_REQUIRED;

        // If any of these properties is available and valid, then broadcast the
        // event with the information we have.
        if (isUnsupported || lastSupportedDate != null || appLatestVersion != null &&
                appLatestVersion.compareTo(
                        new DefaultArtifactVersion(BuildConfig.VERSION_NAME)) > 0) {
            NewVersionAvailableEvent.post(appLatestVersion, lastSupportedDate, isUnsupported);
        }

        return response;
    }
}
