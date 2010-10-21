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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.annotations.*;

import java.math.BigDecimal;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class AllDefaultFieldsBean {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public boolean aBoolean1;

    public Boolean aBoolean2;

    public Date date;

    public BigDecimal decimal;

    public int anInt;

    public Integer anInteger;

    @CAP
    public String cap;

    @CodiceFiscale
    public String codiceFiscale;

    @Email
    public String email;

    @PartitaIva
    public String partitaIva;

    @Password
    public String password;

    @Phone
    public String phone;

    @Radio(labels = {}, values = {})
    public String radio;

    @Select(labels = {}, values = {})
    public String select;

    public String text;

    public int getAPrivateInt() {
        return aPrivateInt;
    }

    public void setAPrivateInt(int aPrivateInt) {
        this.aPrivateInt = aPrivateInt;
    }

    @FileBlob
    public String aBlob;

    // none of the following fields should be detected due to their modifiers

    static int aStaticInt;

    private int aPrivateInt;

    protected int aProtectedInt;

    int aPackageProtectedInt;

}
