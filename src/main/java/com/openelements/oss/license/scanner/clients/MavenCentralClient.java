package com.openelements.oss.license.scanner.clients;

import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenCentralClient {

    private final static Logger log = LoggerFactory.getLogger(MavenCentralClient.class);

    private final HttpClient client;

    public MavenCentralClient() {
        client = HttpClient.newHttpClient();
    }

    public String getPom(MavenIdentifier identifier) throws Exception {
        final String urlPrefix = "https://repo1.maven.org/maven2/";
        final String urlPath = identifier.groupId().replace(".", "/") + "/" + identifier.artifactId();
        final String urlFile = identifier.artifactId() + "-" + identifier.version() + ".pom";
        final String url = urlPrefix + urlPath + "/" + identifier.version() + "/" + urlFile;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url)).build();
        final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        final String pomContent = response.body();
        return pomContent.replaceAll("<hr>", "<hr />");
    }

    public Optional<License> getLicenceFromPom(MavenIdentifier identifier) {
        try {
            log.info("Reading license from pom for " + identifier);
            final String pom = getPom(identifier);
            InputStream stream = new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            final Document document = db.parse(stream);
            document.getDocumentElement().normalize();

            NodeList licensesList = document.getElementsByTagName("licenses");
            if (licensesList.getLength() > 0) {
                Node licensesNode = licensesList.item(0);
                if (licensesNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element licensesElement = (Element) licensesNode;

                    // Suche nach dem <license>-Element innerhalb von <licenses>
                    NodeList licenseList = licensesElement.getElementsByTagName("license");
                    if (licenseList.getLength() > 0) {
                        Element licenseElement = (Element) licenseList.item(0);
                        final String licenseName;
                        NodeList nameList = licenseElement.getElementsByTagName("name");
                        if (nameList.getLength() > 0) {
                            Element nameElement = (Element) nameList.item(0);
                            licenseName = nameElement.getTextContent();
                        } else {
                            licenseName = "unknown";
                        }
                        final String licenseUrl;
                        NodeList urlList = licenseElement.getElementsByTagName("url");
                        if (urlList.getLength() > 0) {
                            Element urlElement = (Element) urlList.item(0);
                            licenseUrl = urlElement.getTextContent();
                        } else {
                            licenseUrl = "unknown";
                        }
                        final License license = LicenseCache.getInstance().computeIfAbsent(identifier.toIdentifier(),
                                () -> new License(licenseName, licenseUrl, "pom(" + identifier + ")"));
                        return Optional.of(license);
                    }
                }
            }

            //get license from parent pom
            NodeList parentList = document.getElementsByTagName("parent");
            if (parentList.getLength() > 0) {
                Node parentNode = parentList.item(0);
                if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element parentElement = (Element) parentNode;
                    NodeList parentArtifactIdList = parentElement.getElementsByTagName("artifactId");
                    NodeList parentGroupIdList = parentElement.getElementsByTagName("groupId");
                    NodeList parentVersionList = parentElement.getElementsByTagName("version");
                    if (parentArtifactIdList.getLength() > 0 && parentGroupIdList.getLength() > 0 && parentVersionList.getLength() > 0) {
                        Element parentArtifactIdElement = (Element) parentArtifactIdList.item(0);
                        Element parentGroupIdElement = (Element) parentGroupIdList.item(0);
                        Element parentVersionElement = (Element) parentVersionList.item(0);
                        MavenIdentifier parentIdentifier = new MavenIdentifier(parentGroupIdElement.getTextContent(), parentArtifactIdElement.getTextContent(), parentVersionElement.getTextContent());
                        return getLicenceFromPom(parentIdentifier);
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error in finding license by pom for " + identifier, e);
        }
    }

    public Optional<String> getRepository(Identifier identifier) {
        return getRepository(new MavenIdentifier(identifier));
    }

    public Optional<String> getRepository(MavenIdentifier identifier) {
        try {
            log.info("Reading repository url from pom for " + identifier);
            final String pom = getPom(identifier);
            InputStream stream = new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            final Document document = db.parse(stream);
            document.getDocumentElement().normalize();

            NodeList scmList = document.getElementsByTagName("scm");
            if (scmList.getLength() > 0) {
                Node scmNode = scmList.item(0);
                if (scmNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element scmElement = (Element) scmNode;

                    // Suche nach dem <url>-Element innerhalb von <scm>
                    NodeList urlList = scmElement.getElementsByTagName("url");
                    if (urlList.getLength() > 0) {
                        Element urlElement = (Element) urlList.item(0);
                        final String urlValue = urlElement.getTextContent();
                        if (urlValue.contains("github.com")) {
                            return Optional.of(urlValue);
                        }
                    }
                }
            }

            //get repository from parent pom
            log.info("checking for parent pom for " + identifier);
            NodeList parentList = document.getElementsByTagName("parent");
            if (parentList.getLength() > 0) {
                Node parentNode = parentList.item(0);
                if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element parentElement = (Element) parentNode;
                    NodeList parentArtifactIdList = parentElement.getElementsByTagName("artifactId");
                    NodeList parentGroupIdList = parentElement.getElementsByTagName("groupId");
                    NodeList parentVersionList = parentElement.getElementsByTagName("version");
                    if (parentArtifactIdList.getLength() > 0 && parentGroupIdList.getLength() > 0 && parentVersionList.getLength() > 0) {
                        Element parentArtifactIdElement = (Element) parentArtifactIdList.item(0);
                        Element parentGroupIdElement = (Element) parentGroupIdList.item(0);
                        Element parentVersionElement = (Element) parentVersionList.item(0);
                        MavenIdentifier parentIdentifier = new MavenIdentifier(parentGroupIdElement.getTextContent(), parentArtifactIdElement.getTextContent(), parentVersionElement.getTextContent());
                        return getRepository(parentIdentifier);
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
           throw new RuntimeException("Error in finding repository for dependency " + identifier, e);
        }
    }
}
