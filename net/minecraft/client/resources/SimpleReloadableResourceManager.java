package net.minecraft.client.resources;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class SimpleReloadableResourceManager implements IReloadableResourceManager {
    private static final Logger logger = LogManager.getLogger();
    private static final Joiner joinerResourcePacks = Joiner.on(", ");
    private final Map<String, FallbackResourceManager> domainResourceManagers = Maps.newHashMap();
    private final List<IResourceManagerReloadListener> reloadListeners = Lists.newArrayList();
    private final Set<String> setResourceDomains = Sets.newLinkedHashSet();
    private final IMetadataSerializer rmMetadataSerializer;

    public SimpleReloadableResourceManager(IMetadataSerializer rmMetadataSerializerIn) {
        rmMetadataSerializer = rmMetadataSerializerIn;
    }

    public void reloadResourcePack(IResourcePack resourcePack) {
        for (String s : resourcePack.getResourceDomains()) {
            setResourceDomains.add(s);
            FallbackResourceManager fallbackresourcemanager = domainResourceManagers.get(s);

            if (fallbackresourcemanager == null) {
                fallbackresourcemanager = new FallbackResourceManager(rmMetadataSerializer);
                domainResourceManagers.put(s, fallbackresourcemanager);
            }

            fallbackresourcemanager.addResourcePack(resourcePack);
        }
    }

    public Set<String> getResourceDomains() {
        return setResourceDomains;
    }

    public IResource getResource(ResourceLocation location) throws IOException {
        IResourceManager iresourcemanager = domainResourceManagers.get(location.getResourceDomain());

        if (iresourcemanager != null) {
            return iresourcemanager.getResource(location);
        } else {
            throw new FileNotFoundException(location.toString());
        }
    }

    public List<IResource> getAllResources(ResourceLocation location) throws IOException {
        IResourceManager iresourcemanager = domainResourceManagers.get(location.getResourceDomain());

        if (iresourcemanager != null) {
            return iresourcemanager.getAllResources(location);
        } else {
            throw new FileNotFoundException(location.toString());
        }
    }

    private void clearResources() {
        domainResourceManagers.clear();
        setResourceDomains.clear();
    }

    public void reloadResources(List<IResourcePack> resourcesPacksList) {
        clearResources();
        logger.info("Reloading ResourceManager: " + joinerResourcePacks.join(Iterables.transform(resourcesPacksList, p_apply_1_ -> p_apply_1_.getPackName())));

        for (IResourcePack iresourcepack : resourcesPacksList) {
            reloadResourcePack(iresourcepack);
        }

        notifyReloadListeners();
    }

    public void registerReloadListener(IResourceManagerReloadListener reloadListener) {
        reloadListeners.add(reloadListener);
        reloadListener.onResourceManagerReload(this);
    }

    private void notifyReloadListeners() {
        for (IResourceManagerReloadListener iresourcemanagerreloadlistener : reloadListeners) {
            iresourcemanagerreloadlistener.onResourceManagerReload(this);
        }
    }
}
