package com.manydesigns.elements.blobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class RepositoryRegistry {

  public static final String copyright = "Copyright (C) 2005-2018 ManyDesigns srl";
  public static final Logger logger = LoggerFactory.getLogger(RepositoryRegistry.class);

  private static RepositoryRegistry instance = null;    // lazy loading
  private Map<String,Repository> registry = null;

  private RepositoryRegistry() {
    synchronized(RepositoryRegistry.class) {
      registry = new HashMap<String,Repository>();
    }
  }

  public static RepositoryRegistry getInstance() {
    if(instance == null) {
      synchronized(RepositoryRegistry.class) {
        if(instance == null) {
          instance = new RepositoryRegistry();
        }
      }
    }
    return instance;
  }

  public Repository getRepository(String key){
    return registry.get(key);
  }

  public void put(Repository repository){
    logger.info("Putting "+repository.getId()+" into RepositoryRegistry");
    registry.put(repository.getId(),repository);
  }

}







