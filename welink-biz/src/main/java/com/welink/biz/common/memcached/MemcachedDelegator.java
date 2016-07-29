package com.welink.biz.common.memcached;

import com.google.common.base.Throwables;
import com.welink.commons.tacker.EventTracker;
import net.spy.memcached.MemcachedClient;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by saarixx on 8/12/14.
 */
public class MemcachedDelegator<K, V> implements Cache<K, V> {

    static Logger log = LoggerFactory.getLogger(Cache.class);

    private String name;

    private MemcachedClient memcachedClient;

    private int expireSecond;

    public MemcachedDelegator(String name, MemcachedClient memcachedClient, int expireSecond) {
        checkArgument(isNotBlank(name));
        checkNotNull(memcachedClient, "memcachedClient");
        checkArgument(expireSecond > 0, "expireSecond should be positive ...");
        this.name = name;
        this.memcachedClient = memcachedClient;
        this.expireSecond = expireSecond;
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            log.debug("Getting object from cache [" + name + "] for key [" + key + "]");

            if (key == null) {
                return null;
            } else {
                return (V) memcachedClient.get(key.toString());
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            EventTracker.track("system", "memcached", t.getMessage());
            throw Throwables.propagate(t);
        }
    }

    @Override
    public V put(K key, V value) throws CacheException {
        try {
            log.debug("Putting object in cache [" + name + "] for key [" + key + "]");

            V previous = (V) memcachedClient.get(key.toString());
            memcachedClient.set((String) key, expireSecond, value);
            return previous;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            EventTracker.track("system", "memcached", t.getMessage());
            throw Throwables.propagate(t);
        }
    }

    @Override
    public V remove(K key) throws CacheException {
        log.trace("Removing object from cache [" + name + "] for key [" + key + "]");

        try {
            V previous = (V) memcachedClient.get(key.toString());
            memcachedClient.delete((String) key);
            return previous;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            EventTracker.track("system", "memcached", t.getMessage());
            throw Throwables.propagate(t);
        }
    }

    @Override
    public void clear() throws CacheException {
        throw new UnsupportedOperationException("memcached cache method clear");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("memcached cache method size");
    }

    @Override
    public Set<K> keys() {
        throw new UnsupportedOperationException("memcached cache method keys");
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("memcached cache method values");
    }

}
