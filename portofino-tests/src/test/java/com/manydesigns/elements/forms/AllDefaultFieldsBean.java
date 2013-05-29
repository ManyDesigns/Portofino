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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.annotations.*;

import java.math.BigDecimal;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class AllDefaultFieldsBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
