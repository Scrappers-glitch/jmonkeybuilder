package com.ss.builder.config;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.ss.builder.JmeApplication;
import com.ss.builder.annotation.FromAnyThread;
import com.ss.builder.util.EditorUtils;
import com.ss.builder.util.TimeTracker;
import com.ss.rlib.common.plugin.Version;
import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.VarTable;
import com.ss.rlib.common.util.os.OperatingSystem;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The configuration of this application.
 *
 * @author JavaSaBr
 */
public final class Config {

    /**
     * The path to config file in classpath.
     */
    private static final String CONFIG_RESOURCE_PATH = "/app-config.json";

    /**
     * The name of editor's folder in user home folder.
     */
    private static final String EDITOR_FOLDER_IN_USER_HOME = ".jmonkeybuilder";

    /**
     * The editor's title.
     */
    public static final String TITLE = "jMonkeyBuilder";

    /**
     * The editor's version.
     */
    public static final Version APP_VERSION = new Version("1.9.0");

    /**
     * The string version.
     */
    public static final String STRING_VERSION = APP_VERSION.toString();

    /**
     * The server API version.
     */
    public static final int SERVER_API_VERSION = 1;

    /**
     * The path to application folder.
     */
    public static final String PROJECT_PATH;

    /**
     * The graphics adapter.
     */
    private static GraphicsDevice graphicsDevice;

    /**
     * The operation system.
     */
    private static OperatingSystem operatingSystem;

    /**
     * The remote control port.
     */
    public static int REMOTE_CONTROL_PORT = -1;

    /**
     * The flag to enable debug mode.
     */
    public static boolean DEV_DEBUG;

    /**
     * The flag to enable camera debug mode.
     */
    public static boolean DEV_CAMERA_DEBUG;

    /**
     * The flag to enable camera checks debug mode.
     */
    public static boolean DEV_CAMERA_CHECKS_DEBUG;

    /**
     * The flag to enable transformations debug mode.
     */
    public static boolean DEV_TRANSFORMS_DEBUG;

    /**
     * The flag to enable JavaFX debug mode.
     */
    public static boolean DEV_DEBUG_JFX;

    /**
     * The flag to enable JavaFX mouse input debug mode.
     */
    public static boolean DEV_DEBUG_JFX_MOUSE_INPUT;

    /**
     * The flag to enable javaFX key input debug mode.
     */
    public static boolean DEV_DEBUG_JFX_KEY_INPUT;

    /**
     * The flag to enable startup debug mode.
     */
    public static boolean DEV_DEBUG_STARTUP;

    /**
     * The flag to enable debug of async event manager.
     */
    public static boolean DEV_DEBUG_ASYNC_EVENT_MANAGER;

    /**
     * The flag to enable PBR render.
     */
    public static boolean ENABLE_PBR;

    /**
     * The flag to enable 3D part of this editor.
     */
    public static boolean ENABLE_3D;

    static {

        TimeTracker.getStartupTracker(TimeTracker.STARTPUL_LEVEL_3)
                .start();

        var vars = VarTable.newInstance();

        try (var reader = new InputStreamReader(EditorUtils.requireInputStream(CONFIG_RESOURCE_PATH))) {

            var object = (JsonObject) Json.parse(reader);
            object.forEach(member -> vars.set(member.getName(), member.getValue().toString()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DEV_DEBUG = vars.getBoolean("Dev.debug", false);
        DEV_CAMERA_DEBUG = vars.getBoolean("Dev.cameraDebug", false);
        DEV_CAMERA_CHECKS_DEBUG = vars.getBoolean("Dev.cameraChecksDebug", false);
        DEV_TRANSFORMS_DEBUG = vars.getBoolean("Dev.transformsDebug", false);
        DEV_DEBUG_JFX_MOUSE_INPUT = vars.getBoolean("Dev.jfxMouseInput", false);
        DEV_DEBUG_JFX_KEY_INPUT = vars.getBoolean("Dev.jfxKeyInput", false);
        DEV_DEBUG_JFX = vars.getBoolean("Dev.debugJFX", false);
        DEV_DEBUG_STARTUP = vars.getBoolean("Dev.debugStartup", false);
        DEV_DEBUG_ASYNC_EVENT_MANAGER = vars.getBoolean("Dev.debugAsyncEventManager", false);
        ENABLE_PBR = vars.getBoolean("Graphics.enablePBR", true);
        ENABLE_3D = vars.getBoolean("Graphics.enable3D", true);

        PROJECT_PATH = Utils.getRootFolderFromClass(JmeApplication.class)
                .toString();

        TimeTracker.getStartupTracker(TimeTracker.STARTPUL_LEVEL_3)
                .finish(() -> "loading of the root config");

    }

    /**
     * Get a folder to store log files.
     *
     * @return the path to a folder to store log files.
     */
    @FromAnyThread
    public static @NotNull Path getFolderForLog() {
        return getAppFolderInUserHome().resolve("log");
    }

    /**
     * Get a path to the folder to store data in a user home.
     *
     * @return the path to the folder to store data in a user home.
     */
    @FromAnyThread
    public static @NotNull Path getAppFolderInUserHome() {
        var userHome = System.getProperty("user.home");
        return Paths.get(userHome, EDITOR_FOLDER_IN_USER_HOME);
    }

    /**
     * Get the information about OS.
     *
     * @return the information about OS.
     */
    @FromAnyThread
    public synchronized static @NotNull OperatingSystem getOperatingSystem() {

        if (operatingSystem == null) {
            operatingSystem = new OperatingSystem();
        }

        return operatingSystem;
    }

    /**
     * Get the information about used graphics device.
     *
     * @return the information about used graphics device.
     */
    @FromAnyThread
    public synchronized static GraphicsDevice getGraphicsDevice() {


        if (graphicsDevice == null) {
            var graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
        }

        return graphicsDevice;
    }
}
