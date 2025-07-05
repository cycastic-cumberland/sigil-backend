package net.cycastic.sigil.command;


import net.cycastic.sigil.service.BackgroundCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "cleanup", mixinStandardHelpOptions = true, description = "Run periodic cleanup")
public class Cleanup implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Cleanup.class);

    @CommandLine.Option(names = "--component")
    private @Nullable String component;

    private final HashMap<String, BackgroundCleaner> cleaners;

    @Autowired
    public Cleanup(List<BackgroundCleaner> backgroundCleaners){
        cleaners = HashMap.newHashMap(backgroundCleaners.size());
        for (var cleaner : backgroundCleaners){
            if (cleaners.containsKey(cleaner.getCleanerId())){
                throw new IllegalStateException("Already added");
            }

            cleaners.put(cleaner.getCleanerId(), cleaner);
        }
    }

    @Override
    public Integer call() {
        if (component != null){
            var cleaner = Objects.requireNonNull(cleaners.get(component));
            logger.info("Executing cleaner: {}", cleaner.getCleanerId());
            cleaner.clean();
            return 0;
        }

        for (var kp : cleaners.entrySet()){
            logger.info("Executing cleaner: {}", kp.getValue().getCleanerId());
            kp.getValue().clean();
        }

        return 0;
    }
}
