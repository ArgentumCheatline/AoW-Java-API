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
import com.github.ykrasik.jerminal.api.filesystem.ShellFileSystem;
import com.gs.collections.api.collection.ImmutableCollection;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.wolftein.steroid.example.console.JavaFxConsole;
import me.wolftein.steroid.framework.Application;
import me.wolftein.steroid.framework.event.annotation.EventHandler;
import me.wolftein.steroid.framework.protocol.event.SessionConnectEvent;
import me.wolftein.steroid.framework.protocol.event.SessionDisconnectEvent;
import me.wolftein.steroid.framework.scheduler.Task;
import me.wolftein.steroid.framework.scheduler.TaskPriority;
import me.wolftein.steroid.world.Heading;
import me.wolftein.steroid.world.WorldEntity;
import me.wolftein.steroid.world.controller.Controller;
import me.wolftein.steroid.world.event.PlayerErrorEvent;
import me.wolftein.steroid.world.event.PlayerJoinEvent;
import me.wolftein.steroid.world.event.PlayerRegisterEvent;
import me.wolftein.steroid.world.event.PlayerUpdateStats;

import java.io.IOException;
import java.util.Random;

/**
 * Encapsulate the bootstrap class for the example.
 */
public final class Bootstrap extends javafx.application.Application {
    /**
     * The {@link Application} framework.
     */
    private final Application mApplication = new Application();

    /**
     * The {@link Controller} framework of the game protocol.
     */
    private final Controller mController = new Controller(mApplication, this);

    /**
     * The console!.
     */
    private JavaFxConsole mConsole;

    private long mTimePotion, mTimeMagic, mTimeMove;
    private Random mRandom = new Random();

    /**
     * The entry of the application.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        launch(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Jerminal");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);

        // Create a console
        final ShellFileSystem fileSystem = new ShellFileSystem().processAnnotationsOfObject(this);
        mConsole = new JavaFxConsole(fileSystem);
        final Parent console = mConsole.build();

        // Create a boring main scene.
        final Scene scene = new Scene(console);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Execute the scheduler of the framework for handling the entire
        // engine of the game's world.
        new Thread(mApplication::start).start();
    }

    /**
     * Handle {@link SessionConnectEvent}.
     */
    @EventHandler
    public void onSessionConnectEvent(SessionConnectEvent event) {
        mConsole.getTerminal().println("Session has been connected.", TerminalColor.BLUE);
    }

    /**
     * Handle {@link SessionDisconnectEvent}.
     */
    @EventHandler
    public void onSessionDisconnectEvent(SessionDisconnectEvent event) {
        mConsole.getTerminal().println("Session has been disconnected.", TerminalColor.BLUE);
    }

    /**
     * Handle {@link PlayerRegisterEvent}.
     */
    @EventHandler
    public void onPlayerRegisterEvent(PlayerRegisterEvent event) {
        if (event.isValid()) {
            mConsole.getTerminal().println("Registered into the server!", TerminalColor.BLUE);
        } else {
            mConsole.getTerminal().println("Error trying to register into the server (" + event.getMessage() + ")",
                    TerminalColor.ORANGE);
        }
    }

    /**
     * Handle {@link PlayerJoinEvent}.
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        mConsole.getTerminal().println("Logged into the server!", TerminalColor.BLUE);
        mConsole.getTerminal().println(event.getMessageOfTheDay(), TerminalColor.BLUE);

        mApplication.getScheduler().invokeDelayedTask(T ->
                mApplication.getScheduler().invokeRepeatingTask(this::onPlayerLoop, TaskPriority.CRITICAL, 0L, 1L),
                TaskPriority.CRITICAL,
                1000L);
    }

    /**
     * Handle {@link PlayerErrorEvent}.
     */
    @EventHandler
    public void onPlayerErrorEvent(PlayerErrorEvent event) {
        mConsole.getTerminal().println("Error from server: (" + event.getMessage() + ")", TerminalColor.ORANGE);
    }

    /**
     * Handle {@link PlayerErrorEvent}.
     */
    @EventHandler
    public void onPlayerUpdate(PlayerUpdateStats event) {
        mConsole.getTerminal().println("UPDATE: (" + "HP: " + event.getEntity().getHealth() + " MP: " + event.getEntity().getManapoint() + ")",
                TerminalColor.ORANGE);
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


    /**
     * Handle authorisation command.
     */
    @ShellPath("player")
    @Command(value = "say", description = "Register an account into the server.")
    public void onPlayerSay(OutputPrinter printer,
                            @StringParam(value = "Say") String pSay) {
        mController.say(pSay);
    }

    /**
     * Handle the looping task.
     */
    public void onPlayerLoop(Task task) {
        if (!mController.isLogged()) {
            return;
        }

        // Retrieves the character of the player.
        final WorldEntity nCharacter = mController.getWorld().getCharacter();

        // Retrieves the current time of the system.
        final long nTime = System.currentTimeMillis();

        if (nTime >= mTimePotion) {
            if (nCharacter.getHealth() < nCharacter.getMaxHealth()) {
                mTimePotion = nTime + 200 + mRandom.nextInt(50);   // 200 msec + (0..50 msec)

                if (mRandom.nextInt(9) >= 2)    // 80% probability of use.
                {
                    mController.use(1);         // Health potion at slot 1.
                    System.out.println("USE HEALTH AT: " + nTime);
                }
            } else if (nCharacter.getManapoint() < nCharacter.getManapoint()) {
                mTimePotion = nTime + 200 + mRandom.nextInt(75);   // 200 msec + (0..75 msec)

                if (mRandom.nextInt(9) >= 2) {    // 80% probability of use.
                    mController.use(2);         // Mana potion at slot 1.
                    System.out.println("USE MANA AT: " + nTime);
                }
            }
        }
        if (nTime >= mTimeMagic && nCharacter.getManapoint() >= 1000) {
            final ImmutableCollection<WorldEntity> nEntities
                    = mController.getWorld().getEntitiesNotPlayer(T -> !T.isAdmin());
            if (nEntities.size() > 0) {
                final WorldEntity nTarget = nEntities.getFirst();
                mTimeMagic = nTime + 800 + mRandom.nextInt(200);   // 800 msec + (0..200 msec)

                if (mRandom.nextInt(9) > 2) {         // 80% probability of throw magic.
                    mController.throwSpell(11, nTarget.getX(), nTarget.getY());
                    System.out.println("USE SPELL IN: " + nTime + " TO-> " + nTarget.getName());
                }
            }
        }
        if (nTime >= mTimeMove) {
            mTimeMove = nTime + 250 + mRandom.nextInt(50);   // 250 msec + (0..50 msec)

            if (mRandom.nextInt(9) >= 3) {            // 70% probability of movement.
                if (mController.move(Heading.values()[mRandom.nextInt(3)]))
                 System.out.println("USE MOVE AT: " + nTime);
            }
        }
    }
}
