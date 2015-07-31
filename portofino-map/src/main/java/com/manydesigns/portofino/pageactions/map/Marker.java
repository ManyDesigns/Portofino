/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.map;

/**
 * @author Emanuele Poggi    - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Marker {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected String title;
    protected String description;
    protected Position position;
    protected String readUrl;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Marker(  String title , String description , Position position , String readUrl ) {
        this.title = title;
        this.description = description;
        this.position = position;
        this.readUrl = readUrl;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getReadUrl() {
        return readUrl;
    }

    public void setReadUrl(String readUrl) {
        this.readUrl = readUrl;
    }
}
