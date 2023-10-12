package com.manydesigns.elements.fields;

import com.manydesigns.elements.KeyValueAccessor;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.reflection.factories.ClassAccessorFactories;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

public class ObjectField extends AbstractField<Object> {

    protected final ClassAccessor classAccessor;
    protected final Form form;
    protected Object value;

    public ObjectField(@NotNull PropertyAccessor accessor, @NotNull Mode mode, @Nullable String prefix) {
        super(accessor, mode, prefix);
        classAccessor = ClassAccessorFactories.get(accessor.getType());
        form = new FormBuilder(classAccessor)
                .configPrefix(StringUtils.defaultString(prefix) + accessor.getName() + ".")
                .build();
    }

    public ObjectField(@NotNull PropertyAccessor accessor, @NotNull Mode mode) {
        this(accessor, mode, null);
    }

    @Override
    public boolean validate() {
        errors.clear();
        if (isRequired() && value == null) {
            errors.add(getText("elements.error.field.required"));
            return false;
        }
        return form.validate();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && form.isValid();
    }

    @Override
    public void writeToObject(Object obj) {
        form.writeToObject(accessor.get(obj));
    }

    @Override
    public void valueToXhtml(XhtmlBuffer xb) {
        form.toXhtml(xb);
    }

    @Override
    public String getStringValue() {
        return null;
    }

    @Override
    public void setStringValue(String stringValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
        form.readFromObject(value);
    }

    @Override
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);
        if (mode.isView(insertable, updatable)) {
            return;
        }
        form.readFromRequest(req);
        setValueFromForm();
    }

    @Override
    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            setValue(null);
        } else {
            setValue(accessor.get(obj));
        }
    }

    @Override
    public void readFrom(KeyValueAccessor keyValueAccessor) {
        if (isReadOnly()) {
            return;
        }
        if(!keyValueAccessor.has(accessor.getName())) {
            setValue(null);
            return;
        }
        bulkChecked = true;
        Object value = keyValueAccessor.get(accessor.getName());
        if (value == null) {
            setValue(null);
        } else {
            form.readFrom(keyValueAccessor.inner(value));
            setValueFromForm();
        }
    }

    protected void setValueFromForm() {
        Object object = classAccessor.newInstance();
        if (object == null) {
            throw new IllegalStateException("Could not create an instance using " + classAccessor);
        }
        form.writeToObject(object);
        this.value = object;
    }
}
