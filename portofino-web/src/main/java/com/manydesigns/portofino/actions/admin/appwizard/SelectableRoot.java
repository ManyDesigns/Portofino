package com.manydesigns.portofino.actions.admin.appwizard;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Updatable;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SelectableRoot {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    @Updatable(false)
    public String tableName;
    @Label("")
    public boolean selected;

    public SelectableRoot(String tableName, boolean selected) {
        this.tableName = tableName;
        this.selected = selected;
    }

    public SelectableRoot() {}
}

