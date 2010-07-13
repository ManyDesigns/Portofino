/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */
package com.manydesigns.elements.fields;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 */
public class DefaultCascadedOptionProvider implements CascadedOptionProvider{
    final List<DefaultSelectFieldOption> roots;

    public DefaultCascadedOptionProvider(ArrayList<DefaultSelectFieldOption> roots) {
        this.roots = roots;
    }

    public Collection<SelectFieldOption> getOptions(int level) {

        List<SelectFieldOption> ancestors =
                new ArrayList<SelectFieldOption>();
        ancestors.addAll(roots);
        for (int i = 0; i < level; i++) {
            List<SelectFieldOption> tmp = new ArrayList<SelectFieldOption>();
            for (SelectFieldOption current : ancestors) {
                tmp.addAll((DefaultSelectFieldOption) current);
            }
            ancestors = tmp;
        }
        return ancestors;
    }

    public Collection<SelectFieldOption> getOptions(int level, String filter) {
        if (filter == null) {
            return Collections.EMPTY_LIST;
        }

        Collection<SelectFieldOption> ancestors = getOptions(level - 1);
        for (SelectFieldOption current : ancestors) {
            if (filter.equals(current.getValue())) {
                return (DefaultSelectFieldOption) current;
            }
        }
        return Collections.EMPTY_LIST;
    }

    public SelectFieldOption getParent(int level, String value) {
        Collection<SelectFieldOption> ancestors = getOptions(level - 1);
        for (SelectFieldOption current : ancestors) {
            for (SelectFieldOption currChild : (DefaultSelectFieldOption) current) {
                if (value.equals(currChild.getValue())) {
                    return current;
                }
            }

        }
        return null;
    }

    public SelectFieldOption getOption(int level, String value) {
        Collection<SelectFieldOption> options = getOptions(level);
        for (SelectFieldOption current : options) {
            if (value.equals(current.getValue())) {
                return current;
            }
        }
        return null;
    }
}
