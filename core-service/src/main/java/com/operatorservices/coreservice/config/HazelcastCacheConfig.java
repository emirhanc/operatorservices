package com.operatorservices.coreservice.config;


import com.hazelcast.config.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class HazelcastCacheConfig {

    @Bean
    public  Config hazelcastConfig(){

        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU).
                setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE);

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("purchases")
                .setMaxIdleSeconds(15)
                .setEvictionConfig(evictionConfig);
        //15 sec. for demo

        Config hazelcastConfig = new Config();
        hazelcastConfig.setInstanceName("hazelcast-cache")
                .addMapConfig(mapConfig);

        return hazelcastConfig;
    }


}
