package com.manydesigns.portofino.resourceactions.crud.export;

import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrudExporterRegistry {

    private final List<CrudExporter> exporters = new CopyOnWriteArrayList<>();

    public CrudExporter get(List<MediaType> mediaTypes) {
        for (MediaType type : mediaTypes) {
            for (CrudExporter exporter : exporters) {
                if (exporter.supports(type)) {
                    return exporter;
                }
            }
        }
        throw new IllegalArgumentException("No exporter known for media types " + mediaTypes);
    }

    public void register(CrudExporter exporter) {
        exporters.add(exporter);
    }

    public void registerBefore(Class<? extends CrudExporter> refClass, CrudExporter exporter) {
        Optional<CrudExporter> existing =
                exporters.stream().filter(e -> refClass.isAssignableFrom(e.getClass())).findFirst();
        if (existing.isPresent()) {
            int i = exporters.indexOf(existing.get());
            exporters.add(i, exporter);
        } else {
            register(exporter);
        }
    }

    public boolean replace(Class<? extends CrudExporter> exporter, CrudExporter replacement) {
        return exporters.stream().filter(e -> e.getClass() == exporter).findFirst().map(e -> {
            int i = exporters.indexOf(e);
            exporters.remove(i);
            exporters.add(i, replacement);
            return true;
        }).orElse(false);
    }

}
