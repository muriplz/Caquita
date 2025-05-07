package com.kryeit.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class CommandKofi {
    public static void handle(SlashCommandInteractionEvent event) {
        event.replyEmbeds(createEmbed())
                .mentionRepliedUser(false)
                .queue();
    }

    public static MessageEmbed createEmbed() {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0))
                .setTitle("Ko-fi", "https://ko-fi.com/kryeit");

        builder.addField(
                "Link",
                "https://www.ko-fi.com/kryeit",
                false
        );
        return builder.build();
    }
}
