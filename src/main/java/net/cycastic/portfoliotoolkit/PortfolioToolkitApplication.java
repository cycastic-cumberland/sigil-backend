package net.cycastic.portfoliotoolkit;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.command.CreateUser;
import net.cycastic.portfoliotoolkit.command.GenerateKeyPair;
import net.cycastic.portfoliotoolkit.command.VerifyPassword;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@SpringBootApplication
@RequiredArgsConstructor
public class PortfolioToolkitApplication implements CommandLineRunner {
    private final CommandLine.IFactory picocliFactory;
    private final Cli cliCommand;

    @Component
    @CommandLine.Command(name = "tools", subcommands = {CreateUser.class, VerifyPassword.class, GenerateKeyPair.class})
    public static class Cli{ }

    public static void main(String[] args) {
        if (args.length > 0){
            System.setProperty("spring.main.web-application-type", "none");
        }
        SpringApplication.run(PortfolioToolkitApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            int exitCode = new CommandLine(cliCommand, picocliFactory)
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .execute(args);
            System.exit(exitCode); // Exit JVM after command execution
        }
    }
}
