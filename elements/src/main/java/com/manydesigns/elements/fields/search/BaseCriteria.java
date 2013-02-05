/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.reflection.PropertyAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class BaseCriteria extends ArrayList<Criterion> implements Criteria {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected OrderBy orderBy;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public BaseCriteria() {
        super();
    }

    //**************************************************************************
    // Criteria building
    //**************************************************************************

    public Criteria eq(PropertyAccessor accessor, Object value) {
        add(new EqCriterion(accessor, value));
        return this;
    }

    public Criteria in(PropertyAccessor accessor, Object[] values) {
        add(new InCriterion(accessor, values));
        return this;
    }

    public Criteria ne(PropertyAccessor accessor, Object value) {
        add(new NeCriterion(accessor, value));
        return this;
    }

    public Criteria between(PropertyAccessor accessor, Object min, Object max) {
        add(new BetweenCriterion(accessor, min, max));
        return this;
    }

    public Criteria gt(PropertyAccessor accessor, Object value) {
        add(new GtCriterion(accessor, value));
        return this;
    }

    public Criteria ge(PropertyAccessor accessor, Object value) {
        add(new GeCriterion(accessor, value));
        return this;
    }

    public Criteria lt(PropertyAccessor accessor, Object value) {
        add(new LtCriterion(accessor, value));
        return this;
    }

    public Criteria le(PropertyAccessor accessor, Object value) {
        add(new LeCriterion(accessor, value));
        return this;
    }

    public Criteria like(PropertyAccessor accessor, String value,
                     TextMatchMode textMatchMode) {
        add(new LikeCriterion(accessor, value, textMatchMode));
        return this;
    }

    public Criteria ilike(PropertyAccessor accessor, String value,
                     TextMatchMode textMatchMode) {
        add(new IlikeCriterion(accessor, value, textMatchMode));
        return this;
    }

    public Criteria isNull(PropertyAccessor accessor) {
        add(new IsNullCriterion(accessor));
        return this;
    }

    public Criteria isNotNull(PropertyAccessor accessor) {
        add(new IsNotNullCriterion(accessor));
        return this;
    }

    public Criteria orderBy(PropertyAccessor accessor, String direction) {
        orderBy = new OrderBy(accessor, direction);
        return this;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    //**************************************************************************
    // Criterion implementation classes
    //**************************************************************************

    public static abstract class AbstractCriterion implements Criterion {
        protected final PropertyAccessor accessor;

        public AbstractCriterion(PropertyAccessor accessor) {
            this.accessor = accessor;
        }

        public PropertyAccessor getPropertyAccessor() {
            return accessor;
        }
    }

    public static class EqCriterion extends AbstractCriterion {
        protected final Object value;

        public EqCriterion(@NotNull PropertyAccessor accessor,
                           @NotNull Object value) {
            super(accessor);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class InCriterion extends AbstractCriterion {
        protected final Object[] values;

        public InCriterion(@NotNull PropertyAccessor accessor,
                           @NotNull Object[] values) {
            super(accessor);
            this.values = values;
            for (Object value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("Null value");
                }
            }
        }

        public Object[] getValues() {
            return values;
        }
    }

    public static class NeCriterion extends AbstractCriterion {
        protected final Object value;

        public NeCriterion(@NotNull PropertyAccessor accessor, @NotNull Object value) {
            super(accessor);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class BetweenCriterion extends AbstractCriterion {
        protected final Object min;
        protected final Object max;

        public BetweenCriterion(PropertyAccessor accessor,
                                Object min, Object max) {
            super(accessor);
            this.min = min;
            this.max = max;
        }

        public Object getMin() {
            return min;
        }

        public Object getMax() {
            return max;
        }
    }

    public static class GtCriterion extends AbstractCriterion {
        protected final Object value;

        public GtCriterion(PropertyAccessor accessor, Object value) {
            super(accessor);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class GeCriterion extends AbstractCriterion {
        protected final Object value;

        public GeCriterion(PropertyAccessor accessor, Object value) {
            super(accessor);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class LtCriterion extends AbstractCriterion {
        protected final Object value;

        public LtCriterion(PropertyAccessor accessor, Object value) {
            super(accessor);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class LeCriterion extends AbstractCriterion {
        protected final Object value;

        public LeCriterion(PropertyAccessor accessor, Object value) {
            super(accessor);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class LikeCriterion extends AbstractCriterion {
        protected final Object value;
        protected final TextMatchMode textMatchMode;

        public LikeCriterion(PropertyAccessor accessor, Object value,
                             TextMatchMode textMatchMode) {
            super(accessor);
            this.value = value;
            this.textMatchMode = textMatchMode;
        }

        public Object getValue() {
            return value;
        }

        public TextMatchMode getTextMatchMode() {
            return textMatchMode;
        }
    }

    public static class IlikeCriterion extends AbstractCriterion {
        protected final Object value;
        protected final TextMatchMode textMatchMode;

        public IlikeCriterion(PropertyAccessor accessor, Object value,
                              TextMatchMode textMatchMode) {
            super(accessor);
            this.value = value;
            this.textMatchMode = textMatchMode;
        }

        public Object getValue() {
            return value;
        }

        public TextMatchMode getTextMatchMode() {
            return textMatchMode;
        }
    }

    public static class IsNullCriterion extends AbstractCriterion {


        public IsNullCriterion(PropertyAccessor accessor) {
            super(accessor);
        }
    }

    public static class IsNotNullCriterion extends AbstractCriterion {


        public IsNotNullCriterion(PropertyAccessor accessor) {
            super(accessor);
        }
    }
}
