package de.hpi.urlcleaner.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hpi.urlcleaner.exceptions.CouldNotCleanURLException;
import de.hpi.urlcleaner.exceptions.ShopBlacklistedException;
import de.hpi.urlcleaner.persistence.BlacklistEntry;
import de.hpi.urlcleaner.persistence.repositories.IBlacklistRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class UrlCleanerServiceTest {

    @Getter(AccessLevel.PRIVATE) private static final long EXAMPLE_SHOP_ID = 20740;
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_SHOP_ROOTURL = "https://www.rakuten.de";

    @Spy
    private TrackerCleanStrategy strategy = new TrackerCleanStrategy(new ObjectMapper());

    @Mock
    private IdealoBridge idealoBridge;

    @Mock
    private IBlacklistRepository iBlacklistRepository;

    @InjectMocks
    private UrlCleanerService urlCleanerService;

    public UrlCleanerServiceTest() throws IOException {}

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this); //inject Mocks into fields (at first only constructor injection)
        doReturn(getEXAMPLE_SHOP_ROOTURL()).when(getIdealoBridge()).resolveShopIDToRootUrl(getEXAMPLE_SHOP_ID());
    }

    @Test
    public void dirtyUrlContainsNoRedirectsAndNoTrackers() throws CouldNotCleanURLException, ShopBlacklistedException {
        String dirtyUrl = "https://www.rakuten.de/produkt/lederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai" +
                "-tucson-1362004065.html";
        String expectedUrl = "https://www.rakuten.de/produkt/lederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai-tucson-1362004065.html";
        assertEquals(expectedUrl, getUrlCleanerService().clean(dirtyUrl, getEXAMPLE_SHOP_ID()));
    }

    @Test
    public void dirtyUrlContainsRedirectsAndNoTrackers() throws CouldNotCleanURLException, ShopBlacklistedException {
        String dirtyUrl = "http://track.productsup.io/click.redir?siteid=462935&version=1.0&pup_e=4622&pup_cid=55475&pup_id=1362004065&redir=http%3A%2F%2Fwww.rakuten.de%2Fprodukt%2Flederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai-tucson-1362004065.html";
        String expectedUrl = "https://www.rakuten.de/produkt/lederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai-tucson-1362004065.html";
        assertEquals(expectedUrl, getUrlCleanerService().clean(dirtyUrl, getEXAMPLE_SHOP_ID()));
    }

    @Test
    public void dirtyUrlContainsNoRedirectsAndTrackers() throws CouldNotCleanURLException, ShopBlacklistedException {
        String dirtyUrl = "https://www.rakuten.de/produkt/lederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai" +
                "-tucson-1362004065.html&utm_source=idealo&partner=123";
        String expectedUrl = "https://www.rakuten.de/produkt/lederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai" +
                "-tucson-1362004065.html";
        assertEquals(expectedUrl, getUrlCleanerService().clean(dirtyUrl, getEXAMPLE_SHOP_ID()));
    }

    @Test
    public void dirtyUrlContainsRedirectsAndTrackersAndEncodedLink() throws CouldNotCleanURLException, ShopBlacklistedException {
        String dirtyUrl = "http://track.productsup.io/click" +
                ".redir?siteid=462935&version=1.0&pup_e=4622&pup_cid=55475&pup_id=1362004065&redir=http%3A%2F%2Fwww.rakuten.de%2Fprodukt%2Flederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai-tucson-1362004065.html%3Fsclid%3Dp_idealo_DE_1362004065_mid59911_catidauto_prc_15250%26portaldv%3D6%26cid%3Didealo";
        String expectedUrl = "https://www.rakuten" +
                ".de/produkt/lederbezug-sitzbezug-sitzbezuege-ranger-aus-echtem-leder-beige-hyundai-tucson-1362004065.html?portaldv=6";
        assertEquals(expectedUrl, getUrlCleanerService().clean(dirtyUrl, getEXAMPLE_SHOP_ID()));
    }

    @Test (expected = ShopBlacklistedException.class)
    public void attemptToCleanBlacklistedUrl() throws CouldNotCleanURLException, ShopBlacklistedException {
        doReturn(new BlacklistEntry(6980L)).when(getIBlacklistRepository()).findByShopID(6980L);
        getUrlCleanerService().clean("https://www.rakuten.de/produkt/lederbezug", 6980L);
    }

    @Test
    public void attemptToCleanNotBlacklistedUrl() throws CouldNotCleanURLException, ShopBlacklistedException {
        doReturn(null).when(getIBlacklistRepository()).findByShopID(6980L);
        doReturn("https://www.rakuten.de").when(getIdealoBridge()).resolveShopIDToRootUrl(6980L);
        assertEquals("https://www.rakuten.de/produkt/lederbezug", getUrlCleanerService().clean("https://www.rakuten.de/produkt/lederbezug", 6980L));
    }
}