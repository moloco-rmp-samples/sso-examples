import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EmbeddedCampaignManagerExample {
  private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
  private static final String RMP_PORTAL_BASE_URL = "https://portal.rmp.moloco.cloud";

  public static void main(String[] args) {
    String baseUrl = RMP_PORTAL_BASE_URL;
    String platformId = "RMP_PLATFORM_ID";
    String adAccountId = "ad-account-id";
    String adAccountTitle = "My Ad Account";
    String path = "/embed/sponsored-ads/cm/a/" + adAccountId;
    String name = "Example User Name";
    String email = "test@example.com";
    String role = "AD_ACCOUNT_OWNER";
    String externalUserId = "user-id";
    String secret = "super-secret";
    String version = "1.0.0";
    String colorMode = "light"; // "light" | "dark" | "useDeviceSetting"
    String language = "en"; // "en" | "ko"

    try {
      String url = createSignedRmpPortalUrl(baseUrl, path, platformId, adAccountId, adAccountTitle, name, email, role,
          externalUserId, secret, version, colorMode, language);
      System.out.println(url);

    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static String createSignedRmpPortalUrl(String baseUrl, String path, String platformId, String adAccountId,
      String adAccountTitle, String name, String email, String role, String externalUserId, String secret,
      String version, String colorMode, String language) throws Exception {
    // unix timestamp in seconds
    String timestamp = Long.toString(System.currentTimeMillis() / 1000L);

    // create nonce
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[20];
    random.nextBytes(bytes);
    String nonce = Base64.getEncoder().encodeToString(bytes);

    // gather params in an alphabetical order
    String params = "";
    params += adAccountId + "\n";
    params += adAccountTitle + "\n";
    params += email + "\n";
    params += externalUserId + "\n";
    params += name + "\n";
    params += nonce + "\n";
    params += path + "\n";
    params += platformId + "\n";
    params += role + "\n";
    params += timestamp + "\n";
    params += version;

    // create signature
    String utf8Charset = StandardCharsets.UTF_8.toString();
    byte[] keyBytes = secret.getBytes();
    SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA256_ALGORITHM);
    Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
    mac.init(signingKey);
    byte[] rawHmac = Base64.getEncoder().encode(mac.doFinal(params.getBytes(utf8Charset)));
    String signature = new String(rawHmac, utf8Charset);

    // construct final url
    String signedUrl = baseUrl + "/sso?";
    signedUrl += "ad_account_id=" + URLEncoder.encode(adAccountId, utf8Charset);
    signedUrl += "&ad_account_title=" + URLEncoder.encode(adAccountTitle, utf8Charset);
    signedUrl += "&email=" + URLEncoder.encode(email, utf8Charset);
    signedUrl += "&external_user_id=" + URLEncoder.encode(externalUserId, utf8Charset);
    signedUrl += "&name=" + URLEncoder.encode(name, utf8Charset);
    signedUrl += "&nonce=" + URLEncoder.encode(nonce, utf8Charset);
    signedUrl += "&path=" + URLEncoder.encode(path, utf8Charset);
    signedUrl += "&platform_id=" + URLEncoder.encode(platformId, utf8Charset);
    signedUrl += "&role=" + URLEncoder.encode(role, utf8Charset);
    signedUrl += "&timestamp=" + URLEncoder.encode(timestamp, utf8Charset);
    signedUrl += "&version=" + URLEncoder.encode(version, utf8Charset);
    signedUrl += "&signature=" + URLEncoder.encode(signature, utf8Charset);
    signedUrl += "&config:colorModePreference=" + URLEncoder.encode(colorMode, utf8Charset);
    signedUrl += "&config:language=" + URLEncoder.encode(language, utf8Charset);

    return signedUrl;
  }
}
