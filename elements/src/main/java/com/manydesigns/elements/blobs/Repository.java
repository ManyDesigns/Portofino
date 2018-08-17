package com.manydesigns.elements.blobs;

import java.io.IOException;
import java.io.InputStream;

public interface Repository {
  public static final String copyright = "Copyright (C) 2005-2018 ManyDesigns srl";

  public String save(Blob blob) throws IOException;
  public InputStream load(Blob blob) throws IOException;
  public Boolean delete(Blob blob) throws IOException;

  public String getId();
}
