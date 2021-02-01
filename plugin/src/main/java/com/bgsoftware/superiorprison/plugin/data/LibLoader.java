package com.bgsoftware.superiorprison.plugin.data;

import com.oop.datamodule.api.loader.LibraryManager;
import com.oop.datamodule.api.loader.classloader.URLClassLoaderHelper;
import com.oop.datamodule.api.loader.logging.adapters.JDKLogAdapter;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

public class LibLoader extends LibraryManager {
  private final URLClassLoaderHelper classLoader;

  public LibLoader(JavaPlugin plugin) {
    super(new JDKLogAdapter(plugin.getLogger()), plugin.getDataFolder().toPath());
    classLoader = new URLClassLoaderHelper((URLClassLoader) plugin.getClass().getClassLoader());
  }

  @Override
  protected void addToClasspath(Path path) {
    classLoader.addToClasspath(path);
  }
}
