package com.manydesigns.elements.blobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class RepositoryRegistry {

  public static final String copyright = "Copyright (C) 2005-2019 ManyDesigns srl";
  public static final Logger logger = LoggerFactory.getLogger(RepositoryRegistry.class);

  private static RepositoryRegistry instance = new RepositoryRegistry();
  private final Map<String, Repository> registry;

  private RepositoryRegistry() {
      registry = new HashMap<>();
  }

  public static RepositoryRegistry getInstance() {
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







