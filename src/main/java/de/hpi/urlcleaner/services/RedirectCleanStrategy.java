package de.hpi.urlcleaner.services;

import de.hpi.urlcleaner.exceptions.CouldNotCleanURLException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;


@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class RedirectCleanStrategy implements ICleanStrategy {

    @Getter(AccessLevel.PRIVATE) private static final String WWW = "www.";

    private String rootUrlWithProtocol;

    private String rootUrlWithoutProtocol;

    RedirectCleanStrategy(long shopID, IdealoBridge idealoBridge) throws CouldNotCleanURLException {
        try {
            setRootUrlWithProtocol(idealoBridge.resolveShopIDToRootUrl(shopID));
            setRootUrlWithoutProtocol(removeProtocolFromURL(getRootUrlWithProtocol()));
        } catch (MalformedURLException e) {
            throw new CouldNotCleanURLException(e.getMessage());
        }
    }

    @Override
    public String clean(String dirtyUrl) throws CouldNotCleanURLException{
        String cleanedUrl = dirtyUrl;
        if (!cleanedUrl.contains(getRootUrlWithoutProtocol())) {
            throw new CouldNotCleanURLException("Failed to clean " + dirtyUrl + "\n" +
                    "Could not find rootUrl: " + getRootUrlWithoutProtocol());
        }
        cleanedUrl = cleanedUrl.substring(cleanedUrl.indexOf(getRootUrlWithoutProtocol()) + getRootUrlWithoutProtocol
                ().length());
        cleanedUrl = getRootUrlWithProtocol() + cleanedUrl;
        return cleanedUrl;
    }

    //actions
    private String removeProtocolFromURL(String cleanedUrl) throws MalformedURLException {
        URL url = new URL(cleanedUrl);
        String urlWithoutProtocol = url.getHost();
        return urlWithoutProtocol.startsWith(getWWW()) ?
                urlWithoutProtocol.substring(getWWW().length()) : urlWithoutProtocol;
    }

}