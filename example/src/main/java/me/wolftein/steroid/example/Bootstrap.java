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

import com.github.ykrasik.jerminal.api.annotation.BoolParam;
import com.github.ykrasik.jerminal.api.annotation.Command;
import com.github.ykrasik.jerminal.api.annotation.ShellPath;
import com.github.ykrasik.jerminal.api.annotation.StringParam;
import com.github.ykrasik.jerminal.api.command.OutputPrinter;
import com.github.ykrasik.jerminal.api.display.terminal.TerminalColor;
import me.wolftein.steroid.framework.Application;
import me.wolftein.steroid.framework.event.annotation.EventHandler;
import me.wolftein.steroid.framework.protocol.event.SessionConnectEvent;
import me.wolftein.steroid.framework.protocol.event.SessionDisconnectEvent;
import me.wolftein.steroid.world.Controller;
import me.wolftein.steroid.world.event.PlayerErrorEvent;
import me.wolftein.steroid.world.event.PlayerJoinEvent;
import me.wolftein.steroid.world.event.PlayerRegisterEvent;

/**
 * Encapsulate the bootstrap class for the example.
 */
public final class Bootstrap {
    /**
     * The {@link Application} framework.
     */
    private final Application mApplication = new Application();

    /**
     * The {@link Controller} framework of the game protocol.
     */
    private final Controller mController = new Controller(mApplication, this);

    /**
     * The {@link BootstrapAdapter} for human input and output.
     */
    private final BootstrapAdapter mAdapter = new BootstrapAdapter(this);

    /**
     * The entry of the application.
     */
    public static void main(String[] argv) {
        new Bootstrap();
    }

    /**
     * Constructor for {@link Bootstrap}
     */
    public Bootstrap() {
        // Execute the UI on another thread other than our executor thread,
        // separating logic from UI.
        new Thread(mAdapter::initialise).start();

        // Execute the scheduler of the framework for handling the entire
        // engine of the game's world.
        mApplication.start();
    }

    /**
     * Handle {@link SessionConnectEvent}.
     */
    @EventHandler
    public void onSessionConnectEvent(SessionConnectEvent event) {
        mAdapter.print("Session has been connected.", TerminalColor.BLUE);
    }

    /**
     * Handle {@link SessionDisconnectEvent}.
     */
    @EventHandler
    public void onSessionDisconnectEvent(SessionDisconnectEvent event) {
        mAdapter.print("Session has been disconnected.", TerminalColor.BLUE);
    }

    /**
     * Handle {@link PlayerRegisterEvent}.
     */
    @EventHandler
    public void onPlayerRegisterEvent(PlayerRegisterEvent event) {
        if (event.isValid()) {
            mAdapter.print("Registered into the server!", TerminalColor.BLUE);
        } else {
            mAdapter.print("Error trying to register into the server (" + event.getMessage() + ")",
                    TerminalColor.ORANGE);
        }
    }

    /**
     * Handle {@link PlayerJoinEvent}.
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        mAdapter.print("Logged into the server!", TerminalColor.BLUE);
    }

    /**
     * Handle {@link PlayerErrorEvent}.
     */
    @EventHandler
    public void onPlayerErrorEvent(PlayerErrorEvent event) {
        mAdapter.print("Error from server: (" + event.getMessage() + ")", TerminalColor.ORANGE);
    }

    /**
     * Handle connect command.
     */
    @ShellPath(value = "connection")
    @Command(value = "connect", description = "Connect into the server.")
    public void onConnectCommand(OutputPrinter printer) {
        mApplication.getScheduler().invokeTask(T -> mController.connect());
    }

    /**
     * Handle connect command.
     */
    @ShellPath(value = "connection")
    @Command(value = "disconnect", description = "Disconnect into the server.")
    public void onDisconnectCommand(OutputPrinter printer) {
        mApplication.getScheduler().invokeTask(T -> mController.disconnect());
    }

    /**
     * Handle authorisation command.
     */
    @ShellPath("account")
    @Command(value = "login", description = "Authorise the account into the server.")
    public void onAccountAuthoriseCommand(OutputPrinter printer,
                                          @StringParam(value = "Username") String pUsername,
                                          @StringParam(value = "Password") String pPassword) {
        mController.authenticate(pUsername, pPassword);
    }

    /**
     * Handle authorisation command.
     */
    @ShellPath("account")
    @Command(value = "register", description = "Register an account into the server.")
    public void onAccountRegisterCommand(OutputPrinter printer,
                                         @StringParam(value = "Username") String pUsername,
                                         @StringParam(value = "Password") String pPassword,
                                         @StringParam(value = "Email") String pEmail,
                                         @BoolParam(value = "IsMale", optional = true, defaultValue = true) boolean isMale) {
        mController.register(pUsername, pPassword, pEmail, isMale, 1);
    }
}
