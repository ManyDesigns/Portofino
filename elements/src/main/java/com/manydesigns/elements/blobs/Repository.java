package com.manydesigns.elements.blobs;

import java.io.IOException;
import java.io.InputStream;

public interface Repository {
  String copyright = "Copyright (C) 2005-2018 ManyDesigns srl";

  String save(Blob blob) throws IOException;
  InputStream load(Blob blob) throws IOException;
  Boolean delete(Blob blob) throws IOException;

  String getId();
}
