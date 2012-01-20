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

package com.manydesigns.portofino.actions.text;

import com.manydesigns.portofino.actions.text.configuration.Attachment;
import com.manydesigns.portofino.actions.text.configuration.TextConfiguration;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TextLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
