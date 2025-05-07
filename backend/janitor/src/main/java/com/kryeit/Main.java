package com.kryeit;

import com.kryeit.commands.CommandListener;
import com.kryeit.listener.ButtonInteractionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Main {

    public static JDA JDA;

    public static void main(String[] args) throws InterruptedException {

        try {
            ConfigReader.readFile(Path.of(""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JDA = JDABuilder.createDefault(readToken())
                .addEventListeners(new CommandListener())
                .build();
        JDA.addEventListener(new ButtonInteractionListener());
        JDA.awaitReady();

        registerCommands();
    }

    private static void registerCommands() {
        Guild guild = JDA.getGuildById(Ids.CAQUITA_GUILD);
        if (guild != null) {
            guild.upsertCommand("kofi", "Sends the donation Link")
                    .queue();
            guild.upsertCommand("donate", "Sends the donation Link")
                    .queue();

            guild.upsertCommand("close", "Closes a ticket")
                    .queue();
            guild.upsertCommand("ticket", "Creates a ticket")
                    .queue();
        } else {
            System.out.println("Guild is null!");
        }
    }

    private static String readToken() {
        try (InputStream in = Main.class.getResourceAsStream("/secret.txt")) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: secret.txt");
            }
            return new String(in.readAllBytes()).trim();
        } catch (IOException ignored) {
            return null;
        }
    }

}