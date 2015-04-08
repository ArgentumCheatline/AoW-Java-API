/*
 * This file is part of jAoW (On Steroids), licensed under the Apache 2.0 License.
 *
 * Copyright (c) 2014 Agustin Alvarez <wolftein1@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.wolftein.steroid.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.github.ykrasik.jerminal.api.Console;
import com.github.ykrasik.jerminal.api.ConsoleImpl;
import com.github.ykrasik.jerminal.api.Shell;
import com.github.ykrasik.jerminal.api.display.DisplayDriver;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalColor;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalConfiguration;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalDisplayDriver;
import com.github.ykrasik.jerminal.api.filesystem.ShellFileSystem;
import com.github.ykrasik.jerminal.libgdx.LibGdxConsole;
import com.github.ykrasik.jerminal.libgdx.VisibleListener;
import com.github.ykrasik.jerminal.libgdx.impl.LibGdxCommandLineDriver;
import com.github.ykrasik.jerminal.libgdx.impl.LibGdxTerminal;
import com.github.ykrasik.jerminal.libgdx.impl.LibGdxWorkingDirectoryListener;

import java.util.Objects;

/**
 * Encapsulate the {@link ApplicationAdapter} for adapting the console into the framework.
 * </br>
 * Every command is registered from this class.
 */
public final class BootstrapAdapter extends ApplicationAdapter {
    private final static String WINDOW_NAME = "Argentum Online W (On Steroids)";
    private final static String WINDOW_MOTD = "Bienvenidos a JAoW";
    private final static int WINDOW_WIDTH = 800;
    private final static int WINDOW_HEIGHT = 600;

    /**
     * The parent of the adapter.
     */
    private final Bootstrap mParent;

    private LibGdxTerminal mTerminal;

    /**
     * The stage of the {@link ApplicationAdapter} view.
     */
    private Stage mStage;

    /**
     * The console instance of the adapter.
     */
    private Table mConsole;

    /**
     * Constructor for {@link BootstrapAdapter}.
     */
    public BootstrapAdapter(Bootstrap parent) {
        this.mParent = parent;
    }

    /**
     * Initialise the adapter and enter in an infinite loop until
     * the user explicit shutdown the window.
     */
    public void initialise() {
        final LwjglApplicationConfiguration nConfig = new LwjglApplicationConfiguration();
        nConfig.title = "Argentum Online W (On Steroids)";
        nConfig.width = WINDOW_HEIGHT;
        nConfig.height = WINDOW_WIDTH;
        nConfig.resizable = false;
        nConfig.samples = 4;

        new LwjglApplication(this, nConfig);
    }

    public void print(String text, TerminalColor color)
    {
        mTerminal.println(text, color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create() {
        final ShellFileSystem nFilesystem
                = new ShellFileSystem().processAnnotationsOfObject(mParent);
        mConsole = createNewConsole(nFilesystem);
        mConsole.setColor(Color.BLACK);
        mConsole.setFillParent(true);

        mStage = new Stage();
        mStage.addActor(mConsole);
        Gdx.input.setInputProcessor(mStage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resize(int width, int height) {
        mStage.setViewport(width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render() {
        mStage.act();
        mStage.draw();
    }

    /**
     * Builds a console that is the console.
     */
    public Table createNewConsole(ShellFileSystem filesystem) {
        final Skin skin = new Skin(Gdx.files.classpath("console/console.cfg"));
        // Create the terminal.
        mTerminal = new LibGdxTerminal(skin, 100);
        mTerminal.setName("terminal");
        mTerminal.bottom().left();

        // Create the shell.
        final DisplayDriver displayDriver = new TerminalDisplayDriver(mTerminal, TerminalConfiguration.DEFAULT_WHITE);
        final Shell shell = new Shell(filesystem, displayDriver, WINDOW_MOTD);

        // Create the current path label.
        final Label currentPath = new Label("", skin, "currentPath");
        currentPath.setName("currentPath");
        shell.registerWorkingDirectoryListener(new LibGdxWorkingDirectoryListener(currentPath));

        // Create the command line.
        final TextField commandLine = new TextField("", skin, "commandLine");
        commandLine.setName("commandLine");
        final LibGdxCommandLineDriver commandLineDriver = new LibGdxCommandLineDriver(commandLine);

        // Create the console.
        final Console console = new ConsoleImpl(shell, commandLineDriver, 30);

        // Hook the commandLine to the console.
        commandLine.addListener(new LibGdxConsoleDriver(console));

        return createConsoleTable(mTerminal, currentPath, commandLine, skin);
    }

    private LibGdxConsole createConsoleTable(LibGdxTerminal terminal, Label currentPath, final TextField commandLine, Skin skin) {
        final LibGdxConsole consoleTable = new LibGdxConsole(skin);
        consoleTable.setName("consoleTable");
        consoleTable.setBackground("consoleBackground");
        consoleTable.addVisibleListener(new VisibleListener() {
            @Override
            public void onVisibleChange(boolean wasVisible, boolean isVisible) {
                if (!wasVisible && isVisible) {
                    final Stage stage = consoleTable.getStage();
                    if (stage != null) {
                        stage.setKeyboardFocus(commandLine);
                    }
                }
            }
        });

        // Some layout.
        final Table currentPathTable = new Table(skin);
        currentPathTable.setName("currentPathTable");
        currentPathTable.setBackground("currentPathBackground");
        currentPathTable.add(currentPath).fill().padLeft(3).padRight(5);

        // The bottom row contains the current path, command line and a close button.
        final Table bottomRow = new Table(skin);
        bottomRow.setName("bottomRow");
        bottomRow.setBackground("bottomRowBackground");
        bottomRow.add(currentPathTable).fill();
        bottomRow.add(commandLine).fill().expandX();

        consoleTable.pad(0);
        consoleTable.add(terminal).fill().expand();
        consoleTable.row();
        consoleTable.add(bottomRow).fill();
        consoleTable.top().left();

        return consoleTable;
    }

    /**
     * Links input events to {@link com.github.ykrasik.jerminal.api.Console} events.
     *
     * @author Yevgeny Krasik
     */
    private static class LibGdxConsoleDriver extends InputListener {
        private final Console console;

        private LibGdxConsoleDriver(Console console) {
            this.console = Objects.requireNonNull(console);
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            switch (keycode) {
                case Input.Keys.ENTER:
                    console.execute();
                    return true;

                case Input.Keys.TAB:
                    console.assist();
                    return true;

                case Input.Keys.DPAD_UP:
                    console.setPrevCommandLineFromHistory();
                    return true;

                case Input.Keys.DPAD_DOWN:
                    console.setNextCommandLineFromHistory();
                    return true;

                case Input.Keys.Z:
                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                        console.clearCommandLine();
                        return true;
                    }
            }

            return false;
        }
    }
}
