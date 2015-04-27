/*
* Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.user;

import com.github.cage.Cage;
import com.github.cage.image.ConstantColorGenerator;
import com.github.cage.image.EffectConfig;
import com.github.cage.image.Painter;
import com.github.cage.image.ScaleConfig;
import com.github.cage.token.RandomCharacterGeneratorFactory;
import com.github.cage.token.RandomTokenGenerator;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Locale;
import java.util.Random;

/**
 * Creates and configures a {@link Cage} instance that can generate captcha
 * images similar to {@link com.github.cage.YCage}, but adapted to fit Twitter Bootstrap graphically.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class CaptchaGenerator extends Cage {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    /**
     * Height of CAPTCHA image. 20px is the height of a standard Bootstrap input field.
     */
    protected static final int HEIGHT = 40;

    /**
     * Width of CAPTCHA image. 220px is the length of a standard Bootstrap input field.
     */
    protected static final int WIDTH = 200;

    /**
     * Character set supplied to the {@link com.github.cage.token.RandomTokenGenerator} used by this
     * template.
     */
    protected static final char[] TOKEN_DEFAULT_CHARACTER_SET = (new String(
            RandomCharacterGeneratorFactory.DEFAULT_DEFAULT_CHARACTER_SET)
            .replaceAll("b|f|i|j|l|m|o|t", "")
            + new String(
                    RandomCharacterGeneratorFactory.DEFAULT_DEFAULT_CHARACTER_SET)
                    .replaceAll("c|i|o", "").toUpperCase(Locale.ENGLISH) + new String(
            RandomCharacterGeneratorFactory.ARABIC_NUMERALS).replaceAll(
            "0|1|9", "")).toCharArray();

    /**
     * Minimum length of token.
     */
    protected static final int TOKEN_LEN_MIN = 6;

    /**
     * Maximum length of token is {@value #TOKEN_LEN_MIN} +
     * {@value #TOKEN_LEN_DELTA}.
     */
    protected static final int TOKEN_LEN_DELTA = 2;

    /**
     * Constructor.
     */
    public CaptchaGenerator() {
        this(new Random(), null);
    }

    public CaptchaGenerator(String format) {
        this(new Random(), format);
    }

    protected CaptchaGenerator(Random rnd, @Nullable String format) {
        super(new Painter(WIDTH, HEIGHT, null, Painter.Quality.MAX, new EffectConfig(true,
                true, false, false, new ScaleConfig(1.0f, 1.0f)), rnd), null,
                new ConstantColorGenerator(Color.BLACK), format,
                1.0f, new RandomTokenGenerator(rnd,
                        new RandomCharacterGeneratorFactory(
                                TOKEN_DEFAULT_CHARACTER_SET, null, rnd),
                        TOKEN_LEN_MIN, TOKEN_LEN_DELTA), rnd);
    }
}
