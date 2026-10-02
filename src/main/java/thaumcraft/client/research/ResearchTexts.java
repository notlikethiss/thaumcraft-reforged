package thaumcraft.client.research;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import thaumcraft.Thaumcraft;

public final class ResearchTexts implements ResourceManagerReloadListener {
    public static final ResearchTexts INSTANCE = new ResearchTexts();
    private static final String FALLBACK = "en_us";

    private final Map<String, Map<String, Entry>> cache = new HashMap<>();

    public record Page(String type, String content) {
    }

    public record Entry(String name, String popup, String longText, List<Page> pages) {
    }

    private ResearchTexts() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        cache.clear();
    }

    public static @Nullable Entry get(String key) {
        String language = Minecraft.getInstance().getLanguageManager().getSelected();
        Entry entry = INSTANCE.load(language).get(key);
        if (entry == null && !FALLBACK.equals(language)) {
            entry = INSTANCE.load(FALLBACK).get(key);
        }
        return entry;
    }

    public static String name(String key) {
        Entry entry = get(key);
        return entry == null ? key : entry.name();
    }

    private Map<String, Entry> load(String language) {
        return cache.computeIfAbsent(language, this::parse);
    }

    private Map<String, Entry> parse(String language) {
        Map<String, Entry> result = new HashMap<>();
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> resource = manager.getResource(Thaumcraft.id("research/" + language + ".xml"));
        if (resource.isEmpty()) {
            return result;
        }
        try (InputStream stream = resource.get().open()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(stream);
            NodeList projects = document.getElementsByTagName("project");
            for (int i = 0; i < projects.getLength(); i++) {
                Element project = (Element) projects.item(i);
                List<Page> pages = new ArrayList<>();
                NodeList pageNodes = project.getElementsByTagName("page");
                for (int j = 0; j < pageNodes.getLength(); j++) {
                    Element page = (Element) pageNodes.item(j);
                    pages.add(new Page(page.getAttribute("type"), page.getTextContent()));
                }
                result.put(
                    project.getAttribute("id"),
                    new Entry(text(project, "name"), text(project, "popupText"), text(project, "longText"), List.copyOf(pages))
                );
            }
        } catch (Exception exception) {
            Thaumcraft.LOGGER.error("Failed to load research texts for {}", language, exception);
        }
        return result;
    }

    private static String text(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }
}
