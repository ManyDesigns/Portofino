package com.manydesigns.portofino.database.model;

import com.manydesigns.portofino.model.Model;
import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.annotation.*;

/**
 * A mapped database view
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@XmlRootElement(name = "view")
public class View extends Table {

    protected boolean insertable = false;
    protected boolean updatable = false;

    public View() {}

    public View(Schema schema) {
        super(schema);
    }

    @XmlAttribute
    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    @XmlAttribute
    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }
}
