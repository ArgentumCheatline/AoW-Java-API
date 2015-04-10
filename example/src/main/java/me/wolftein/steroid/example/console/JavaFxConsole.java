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
package me.wolftein.steroid.example.console;

import com.github.ykrasik.jerminal.api.CommandLineDriver;
import com.github.ykrasik.jerminal.api.Console;
import com.github.ykrasik.jerminal.api.ConsoleImpl;
import com.github.ykrasik.jerminal.api.Shell;
import com.github.ykrasik.jerminal.api.display.DisplayDriver;
import com.github.ykrasik.jerminal.api.display.terminal.Terminal;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalConfiguration;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalDisplayDriver;
import com.github.ykrasik.jerminal.api.filesystem.ShellFileSystem;
import com.github.ykrasik.jerminal.javafx.impl.JavaFxCommandLineDriver;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 *
 */
public final class JavaFxConsole {
    private final ShellFileSystem fileSystem;
    private final FXMLLoader loader;

    private TerminalConfiguration configuration = TerminalConfiguration.DEFAULT_BLACK;
    private String welcomeMessage = "Welcome to Jerminal!\n";
    private int maxCommandHistory = 30;
    private Terminal mTerminal;

    /**
     * Constructs a console from the fileSystem using a default layout..
     *
     * @param fileSystem FileSystem to use.
     */
    public JavaFxConsole(ShellFileSystem fileSystem) {
        this(fileSystem, "/console/console.fxml");
    }

    /**
     * Constructs a console from the fileSystem using the .fxml pointed to by the path.
     *
     * @param fileSystem FileSystem to use.
     * @param fxmlPath   Path to the .fxml file.
     */
    public JavaFxConsole(ShellFileSystem fileSystem, String fxmlPath) {
        this(fileSystem, Objects.requireNonNull(
                JavaFxConsole.class.getResource(fxmlPath), "Fxml not found on classpath: " + fxmlPath));
    }

    /**
     * Constructs a console from the fileSystem using the .fxml pointed to by the url.
     *
     * @param fileSystem FileSystem to use.
     * @param fxmlUrl    URL to the .fxml file.
     */
    public JavaFxConsole(ShellFileSystem fileSystem, URL fxmlUrl) {
        this.fileSystem = Objects.requireNonNull(fileSystem);
        this.loader = new FXMLLoader(Objects.requireNonNull(fxmlUrl, "Fxml url is null!"));
    }

    /**
     * @throws IOException If an error loading the .fxml file occurs.
     */
    public Parent build() throws IOException {
        final Parent consoleNode = (Parent) loader.load();

        // Create the terminal.
        final TextArea textArea = (TextArea) consoleNode.lookup("#terminal");
        textArea.setFocusTraversable(false);
        mTerminal = new JavaFxTerminal(textArea);

        // Create the shell.
        final DisplayDriver displayDriver = new TerminalDisplayDriver(mTerminal, configuration);
        final Shell shell = new Shell(fileSystem, displayDriver, welcomeMessage);

        // Create the command line.
        final TextField commandLine = (TextField) consoleNode.lookup("#commandLine");
        final CommandLineDriver commandLineDriver = new JavaFxCommandLineDriver(commandLine);

        // Create the console.
        final Console console = new ConsoleImpl(shell, commandLineDriver, maxCommandHistory);

        // Hook the commandLine to the console.
        commandLine.requestFocus();
        commandLine.addEventFilter(KeyEvent.KEY_PRESSED, new JavaFxConsoleDriver(console));

        return consoleNode;
    }

    public Terminal getTerminal() {
        return mTerminal;
    }
    /**
     * @param configuration Configuration to use.
     *
     * @return this, for chained execution.
     */
    public JavaFxConsole setConfiguration(TerminalConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * @param welcomeMessage Welcome message to set.
     *
     * @return this, for chained execution.
     */
    public JavaFxConsole setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = Objects.requireNonNull(welcomeMessage);
        return this;
    }

    /**
     * @param maxCommandHistory Max command history to set.
     *
     * @return this, for chained execution.
     */
    public JavaFxConsole setMaxCommandHistory(int maxCommandHistory) {
        this.maxCommandHistory = maxCommandHistory;
        return this;
    }

    /**
     * Links keyEvents to {@link Console} events.
     *
     * @author Yevgeny Krasik
     */
    private static class JavaFxConsoleDriver implements EventHandler<KeyEvent> {
        private final Console console;

        private JavaFxConsoleDriver(Console console) {
            this.console = Objects.requireNonNull(console);
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            switch (keyEvent.getCode()) {
                case ENTER:
                    console.execute();
                    keyEvent.consume();
                    break;

                case TAB:
                    console.assist();
                    keyEvent.consume();
                    break;

                case UP:
                    console.setPrevCommandLineFromHistory();
                    keyEvent.consume();
                    break;

                case DOWN:
                    console.setNextCommandLineFromHistory();
                    keyEvent.consume();
                    break;

                default:
                    break;
            }
        }
    }
}
