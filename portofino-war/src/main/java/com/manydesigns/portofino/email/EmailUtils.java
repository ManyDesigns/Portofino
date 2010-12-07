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
package com.manydesigns.portofino.email;

import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.system.model.email.EmailBean;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class EmailUtils {

    //STATI POSSIBILI DELLA MAIL
    public static final long SENDING  = 0;
    public static final long TOBESENT  = 1;
    public static final long SENT  = 2;
    public static final long REJECTED  = 3;
    public static final long BOUNCED  = 4;


    public static final String SUBJECT = "subject";
    public static final String BODY = "body";
    public static final String TO = "to";
    public static final String FROM = "from";
    public static final String CREATEDATE = "createdate";
    public static final String STATE = "state";
    public static final String ID = "id";
    public static final String ATTACHMENT_PATH = "attachmentPath";
    public static final String ATTACHMENT_DESCRIPTION = "attachmentDescription";
    public static final String ATTACHMENT_NAME = "attachmentName";
    public static final String EMAILQUEUE_TABLE = "portofino.public.emailqueue";
    public static final String PORTOFINO = "portofino";



    public static synchronized void addEmail(Context context, String subject, String body,
    String to, String from) {
        EmailBean email = new EmailBean(subject, body, to,
                from);
        context.saveObject(EmailUtils.EMAILQUEUE_TABLE, email);
    }

}
