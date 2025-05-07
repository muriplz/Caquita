package com.kryeit.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class CommandTicket {
    public static void handle(SlashCommandInteractionEvent event) {
        event.replyEmbeds(createEmbed())
                .addActionRow(Button.primary("ticket", "Create a ticket"))
                .mentionRepliedUser(false)
                .queue();
    }

    public static MessageEmbed createEmbed() {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0))
                .setTitle("Create a ticket");

        builder.addField(
                "If you need support",
                "contact us by creating a ticket and explaining your issue",
                false);

        builder.addField(
                "State a clear title",
                "The clearer it is the easier we have to fix it.",
                false
        );
        return builder.build();
    }
}
