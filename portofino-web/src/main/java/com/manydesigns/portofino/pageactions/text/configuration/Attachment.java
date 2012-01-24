/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.text.configuration;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Attachment {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String id;
    protected String contentType;
    protected String filename;
    protected boolean downloadable = true;
    protected long size;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(Attachment.class);

    //**************************************************************************
    // Contruction
    //**************************************************************************

    public Attachment() {
        this(null);
    }

    public Attachment(@Nullable String id) {
        this.id = id;
    }

    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(required = true)
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @XmlAttribute(required = true)
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @XmlAttribute(required = true)
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @XmlAttribute
    public boolean isDownloadable() {
        return downloadable;
    }

    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }
}
