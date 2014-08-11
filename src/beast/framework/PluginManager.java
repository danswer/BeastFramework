/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beast.framework;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;

/**
 *
 * @author Jorge.Molina & Daniel Jones
 */
public class PluginManager {

    private static final String PLUG_IN_DIR = "./plugins/";
    private static final String REM_PLUG_IN_DIR = "removed";
    private static final HashMap<String, PluginListener> registeredListeners = new HashMap<>();
    private static final List<Plugin> plugins = new ArrayList<>();

    public PluginManager() {

    }

    public static String GetPluginDir() {
        return PLUG_IN_DIR;
    }

    public static String GetRemovedPlugInDir() {
        return REM_PLUG_IN_DIR;
    }

    /**
     * Given the path of a plugin jar, it installs it
     *
     * @param jarPath - absolute path to jar
     * @return true if installed, false otherwise
     */
    public static boolean InstallPlugin(String jarPath) {

        boolean retVal = false;
        File jar = new File(jarPath);
        File pluginDir = new File(PLUG_IN_DIR);

        /*
         * If plugins directory doesn't exists
         * in current directory create it
         */
        if (!pluginDir.exists()) {
            try {
                pluginDir.mkdir();
            } catch (SecurityException ex) {
                Logger.getLogger(PluginLoader.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }

        /*
         * Check if jar exists, if it exists then copy to plugins directory
         */
        if (!jar.exists()) {
            Logger.getLogger(PluginLoader.class.getName()).
                    log(Level.SEVERE, null, "Jar: " + jarPath + " doesn't exists");
        } else {

            File destFile = new File(PLUG_IN_DIR + jar.getName());
            try {
                /*
                 * Transfer jar
                 */
                CopyFileUsingFileChannels(jar, destFile);
                retVal = true;
            } catch (IOException ex) {
                Logger.getLogger(PluginManager.class.getName()).
                        log(Level.SEVERE, null, ex);
            }

        }
        return retVal;
    }

    /**
     * Copies a file from one location to another.
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    private static void CopyFileUsingFileChannels(File source, File dest)
            throws IOException {

        FileChannel inputChannel = null;
        FileChannel outputChannel = null;

        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    /**
     * Removes given plugin. It actually moves the plugin to an internal
     * directory
     *
     * @param pluginName
     * @return
     */
    public static boolean RemovePlugin(String pluginName) {
        boolean retVal = false;

        File pluginRm = new File(pluginName);

        /*
         * Check if plugin's jar exists in the plugins folder
         */
        if (pluginRm.exists()) {

            File removedDir = new File(PLUG_IN_DIR + REM_PLUG_IN_DIR);

            /*
             * Check if the "removed" directory exists otherwise
             * create it
             */
            if (!removedDir.exists()) {

                if (!removedDir.mkdir()) {
                    Logger.getLogger(PluginManager.class.getName()).
                            log(Level.SEVERE, null, "Failed to create \"" + REM_PLUG_IN_DIR + "\" dir");
                } else {
                    try {

                        File destFile = new File(PLUG_IN_DIR + REM_PLUG_IN_DIR + "/" + pluginRm.getName());

                        /*
                         * Copy existing plug's jar
                         */
                        CopyFileUsingFileChannels(pluginRm, destFile);
                        /*
                         * Delete old jar
                         */
                        if (!pluginRm.delete()) {
                            Logger.getLogger(PluginManager.class.getName()).
                                    log(Level.SEVERE, null, "Failed to delete: " + pluginName);
                        } else {
                            retVal = true;
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } else {
            Logger.getLogger(PluginManager.class.getName()).
                    log(Level.SEVERE, null, "Plugin: " + pluginName + " doesn't exists");
        }

        return retVal;
    }

    /**
     *
     * @param currPlugin
     * @param newPlugin
     * @return
     */
    public static boolean UpdatePlugin(String currPlugin, String newPlugin) {
        return (RemovePlugin(currPlugin) && InstallPlugin(newPlugin));
    }

    /**
     * Gets the list of installed plugins
     *
     * @return
     */
    public static void BuildInstalledPlugins() {
        File pluginDir = new File(PLUG_IN_DIR);
        Object o;
        File[] files = pluginDir.listFiles();
        plugins.clear();
        System.out.println(plugins.size());
        String jarName = "";
        Pattern regExp = Pattern.compile("[0-9A-Za-z_]+.jar");
        Matcher matcher;

        /*
         * Iterate through all the available plugins and gather their
         * names (jar names)
         */
        for (File jarPlugin : files) {
            jarName = jarPlugin.getName();

            matcher = regExp.matcher(jarName);
            if (matcher.find()) {
                ImageIcon icon;
                String description;
                String version;
                boolean needAPAS;
                boolean runsWindows;
                boolean runsUnix;
                PluginListener beastListener = new PluginListener() {

                    @Override
                    public void PluginEventReceived(PluginEvent event) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };
                o = PluginLoader.LoadPlugIn(PLUG_IN_DIR + jarName);
                icon = (ImageIcon) PluginLoader.CallMethod("GetIconImage", o, new Object[]{});
                description = (String) PluginLoader.CallMethod("GetDescription", o, new Object[]{});
                version = (String) PluginLoader.CallMethod("GetVersion", o, new Object[]{});
                runsWindows = (boolean) PluginLoader.CallMethod("RunsOnWindows", o, new Object[]{});
                runsUnix = (boolean) PluginLoader.CallMethod("RunsOnUnix", o, new Object[]{});
                needAPAS = (boolean) PluginLoader.CallMethod("NeedsAPAS", o, new Object[]{});
                try {
                    beastListener = getPluginListner(o);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                Plugin plugin = new Plugin(jarName, version, description, needAPAS, runsWindows, runsUnix, icon, beastListener);
                plugins.add(plugin);
            }

        }              
    }
    public static List<Plugin> GetInstalledPlugins (){
        return plugins;
    }
    public static Object InitPlugIn(String pluginPath) {
        Object testPlugin = PluginLoader.LoadPlugIn(pluginPath);
        PluginLoader.CallMethod("init", testPlugin, new Object[]{});
        return testPlugin;
    }
    /*
     * Gets the list of removed plugins
     * @return
     */

    public synchronized static void RegisterListener(String pluginName, PluginListener regListener) {
        System.out.println("We registering");
        if (!registeredListeners.containsKey(pluginName)) {
            registeredListeners.put(pluginName, regListener);
        }
    }

    public synchronized static void UnregisterListener(String pluginName, PluginListener regListener) {
        registeredListeners.remove(pluginName);
    }

    public synchronized static void BroadCastMessage(PluginEvent message) {

        //Iterator listeners = registeredListeners.iterator();
        PluginListener e;
        Iterator<String> listeners = registeredListeners.keySet().iterator();
        while (listeners.hasNext()) {
            System.out.println(registeredListeners.size());
            registeredListeners.get(listeners.next()).PluginEventReceived(message);
        }
    }

    public static PluginListener getPluginListner(Object testPlugin) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Method getBeastListener = testPlugin.getClass().getMethod("getToBeastListener");
        return (PluginListener) getBeastListener.invoke(testPlugin);
    }

    public static List<String> GetRemovedPlugins() {
        File pluginDir = new File(PLUG_IN_DIR + REM_PLUG_IN_DIR);
        File[] files = pluginDir.listFiles();
        List<String> plugins = new ArrayList<String>();
        int index = 0;

        /*
         * Iterate through all the available plugins and gather their
         * names (jar names)
         */
        for (File jarPlugin : files) {
            plugins.add(jarPlugin.getName());
            index++;
        }
        return plugins;
    }

}
