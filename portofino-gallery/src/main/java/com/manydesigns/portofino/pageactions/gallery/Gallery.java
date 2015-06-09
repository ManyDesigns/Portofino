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

package com.manydesigns.portofino.pageactions.gallery;

import com.manydesigns.elements.gfx.ColorUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Emanuele Poggi     - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Gallery {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected String id;
    protected String name;
    protected List<Image> images ;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Gallery() {
       this.images = new ArrayList<Image>();
    }

    public Gallery( String id, String name ) {
       this.id = id;
       this.name = name;
       this.images = new ArrayList<Image>();
    }

    public Gallery(String id, String name, List<Image> images ) {
        this.id = id;
        this.name = name;
        this.images = images;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean addImage( Image image ){
        return images.add(image);
    }

    public List<Image> getImages(){
        return images;
    }

}
