package com.bgsoftware.superiorprison.plugin.commands.args;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FileArg extends CommandArgument<File> {

    private File directory;
    private Predicate<String> fileNameFilter;

    public FileArg(File directory, Predicate<String> filter) {
        this.directory = directory;
        this.fileNameFilter = filter;
        setIdentity("file");
        setDescription("file");

        setMapper(fileName -> {
            Path resolve = directory.toPath().resolve(fileName);
            if (!Files.exists(resolve))
                return new OPair<>(null, "Invalid file");

            return new OPair<>(resolve.toFile(), null);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> {
            List<String> files = new ArrayList<>();

            for (File file : directory.listFiles()) {
                if (fileNameFilter.test(file.getName()))
                    files.add(file.getName());
            }

            return files;
        });
    }
}
