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

package com.manydesigns.portofino.pageactions.text;

import com.manydesigns.portofino.pageactions.text.configuration.Attachment;
import com.manydesigns.portofino.pageactions.text.configuration.TextConfiguration;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TextLogic {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static Attachment createAttachment(TextConfiguration textConfiguration, String id, String fileName,
                                              String contentType, long size) {
        Attachment attachment = new Attachment(id);
        attachment.setFilename(fileName);
        attachment.setContentType(contentType);
        attachment.setSize(size);
        textConfiguration.getAttachments().add(attachment);
        return attachment;
    }

    public static Attachment findAttachmentById(
            TextConfiguration textConfiguration, String code) {
        for (Attachment current : textConfiguration.getAttachments()) {
            if (current.getId().equals(code)) {
                return current;
            }
        }
        return null;
    }

    public static Attachment deleteAttachmentByCode(TextConfiguration textConfiguration, String code) {
        Attachment attachment = findAttachmentById(textConfiguration, code);
        if (attachment == null) {
            return null;
        } else {
            textConfiguration.getAttachments().remove(attachment);
            return attachment;
        }
    }
}
