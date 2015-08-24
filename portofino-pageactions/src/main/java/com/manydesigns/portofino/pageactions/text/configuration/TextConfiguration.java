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

package com.manydesigns.portofino.pageactions.text.configuration;

import com.manydesigns.portofino.dispatcher.PageActionConfiguration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class TextConfiguration implements PageActionConfiguration {

    protected final List<Attachment> attachments;

    public TextConfiguration() {
        super();
        attachments = new ArrayList<Attachment>();
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void init() {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElementWrapper(name="attachments")
    @XmlElement(name = "attachment", type = Attachment.class)
    public List<Attachment> getAttachments() {
        return attachments;
    }
}
