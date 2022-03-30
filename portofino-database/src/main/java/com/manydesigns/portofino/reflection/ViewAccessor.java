package com.manydesigns.portofino.reflection;

import com.manydesigns.elements.annotations.Insertable;
import com.manydesigns.elements.annotations.Updatable;
import com.manydesigns.elements.annotations.impl.InsertableImpl;
import com.manydesigns.elements.annotations.impl.UpdatableImpl;
import com.manydesigns.portofino.database.model.View;
import org.jetbrains.annotations.NotNull;

public class ViewAccessor extends TableAccessor {
    public ViewAccessor(@NotNull View view) {
        super(view);
        annotations.put(Insertable.class, new InsertableImpl(view.isInsertable()));
        annotations.put(Updatable.class, new UpdatableImpl(view.isInsertable()));
    }
}
