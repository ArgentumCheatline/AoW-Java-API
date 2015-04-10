/******************************************************************************
 * Copyright (C) 2014 Yevgeny Krasik                                          *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package me.wolftein.steroid.example.console;

import com.github.ykrasik.jerminal.api.display.terminal.Terminal;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalColor;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * A {@link Terminal} implemented as as a JavaFx {@link TextArea}.
 *
 * @author Yevgeny Krasik
 */
public class JavaFxTerminal implements Terminal {
    private final TextArea textArea;

    public JavaFxTerminal(TextArea textArea) {
        this.textArea = Objects.requireNonNull(textArea);
    }

    @Override
    public String getTab() {
        return "\t";
    }

    @Override
    public void begin() {
        // Nothing to do here.
    }

    @Override
    public void end() {
        // Nothing to do here.
    }

    @Override
    public void println(String text, TerminalColor color) {
        // Color printing is unsupported at this point.
        textArea.appendText(text);
        textArea.appendText("\n");
    }

    private String toRgbString(Color c) {
        return "rgb("
                + to255Int(c.getRed())
                + "," + to255Int(c.getGreen())
                + "," + to255Int(c.getBlue())
                + ")";
    }

    private int to255Int(double d) {
        return (int) (d * 255);
    }
    private Color translateColor(TerminalColor color) {
        switch (color) {
            case BLACK: return Color.BLACK;
            case WHITE: return Color.WHITE;
            case GRAY: return Color.GRAY;
            case RED: return Color.PINK;
            case ORANGE: return Color.ORANGE;
            case YELLOW: return Color.YELLOW;
            case GREEN: return Color.GREEN;
            case BLUE: return Color.BLUE;
            case VIOLET: return Color.MAGENTA;
            default: throw new IllegalArgumentException("Invalid color: " + color);
        }
    }
}